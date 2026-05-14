package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

public final class DialogueOverlayRenderer {
    private final Matrix4 textProjection = new Matrix4();
    private final Matrix4 shapeProjection = new Matrix4();
    private final GlyphLayout layout = new GlyphLayout();
    private final List<String> lines = new ArrayList<>();
    private String speakerName = "";
    private int lineIndex;
    private boolean visible;

    public void open(String speakerName, List<String> dialogueLines) {
        this.speakerName = speakerName == null || speakerName.isBlank() ? "Unknown" : speakerName;
        lines.clear();
        if (dialogueLines == null || dialogueLines.isEmpty()) {
            lines.add("...");
        } else {
            lines.addAll(dialogueLines);
        }
        lineIndex = 0;
        visible = true;
    }

    public boolean advance() {
        if (!visible) {
            return false;
        }
        if (lineIndex + 1 < lines.size()) {
            lineIndex++;
            return true;
        }
        close();
        return false;
    }

    public void close() {
        visible = false;
        lineIndex = 0;
    }

    public boolean isVisible() {
        return visible;
    }

    public String speakerName() {
        return speakerName;
    }

    public String currentLine() {
        return lines.isEmpty() ? "..." : lines.get(lineIndex);
    }

    public void render(SpriteBatch batch, BitmapFont font, ShapeRenderer shapes, int screenWidth, int screenHeight) {
        if (!visible) {
            return;
        }
        textProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        shapeProjection.setToOrtho(0f, screenWidth, screenHeight, 0f, 0f, 1f);
        batch.setProjectionMatrix(textProjection);

        float panelWidth = Math.min(760f, screenWidth - 160f);
        float panelHeight = 200f;
        float panelX = (screenWidth - panelWidth) * 0.5f;
        float panelY = screenHeight - panelHeight - 48f;
        float textX = panelX + UiPalette.PANEL_PADDING;
        float textWidth = panelWidth - UiPalette.PANEL_PADDING * 2f;
        float lineY = screenHeight - (panelY + 30f);

        UiPanelRenderer.drawPanel(shapes, shapeProjection, panelX, panelY, panelWidth, panelHeight);

        batch.begin();
        font.setColor(UiPalette.ACCENT);
        font.draw(batch, speakerName, textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT * 1.5f;
        font.setColor(UiPalette.TEXT);
        layout.setText(font, currentLine(), font.getColor(), textWidth, Align.left, true);
        font.draw(batch, layout, textX, lineY);

        lineY -= Math.max(UiPalette.LINE_HEIGHT * 2f, layout.height + UiPalette.LINE_HEIGHT);
        font.setColor(UiPalette.TEXT_MUTED);
        font.draw(batch, "Enter continue  |  Esc close", textX, lineY);
        batch.end();
    }
}
