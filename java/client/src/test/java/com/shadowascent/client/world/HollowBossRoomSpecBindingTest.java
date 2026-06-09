package com.shadowascent.client.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class HollowBossRoomSpecBindingTest {

    @Test
    void weightboundBossEntityIsPlacedAndEncounterDefinitionReferencesIt() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        RoomSpec weightboundRoom = catalog.room("hd_weightbound_mines")
                .orElseThrow(() -> new AssertionError("hd_weightbound_mines not found in catalog"));

        assertTrue(weightboundRoom.enemyPlacements().stream()
                .anyMatch(e -> e.enemyId().contains("weightbound")));
        assertTrue(weightboundRoom.encounters().stream()
                .flatMap(id -> catalog.encounter(id).stream())
                .anyMatch(enc -> enc.enemyIds().stream().anyMatch(id -> id.contains("weightbound"))),
                "Encounter definition must reference the weightbound boss entity");
    }

    @Test
    void shatterMothBossEntityIsPlacedAndEncounterDefinitionReferencesIt() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        RoomSpec mothRoom = catalog.room("hd_shattermoth_grove")
                .orElseThrow(() -> new AssertionError("hd_shattermoth_grove not found in catalog"));

        assertTrue(mothRoom.enemyPlacements().stream()
                .anyMatch(e -> e.enemyId().contains("shatter")));
        assertTrue(mothRoom.encounters().stream()
                .flatMap(id -> catalog.encounter(id).stream())
                .anyMatch(enc -> enc.enemyIds().stream().anyMatch(id -> id.contains("shatter"))),
                "Encounter definition must reference the shatter moth boss entity");
    }

    @Test
    void stoneJudgeBossEntityIsPlacedAndEncounterDefinitionReferencesIt() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        RoomSpec stoneRoom = catalog.room("hd_stone_tribunal")
                .orElseThrow(() -> new AssertionError("hd_stone_tribunal not found in catalog"));

        assertTrue(stoneRoom.enemyPlacements().stream()
                .anyMatch(e -> e.enemyId().contains("stone_judge")));
        assertTrue(stoneRoom.encounters().stream()
                .flatMap(id -> catalog.encounter(id).stream())
                .anyMatch(enc -> enc.enemyIds().stream().anyMatch(id -> id.contains("stone_judge"))),
                "Encounter definition must reference the stone judge boss entity");
    }
}
