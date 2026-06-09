package com.shadowascent.client.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmberAbilityUnlockTransitionTest {

    @Test
    void emberHearthTrialTransitionSetsAirDodgeAndGrappleFlags() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        RoomSpec courtyardRoom = catalog.room("em_courtyard_hub")
                .orElseThrow(() -> new AssertionError("em_courtyard_hub not in catalog"));

        RoomTransitionSpec unlockTransition = courtyardRoom.transitions().stream()
                .filter(t -> "em_hearth_trial".equals(t.targetRoomId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No transition to em_hearth_trial"));

        assertTrue(unlockTransition.setFlags().contains("air_dodge_restored"),
                "Hearth trial transition must set air-dodge flag");
        assertTrue(unlockTransition.setFlags().contains("grapple_restored"),
                "Hearth trial transition must set grapple flag");
    }

    @Test
    void rogaDojoTransitionSetsWallClingFlag() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        boolean wallClingSet = catalog.allRooms().stream()
                .flatMap(r -> r.transitions().stream())
                .filter(t -> "em_roga_dojo".equals(t.targetRoomId()))
                .anyMatch(t -> t.setFlags().contains("wall_cling_mastered"));
        assertTrue(wallClingSet, "A transition into em_roga_dojo must set wall-cling flag");
    }
}
