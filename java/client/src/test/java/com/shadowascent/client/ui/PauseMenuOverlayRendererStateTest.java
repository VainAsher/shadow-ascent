package com.shadowascent.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PauseMenuOverlayRendererStateTest {

    @Test
    void selectionMovesWithinMenuBounds() {
        PauseMenuOverlayRenderer overlay = new PauseMenuOverlayRenderer();

        overlay.moveDown();
        overlay.moveDown();
        overlay.moveUp();

        assertEquals(1, overlay.selectedIndex());
    }
}
