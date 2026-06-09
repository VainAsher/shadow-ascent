package com.shadowascent.client.world;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlateauGeometryFidelityTest {
    private static final List<String> NEW_PLATEAUS = List.of(
            "SUMMIT_SHRINE", "HOLLOW_DEPTHS", "EMBER_MONASTERY",
            "WINDING_SKYROAD", "MIRROR_SUMMIT", "BEACON_CLIFF");

    @Test
    void allNewPlateausHaveVerticalDepth() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        for (String plateauId : NEW_PLATEAUS) {
            assertPlateauHasVerticalDepth(catalog, plateauId);
        }
    }

    @Test
    void allNewPlateausHaveAtLeastThreeRooms() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        for (String plateauId : NEW_PLATEAUS) {
            long count = catalog.roomsForPlateau(plateauId).stream()
                    .filter(r -> r.transitions().isEmpty() || !r.sceneRole().endsWith("_night"))
                    .count();
            assertTrue(count >= 3, plateauId + " must have at least 3 rooms (found " + count + ")");
        }
    }

    @Test
    void allNewPlateausHaveBidirectionalTransitionPair() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        for (String plateauId : NEW_PLATEAUS) {
            assertPlateauHasBidirectionalPair(catalog, plateauId);
        }
    }

    @Test
    void allNewPlateausHaveAtLeastOneEncounterGate() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        for (String plateauId : NEW_PLATEAUS) {
            boolean found = catalog.roomsForPlateau(plateauId).stream()
                    .flatMap(r -> r.transitions().stream())
                    .anyMatch(t -> "encounter_gate".equals(t.type()));
            assertTrue(found, plateauId + " must have at least one encounter_gate transition");
        }
    }

    private static void assertPlateauHasVerticalDepth(RoomSpecCatalog catalog, String plateauId) {
        boolean found = catalog.roomsForPlateau(plateauId).stream().anyMatch(room -> {
            if (room.geometry().isEmpty()) {
                return false;
            }
            float minY = room.geometry().stream()
                    .map(RoomSpec.GeometrySpec::y).min(Float::compare).orElse(0f);
            float maxY = room.geometry().stream()
                    .map(RoomSpec.GeometrySpec::y).max(Float::compare).orElse(0f);
            return (maxY - minY) >= 150f;
        });
        assertTrue(found, plateauId + " must have at least one room with >=150 units vertical spread");
    }

    private static void assertPlateauHasBidirectionalPair(RoomSpecCatalog catalog, String plateauId) {
        List<RoomSpec> rooms = catalog.roomsForPlateau(plateauId);
        Set<String> roomIds = rooms.stream().map(RoomSpec::id).collect(Collectors.toSet());
        boolean found = rooms.stream().anyMatch(roomA ->
                roomA.transitions().stream().anyMatch(t -> {
                    String targetId = t.targetRoomId();
                    if (!roomIds.contains(targetId)) {
                        return false;
                    }
                    return catalog.room(targetId)
                            .map(roomB -> roomB.transitions().stream()
                                    .anyMatch(bt -> roomA.id().equals(bt.targetRoomId())))
                            .orElse(false);
                })
        );
        assertTrue(found, plateauId + " must have at least one bidirectional room pair");
    }
}
