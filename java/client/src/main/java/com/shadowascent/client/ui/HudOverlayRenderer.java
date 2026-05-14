package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;

import java.util.List;

public final class HudOverlayRenderer {
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeRenderer shapes;
    private final Matrix4 shapeProjection = new Matrix4();
    private final Matrix4 textProjection = new Matrix4();
    private final GlyphLayout layout = new GlyphLayout();

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

        float statusHeight = 268f;
        float statusX = UiPalette.PANEL_PADDING;
        float statusY = UiPalette.PANEL_PADDING;
        float feedX = UiPalette.PANEL_PADDING;
        float feedY = screenHeight - UiPalette.PANEL_PADDING - 148f;
        float panelWidth = Math.min(500f, screenWidth * 0.42f);

        drawPanel(statusX, statusY, panelWidth, statusHeight);
        drawPanel(feedX, feedY, panelWidth, 148f);

        batch.begin();
        drawStatusBlock(state, statusX, statusY, panelWidth, screenHeight);
        drawFeedBlock(state.eventFeedLines(), feedX, feedY, panelWidth, screenHeight);
        batch.end();
    }

    private void drawPanel(float x, float y, float width, float height) {
        UiPanelRenderer.drawPanel(shapes, shapeProjection, x, y, width, height);
    }

    private void drawStatusBlock(HudOverlayState state, float panelX, float panelY, float panelWidth, int screenHeight) {
        float textX = panelX + UiPalette.PANEL_PADDING;
        float textWidth = panelWidth - UiPalette.PANEL_PADDING * 2f;
        float lineY = baselineFromTop(panelY + 24f, screenHeight);

        font.setColor(UiPalette.TEXT);
        font.draw(batch, "Shadow Ascent HUD", textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(UiPalette.TEXT_MUTED);
        lineY = drawWrapped("Act: " + state.actId() + "  |  Plateau: " + state.plateauId(), textX, lineY, textWidth);

        font.setColor(UiPalette.TEXT_MUTED);
        lineY = drawWrapped("Area: " + UiText.areaName(state.areaId()), textX, lineY, textWidth);

        font.setColor(UiPalette.TEXT_MUTED);
        lineY = drawWrapped(
                "Room: " + UiText.missionTitle(state.roomDisplayName()) + "  |  Scene: " + UiText.humanizeToken(state.sceneRole()),
                textX,
                lineY,
                textWidth);

        font.setColor(state.playerHealth() <= 1 ? UiPalette.DANGER : UiPalette.ACCENT);
        lineY = drawWrapped(UiText.healthLine(state.playerHealth(), state.playerMaxHealth()), textX, lineY, textWidth);

        font.setColor(UiPalette.TEXT);
        lineY = drawWrapped("Mission: " + UiText.missionTitle(state.missionTitle()), textX, lineY, textWidth);

        lineY = drawWrapped("Objective: " + UiText.objectiveLine(state.objectiveLine()), textX, lineY, textWidth);

        font.setColor(UiPalette.WARNING);
        lineY = drawWrapped(UiText.contextualHint(state.contextualHint()), textX, lineY, textWidth);

        font.setColor(UiPalette.TEXT);
        lineY = drawWrapped(UiText.contextualHint(state.interactionHint()), textX, lineY, textWidth);

        font.setColor(UiPalette.TEXT_MUTED);
        lineY = drawWrapped(state.overlayStatus(), textX, lineY, textWidth);

        drawWrapped("Minimap: " + (state.showMinimap() ? "planned visible" : "hidden"), textX, lineY, textWidth);
    }

    private void drawFeedBlock(List<String> eventFeedLines, float panelX, float panelY, float panelWidth, int screenHeight) {
        float textX = panelX + UiPalette.PANEL_PADDING;
        float textWidth = panelWidth - UiPalette.PANEL_PADDING * 2f;
        float lineY = baselineFromTop(panelY + 24f, screenHeight);

        font.setColor(UiPalette.TEXT);
        font.draw(batch, "Event Feed", textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(UiPalette.TEXT_MUTED);
        if (eventFeedLines.isEmpty()) {
            drawWrapped("No recent events.", textX, lineY, textWidth);
            return;
        }

        int visibleLines = Math.min(4, eventFeedLines.size());
        for (int i = 0; i < visibleLines; i++) {
            lineY = drawWrapped(eventFeedLines.get(i), textX, lineY, textWidth);
        }
    }

    private float drawWrapped(String text, float x, float baselineY, float width) {
        layout.setText(font, text, font.getColor(), width, Align.left, true);
        font.draw(batch, layout, x, baselineY);
        return baselineY - Math.max(UiPalette.LINE_HEIGHT, layout.height + 4f);
    }

    private static float baselineFromTop(float topY, int screenHeight) {
        return screenHeight - topY;
    }
}
