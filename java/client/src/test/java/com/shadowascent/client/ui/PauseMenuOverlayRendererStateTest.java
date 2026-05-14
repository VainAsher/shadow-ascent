package com.shadowascent.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

final class PauseMenuOverlayRendererStateTest {

    @Test
    void selectionMovesWithinMenuBounds() {
        PauseMenuOverlayRenderer overlay = new PauseMenuOverlayRenderer();

        overlay.moveDown();
        overlay.moveDown();
        overlay.moveUp();

        assertEquals(1, overlay.selectedIndex());
    }

    @Test
    void resetAndBoundsPreserveExpectedPauseFlowOptions() {
        PauseMenuOverlayRenderer overlay = new PauseMenuOverlayRenderer();

        overlay.moveUp();
        assertEquals(0, overlay.selectedIndex());
        assertEquals("Resume", overlay.selectedOption());

        overlay.moveDown();
        overlay.moveDown();
        overlay.moveDown();
        overlay.moveDown();

        assertEquals("Quit To Title", overlay.selectedOption());

        overlay.resetSelection();
        assertEquals("Resume", overlay.selectedOption());
        assertIterableEquals(
                java.util.List.of("Resume", "Save", "Load", "Quit To Title"),
                java.util.List.of("Resume", "Save", "Load", "Quit To Title"));
    }
}
