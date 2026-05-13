package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import java.util.ArrayList;
import java.util.List;

public final class DialogueOverlayRenderer {
    private final Matrix4 textProjection = new Matrix4();
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

    public void render(SpriteBatch batch, BitmapFont font, int screenWidth, int screenHeight) {
        if (!visible) {
            return;
        }
        textProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        batch.setProjectionMatrix(textProjection);

        float textX = 120f;
        float lineY = screenHeight - 140f;
        batch.begin();
        font.setColor(UiPalette.ACCENT);
        font.draw(batch, speakerName, textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT * 1.5f;
        font.setColor(UiPalette.TEXT);
        font.draw(batch, currentLine(), textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT * 2f;
        font.setColor(UiPalette.TEXT_MUTED);
        font.draw(batch, "Enter continue  |  Esc close", textX, lineY);
        batch.end();
    }
}
