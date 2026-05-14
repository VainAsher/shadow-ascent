package com.shadowascent.core.simulation;

import com.shadowascent.core.physics.PhysicsConstants;
import com.shadowascent.core.physics.PhysicsState;

/**
 * Server-side player simulation state.
 * Imported from indie-ninja-adventures (com.indieniinja.sim.SimPlayer).
 *
 * Mirrors the subset of Python's Player/PlayerState that the server needs for
 * authoritative simulation: physics, health, facing, and animation.
 * In Phase B movement is client-authoritative (positions from INPUT messages).
 * Phase C will drive these from the full Java mechanics pipeline.
 *
 * GameConfig constants are inlined (no GameConfig class in core.simulation).
 */
public final class SimPlayer {

    public final String playerId;
    public final int    slot;

    public final PhysicsState physics;

    public int     health;
    public int     maxHealth  = 5;
    public int     facing     = 1;
    public boolean isDead     = false;
    public String  animState  = "";

    public float   respawnTimer = -1f;
    public float   spawnX, spawnY;

    public int invincibilityTicks = 0;

    // ── Advanced movement mechanics state ─────────────────────────────────────
    public float   dashTimer    = 0f;
    public float   dashCooldown = 0f;
    public boolean isDashing    = false;

    public int     jumpCount   = 0;
    public float   coyoteTimer = 0f;
    public float   jumpBuffer  = 0f;

    public boolean prevJump        = false;
    public boolean prevDash        = false;
    public boolean prevTeleport    = false;
    public boolean prevStanceSwitch = false;
    public boolean wasOnGround     = false;

    // ── Traverse state ────────────────────────────────────────────────────────
    public boolean isClimbing      = false;
    public boolean isOnLedge       = false;
    public boolean isLedgeClimbing = false;
    public boolean atWaterSurface  = false;
    public float   ledgeTargetX    = 0f;
    public float   ledgeTargetY    = 0f;
    public float   ledgeHangY      = 0f;
    public float   ledgeClimbTimer = 0f;

    // ── Combat state ──────────────────────────────────────────────────────────
    public boolean isAttacking       = false;
    public int     attackActiveTicks = 0;
    public float   attackCooldown    = 0f;
    public boolean meleeHitConsumed  = false;
    public boolean prevAttack        = false;
    public boolean pendingShuriken   = false;
    public boolean isBlocking        = false;
    public boolean isParrying        = false;
    public float   blockHeldTime     = 0f;
    public float   blockHitTimer     = 0f;
    public String  blockHitAnim      = "";

    public int     shurikenAmmo   = 5;
    public float   throwCooldown  = 0f;
    public boolean isThrowing     = false;
    public boolean prevThrow      = false;

    public String  interactionState = "";
    public float   interactionTimer = 0f;

    // Combat constants (inlined from GameConfig)
    public static final int   MELEE_DAMAGE         = 1;
    public static final int   ARMED_MELEE_DAMAGE   = 2;
    public static final int   MELEE_ACTIVE_TICKS   = 8;
    public static final float MELEE_COOLDOWN       = 0.4f;
    public static final float MELEE_REACH          = 48f;
    public static final float MELEE_HEIGHT         = 40f;
    public static final float SHURIKEN_SPEED       = 10f;
    public static final float SHURIKEN_COOLDOWN    = 0.35f;
    public static final int   SHURIKEN_DAMAGE      = 1;
    public static final int   SHURIKEN_MAX_AMMO    = 5;
    public static final float PARRY_WINDOW         = 0.10f;
    public static final float BLOCK_DAMAGE_MULT    = 0.35f;
    public static final float BLOCK_HIT_TIME       = 0.16f;
    public static final float PARRY_HIT_TIME       = 0.20f;
    public static final float INTERACT_LEVER_TIME  = 0.45f;
    public static final float INTERACT_BUTTON_TIME = 0.32f;
    public static final float INTERACT_PICKUP_TIME = 0.22f;
    public static final float CLIMB_SPEED          = 3.0f;
    public static final float LEDGE_HANG_OFFSET    = 10f;
    public static final float LEDGE_CLIMB_TIME     = 0.22f;
    public static final int   INVINCIBILITY_TICKS  = 30;

    // ── Teleport state ────────────────────────────────────────────────────────
    public float   teleportCooldown    = 0f;
    public boolean teleportPhaseMode   = false;
    public float   teleportCursorX     = 0f;
    public float   teleportCursorY     = 0f;
    public float   teleportOriginX     = 0f;
    public float   teleportOriginY     = 0f;
    public boolean isTeleporting       = false;
    public float   teleportInvulnTimer = 0f;
    public float   teleportPhaseTimer  = 0f;

    public static final float TELEPORT_RANGE        = 256f;
    public static final float TELEPORT_PHASE_TIME   = 0.6f;
    public static final float TELEPORT_COOLDOWN     = 3.0f;
    public static final float TELEPORT_INVULN       = 0.25f;
    public static final float TELEPORT_CURSOR_SPEED = 420f;

    // ── Stamina + Mana ────────────────────────────────────────────────────────
    public float stamina = STAMINA_MAX;
    public float mana    = MANA_MAX;

    public static final float STAMINA_MAX        = 30f;
    public static final float STAMINA_REGEN_RATE = 12f;
    public static final float STAMINA_RUN_DRAIN  = 4f;
    public static final float MANA_MAX           = 30f;
    public static final float MANA_REGEN_RATE    = 6f;

    // ── Ninjutsu state ────────────────────────────────────────────────────────
    public boolean ninjutsuHeld     = false;
    public boolean ninjutsuCasting  = false;
    public float   ninjutsuCooldown = 0f;
    public float   ninjutsuCastTimer = 0f;

    public static final float NINJUTSU_MANA_COST = 25f;
    public static final float NINJUTSU_COOLDOWN  = 12f;
    public static final float NINJUTSU_CAST_TIME = 0.4f;

    // ── Wall jump state ───────────────────────────────────────────────────────
    public float wallCoyoteTimer   = 0f;
    public int   lastWallDir       = 0;
    public float wallJumpLockTimer = 0f;

    public static final float WALL_JUMP_INPUT_LOCK = 0.2f;

    // ── Wall slide state ──────────────────────────────────────────────────────
    public float   wallSlideStamina        = WALL_SLIDE_MAX_STAMINA;
    public boolean isWallSliding           = false;
    public boolean awaitGroundAfterExhaust = false;
    public int     exhaustDetachFrames     = 0;

    public static final float WALL_SLIDE_SPEED           = 3.0f;
    public static final float WALL_SLIDE_MAX_STAMINA     = 3.0f;
    public static final float WALL_SLIDE_REGEN_RATE      = 3.0f / 2.0f;
    public static final float WALL_SLIDE_MIN_STAMINA     = 0.3f;
    public static final float WALL_SLIDE_EXHAUST_THRESH  = 0.5f;
    public static final float WALL_SLIDE_EXHAUST_PENALTY = 0.25f;
    public static final float WALL_SLIDE_DRAIN_MULT      = 1.6f;
    public static final float WALL_FRICTION_SPEED        = 6.0f;

    // ── Progression ───────────────────────────────────────────────────────────
    public int experience = 0;
    public int level      = 1;
    public final java.util.Set<String> unlockedAbilities = new java.util.LinkedHashSet<>();
    public int maxMana    = (int) MANA_MAX;
    public int maxStamina = (int) STAMINA_MAX;

    public int xpToNextLevel() { return level * 50; }

    public int addXp(int xp) {
        experience += xp;
        int levelsGained = 0;
        while (experience >= xpToNextLevel()) {
            experience -= xpToNextLevel();
            level++;
            levelsGained++;
            maxHealth++;
            health = Math.min(health + 1, maxHealth);
            if (level % 3 == 0) maxMana += 5;
            if (level % 4 == 0) maxStamina += 3;
            if (level % 5 == 0 && shurikenAmmo < SHURIKEN_MAX_AMMO + 3) shurikenAmmo++;
            grantAbilitiesForLevel(level);
        }
        return levelsGained;
    }

    private void grantAbilitiesForLevel(int lvl) {
        switch (lvl) {
            case 2  -> unlockedAbilities.add("double_jump");
            case 3  -> unlockedAbilities.add("dash");
            case 5  -> unlockedAbilities.add("wall_jump");
            case 7  -> unlockedAbilities.add("shuriken");
            case 10 -> unlockedAbilities.add("teleport");
            case 12 -> unlockedAbilities.add("ninjutsu");
            default -> {}
        }
    }

    // ── Yin/Yang & Lantern (M4 — GDD §3.3/§3.4) ──────────────────────────────
    public final YinYangComponent yinYang = new YinYangComponent(-1);
    public final LanternComponent lantern = new LanternComponent(-1);
    public String stanceMode = "yin";

    // ── Flow recency ──────────────────────────────────────────────────────────
    public float lastMeaningfulActionTimer = 0f;

    // ── Noise / stealth ───────────────────────────────────────────────────────
    public float noiseLevel  = 0f;
    public float noiseRadius = 0f;

    // ── Teleport variant ──────────────────────────────────────────────────────
    public String teleportType = "shadow";

    // ── Weapon / animation state ──────────────────────────────────────────────
    public String weaponState              = "unarmed";
    public String yangPreferredWeaponState = "";

    // ── Inventory ─────────────────────────────────────────────────────────────
    public final SimInventory inventory = new SimInventory();

    // ── Input + echo ──────────────────────────────────────────────────────────
    public InputCommand latestInput = InputCommand.neutral(0);
    public final EchoRecorder echoRecorder = new EchoRecorder();

    public SimPlayer(String playerId, int slot, float spawnX, float spawnY) {
        this.playerId = playerId;
        this.slot     = slot;
        this.spawnX   = spawnX;
        this.spawnY   = spawnY;
        this.physics  = new PhysicsState(
            spawnX, spawnY,
            PhysicsConstants.PLAYER_WIDTH,
            PhysicsConstants.PLAYER_HEIGHT
        );
        this.health = maxHealth;
    }

    public boolean isAlive() {
        return health > 0 && !isDead;
    }

    public void takeDamage(int dmg) {
        if (invincibilityTicks > 0) return;
        health = Math.max(0, health - dmg);
        if (health <= 0) isDead = true;
        invincibilityTicks = INVINCIBILITY_TICKS;
        lantern.onDamage();
    }

    public void tickInvincibility() {
        if (invincibilityTicks > 0) invincibilityTicks--;
    }
}
