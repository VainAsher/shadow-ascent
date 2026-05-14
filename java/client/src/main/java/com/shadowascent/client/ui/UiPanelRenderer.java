package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

final class UiPanelRenderer {
    private UiPanelRenderer() {
    }

    static void drawPanel(ShapeRenderer shapes, Matrix4 projection, float x, float y, float width, float height) {
        if (shapes == null || projection == null) {
            return;
        }
        shapes.setProjectionMatrix(projection);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(UiPalette.PANEL_FILL);
        shapes.rect(x, y, width, height);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(UiPalette.PANEL_BORDER);
        shapes.rect(x, y, width, height);
        shapes.end();
    }
}
