package com.shadowascent.client.ui;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HudOverlayStateTest {

    @Test
    void createsCompactHudSnapshot() {
        HudOverlayState state = new HudOverlayState(
                "ACT_1",
                "LANTERN_HUB",
                "mission_intro",
                "Reach the east gate",
                2,
                3,
                "Hint line",
                "Overlay status",
                List.of("line1", "line2"),
                true
        );

        assertEquals("ACT_1", state.actId());
        assertEquals("Reach the east gate", state.objectiveLine());
        assertEquals(2, state.playerHealth());
        assertTrue(state.showMinimap());
    }

    @Test
    void defensivelyCopiesEventFeedLines() {
        List<String> feedLines = new ArrayList<>(List.of("line1", "line2"));

        HudOverlayState state = new HudOverlayState(
                "ACT_1",
                "LANTERN_HUB",
                "mission_intro",
                "Reach the east gate",
                2,
                3,
                "Hint line",
                "Overlay status",
                feedLines,
                true
        );

        feedLines.add("line3");

        assertEquals(List.of("line1", "line2"), state.eventFeedLines());
        assertThrows(UnsupportedOperationException.class, () -> state.eventFeedLines().add("line4"));
    }

    @Test
    void preservesNewestFirstOrderingAndFormatsOverlayFallback() {
        HudOverlayState state = new HudOverlayState(
                "ACT_1",
                "LANTERN_HUB",
                "mission_intro",
                "Reach the east gate",
                2,
                3,
                "Hint line",
                UiText.overlayStatus(null),
                List.of("newest", "older", "oldest"),
                true
        );

        assertEquals("newest", state.eventFeedLines().getFirst());
        assertEquals("Overlay: none", state.overlayStatus());
    }

    @Test
    void minimapVisibilityFlagTracksHudToggleState() {
        HudOverlayState shown = new HudOverlayState("A", "P", "M", "O", 3, 3, "H", "S", List.of(), true);
        HudOverlayState hidden = new HudOverlayState("A", "P", "M", "O", 3, 3, "H", "S", List.of(), false);

        assertTrue(shown.showMinimap());
        assertFalse(hidden.showMinimap());
    }
}
