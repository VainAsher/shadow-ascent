package com.shadowascent.client.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.SimPlayer;

import java.util.List;

public final class MinimapOverlayRenderer {
    private static final float PANEL_WIDTH = 204f;
    private static final float PANEL_HEIGHT = 160f;
    private static final float CONTENT_PADDING = 10f;
    private static final float MARKER_SIZE = 4f;

    private final ShapeRenderer shapes;
    private final Matrix4 screenProjection = new Matrix4();

    public MinimapOverlayRenderer(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    public void render(
            HudOverlayState state,
            GameSimulator simulator,
            List<TileRect> worldTiles,
            RunGameContentProfile contentProfile,
            float x,
            float y) {
        if (!state.showMinimap()) {
            return;
        }

        screenProjection.setToOrtho(0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0f, 0f, 1f);
        shapes.setProjectionMatrix(screenProjection);

        float contentX = x + CONTENT_PADDING;
        float contentY = y + CONTENT_PADDING;
        float contentWidth = PANEL_WIDTH - CONTENT_PADDING * 2f;
        float contentHeight = PANEL_HEIGHT - CONTENT_PADDING * 2f;

        drawPanel(x, y);

        if (worldTiles == null || worldTiles.isEmpty()) {
            return;
        }

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for (TileRect tile : worldTiles) {
            minX = Math.min(minX, tile.x());
            minY = Math.min(minY, tile.y());
            maxX = Math.max(maxX, tile.right());
            maxY = Math.max(maxY, tile.bottom());
        }

        float worldWidth = Math.max(1f, maxX - minX);
        float worldHeight = Math.max(1f, maxY - minY);
        float scale = Math.min(contentWidth / worldWidth, contentHeight / worldHeight);
        float drawWidth = worldWidth * scale;
        float drawHeight = worldHeight * scale;
        float offsetX = contentX + (contentWidth - drawWidth) * 0.5f;
        float offsetY = contentY + (contentHeight - drawHeight) * 0.5f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(UiPalette.TEXT_MUTED);
        for (TileRect tile : worldTiles) {
            float tileX = offsetX + (tile.x() - minX) * scale;
            float tileY = offsetY + (tile.y() - minY) * scale;
            shapes.rect(tileX, tileY, Math.max(1f, tile.w() * scale), Math.max(1f, tile.h() * scale));
        }

        shapes.setColor(UiPalette.ACCENT);
        for (SimPlayer player : simulator.getPlayers()) {
            float playerCenterX = player.physics.x + player.physics.width * 0.5f;
            float playerCenterY = player.physics.y + player.physics.height * 0.5f;
            float markerX = mapMarkerCoordinate(
                    playerCenterX, minX, scale, contentX, offsetX, contentWidth, drawWidth, MARKER_SIZE
            );
            float markerY = mapMarkerCoordinate(
                    playerCenterY, minY, scale, contentY, offsetY, contentHeight, drawHeight, MARKER_SIZE
            );
            shapes.rect(markerX, markerY, MARKER_SIZE, MARKER_SIZE);
        }
        if (contentProfile != null) {
            shapes.setColor(UiPalette.TEXT);
            for (RunGameContentProfile.NpcPlacement npc : contentProfile.npcPlacements()) {
                boolean highlighted = state.highlightedNpcIds().stream().anyMatch(npc.npcId()::equalsIgnoreCase);
                shapes.setColor(highlighted ? UiPalette.ACCENT : UiPalette.TEXT_MUTED);
                float markerSize = highlighted ? MARKER_SIZE + 2f : MARKER_SIZE;
                float markerX = mapMarkerCoordinate(
                        npc.x(), minX, scale, contentX, offsetX, contentWidth, drawWidth, markerSize
                );
                float markerY = mapMarkerCoordinate(
                        npc.y(), minY, scale, contentY, offsetY, contentHeight, drawHeight, markerSize
                );
                shapes.rect(markerX, markerY, markerSize, markerSize);
            }
        }
        if (contentProfile != null) {
            shapes.setColor(UiPalette.WARNING);
            for (com.shadowascent.client.world.RoomTransitionSpec transition : contentProfile.roomTransitions()) {
                float gateX = mapGateCoordinate(
                        transition.minX(), transition.maxX(), minX, scale, contentX, offsetX, contentWidth, drawWidth, MARKER_SIZE
                );
                float gateY = mapGateVerticalCoordinate(
                        transition.minY(),
                        transition.maxY(),
                        minY,
                        scale,
                        contentY,
                        offsetY,
                        contentHeight,
                        drawHeight,
                        MARKER_SIZE
                );
                shapes.rect(gateX, gateY, MARKER_SIZE, MARKER_SIZE);
            }
            for (RunGameContentProfile.AreaGate gate : contentProfile.areaGates()) {
                float gateX = mapGateCoordinate(
                        gate.minX(), gate.maxX(), minX, scale, contentX, offsetX, contentWidth, drawWidth, MARKER_SIZE
                );
                float gateY = contentY + contentHeight - MARKER_SIZE - 6f;
                shapes.rect(gateX, gateY, MARKER_SIZE, MARKER_SIZE);
            }
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(UiPalette.PANEL_BORDER);
        shapes.rect(contentX, contentY, contentWidth, contentHeight);
        shapes.end();
    }

    private void drawPanel(float x, float y) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(UiPalette.PANEL_FILL);
        shapes.rect(x, y, PANEL_WIDTH, PANEL_HEIGHT);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(UiPalette.PANEL_BORDER);
        shapes.rect(x, y, PANEL_WIDTH, PANEL_HEIGHT);
        shapes.end();
    }

    static float mapMarkerCoordinate(float worldCoordinate,
                                     float worldMin,
                                     float scale,
                                     float contentStart,
                                     float drawStart,
                                     float contentSize,
                                     float drawSize,
                                     float markerSize) {
        float mapped = drawStart + (worldCoordinate - worldMin) * scale - markerSize * 0.5f;
        float minStart = Math.max(contentStart, drawStart);
        float maxStart = Math.min(
                contentStart + Math.max(0f, contentSize - markerSize),
                drawStart + Math.max(0f, drawSize - markerSize)
        );
        return Math.max(minStart, Math.min(mapped, maxStart));
    }

    static float mapGateCoordinate(float gateMinX,
                                   float gateMaxX,
                                   float worldMin,
                                   float scale,
                                   float contentStart,
                                   float drawStart,
                                   float contentSize,
                                   float drawSize,
                                   float markerSize) {
        float center = (gateMinX + gateMaxX) * 0.5f;
        return mapMarkerCoordinate(center, worldMin, scale, contentStart, drawStart, contentSize, drawSize, markerSize);
    }

    static float mapGateVerticalCoordinate(Float gateMinY,
                                           Float gateMaxY,
                                           float worldMin,
                                           float scale,
                                           float contentStart,
                                           float drawStart,
                                           float contentSize,
                                           float drawSize,
                                           float markerSize) {
        if (gateMinY == null && gateMaxY == null) {
            return contentStart + contentSize - markerSize - 6f;
        }
        float minY = gateMinY == null ? gateMaxY.floatValue() : gateMinY;
        float maxY = gateMaxY == null ? gateMinY.floatValue() : gateMaxY;
        float center = (minY + maxY) * 0.5f;
        return mapMarkerCoordinate(center, worldMin, scale, contentStart, drawStart, contentSize, drawSize, markerSize);
    }
}
