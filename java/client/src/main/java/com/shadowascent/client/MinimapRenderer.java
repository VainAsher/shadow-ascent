package com.shadowascent.client;

import com.shadowascent.core.NPC;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.physics.TileRect;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Standalone minimap panel renderer for PlaytestClient.
 *
 * Extracted from UISubsystem.drawMinimap — same Swing Graphics2D rendering,
 * same data dependencies, isolated for independent test and future iteration.
 * Adapted from indie-ninja-adventures MinimapRenderer (LibGDX → Swing).
 */
final class MinimapRenderer {

    static final int PANEL_W          = 240;
    static final int PANEL_H          = 165;
    static final int INNER_PAD        = 10;
    static final int PANEL_MARGIN_RIGHT = 16;
    static final int PANEL_TOP        = 102;

    private final WorldGeometry geometry;
    private final List<TileRect> collisionTiles;
    private final TraversalSubsystem traversalSubsystem;
    private final CombatSubsystem combatSubsystem;
    private final StoryState storyState;
    private final Map<String, Point2D.Float> npcPositions;

    MinimapRenderer(WorldGeometry geometry,
                    List<TileRect> collisionTiles,
                    TraversalSubsystem traversalSubsystem,
                    CombatSubsystem combatSubsystem,
                    StoryState storyState,
                    Map<String, Point2D.Float> npcPositions) {
        this.geometry          = geometry;
        this.collisionTiles    = collisionTiles;
        this.traversalSubsystem = traversalSubsystem;
        this.combatSubsystem   = combatSubsystem;
        this.storyState        = storyState;
        this.npcPositions      = npcPositions;
    }

    void draw(Graphics2D g, UISubsystem.RenderState state) {
        int panelX = (int) geometry.windowWidth() - PANEL_W - PANEL_MARGIN_RIGHT;
        int panelY = PANEL_TOP;

        g.setColor(new Color(8, 12, 20, 218));
        g.fillRoundRect(panelX, panelY, PANEL_W, PANEL_H, 14, 14);
        g.setColor(new Color(173, 201, 255));
        g.drawRoundRect(panelX, panelY, PANEL_W, PANEL_H, 14, 14);
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.drawString("Minimap", panelX + 12, panelY + 18);

        float mapLeft   = panelX + INNER_PAD;
        float mapTop    = panelY + 24;
        float mapWidth  = PANEL_W - (INNER_PAD * 2f);
        float mapHeight = PANEL_H - 34f;

        g.setColor(new Color(23, 36, 58));
        g.fillRect(Math.round(mapLeft), Math.round(mapTop), Math.round(mapWidth), Math.round(mapHeight));
        g.setColor(new Color(88, 118, 171));
        g.drawRect(Math.round(mapLeft), Math.round(mapTop), Math.round(mapWidth), Math.round(mapHeight));

        float worldWidth  = geometry.worldRightX() - geometry.worldLeftX();
        float worldHeight = geometry.floorY()       - geometry.ceilingY();

        for (TileRect tile : collisionTiles) {
            float normX = (tile.x() - geometry.worldLeftX()) / worldWidth;
            float normY = (tile.y() - geometry.ceilingY())   / worldHeight;
            float normW = tile.w() / worldWidth;
            float normH = tile.h() / worldHeight;
            int rx = Math.round(mapLeft + (normX * mapWidth));
            int ry = Math.round(mapTop  + (normY * mapHeight));
            int rw = Math.max(2, Math.round(normW * mapWidth));
            int rh = Math.max(2, Math.round(normH * mapHeight));
            g.setColor(tile.isPlatform() ? new Color(142, 202, 255) : new Color(111, 153, 218));
            g.fillRect(rx, ry, rw, rh);
        }

        for (MovingPlatform platform : traversalSubsystem.allMovingPlatforms()) {
            TileRect tile = platform.toTileRect();
            float normX = (tile.x() - geometry.worldLeftX()) / worldWidth;
            float normY = (tile.y() - geometry.ceilingY())   / worldHeight;
            int rx = Math.round(mapLeft + (normX * mapWidth));
            int ry = Math.round(mapTop  + ((tile.y() - geometry.ceilingY()) / worldHeight * mapHeight));
            int rw = Math.max(2, Math.round((tile.w() / worldWidth) * mapWidth));
            int rh = Math.max(2, Math.round((tile.h() / worldHeight) * mapHeight));
            g.setColor(new Color(245, 198, 125));
            g.fillRect(rx, ry, rw, rh);
        }

        for (TriggeredPlatform platform : traversalSubsystem.allTriggeredPlatforms()) {
            float normX = (platform.x() - geometry.worldLeftX()) / worldWidth;
            float normY = (platform.y() - geometry.ceilingY())   / worldHeight;
            int rx = Math.round(mapLeft + (normX * mapWidth));
            int ry = Math.round(mapTop  + (normY * mapHeight));
            int rw = Math.max(2, Math.round((platform.width()  / worldWidth)  * mapWidth));
            int rh = Math.max(2, Math.round((platform.height() / worldHeight) * mapHeight));
            if (traversalSubsystem.isTriggerActivated(platform.requiredTriggerId())) {
                g.setColor(new Color(138, 246, 188, 200));
                g.fillRect(rx, ry, rw, rh);
            } else {
                g.setColor(new Color(102, 126, 160, 130));
                g.drawRect(rx, ry, rw, rh);
            }
        }

        for (CombatBarrier barrier : traversalSubsystem.allCombatBarriers()) {
            float normX = (barrier.x() - geometry.worldLeftX()) / worldWidth;
            float normY = (barrier.y() - geometry.ceilingY())   / worldHeight;
            int rx = Math.round(mapLeft + (normX * mapWidth));
            int ry = Math.round(mapTop  + (normY * mapHeight));
            int rw = Math.max(2, Math.round((barrier.width()  / worldWidth)  * mapWidth));
            int rh = Math.max(2, Math.round((barrier.height() / worldHeight) * mapHeight));
            g.setColor(barrier.isUnlocked(state.clearedCombatEncounterIds())
                    ? new Color(96, 218, 152, 86) : new Color(230, 144, 89, 194));
            g.fillRect(rx, ry, rw, rh);
        }

        for (AbilityGate gate : traversalSubsystem.allGates()) {
            float normX = (gate.x() - geometry.worldLeftX()) / worldWidth;
            float normY = (gate.y() - geometry.ceilingY())   / worldHeight;
            int rx = Math.round(mapLeft + (normX * mapWidth));
            int ry = Math.round(mapTop  + (normY * mapHeight));
            int rw = Math.max(2, Math.round((gate.width()  / worldWidth)  * mapWidth));
            int rh = Math.max(2, Math.round((gate.height() / worldHeight) * mapHeight));
            g.setColor(gate.isUnlocked(storyState)
                    ? new Color(92, 208, 132, 120) : new Color(230, 112, 112, 190));
            g.fillRect(rx, ry, rw, rh);
        }

        for (CombatEncounter encounter : combatSubsystem.allEncounters()) {
            int ex = Math.round(mapLeft + (((encounter.centerX() - geometry.worldLeftX()) / worldWidth) * mapWidth));
            int ey = Math.round(mapTop  + (((encounter.centerY() - geometry.ceilingY())   / worldHeight) * mapHeight));
            g.setColor(encounter.renderColorMap());
            g.fillOval(ex - 3, ey - 3, 7, 7);
        }

        for (AbilityTrigger trigger : traversalSubsystem.allTriggers()) {
            float normX = (trigger.x() - geometry.worldLeftX()) / worldWidth;
            float normY = (trigger.y() - geometry.ceilingY())   / worldHeight;
            int rx = Math.round(mapLeft + (normX * mapWidth));
            int ry = Math.round(mapTop  + (normY * mapHeight));
            int rw = Math.max(2, Math.round((trigger.width()  / worldWidth)  * mapWidth));
            int rh = Math.max(2, Math.round((trigger.height() / worldHeight) * mapHeight));
            if (traversalSubsystem.isTriggerActivated(trigger.id())) {
                g.setColor(new Color(123, 244, 194));
            } else if (storyState.hasAbility(trigger.requiredAbility())) {
                g.setColor(trigger.mode() == TriggerMode.DASH_PASS
                        ? new Color(129, 221, 249) : new Color(248, 214, 126));
            } else {
                g.setColor(new Color(206, 117, 117));
            }
            g.fillRect(rx, ry, rw, rh);
        }

        float viewWorldLeft = clamp(state.cameraX(), geometry.worldLeftX(), geometry.worldRightX());
        int viewLeft  = Math.round(mapLeft + (((viewWorldLeft - geometry.worldLeftX()) / worldWidth) * mapWidth));
        int viewWidth = Math.max(8, Math.round((geometry.windowWidth() / worldWidth) * mapWidth));
        int mapRight  = Math.round(mapLeft + mapWidth);
        int mapLeftInt = Math.round(mapLeft);
        viewLeft = Math.max(mapLeftInt, Math.min(viewLeft, mapRight - viewWidth));
        g.setColor(new Color(228, 242, 255, 70));
        g.fillRect(viewLeft, Math.round(mapTop), viewWidth, Math.round(mapHeight));
        g.setColor(new Color(228, 242, 255, 180));
        g.drawRect(viewLeft, Math.round(mapTop), viewWidth, Math.round(mapHeight));

        for (NPC npc : activeNpcsSorted()) {
            Point2D.Float point = npcPositions.get(npc.getId());
            if (point == null) continue;
            int px = Math.round(mapLeft + (((point.x - geometry.worldLeftX()) / worldWidth) * mapWidth));
            int py = Math.round(mapTop  + (((point.y - geometry.ceilingY())   / worldHeight) * mapHeight));
            g.setColor(new Color(255, 224, 144));
            g.fillOval(px - 2, py - 2, 4, 4);
        }

        int playerMapX = Math.round(mapLeft + (((state.playerX() - geometry.worldLeftX()) / worldWidth) * mapWidth));
        int playerMapY = Math.round(mapTop  + (((state.playerY() - geometry.ceilingY())   / worldHeight) * mapHeight));
        g.setColor(new Color(124, 255, 208));
        g.fillOval(playerMapX - 3, playerMapY - 3, 6, 6);
    }

    private List<NPC> activeNpcsSorted() {
        List<NPC> active = new ArrayList<>();
        for (NPC npc : storyState.getAllNPCs().values()) {
            if (npc.isActive()) active.add(npc);
        }
        active.sort(Comparator.comparing(NPC::getId));
        return active;
    }

    private static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }
}
