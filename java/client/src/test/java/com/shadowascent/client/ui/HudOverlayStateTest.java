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
                "area_lantern_heights_hub",
                "Lantern Heights Hub",
                "social_hub",
                "mission_intro",
                "Reach the east gate",
                2,
                3,
                "Hint line",
                "Overlay status",
                List.of("line1", "line2"),
                true,
                "[E] Talk",
                List.of("SAMSON")
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
                "area_lantern_heights_hub",
                "Lantern Heights Hub",
                "social_hub",
                "mission_intro",
                "Reach the east gate",
                2,
                3,
                "Hint line",
                "Overlay status",
                feedLines,
                true,
                "[E] Talk",
                List.of("SAMSON")
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
                "area_lantern_heights_hub",
                "Lantern Heights Hub",
                "social_hub",
                "mission_intro",
                "Reach the east gate",
                2,
                3,
                "Hint line",
                UiText.overlayStatus(null),
                List.of("newest", "older", "oldest"),
                true,
                "[E] Talk",
                List.of("SAMSON")
        );

        assertEquals("newest", state.eventFeedLines().getFirst());
        assertEquals("Overlay: none", state.overlayStatus());
    }

    @Test
    void minimapVisibilityFlagTracksHudToggleState() {
        HudOverlayState shown = new HudOverlayState("A", "P", "area", "Room", "scene", "M", "O", 3, 3, "H", "S", List.of(), true, "I", List.of());
        HudOverlayState hidden = new HudOverlayState("A", "P", "area", "Room", "scene", "M", "O", 3, 3, "H", "S", List.of(), false, "I", List.of());

        assertTrue(shown.showMinimap());
        assertFalse(hidden.showMinimap());
    }

    @Test
    void hudStateCarriesInteractionHintIndependentlyFromContextualHint() {
        HudOverlayState state = new HudOverlayState(
                "Act I",
                "Lantern Heights",
                "area_lantern_heights_hub",
                "Lantern Heights Hub",
                "social_hub",
                "Mission",
                "Objective",
                3,
                3,
                "Context",
                "Overlay: none",
                List.of(),
                true,
                "[E] Talk to Merchant Rilu",
                List.of("MERCHANT_RILU")
        );

        assertEquals("[E] Talk to Merchant Rilu", state.interactionHint());
    }

    @Test
    void hudStateCarriesAuthoredAreaIdentity() {
        HudOverlayState state = new HudOverlayState(
                "Act I",
                "Lantern Heights",
                "area_hollow_depths_camp",
                "Hollow Depths Camp",
                "camp_intro",
                "Mission",
                "Objective",
                3,
                3,
                "Context",
                "Overlay: none",
                List.of(),
                true,
                "[E] Talk",
                List.of("SHADE_HERMIT")
        );

        assertEquals("area_hollow_depths_camp", state.areaId());
    }

    @Test
    void hudStateCarriesRoomIdentitySeparatelyFromAreaIdentity() {
        HudOverlayState state = new HudOverlayState(
                "Act I",
                "Lantern Heights",
                "area_lantern_heights_hub",
                "Lantern Heights Balcony",
                "opening",
                "Mission",
                "Objective",
                3,
                3,
                "Context",
                "Overlay: none",
                List.of(),
                true,
                "[E] Talk",
                List.of("INSTRUCTOR_TAI")
        );

        assertEquals("Lantern Heights Balcony", state.roomDisplayName());
        assertEquals("opening", state.sceneRole());
    }

    @Test
    void hudStateCarriesHighlightedNpcIdsForRouteSurfacing() {
        HudOverlayState state = new HudOverlayState(
                "Act I",
                "Lantern Heights",
                "area_lantern_heights_hub_dimming",
                "Lantern Heights Dimming Hub",
                "return_changed",
                "Mission",
                "Objective",
                3,
                3,
                "Context",
                "Overlay: none",
                List.of(),
                true,
                "[E] Listen to Samson",
                List.of("SAMSON", "HAZEL")
        );

        assertEquals(List.of("SAMSON", "HAZEL"), state.highlightedNpcIds());
        assertThrows(UnsupportedOperationException.class, () -> state.highlightedNpcIds().add("SOPHIA"));
    }
}
