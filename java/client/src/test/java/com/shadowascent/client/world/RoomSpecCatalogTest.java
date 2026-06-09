package com.shadowascent.client.world;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RoomSpecCatalogTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsActIVerticalSliceRoomSpecsFromRepoData() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();

        assertEquals("lh_balcony_opening", catalog.room("lh_balcony_opening").orElseThrow().id());
        assertEquals("mistwood_first_encounter", catalog.room("mistwood_first_encounter").orElseThrow().id());
        assertTrue(catalog.roomsForPlateau("LANTERN_HEIGHTS").stream().anyMatch(room -> "lh_hub_social".equals(room.id())));
        assertTrue(catalog.room("lh_fixture_annex").isEmpty(), "fixture room must not be loaded by default scans");
        assertTrue(catalog.room("fixture_hd_fallback").isEmpty(), "multi-plateau fixture room must not be loaded by default scans");
    }

    @Test
    void explicitCatalogLoadCanIncludeAuthoringFixtureFile() {
        Path fixturePath = resolveFixturePath();
        List<Path> files = new ArrayList<>(RoomSpecCatalog.defaultRoomSpecFiles());
        files.add(fixturePath);

        RoomSpecCatalog catalog = RoomSpecCatalog.load(files);

        assertEquals("lh_fixture_annex", catalog.room("lh_fixture_annex").orElseThrow().id());
    }

    @Test
    void rejectsUnknownTransitionTargets() throws Exception {
        Path roomFile = tempDir.resolve("broken_rooms.json");
        Files.writeString(roomFile, """
                {
                  "schema": "room_specs.v1",
                  "rooms": [
                    {
                      "id": "lh_test_room",
                      "display_name": "Test Room",
                      "plateau_id": "LANTERN_HEIGHTS",
                      "area_id": "area_lantern_heights_hub",
                      "scene_role": "hub_test",
                      "spawn_points": [{"id":"spawn_main","x":120,"y":280}],
                      "geometry": [{"type":"floor","x":0,"y":360,"w":1200,"h":30}],
                      "npc_anchors": [],
                      "encounters": [],
                      "transitions": [
                        {
                          "id":"gate_missing_target",
                          "type":"free_exit",
                          "target_room_id":"missing_room",
                          "target_spawn_id":"spawn_main",
                          "min_x":900,
                          "max_x":980
                        }
                      ]
                    }
                  ]
                }
                """);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> RoomSpecCatalog.load(List.of(roomFile)));

        assertTrue(error.getMessage().contains("missing_room"));
    }

    @Test
    void rejectsDuplicateRoomIdsAcrossFiles() throws Exception {
        Path fileA = tempDir.resolve("a.json");
        Path fileB = tempDir.resolve("b.json");
        String duplicateRoom = """
                {
                  "schema": "room_specs.v1",
                  "rooms": [
                    {
                      "id": "duplicate_room",
                      "display_name": "Duplicate Room",
                      "plateau_id": "LANTERN_HEIGHTS",
                      "area_id": "area_lantern_heights_hub",
                      "scene_role": "duplicate",
                      "spawn_points": [{"id":"spawn_main","x":120,"y":280}],
                      "geometry": [{"type":"floor","x":0,"y":360,"w":1200,"h":30}],
                      "npc_anchors": [],
                      "encounters": [],
                      "transitions": []
                    }
                  ]
                }
                """;
        Files.writeString(fileA, duplicateRoom);
        Files.writeString(fileB, duplicateRoom);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> RoomSpecCatalog.load(List.of(fileA, fileB)));

        assertTrue(error.getMessage().contains("duplicate_room"));
    }

    private static Path resolveFixturePath() {
        for (Path cursor = Path.of("").toAbsolutePath(); cursor != null; cursor = cursor.getParent()) {
            Path candidate = cursor.resolve("data").resolve("room_specs").resolve("act_i_authoring_fixture.json");
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to resolve act_i_authoring_fixture.json");
    }
}
