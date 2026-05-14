package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

import java.util.List;

public final class HudOverlayRenderer {
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeRenderer shapes;
    private final Matrix4 shapeProjection = new Matrix4();
    private final Matrix4 textProjection = new Matrix4();

    public HudOverlayRenderer(SpriteBatch batch, BitmapFont font, ShapeRenderer shapes) {
        this.batch = batch;
        this.font = font;
        this.shapes = shapes;
    }

    public void render(HudOverlayState state, int screenWidth, int screenHeight) {
        shapeProjection.setToOrtho(0f, screenWidth, screenHeight, 0f, 0f, 1f);
        textProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        shapes.setProjectionMatrix(shapeProjection);
        batch.setProjectionMatrix(textProjection);

        float statusHeight = 210f;
        float statusX = UiPalette.PANEL_PADDING;
        float statusY = UiPalette.PANEL_PADDING;
        float feedX = UiPalette.PANEL_PADDING;
        float feedY = screenHeight - UiPalette.PANEL_PADDING - UiPalette.FEED_HEIGHT;

        drawPanel(statusX, statusY, UiPalette.PANEL_WIDTH, statusHeight);
        drawPanel(feedX, feedY, UiPalette.PANEL_WIDTH, UiPalette.FEED_HEIGHT);

        batch.begin();
        drawStatusBlock(state, statusX, statusY, screenHeight);
        drawFeedBlock(state.eventFeedLines(), feedX, feedY, screenHeight);
        batch.end();
    }

    private void drawPanel(float x, float y, float width, float height) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(UiPalette.PANEL_FILL);
        shapes.rect(x, y, width, height);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(UiPalette.PANEL_BORDER);
        shapes.rect(x, y, width, height);
        shapes.end();
    }

    private void drawStatusBlock(HudOverlayState state, float panelX, float panelY, int screenHeight) {
        float textX = panelX + UiPalette.PANEL_PADDING;
        float lineY = baselineFromTop(panelY + 24f, screenHeight);

        font.setColor(UiPalette.TEXT);
        font.draw(batch, "Shadow Ascent HUD", textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(UiPalette.TEXT_MUTED);
        font.draw(batch, "Act: " + state.actId() + "  |  Plateau: " + state.plateauId(), textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.draw(batch, "Area: " + UiText.areaName(state.areaId()), textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(state.playerHealth() <= 1 ? UiPalette.DANGER : UiPalette.ACCENT);
        font.draw(batch, UiText.healthLine(state.playerHealth(), state.playerMaxHealth()), textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(UiPalette.TEXT);
        font.draw(batch, "Mission: " + UiText.missionTitle(state.missionTitle()), textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.draw(batch, "Objective: " + UiText.objectiveLine(state.objectiveLine()), textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(UiPalette.WARNING);
        font.draw(batch, UiText.contextualHint(state.contextualHint()), textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(UiPalette.TEXT);
        font.draw(batch, UiText.contextualHint(state.interactionHint()), textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(UiPalette.TEXT_MUTED);
        font.draw(batch, state.overlayStatus(), textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.draw(batch, "Minimap: " + (state.showMinimap() ? "planned visible" : "hidden"), textX, lineY);
    }

    private void drawFeedBlock(List<String> eventFeedLines, float panelX, float panelY, int screenHeight) {
        float textX = panelX + UiPalette.PANEL_PADDING;
        float lineY = baselineFromTop(panelY + 24f, screenHeight);

        font.setColor(UiPalette.TEXT);
        font.draw(batch, "Event Feed", textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(UiPalette.TEXT_MUTED);
        if (eventFeedLines.isEmpty()) {
            font.draw(batch, "No recent events.", textX, lineY);
            return;
        }

        int visibleLines = Math.min(4, eventFeedLines.size());
        for (int i = 0; i < visibleLines; i++) {
            font.draw(batch, eventFeedLines.get(i), textX, lineY);
            lineY -= UiPalette.LINE_HEIGHT;
        }
    }

    private static float baselineFromTop(float topY, int screenHeight) {
        return screenHeight - topY;
    }
}
