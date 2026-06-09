package com.shadowascent.client.tools;

import com.shadowascent.client.world.RoomSpecCatalog;
import com.shadowascent.core.data.GameDataContracts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ActIAuthoringDiagnosticsTest {

    @TempDir
    Path tempDir;

    @Test
    void productionActIDataPassesDiagnostics() {
        ActIAuthoringDiagnostics.Report report = ActIAuthoringDiagnostics.validate(
                RoomSpecCatalog.loadDefault(),
                GameDataContracts.loadDefault());

        assertTrue(report.isClean(), () -> "expected clean Act I diagnostics, got: " + report.issues());
    }

    @Test
    void diagnosticsNameEachBrokenAuthoringSurface() throws Exception {
        Path copiedData = copyRepoDataTree(tempDir.resolve("data"));
        Path fixturePath = copiedData.resolve("room_specs").resolve("act_i_authoring_fixture.json");
        Files.writeString(fixturePath, """
                {
                  "schema": "room_specs.v1",
                  "rooms": [
                    {
                      "id": "lh_diag_overlap_one",
                      "display_name": "Diag Overlap One",
                      "plateau_id": "LANTERN_HEIGHTS",
                      "area_id": "area_lantern_heights_hub",
                      "scene_role": "optional_side",
                      "route_order": 19,
                      "required_flags": ["opening_seen", "diag_overlap"],
                      "set_flags": [],
                      "spawn_points": [{"id":"spawn_main","x":120,"y":280}],
                      "geometry": [{"type":"floor","x":0,"y":360,"w":1200,"h":30}],
                      "npc_anchors": [{"npc_id":"UNKNOWN_NPC","role":"optional_npc","x":320,"y":288,"patrol_min_x":300,"patrol_max_x":340}],
                      "enemy_placements": [],
                      "encounters": [],
                      "transitions": [
                        {"id":"gate_diag_free","type":"free_exit","target_room_id":"lh_hub_social","target_spawn_id":"spawn_balcony_entry","min_x":960,"max_x":1100},
                        {"id":"gate_diag_block","type":"mission_gate","target_room_id":"lh_hub_social","target_spawn_id":"spawn_balcony_entry","min_x":1000,"max_x":1120}
                      ]
                    },
                    {
                      "id": "lh_diag_overlap_two",
                      "display_name": "Diag Overlap Two",
                      "plateau_id": "LANTERN_HEIGHTS",
                      "area_id": "area_lantern_heights_hub",
                      "scene_role": "optional_side",
                      "route_order": 20,
                      "required_flags": ["opening_seen", "diag_overlap"],
                      "set_flags": [],
                      "spawn_points": [{"id":"spawn_main","x":160,"y":280}],
                      "geometry": [{"type":"floor","x":0,"y":360,"w":1200,"h":30}],
                      "npc_anchors": [],
                      "enemy_placements": [],
                      "encounters": [],
                      "transitions": []
                    }
                  ],
                  "encounter_definitions": []
                }
                """);

        Path beatsPath = copiedData.resolve("narrative_beats.json");
        String beats = Files.readString(beatsPath);
        Files.writeString(beatsPath, beats.replaceFirst("\"area_lantern_heights_hub\"", "\"area_missing_fixture_diag\""));

        GameDataContracts contracts = GameDataContracts.load(copiedData);
        List<Path> roomFiles = new ArrayList<>(Files.list(copiedData.resolve("room_specs"))
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .filter(path -> !"act_i_authoring_fixture.json".equals(path.getFileName().toString()))
                .sorted(Comparator.naturalOrder())
                .toList());
        roomFiles.add(fixturePath);
        RoomSpecCatalog catalog = RoomSpecCatalog.load(roomFiles);

        ActIAuthoringDiagnostics.Report report = ActIAuthoringDiagnostics.validate(catalog, contracts);

        assertTrue(report.issues().stream().anyMatch(issue -> issue.contains("UNKNOWN_NPC")));
        assertTrue(report.issues().stream().anyMatch(issue -> issue.contains("lh_diag_overlap_one") && issue.contains("lh_diag_overlap_two")));
        assertTrue(report.issues().stream().anyMatch(issue -> issue.contains("gate_diag_free") && issue.contains("gate_diag_block")));
        assertTrue(report.issues().stream().anyMatch(issue -> issue.contains("beat `beat_meet_villagers`") && issue.contains("area_missing_fixture_diag")));
        assertTrue(report.issues().stream().anyMatch(issue -> issue.contains("dialogue `dlg_samson_intro`") && issue.contains("beat_meet_villagers")));
    }

    private static Path copyRepoDataTree(Path destination) throws IOException {
        Path source = resolveRepoDataDir();
        Files.walk(source).forEach(path -> {
            try {
                Path relative = source.relativize(path);
                Path target = destination.resolve(relative.toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        return destination;
    }

    private static Path resolveRepoDataDir() {
        for (Path cursor = Path.of("").toAbsolutePath(); cursor != null; cursor = cursor.getParent()) {
            Path candidate = cursor.resolve("data");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to resolve repo data directory");
    }
}
