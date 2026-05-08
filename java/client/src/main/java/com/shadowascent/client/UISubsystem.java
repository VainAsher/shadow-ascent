package com.shadowascent.client;

import com.shadowascent.core.MissionManager;
import com.shadowascent.core.NPC;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.physics.TileRect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class UISubsystem {

    private static final float GATE_FEEDBACK_COOLDOWN_SECONDS = 1.2f;
    private static final float FEEDBACK_FLASH_SECONDS = 4.5f;
    private static final int   EVENT_LOG_MAX_SIZE = 7;

    private final StoryState storyState;
    private final TraversalSubsystem traversalSubsystem;
    private final CombatSubsystem combatSubsystem;
    private final List<TileRect> collisionTiles;
    private final WorldGeometry geometry;
    private final Consumer<String> evidenceLogger;

    private final Map<String, Point2D.Float> npcPositions = new HashMap<>();
    private final MinimapRenderer minimapRenderer;
    private final Deque<String> eventLog = new ArrayDeque<>();
    private final HudRenderer hudRenderer;

    private float gateFeedbackCooldownSeconds;

    public record RenderState(
            float playerX,
            float playerY,
            float cameraX,
            boolean isDashing,
            float dashTimerSeconds,
            float dashCooldownSeconds,
            int playerHealth,
            boolean playerDead,
            int jumpCount,
            float wallStaminaSeconds,
            boolean wallExhaustedAwaitGround,
            float attackCooldownSeconds,
            Set<String> clearedCombatEncounterIds
    ) {}

    UISubsystem(
            StoryState storyState,
            MissionManager missionManager,
            TraversalSubsystem traversalSubsystem,
            CombatSubsystem combatSubsystem,
            List<TileRect> collisionTiles,
            WorldGeometry geometry,
            Consumer<String> evidenceLogger) {
        this.storyState = storyState;
        this.traversalSubsystem = traversalSubsystem;
        this.combatSubsystem = combatSubsystem;
        this.collisionTiles = collisionTiles;
        this.geometry = geometry;
        this.evidenceLogger = evidenceLogger;
        this.minimapRenderer = new MinimapRenderer(geometry, collisionTiles,
                traversalSubsystem, combatSubsystem, storyState, npcPositions);
        this.hudRenderer = new HudRenderer(storyState, missionManager,
                combatSubsystem, traversalSubsystem, this::addEventLogLine);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void drawFrame(Graphics2D g, RenderState state) {
        drawBackground(g);
        Graphics2D worldG = (Graphics2D) g.create();
        worldG.translate(-state.cameraX(), 0f);
        drawHubRing(worldG);
        drawWorldGeometry(worldG, state);
        drawNpcs(worldG, state);
        drawPlayer(worldG, state);
        worldG.dispose();
        hudRenderer.draw(g, state);
        drawEventLog(g);
        if (hudRenderer.isShowMinimap()) {
            minimapRenderer.draw(g, state);
        }
    }

    public void tickAndUpdate(float playerX, float playerY, float dt) {
        gateFeedbackCooldownSeconds = Math.max(0f, gateFeedbackCooldownSeconds - dt);
        updateNpcPositions();
        hudRenderer.tick(playerX, playerY, dt);
    }

    public void addEventLogLine(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        eventLog.addFirst(message);
        while (eventLog.size() > EVENT_LOG_MAX_SIZE) {
            eventLog.removeLast();
        }
        evidenceLogger.accept(message);
    }

    public void setMissionFeedback(String line, float seconds) {
        hudRenderer.setMissionFeedback(line, seconds);
    }

    public void notifyAbilityGateBlocked(TileRect tile, Set<String> clearedCombatEncounterIds) {
        if (gateFeedbackCooldownSeconds > 0f) {
            return;
        }
        AbilityGate gate = traversalSubsystem.gateForTile(tile);
        if (gate != null && !gate.isUnlocked(storyState)) {
            addEventLogLine("Traversal gate locked: " + gate.displayName()
                    + " (requires ability `" + gate.requiredAbility() + "`).");
            gateFeedbackCooldownSeconds = GATE_FEEDBACK_COOLDOWN_SECONDS;
            hudRenderer.setMissionFeedback(
                    "Gate blocked: " + gate.displayName() + " needs " + gate.requiredAbility() + ".",
                    FEEDBACK_FLASH_SECONDS);
            return;
        }
        CombatBarrier combatBarrier = traversalSubsystem.barrierForTile(tile);
        if (combatBarrier != null && !combatBarrier.isUnlocked(clearedCombatEncounterIds)) {
            addEventLogLine("Combat barrier active: " + combatBarrier.displayName()
                    + " (clear encounter `" + combatBarrier.requiredEncounterId() + "`).");
            gateFeedbackCooldownSeconds = GATE_FEEDBACK_COOLDOWN_SECONDS;
            hudRenderer.setMissionFeedback(
                    "Barrier active: clear " + combatBarrier.displayName() + ".",
                    FEEDBACK_FLASH_SECONDS);
        }
    }

    public void toggleMinimap() {
        hudRenderer.toggleMinimap();
    }

    public void seedAbilitySnapshot(Set<String> currentAbilities) {
        hudRenderer.seedAbilitySnapshot(currentAbilities);
    }

    public void setOverlayStatusLine(String statusLine) {
        hudRenderer.setOverlayStatusLine(statusLine);
    }

    public Map<String, Point2D.Float> getNpcPositions() {
        return java.util.Collections.unmodifiableMap(npcPositions);
    }

    // -------------------------------------------------------------------------
    // Display updates
    // -------------------------------------------------------------------------

    private void updateNpcPositions() {
        List<NPC> active = activeNpcsSorted();
        float minX = geometry.worldLeftX() + 120f;
        float maxX = geometry.hubRoomEndX() - 120f;
        float y = geometry.floorY() - 28f;

        if (active.isEmpty()) {
            npcPositions.clear();
            return;
        }

        if (active.size() == 1) {
            npcPositions.put(active.get(0).getId(), new Point2D.Float((minX + maxX) * 0.5f, y));
            return;
        }

        for (int i = 0; i < active.size(); i++) {
            NPC npc = active.get(i);
            float t = i / (float) (active.size() - 1);
            float x = minX + ((maxX - minX) * t);
            npcPositions.put(npc.getId(), new Point2D.Float(x, y));
        }
    }

    // -------------------------------------------------------------------------
    // Draw methods
    // -------------------------------------------------------------------------

    private void drawBackground(Graphics2D g) {
        g.setColor(new Color(20, 27, 44));
        g.fillRect(0, 0, (int) geometry.windowWidth(), (int) geometry.windowHeight());

        g.setColor(new Color(26, 39, 66));
        g.fillRect(0, (int) geometry.floorY(), (int) geometry.windowWidth(),
                (int) geometry.windowHeight() - (int) geometry.floorY());

        g.setColor(new Color(60, 79, 121));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(0, (int) geometry.floorY(), (int) geometry.windowWidth(), (int) geometry.floorY());
    }

    private void drawHubRing(Graphics2D g) {
        g.setStroke(new BasicStroke(1.5f));
        drawRoomFrame(g, geometry.worldLeftX() + 8f,
                geometry.hubRoomEndX() - geometry.worldLeftX() - 16f, "Lantern Hub");
        drawRoomFrame(g, geometry.hubRoomEndX() + 8f,
                geometry.forgeRoomEndX() - geometry.hubRoomEndX() - 16f, "Forge Terraces");
        drawRoomFrame(g, geometry.forgeRoomEndX() + 8f,
                geometry.shaftRoomEndX() - geometry.forgeRoomEndX() - 16f, "Hollow Shaft");
        drawRoomFrame(g, geometry.shaftRoomEndX() + 8f,
                geometry.summitRoomEndX() - geometry.shaftRoomEndX() - 16f, "Summit Route");
    }

    private void drawRoomFrame(Graphics2D g, float roomStartX, float roomWidth, String label) {
        float top = geometry.ceilingY() + 24f;
        float height = geometry.floorY() - geometry.ceilingY() - 44f;
        g.setColor(new Color(116, 145, 204, 92));
        g.drawRoundRect(Math.round(roomStartX), Math.round(top), Math.round(roomWidth), Math.round(height), 26, 26);
        g.setColor(new Color(186, 216, 255, 210));
        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.drawString(label, roomStartX + 12f, top + 18f);
    }

    private void drawWorldGeometry(Graphics2D g, RenderState state) {
        for (TileRect tile : collisionTiles) {
            if (tile.isPlatform()) {
                g.setColor(new Color(140, 188, 248, 180));
            } else {
                g.setColor(new Color(89, 121, 173, 190));
            }
            g.fillRect((int) tile.x(), (int) tile.y(), Math.round(tile.w()), Math.round(tile.h()));
            g.setColor(new Color(24, 37, 64, 220));
            g.drawRect((int) tile.x(), (int) tile.y(), Math.round(tile.w()), Math.round(tile.h()));
        }

        for (MovingPlatform platform : traversalSubsystem.allMovingPlatforms()) {
            TileRect tile = platform.toTileRect();
            g.setColor(new Color(247, 196, 120, 210));
            g.fillRect((int) tile.x(), (int) tile.y(), Math.round(tile.w()), Math.round(tile.h()));
            g.setColor(new Color(78, 52, 28, 230));
            g.drawRect((int) tile.x(), (int) tile.y(), Math.round(tile.w()), Math.round(tile.h()));
        }

        for (TriggeredPlatform platform : traversalSubsystem.allTriggeredPlatforms()) {
            boolean active = traversalSubsystem.isTriggerActivated(platform.requiredTriggerId());
            if (active) {
                g.setColor(new Color(140, 244, 186, 215));
                g.fillRect(Math.round(platform.x()), Math.round(platform.y()),
                        Math.round(platform.width()), Math.round(platform.height()));
                g.setColor(new Color(46, 108, 74, 235));
                g.drawRect(Math.round(platform.x()), Math.round(platform.y()),
                        Math.round(platform.width()), Math.round(platform.height()));
            } else {
                g.setColor(new Color(122, 144, 176, 75));
                g.drawRect(Math.round(platform.x()), Math.round(platform.y()),
                        Math.round(platform.width()), Math.round(platform.height()));
            }
        }

        for (CombatBarrier barrier : traversalSubsystem.allCombatBarriers()) {
            boolean unlocked = barrier.isUnlocked(state.clearedCombatEncounterIds());
            if (unlocked) {
                g.setColor(new Color(82, 210, 129, 50));
                g.fillRect(Math.round(barrier.x()), Math.round(barrier.y()),
                        Math.round(barrier.width()), Math.round(barrier.height()));
                g.setColor(new Color(82, 210, 129, 132));
            } else {
                g.setColor(new Color(232, 142, 90, 112));
                g.fillRect(Math.round(barrier.x()), Math.round(barrier.y()),
                        Math.round(barrier.width()), Math.round(barrier.height()));
                g.setColor(new Color(232, 142, 90, 228));
            }
            g.drawRect(Math.round(barrier.x()), Math.round(barrier.y()),
                    Math.round(barrier.width()), Math.round(barrier.height()));
        }

        for (AbilityGate gate : traversalSubsystem.allGates()) {
            boolean unlocked = gate.isUnlocked(storyState);
            if (unlocked) {
                g.setColor(new Color(82, 210, 129, 60));
            } else {
                g.setColor(new Color(224, 102, 102, 110));
            }
            g.fillRect((int) gate.x(), (int) gate.y(), Math.round(gate.width()), Math.round(gate.height()));
            g.setColor(unlocked ? new Color(82, 210, 129, 160) : new Color(224, 102, 102, 230));
            g.drawRect((int) gate.x(), (int) gate.y(), Math.round(gate.width()), Math.round(gate.height()));
        }

        for (AbilityTrigger trigger : traversalSubsystem.allTriggers()) {
            boolean active = traversalSubsystem.isTriggerActivated(trigger.id());
            boolean unlocked = storyState.hasAbility(trigger.requiredAbility());
            if (active) {
                g.setColor(new Color(118, 250, 198, 195));
            } else if (unlocked) {
                g.setColor(trigger.mode() == TriggerMode.DASH_PASS
                        ? new Color(120, 225, 255, 190)
                        : new Color(251, 215, 132, 200));
            } else {
                g.setColor(new Color(193, 109, 109, 180));
            }
            g.fillRoundRect(Math.round(trigger.x()), Math.round(trigger.y()),
                    Math.round(trigger.width()), Math.round(trigger.height()), 12, 12);
            g.setColor(new Color(19, 24, 38, 235));
            g.drawRoundRect(Math.round(trigger.x()), Math.round(trigger.y()),
                    Math.round(trigger.width()), Math.round(trigger.height()), 12, 12);
        }

        for (CombatEncounter encounter : combatSubsystem.allEncounters()) {
            g.setColor(encounter.renderColorWorld());
            int radius = Math.round(encounter.activationRadius());
            int cx = Math.round(encounter.centerX());
            int cy = Math.round(encounter.centerY());
            g.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
            g.fillOval(cx - 12, cy - 12, 24, 24);
            g.setColor(new Color(14, 22, 34, 230));
            g.setFont(new Font("Dialog", Font.BOLD, 11));
            g.drawString(Integer.toString(Math.max(0, encounter.hitsRemaining())), cx - 4, cy + 4);
            g.setFont(new Font("Dialog", Font.PLAIN, 10));
            g.drawString(encounter.phaseShortLabel(), cx - 16, cy - 16);
        }
    }

    private void drawNpcs(Graphics2D g, RenderState state) {
        List<NPC> active = activeNpcsSorted();
        for (NPC npc : active) {
            Point2D.Float point = npcPositions.get(npc.getId());
            if (point == null) {
                continue;
            }

            float dist = distance(point.x, point.y, state.playerX(), state.playerY());
            boolean near = dist <= geometry.interactRadius();

            g.setColor(near ? new Color(255, 221, 126) : new Color(179, 212, 255));
            g.fillOval((int) point.x - 16, (int) point.y - 16, 32, 32);
            g.setColor(new Color(18, 22, 36));
            g.drawOval((int) point.x - 16, (int) point.y - 16, 32, 32);
            g.setColor(java.awt.Color.WHITE);
            g.setFont(new Font("Dialog", Font.BOLD, 12));
            g.drawString(npc.getDisplayName(), point.x - 26, point.y - 24);
            if (near) {
                g.setColor(new Color(255, 242, 196));
                g.drawString("[E] Talk", point.x - 24, point.y + 30);
            }
        }
    }

    private void drawPlayer(Graphics2D g, RenderState state) {
        g.setColor(new Color(142, 255, 204));
        g.fillOval((int) state.playerX() - 14, (int) state.playerY() - 14, 28, 28);
        g.setColor(new Color(20, 36, 28));
        g.drawOval((int) state.playerX() - 14, (int) state.playerY() - 14, 28, 28);
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.drawString("Aen", state.playerX() - 10, state.playerY() - 18);
    }

    private void drawEventLog(Graphics2D g) {
        g.setColor(new Color(8, 12, 20, 210));
        g.fillRoundRect(14, 565, (int) geometry.windowWidth() - 28, 140, 14, 14);
        g.setColor(new Color(173, 201, 255));
        g.drawRoundRect(14, 565, (int) geometry.windowWidth() - 28, 140, 14, 14);

        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        g.setColor(java.awt.Color.WHITE);

        int y = 586;
        for (String line : eventLog) {
            g.drawString(line, 26, y);
            y += 16;
            if (y > 694) {
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<NPC> activeNpcsSorted() {
        List<NPC> active = new ArrayList<>();
        for (NPC npc : storyState.getAllNPCs().values()) {
            if (npc.isActive()) {
                active.add(npc);
            }
        }
        active.sort(Comparator.comparing(NPC::getId));
        return active;
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
