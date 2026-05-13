package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import java.util.List;

public final class PauseMenuOverlayRenderer {
    private final Matrix4 textProjection = new Matrix4();
    private final List<String> options = List.of("Resume", "Save", "Load", "Quit To Title");
    private int selectedIndex;

    public void moveUp() {
        selectedIndex = Math.max(0, selectedIndex - 1);
    }

    public void moveDown() {
        selectedIndex = Math.min(options.size() - 1, selectedIndex + 1);
    }

    public void resetSelection() {
        selectedIndex = 0;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public String selectedOption() {
        return options.get(selectedIndex);
    }

    public void render(SpriteBatch batch, BitmapFont font, int screenWidth, int screenHeight) {
        textProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        batch.setProjectionMatrix(textProjection);

        float textX = 140f;
        float lineY = screenHeight - 180f;
        batch.begin();
        font.setColor(UiPalette.ACCENT);
        font.draw(batch, "Paused", textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT * 2f;
        for (int i = 0; i < options.size(); i++) {
            font.setColor(i == selectedIndex ? UiPalette.ACCENT : UiPalette.TEXT);
            font.draw(batch, (i == selectedIndex ? "> " : "  ") + options.get(i), textX, lineY - i * UiPalette.LINE_HEIGHT);
        }

        font.setColor(UiPalette.TEXT_MUTED);
        font.draw(batch, "Enter select  |  Esc resume", textX,
                lineY - options.size() * UiPalette.LINE_HEIGHT - UiPalette.LINE_HEIGHT);
        batch.end();
    }
}
