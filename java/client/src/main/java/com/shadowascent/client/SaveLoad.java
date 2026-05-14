package com.shadowascent.client;

import com.shadowascent.core.GameState;
import com.shadowascent.core.simulation.EnemyAIState;
import com.shadowascent.core.simulation.EnemyAwarenessState;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.InputCommand;
import com.shadowascent.core.simulation.SimEnemy;
import com.shadowascent.core.simulation.SimInventory;
import com.shadowascent.core.simulation.SimPlayer;
import com.shadowascent.core.world.streaming.MutationOverlay;
import com.shadowascent.core.world.streaming.OverlayPayloadCodec;
import com.shadowascent.core.world.streaming.RegionInstance;
import com.shadowascent.core.world.streaming.ZoneOverride;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Encapsulates GameState persistence and overlay codec operations for PlaytestClient.
 * Handles the pure I/O layer (save-file reads/writes + overlay encode/decode).
 * PlaytestClient retains post-load refresh callbacks and mutable region state.
 */
final class SaveLoad {

    record LoadResult(Map<String, List<ZoneOverride>> overlays) {}

    private final GameState gameState;
    private final Path savePath;

    SaveLoad(GameState gameState, Path savePath) {
        this.gameState = gameState;
        this.savePath = savePath;
    }

    /** Encodes the current active-region overlay state to Base64 for the save envelope. */
    String buildOverlaysB64(List<RegionInstance> activeRegions) throws Exception {
        MutationOverlay overlay = new MutationOverlay();
        return OverlayPayloadCodec.encodeToB64(overlay.extractSaveState(activeRegions));
    }

    /** Writes the save envelope (story state + overlaysB64) to disk. */
    void save() throws Exception {
        save("");
    }

    void save(String overlaysB64) throws Exception {
        gameState.save(savePath, overlaysB64);
    }

    void saveRunGame(GameSimulator simulator, String playerId) throws Exception {
        save();

        SimPlayer player = simulator.getPlayer(playerId);
        if (player == null) {
            throw new IllegalStateException("Missing player `" + playerId + "` for runtime save.");
        }

        Properties properties = new Properties();
        properties.setProperty("room.currentId", emptyIfNull(gameState.getCurrentRoomId()));
        properties.setProperty("room.pendingSpawnId", emptyIfNull(gameState.getPendingRoomSpawnId()));
        properties.setProperty("player.x", Float.toString(player.physics.x));
        properties.setProperty("player.y", Float.toString(player.physics.y));
        properties.setProperty("player.vx", Float.toString(player.physics.vx));
        properties.setProperty("player.vy", Float.toString(player.physics.vy));
        properties.setProperty("player.onGround", Boolean.toString(player.physics.onGround));
        properties.setProperty("player.health", Integer.toString(player.health));
        properties.setProperty("player.maxHealth", Integer.toString(player.maxHealth));
        properties.setProperty("player.isDead", Boolean.toString(player.isDead));
        properties.setProperty("player.facing", Integer.toString(player.facing));
        properties.setProperty("inventory.currency", Integer.toString(player.inventory.currency));
        properties.setProperty("inventory.equippedWeapon", emptyIfNull(player.inventory.equippedWeapon));
        properties.setProperty("inventory.equippedArmor", emptyIfNull(player.inventory.equippedArmor));

        for (int i = 0; i < player.inventory.slots.length; i++) {
            SimInventory.Slot slot = player.inventory.slots[i];
            if (slot == null) {
                continue;
            }
            String prefix = "slot." + i + ".";
            properties.setProperty(prefix + "itemId", slot.itemId());
            properties.setProperty(prefix + "quantity", Integer.toString(slot.quantity()));
            properties.setProperty(prefix + "equipped", Boolean.toString(slot.equipped()));
        }

        properties.setProperty("enemy.count", Integer.toString(simulator.getEnemies().size()));
        for (int i = 0; i < simulator.getEnemies().size(); i++) {
            SimEnemy enemy = simulator.getEnemies().get(i);
            String prefix = "enemy." + i + ".";
            properties.setProperty(prefix + "id", enemy.enemyId);
            properties.setProperty(prefix + "x", Float.toString(enemy.physics.x));
            properties.setProperty(prefix + "y", Float.toString(enemy.physics.y));
            properties.setProperty(prefix + "hp", Integer.toString(enemy.hp));
            properties.setProperty(prefix + "removed", Boolean.toString(enemy.removed));
            properties.setProperty(prefix + "knockedOut", Boolean.toString(enemy.knockedOut));
            properties.setProperty(prefix + "facingRight", Boolean.toString(enemy.facingRight));
        }

        Path runtimePath = runtimePath();
        Path parent = runtimePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (OutputStream out = Files.newOutputStream(runtimePath)) {
            properties.store(out, "runGame runtime snapshot");
        }
    }

    /**
     * Reads the save envelope from disk and decodes the overlay payload.
     * Caller is responsible for applying the returned overlays and triggering post-load refresh.
     */
    LoadResult load() throws Exception {
        gameState.load(savePath);
        String overlaysB64 = gameState.loadOverlaysB64(savePath);
        Map<String, List<ZoneOverride>> overlays = OverlayPayloadCodec.decodeFromB64(overlaysB64);
        return new LoadResult(overlays);
    }

    boolean loadRunGame(GameSimulator simulator, String playerId) throws Exception {
        if (!hasRunGameSave()) {
            return false;
        }

        load();

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(runtimePath())) {
            properties.load(in);
        }

        SimPlayer player = simulator.getPlayer(playerId);
        if (player == null) {
            throw new IllegalStateException("Missing player `" + playerId + "` for runtime load.");
        }

        gameState.setCurrentRoomId(nullIfEmpty(properties.getProperty("room.currentId")));
        gameState.setPendingRoomSpawnId(nullIfEmpty(properties.getProperty("room.pendingSpawnId")));
        restorePlayer(player, properties);
        restoreEnemies(simulator, properties);
        simulator.drainEvents();
        return true;
    }

    boolean exists() {
        return Files.exists(savePath);
    }

    boolean hasRunGameSave() {
        return exists() && Files.exists(runtimePath());
    }

    Path savePath() {
        return savePath;
    }

    private Path runtimePath() {
        return savePath.resolveSibling(savePath.getFileName().toString() + ".runtime.properties");
    }

    private static void restorePlayer(SimPlayer player, Properties properties) {
        player.physics.x = floatValue(properties, "player.x", player.physics.x);
        player.physics.y = floatValue(properties, "player.y", player.physics.y);
        player.physics.vx = floatValue(properties, "player.vx", 0f);
        player.physics.vy = floatValue(properties, "player.vy", 0f);
        player.physics.onGround = boolValue(properties, "player.onGround", player.physics.onGround);
        player.physics.onWall = false;
        player.physics.wallDir = 0;
        player.physics.jumpCutActive = false;
        player.physics.fastFallActive = false;
        player.physics.gravityFrozen = false;
        player.health = intValue(properties, "player.health", player.health);
        player.maxHealth = intValue(properties, "player.maxHealth", player.maxHealth);
        player.isDead = boolValue(properties, "player.isDead", player.isDead);
        player.facing = intValue(properties, "player.facing", player.facing);
        player.invincibilityTicks = 0;
        player.isAttacking = false;
        player.attackActiveTicks = 0;
        player.attackCooldown = 0f;
        player.meleeHitConsumed = false;
        player.attackAimX = player.facing >= 0 ? 1 : -1;
        player.attackAimY = 0;
        player.comboStep = 0;
        player.comboQueued = false;
        player.queuedAttackAimX = player.attackAimX;
        player.queuedAttackAimY = 0;
        player.isDashing = false;
        player.dashTimer = 0f;
        player.prevAttack = false;
        player.prevJump = false;
        player.prevDash = false;
        player.latestInput = InputCommand.neutral(0);
        restoreInventory(player.inventory, properties);
    }

    private static void restoreInventory(SimInventory inventory, Properties properties) {
        for (int i = 0; i < inventory.slots.length; i++) {
            inventory.slots[i] = null;
        }
        inventory.currency = intValue(properties, "inventory.currency", 0);
        inventory.equippedWeapon = nullIfEmpty(properties.getProperty("inventory.equippedWeapon"));
        inventory.equippedArmor = nullIfEmpty(properties.getProperty("inventory.equippedArmor"));
        for (int i = 0; i < inventory.slots.length; i++) {
            String prefix = "slot." + i + ".";
            String itemId = properties.getProperty(prefix + "itemId");
            if (itemId == null || itemId.isBlank()) {
                continue;
            }
            inventory.slots[i] = new SimInventory.Slot(
                    itemId,
                    intValue(properties, prefix + "quantity", 1),
                    boolValue(properties, prefix + "equipped", false)
            );
        }
    }

    private static void restoreEnemies(GameSimulator simulator, Properties properties) {
        Map<String, SimEnemy> enemiesById = new LinkedHashMap<>();
        for (SimEnemy enemy : simulator.getEnemies()) {
            enemiesById.put(enemy.enemyId, enemy);
        }

        int enemyCount = intValue(properties, "enemy.count", 0);
        for (int i = 0; i < enemyCount; i++) {
            String prefix = "enemy." + i + ".";
            String enemyId = properties.getProperty(prefix + "id");
            SimEnemy enemy = enemiesById.get(enemyId);
            if (enemy == null) {
                continue;
            }
            enemy.physics.x = floatValue(properties, prefix + "x", enemy.physics.x);
            enemy.physics.y = floatValue(properties, prefix + "y", enemy.physics.y);
            enemy.physics.vx = 0f;
            enemy.physics.vy = 0f;
            enemy.hp = intValue(properties, prefix + "hp", enemy.hp);
            enemy.removed = boolValue(properties, prefix + "removed", enemy.removed);
            enemy.knockedOut = boolValue(properties, prefix + "knockedOut", enemy.knockedOut);
            enemy.facingRight = boolValue(properties, prefix + "facingRight", enemy.facingRight);
            enemy.attackTimer = 0f;
            enemy.stunTimer = 0f;
            enemy.fleeTimer = 0f;
            enemy.guardTimer = 0f;
            enemy.attackWindupTimer = 0f;
            enemy.attackActiveTimer = 0f;
            enemy.attackRecoveryTimer = 0f;
            enemy.aiState = enemy.removed ? EnemyAIState.DEAD : EnemyAIState.PATROL;
            enemy.awarenessState = EnemyAwarenessState.UNAWARE;
            enemy.awarenessTimer = 0f;
        }
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private static String nullIfEmpty(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static int intValue(Properties properties, String key, int fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Integer.parseInt(value);
    }

    private static float floatValue(Properties properties, String key, float fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Float.parseFloat(value);
    }

    private static boolean boolValue(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }
}
