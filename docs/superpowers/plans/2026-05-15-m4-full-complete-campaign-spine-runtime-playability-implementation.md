# M4 Full Complete Campaign Spine Runtime Playability Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `runGame` the truthful runtime host for the full currently-authored campaign spine, including all staged plateaus, plateau-local optional content, meaningful traversal-ready geometry, and a stable post-climax free-roam state that survives save/load.

**Architecture:** Keep `runGame` on the room-spec-driven authoring path. First unlock multi-plateau room resolution and broaden authoring diagnostics, then stage each plateau as a room-spec slice with plateau-local bootstrap tests and optional-content tests. Use small targeted runtime extensions only when a contract-authored beat or quest exposes a concrete gap, and keep legacy fallback growth frozen except where HOLLOW_DEPTHS migration must compare and then replace the existing legacy chain.

**Tech Stack:** Java 21, Gradle Kotlin DSL, LibGDX client runtime, Jackson JSON loaders, contract JSON data under `data/`, JUnit client/core tests, existing regression harness via `runRegressionTests`.

---

## Required Pre-Work: Ability Flag Canonical Mapping

Before authoring any room-spec transitions that set ability-unlock flags (Tasks 4 and 6), establish the canonical flag name for each ability unlock by reading `data/story_flags.json`. The table below shows the expected mapping based on area-catalog beat anchors. **Verify every flag name against `story_flags.json` before writing any transition `set_flags` entry. Do not invent flag names.**

| Ability | Expected flag name | Unlocked at | Source beat in area_catalog |
| --- | --- | --- | --- |
| Dash | verify in story_flags.json | HOLLOW_DEPTHS — Weightbound Ogre arena | `beat_weightbound_ogre` |
| Double jump | verify in story_flags.json | HOLLOW_DEPTHS — Fractured Contact High Winds | `beat_fractured_contact_double_jump` |
| Air dodge + grapple | verify in story_flags.json | EMBER_MONASTERY — Hearth of Brothers Trial | `beat_airdodge_grapple_unlocked` |
| Wall cling | verify in story_flags.json | EMBER_MONASTERY — Roga Dojo | `beat_roga_training_wallcling` |
| Glide | verify in story_flags.json | Verify: is this HOLLOW_DEPTHS Abyssal Gate or EMBER_MONASTERY? | Check narrative_beats.json |

Replace all placeholder flag names in Tasks 4, 6, and 7 with the verified canonical names from this table before committing any room-spec JSON.

---

## Required Pre-Work: Siren Boss Encounter Pattern Investigation

Before authoring `summit_shrine_vertical_slice.json`, determine how the SIREN confrontation ends. The SIREN fight is a scripted-loss encounter in the narrative — Aen is supposed to lose. Check `BossPatternLibrary.java` for the SIREN boss type:

- If a scripted-loss phase exists: the encounter definition cannot use `clear_type: "enemy_ids"` (the enemy never dies). You will need a `clear_type: "scripted_phase"` or equivalent, which may require a small targeted runtime extension. Add this extension before authoring the encounter.
- If no scripted-loss exists yet: author a stub encounter using `clear_type: "enemy_ids"` with a placeholder enemy, and file a tracked follow-up to wire the scripted loss. Document the stub clearly in the room spec.

Do not assume `clear_type: "enemy_ids"` works for this fight without this check.

---

## Required Pre-Work: Verify Save/Load API Entry Point

Before Task 5, confirm the actual save/load method signatures by reading `SaveLoadRuntimeStateTest.java` (which is currently passing). The save/load entry point may be `GameState.save(Path)` / `GameState.load(Path)` rather than a standalone `SaveLoad.java` class. All Task 5 test code must use whichever signature the existing passing tests use. Do not guess.

---

## File Structure Map

### Existing files that will be modified

- `build.gradle.kts` — add the generalized room-authoring diagnostics task; preserve `runActIAuthoringDiagnostics` as a compatibility alias.
- `data/area_catalog.json` — correct four family mismatches before new room authoring depends on those area IDs.
- `data/room_specs/lantern_heights_vertical_slice.json` — preserve Act I invariants; no changes expected.
- `data/room_specs/mistwood_vertical_slice.json` — preserve existing behavior; no changes expected.
- `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java` — remove the `LANTERN_HEIGHTS`-only guard; add flag-based plateau fallback room resolution.
- `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java` — add plateau routing cases for all newly staged plateaus; add global post-climax pre-check. **Not touched in Task 1 — modified starting in Task 3.**
- `java/client/src/main/java/com/shadowascent/client/world/RoomSpecCatalog.java` — broaden validation and expose transition inspection helpers needed by the geometry fidelity test.
- `java/client/src/main/java/com/shadowascent/client/RunGameAreaTransition.java` — keep aligned with new plateau room graphs, post-climax routing, and encounter gating.
- `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java` — surface plateau-local optional content and post-climax NPC interaction truth.
- `java/client/src/main/java/com/shadowascent/client/HubScreen.java` — keep mission/hint/event surfacing aligned as new plateau content lands.
- `java/core/src/main/java/com/shadowascent/core/MissionManager.java` — keep mainline and optional content progression synchronized.
- `java/core/src/main/java/com/shadowascent/core/GameState.java` — extend only if a plateau or post-climax state needs additional durable runtime fields.
- `java/core/src/main/java/com/shadowascent/core/StoryState.java` — fix continuity truth only if the regression root cause requires it.
- `java/core/src/main/java/com/shadowascent/core/RegressionTest.java` — fix the `Campaign Continuity` section; add milestone-closure regression coverage.
- `docs/CURRENT_STATE.md`
- `docs/NORTH_STAR_EXECUTION_MATRIX.md`
- `docs/IMPLEMENTATION_BACKLOG.md` — promote M4 Full closure truth after the gate is actually green.
- `docs/MILESTONE_B_GATE.md` — close M4b cleanly before treating it as a completed prerequisite.

### New data files to create

- `data/room_specs/multi_plateau_routing_fixture.json` — minimal fixture used only in Task 1 routing test; excluded from production diagnostics via existing fixture-exclusion logic.
- `data/room_specs/summit_shrine_vertical_slice.json`
- `data/room_specs/hollow_depths_vertical_slice.json`
- `data/room_specs/ember_monastery_vertical_slice.json`
- `data/room_specs/winding_skyroad_vertical_slice.json`
- `data/room_specs/mirror_summit_vertical_slice.json`
- `data/room_specs/beacon_cliff_vertical_slice.json`

### New diagnostics and gate files to create

- `docs/MILESTONE_GATE_M4_FULL.md`
- `docs/guides/ROOM_AUTHORING_GUIDE.md` — only create if the M4b authoring guide does not already cover the generalized multi-plateau path.

### New tests to create

- `java/client/src/test/java/com/shadowascent/client/world/ResolveRoomSpecMultiPlateauFallbackTest.java`
- `java/client/src/test/java/com/shadowascent/client/tools/RunGameAuthoringDiagnosticsTest.java`
- `java/client/src/test/java/com/shadowascent/client/world/SummitShrineVerticalSliceBootstrapTest.java`
- `java/client/src/test/java/com/shadowascent/client/SummitShrineOptionalContentTest.java`
- `java/client/src/test/java/com/shadowascent/client/world/HollowDepthsVerticalSliceBootstrapTest.java`
- `java/client/src/test/java/com/shadowascent/client/HollowDepthsOptionalContentTest.java`
- `java/client/src/test/java/com/shadowascent/client/world/HollowBossRoomSpecBindingTest.java`
- `java/client/src/test/java/com/shadowascent/client/world/EmberMonasteryVerticalSliceBootstrapTest.java`
- `java/client/src/test/java/com/shadowascent/client/EmberMonasteryOptionalContentTest.java`
- `java/client/src/test/java/com/shadowascent/client/world/EmberAbilityUnlockTransitionTest.java`
- `java/client/src/test/java/com/shadowascent/client/world/WindingSkyRoadVerticalSliceBootstrapTest.java`
- `java/client/src/test/java/com/shadowascent/client/world/MirrorSummitVerticalSliceBootstrapTest.java`
- `java/client/src/test/java/com/shadowascent/client/world/BeaconCliffVerticalSliceBootstrapTest.java`
- `java/client/src/test/java/com/shadowascent/client/world/PlateauGeometryFidelityTest.java`
- `java/client/src/test/java/com/shadowascent/client/FullCampaignRouteEndToEndTest.java`
- `java/client/src/test/java/com/shadowascent/client/PostClimaxStatePersistenceTest.java`
- `java/client/src/test/java/com/shadowascent/client/CampaignContinuitySaveLoadBoundaryTest.java`

## Commit Strategy

- Commit 1: multi-plateau routing unlock + area catalog cleanup
- Commit 2: authoring diagnostics generalization + M4b closure
- Commit 3: Summit Shrine runtime slice
- Commit 4: Hollow Depths room-spec migration + boss gating
- Commit 5: Campaign Continuity fix
- Commit 6: Ember Monastery runtime slice + ability unlock transitions
- Commit 7: Winding Skyroad runtime slice
- Commit 8: Mirror Summit + Beacon Cliff + post-climax state
- Commit 9: Full campaign route smoke + geometry fidelity tests + gate/docs closure

---

## Task 1: Close Phase 0 And Unlock Multi-Plateau Room Resolution

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `data/area_catalog.json`
- Create: `data/room_specs/multi_plateau_routing_fixture.json`
- Create: `java/client/src/test/java/com/shadowascent/client/world/ResolveRoomSpecMultiPlateauFallbackTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/RoomSpecCatalogTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`

**Note:** `AreaPlacementResolver.java` and `RoomSpecCatalog.java` are NOT modified in this task. `AreaPlacementResolver` is first modified in Task 3. `RoomSpecCatalog.roomsForPlateau()` already exists and needs no changes here.

- [ ] **Step 0: Confirm M4b prerequisite is green before proceeding**

Run:

```powershell
./gradlew.bat --console=plain runActIAuthoringDiagnostics
./gradlew.bat --console=plain runRegressionTests
```

Expected: both PASS. Do not proceed to Step 1 until these are green. If `runActIAuthoringDiagnostics` fails, fix the diagnostics issue first — that is M4b work that must complete before this task.

- [ ] **Step 1: Create the minimal fixture file for the routing test**

Create `data/room_specs/multi_plateau_routing_fixture.json`. This file must follow the `room_specs.v1` schema exactly. It is used only by the routing test and is excluded from production diagnostics because `RoomSpecCatalog.loadDefault()` already excludes any file named `*fixture*.json` — verify this exclusion pattern in `RoomSpecCatalog.defaultRoomSpecFiles()` before relying on it; add the fixture file name to the exclusion if needed.

```json
{
  "schema": "room_specs.v1",
  "rooms": [
    {
      "id": "fixture_ss_approach",
      "display_name": "Fixture Summit Approach",
      "plateau_id": "SUMMIT_SHRINE",
      "area_id": "area_summit_shrine_gate",
      "scene_role": "entry",
      "route_order": 10,
      "required_flags": ["npc_withdrawal_started"],
      "set_flags": [],
      "spawn_points": [{"id": "spawn_entry", "x": 160, "y": 280}],
      "geometry": [
        {"type": "floor", "x": 0, "y": 360, "w": 1800, "h": 30},
        {"type": "platform", "x": 420, "y": 220, "w": 180, "h": 15},
        {"type": "platform", "x": 900, "y": 150, "w": 160, "h": 15}
      ],
      "npc_anchors": [],
      "enemy_placements": [],
      "encounters": [],
      "transitions": []
    }
  ],
  "encounter_definitions": []
}
```

- [ ] **Step 2: Write the failing multi-plateau fallback test using the fixture file**

Create `ResolveRoomSpecMultiPlateauFallbackTest.java`. The test loads the fixture file — not the not-yet-existing production file:

```java
@Test
void resolvesNonLanternPlateauRoomByFlagsWhenAreaSpecificLookupMisses() {
    GameState gameState = new GameState();
    gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
    gameState.getStoryState().setFlag("npc_withdrawal_started");
    // Do NOT set "fixture_ss_approach"'s set_flags value here;
    // set_flags is empty in the fixture so this is not a concern.

    RoomSpecCatalog catalog = RoomSpecCatalog.load(List.of(
            resolveDataPath("room_specs/lantern_heights_vertical_slice.json"),
            resolveDataPath("room_specs/multi_plateau_routing_fixture.json")));
    AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap(
            new AreaPlacementResolver(), catalog);

    RunGameContentProfile profile = bootstrap.bootstrap(gameState);

    assertEquals("SUMMIT_SHRINE", profile.plateauId());
    assertEquals("fixture_ss_approach", profile.id());
    // roomTransitions is empty because the fixture room has no transitions;
    // this test is specifically verifying plateau routing, not transition content.
    assertNotNull(profile.roomTransitions());
}

private static Path resolveDataPath(String relative) {
    Path current = Paths.get("").toAbsolutePath();
    for (Path cursor = current; cursor != null; cursor = cursor.getParent()) {
        Path candidate = cursor.resolve("data").resolve(relative);
        if (Files.exists(candidate)) return candidate;
    }
    throw new IllegalStateException("Cannot find data/" + relative + " from " + current);
}
```

- [ ] **Step 3: Run the new test and verify it fails on the current guard**

Run:

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.ResolveRoomSpecMultiPlateauFallbackTest"
```

Expected: FAIL because `resolveRoomSpec` returns `null` for non-`LANTERN_HEIGHTS` plateaus, so `bootstrap()` falls to the legacy path and returns a non-SUMMIT_SHRINE profile.

- [ ] **Step 4: Implement the multi-plateau room resolution change**

Replace the `resolveRoomSpec` method in `AuthoringWorldBootstrap.java`. The change removes the `LANTERN_HEIGHTS`-only guard and adds a plateau-wide flag-based fallback:

```java
private RoomSpec resolveRoomSpec(GameState gameState, String areaId, String plateauId) {
    if (gameState == null) {
        return null;
    }
    // Step 1: try to re-enter the pinned room if it is still valid.
    String currentRoomId = gameState.getCurrentRoomId();
    if (currentRoomId != null && !currentRoomId.isBlank()) {
        RoomSpec pinnedRoom = roomSpecCatalog.room(currentRoomId)
                .filter(room -> plateauId.equals(room.plateauId()))
                .filter(room -> room.requiredFlags().stream()
                        .allMatch(gameState.getStoryState()::hasFlag))
                .orElse(null);
        if (pinnedRoom != null) {
            return pinnedRoom;
        }
        gameState.setCurrentRoomId(null);
        gameState.setPendingRoomSpawnId(null);
    }

    // Step 2: area-id-specific match (preserves existing LANTERN_HEIGHTS behaviour).
    Optional<RoomSpec> areaMatch = roomSpecCatalog.roomsForPlateau(plateauId).stream()
            .filter(room -> areaId.equals(room.areaId()))
            .filter(room -> room.requiredFlags().stream()
                    .allMatch(gameState.getStoryState()::hasFlag))
            .filter(room -> room.setFlags().isEmpty()
                    || room.setFlags().stream()
                            .anyMatch(flag -> !gameState.getStoryState().hasFlag(flag)))
            .min(Comparator.comparingInt(RoomSpec::routeOrder).thenComparing(RoomSpec::id));
    if (areaMatch.isPresent()) {
        gameState.setCurrentRoomId(areaMatch.get().id());
        gameState.setPendingRoomSpawnId(null);
        return areaMatch.get();
    }

    // Step 3: plateau-wide flag-based fallback for plateaus where AreaPlacementResolver
    // does not return an area_id that matches any authored room spec area_id.
    Optional<RoomSpec> plateauFallback = roomSpecCatalog.roomsForPlateau(plateauId).stream()
            .filter(room -> room.requiredFlags().stream()
                    .allMatch(gameState.getStoryState()::hasFlag))
            .filter(room -> room.setFlags().isEmpty()
                    || room.setFlags().stream()
                            .anyMatch(flag -> !gameState.getStoryState().hasFlag(flag)))
            .min(Comparator.comparingInt(RoomSpec::routeOrder).thenComparing(RoomSpec::id));
    plateauFallback.ifPresent(room -> {
        gameState.setCurrentRoomId(room.id());
        gameState.setPendingRoomSpawnId(null);
    });
    return plateauFallback.orElse(null);
    // Step 4: null → legacy fallback path (backwards compatible for plateaus with no room specs).
}
```

- [ ] **Step 5: Correct the known `area_catalog.json` family mismatches**

Update exactly these four entries in `data/area_catalog.json`. Change only the `"family"` field value; leave all other fields unchanged:

| area_id | old family | new family |
| --- | --- | --- |
| `area_mirror_summit_gate` | `summit_shrine` | `winding_skyroad` |
| `area_mirror_summit_peak` | `summit_shrine` | `mirror_summit` |
| `area_hollow_reflection_arena` | `hollow_depths` | `mirror_summit` |
| `area_lantern_silk_workshop` | `lantern_village` | `ember_monastery` |

- [ ] **Step 6: Run focused verification for the routing unlock**

Run:

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.ResolveRoomSpecMultiPlateauFallbackTest" --tests "com.shadowascent.client.world.ActIVerticalSliceBootstrapTest" --tests "com.shadowascent.client.world.RoomSpecCatalogTest"
./gradlew.bat --console=plain runDataContractDiagnostics
```

Expected:
- new fallback test PASS
- Act I and Mistwood bootstrap tests still PASS (regression check)
- data-contract diagnostics PASS after area-family corrections

- [ ] **Step 7: Commit the routing unlock**

```powershell
git add java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java data/area_catalog.json data/room_specs/multi_plateau_routing_fixture.json java/client/src/test/java/com/shadowascent/client/world/ResolveRoomSpecMultiPlateauFallbackTest.java java/client/src/test/java/com/shadowascent/client/world/RoomSpecCatalogTest.java java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java
git commit -m "feat: unlock multi-plateau room spec routing"
```

---

## Task 2: Generalize The Authoring Diagnostics Gate And Close M4b

**Files:**
- Modify: `build.gradle.kts`
- Rename/replace: `java/client/src/main/java/com/shadowascent/client/tools/ActIAuthoringDiagnostics.java` → `RunGameAuthoringDiagnostics.java`
- Migrate: `java/client/src/test/java/com/shadowascent/client/tools/ActIAuthoringDiagnosticsTest.java` → `RunGameAuthoringDiagnosticsTest.java`
- Modify: `docs/MILESTONE_B_GATE.md`
- Create or modify: `docs/guides/ROOM_AUTHORING_GUIDE.md`

- [ ] **Step 1: Write the failing multi-plateau diagnostics test**

Create `RunGameAuthoringDiagnosticsTest.java`. The old test class (`ActIAuthoringDiagnosticsTest.java`) must be deleted as part of this step — do not leave both. Migrate any passing assertions from the old test into the new file before deleting:

```java
@Test
void diagnosticsScanAllProductionRoomSpecFiles() {
    RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
    GameDataContracts contracts = GameDataContracts.loadDefault();

    RunGameAuthoringDiagnostics.Report report =
            RunGameAuthoringDiagnostics.validate(catalog, contracts);

    assertTrue(report.errors().isEmpty(),
            "Authoring diagnostics reported errors:\n" + String.join("\n", report.errors()));
}
```

- [ ] **Step 2: Run the diagnostics test and verify it fails before generalization**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.tools.RunGameAuthoringDiagnosticsTest"
```

Expected: compile error or FAIL because `RunGameAuthoringDiagnostics` does not yet exist.

- [ ] **Step 3: Rename and generalize the diagnostics class**

Rename `ActIAuthoringDiagnostics.java` to `RunGameAuthoringDiagnostics.java`. Update the class declaration and all internal references. The class must scan all room specs loaded by `RoomSpecCatalog.loadDefault()`, not just Act I files. The validation rules to enforce:

```java
// For each room spec in the catalog:
// 1. Transition targets exist in the catalog.
if (!roomsById.containsKey(transition.targetRoomId())) {
    errors.add("Room `" + room.id() + "` transition `" + transition.id()
            + "` targets unknown room `" + transition.targetRoomId() + "`");
}
// 2. Spawn IDs referenced by transitions exist in the target room.
boolean spawnExists = roomsById.get(transition.targetRoomId()).spawnPoints()
        .stream().anyMatch(sp -> sp.id().equals(transition.targetSpawnId()));
if (!spawnExists) {
    errors.add("Transition `" + transition.id() + "` targets missing spawn `"
            + transition.targetSpawnId() + "` in room `" + transition.targetRoomId() + "`");
}
// 3. NPC anchor IDs exist in npc_registry.json.
if (!contracts.isKnownNpcId(anchor.npcId())) {
    errors.add("Room `" + room.id() + "` anchors unknown NPC `" + anchor.npcId() + "`");
}
// 4. Encounter IDs referenced by rooms exist in encounter_definitions.
if (!encountersById.containsKey(encounterId)) {
    errors.add("Room `" + room.id() + "` references unknown encounter `" + encounterId + "`");
}
// 5. Enemy IDs in encounter definitions match enemy_placements in the room.
// (This prevents encounter definitions that reference enemies that are not placed.)
```

**Do not** add dialogue-ref validation. The `RoomSpec` schema has no `dialogue_refs` field. NPC IDs are validated via rule 3 above; dialogue correctness is tested by `runDataContractDiagnostics` separately.

- [ ] **Step 4: Update the Gradle task — use a clean name, not a double-"run" name**

```kotlin
task<JavaExec>("runAuthoringDiagnostics") {
    group = "verification"
    description = "Validate all room-spec files for authoring mistakes across all plateau families."
    classpath = project(":client").sourceSets["main"].runtimeClasspath
    mainClass.set("com.shadowascent.client.tools.RunGameAuthoringDiagnostics")
}

// Backward-compatibility alias for any script or doc that still references the Act I name.
task("runActIAuthoringDiagnostics") {
    dependsOn("runAuthoringDiagnostics")
}
```

- [ ] **Step 5: Update the authoring guide**

Add a multi-plateau section:

```markdown
## Multi-Plateau Authoring Rules
- Place every new room spec in `data/room_specs/<plateau>_vertical_slice.json`.
- Use only the five supported transition types: `free_exit`, `npc_handoff_gate`, `mission_gate`, `encounter_gate`, `return_gate`.
- Every `npc_id` in `npc_anchors` must exist in `data/npc_registry.json`.
- Every encounter ID in a room's `encounters` array must have a matching definition in the same file's `encounter_definitions`.
- Every transition `target_room_id` and `target_spawn_id` must exist in the catalog.
- Run `./gradlew runAuthoringDiagnostics` before pushing any room-spec change.
- Verify all ability-unlock flag names against `data/story_flags.json` before authoring transitions that set them.
```

- [ ] **Step 6: Close the M4b gate**

Update `docs/MILESTONE_B_GATE.md`. Mark complete the items that are now provably true:

- Diagnostics class generalized beyond Act I
- Diagnostics Gradle task updated and aliased
- Zero-Java room addition proof (fixture test from Task 1)
- Authoring guide updated with multi-plateau section

- [ ] **Step 7: Run diagnostics verification**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.tools.RunGameAuthoringDiagnosticsTest"
./gradlew.bat --console=plain runAuthoringDiagnostics
```

Expected: both PASS on the current room-spec corpus.

- [ ] **Step 8: Commit the diagnostics generalization and M4b closure**

```powershell
git add build.gradle.kts java/client/src/main/java/com/shadowascent/client/tools/RunGameAuthoringDiagnostics.java java/client/src/test/java/com/shadowascent/client/tools/RunGameAuthoringDiagnosticsTest.java docs/MILESTONE_B_GATE.md docs/guides/ROOM_AUTHORING_GUIDE.md
git rm java/client/src/main/java/com/shadowascent/client/tools/ActIAuthoringDiagnostics.java
git rm java/client/src/test/java/com/shadowascent/client/tools/ActIAuthoringDiagnosticsTest.java
git commit -m "feat: generalize rungame authoring diagnostics and close m4b"
```

---

## Task 3: Author And Stage Summit Shrine As The First New Plateau Slice

**Prerequisite:** Complete the Siren boss encounter pattern investigation (Required Pre-Work section above) before Step 3.

**Files:**
- Create: `data/room_specs/summit_shrine_vertical_slice.json`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- Create: `java/client/src/test/java/com/shadowascent/client/world/SummitShrineVerticalSliceBootstrapTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/SummitShrineOptionalContentTest.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java` (if beat/mission surfacing gaps found)
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java` (if optional content routing gaps found)

- [ ] **Step 1: Write the Summit Shrine bootstrap test first**

Create `SummitShrineVerticalSliceBootstrapTest.java`. The test sets `npc_withdrawal_started` to satisfy the entry room's `required_flags`, and does **not** pre-set any flag that appears in the room's `set_flags` (doing so would cause the room-selection filter to skip the room):

```java
@Test
void routesIntoSummitShrineApproachWhenLanternHeightsCompletionFlagIsSet() {
    GameState gameState = new GameState();
    gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
    gameState.getStoryState().setFlag("npc_withdrawal_started");
    // Do NOT set "entered_summit" — that is a set_flag on ss_shrine_approach
    // and pre-setting it causes the selection filter to skip the room.

    RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

    assertEquals("SUMMIT_SHRINE", profile.plateauId());
    assertEquals("ss_shrine_approach", profile.id());
    assertFalse(profile.roomTransitions().isEmpty());
}

@Test
void routesIntoShrineKeeperCourtyard_afterEnteringSummit() {
    GameState gameState = new GameState();
    gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
    gameState.getStoryState().setFlag("npc_withdrawal_started");
    gameState.getStoryState().setFlag("entered_summit");

    RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

    assertEquals("ss_shrine_courtyard", profile.id());
    assertTrue(profile.npcPlacements().stream().anyMatch(n -> "LISTENING_ELDER".equals(n.npcId())));
    assertTrue(profile.npcPlacements().stream().anyMatch(n -> "SHADE_HERMIT".equals(n.npcId())));
}

@Test
void arenaRoomIsGatedBehindMaskTruthBeat() {
    GameState gameState = new GameState();
    gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
    gameState.getStoryState().setFlag("npc_withdrawal_started");
    gameState.getStoryState().setFlag("entered_summit");
    gameState.getStoryState().setFlag("mask_truth_seen");

    RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

    assertEquals("ss_summit_arena", profile.id());
    assertFalse(profile.encounters().isEmpty());
}
```

- [ ] **Step 2: Run the Summit test and verify it fails**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.SummitShrineVerticalSliceBootstrapTest"
```

Expected: FAIL because the room-spec file does not exist yet.

- [ ] **Step 3: Author the complete Summit Shrine room-spec file**

Create `data/room_specs/summit_shrine_vertical_slice.json`. Every room must be schema-complete (see `lantern_heights_vertical_slice.json` for the required field set). All four rooms must be in the file; stub objects are not acceptable since `RoomSpecCatalog.validate()` will reject missing spawn points.

The `area_id` values must be new, distinct IDs that do not collide with existing areas — use `area_summit_shrine_courtyard` for the courtyard rather than reusing `area_empty_lantern_heights_hub` (which belongs to the `lantern_village` family and would cause routing ambiguity).

Room graph and requirements:

| Room ID | area_id | required_flags | set_flags | scene_role |
| --- | --- | --- | --- | --- |
| `ss_shrine_approach` | `area_summit_shrine_gate` | `["npc_withdrawal_started"]` | `["entered_summit"]` | `entry` |
| `ss_shrine_courtyard` | `area_summit_shrine_courtyard` | `["entered_summit"]` | `[]` | `social_hub` |
| `ss_inner_sanctum` | `area_summit_shrine_inner` | `["entered_summit"]` | `["mask_truth_seen"]` | `critical` |
| `ss_summit_arena` | `area_summit_shrine_arena` | `["mask_truth_seen"]` | `[]` | `boss` |

Bidirectionality requirement: `ss_shrine_approach` and `ss_shrine_courtyard` must each have a transition back to the other (one `free_exit` in each direction) so the spec geometry requirement for at least one bidirectional pair is satisfied.

Geometry requirement: at least one room in the graph must have a platform height spread ≥ 150 units (floor Y minus highest platform Y ≥ 150). In screen coordinates with floor at Y=360, the highest platform must be at Y ≤ 210 in at least one room.

NPC anchors in `ss_shrine_courtyard`: `LISTENING_ELDER` and `SHADE_HERMIT` with complete `x`, `y`, `patrol_min_x`, `patrol_max_x` values.

Encounter in `ss_summit_arena`: use the encounter pattern determined by the Siren boss investigation pre-work. If the scripted-loss path requires a runtime extension, author a clearly labeled stub encounter and create a follow-up issue.

- [ ] **Step 4: Add `area_summit_shrine_courtyard` and `area_summit_shrine_inner` to `area_catalog.json`**

Add two new entries to the `area_instances` array in `data/area_catalog.json`:

```json
{"id": "area_summit_shrine_courtyard", "family": "summit_shrine",
 "source_beat": "beat_siren_reveal", "authoring_status": "prototype_contract",
 "can_generate_elastic_neighbors": false},
{"id": "area_summit_shrine_inner", "family": "summit_shrine",
 "source_beat": "beat_siren_reveal", "authoring_status": "prototype_contract",
 "can_generate_elastic_neighbors": false}
```

- [ ] **Step 5: Add Summit Shrine routing to `AreaPlacementResolver`**

Add a `SUMMIT_SHRINE` case. Use only area IDs that exist in `area_catalog.json`:

```java
case SUMMIT_SHRINE -> {
    if (storyState.hasFlag("mask_truth_seen")) yield "area_summit_shrine_arena";
    if (storyState.hasFlag("entered_summit"))  yield "area_summit_shrine_courtyard";
    yield "area_summit_shrine_gate";
}
```

- [ ] **Step 6: Add Summit optional-content coverage with full lifecycle**

Create `SummitShrineOptionalContentTest.java`. The test must verify start, at least one progression step, and flag persistence across save/load — not just availability. Use whichever save/load API the passing `SaveLoadRuntimeStateTest` uses:

```java
@Test
void summitOptionalQuestStartsProgressesAndPersistsAcrossSaveLoad() throws Exception {
    GameState gameState = new GameState();
    gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
    gameState.getStoryState().setFlag("npc_withdrawal_started");
    gameState.getStoryState().setFlag("entered_summit");

    // Verify at least one optional quest becomes available at this plateau.
    MissionManager manager = new MissionManager(gameState);
    manager.updateAvailableMissions();
    List<Mission> available = manager.getAvailableMissions().stream()
            .filter(m -> "SUMMIT_SHRINE".equals(m.getPlateauId()))
            .toList();
    // If contracts define no optional quests for SUMMIT_SHRINE, this assertion
    // should be replaced with: assertTrue(available.isEmpty(), "expected no optional quests");
    // and the test documents the confirmed-empty state.
    assertFalse(available.isEmpty(), "Expected at least one optional quest for SUMMIT_SHRINE");

    // Advance the first available quest by one step.
    Mission quest = available.get(0);
    String progressFlag = quest.getSteps().get(0).getSetFlag();
    gameState.getStoryState().setFlag(progressFlag);

    // Save and reload.
    Path saveFile = Files.createTempFile("summit-optional", ".sav");
    // Use the same save/load API as SaveLoadRuntimeStateTest — verify the exact method signature.
    GameState loaded = roundTripSaveLoad(gameState, saveFile);

    assertTrue(loaded.getStoryState().hasFlag(progressFlag),
            "Progress flag must survive save/load");
}
```

- [ ] **Step 7: Verify Summit Shrine slice**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.SummitShrineVerticalSliceBootstrapTest" --tests "com.shadowascent.client.SummitShrineOptionalContentTest"
./gradlew.bat --console=plain runAuthoringDiagnostics
```

Expected: all PASS.

- [ ] **Step 8: Commit Summit Shrine**

```powershell
git add data/room_specs/summit_shrine_vertical_slice.json data/area_catalog.json java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java java/client/src/test/java/com/shadowascent/client/world/SummitShrineVerticalSliceBootstrapTest.java java/client/src/test/java/com/shadowascent/client/SummitShrineOptionalContentTest.java
git commit -m "feat: stage summit shrine runtime slice"
```

---

## Task 4: Lift And Replace Hollow Depths From Legacy Area Gates To Room-Spec Routing

**Prerequisite:** Complete the ability flag canonical mapping table (Required Pre-Work) before authoring any `set_flags` in encounter definitions.

**Files:**
- Create: `data/room_specs/hollow_depths_vertical_slice.json`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- Create: `java/client/src/test/java/com/shadowascent/client/world/HollowDepthsVerticalSliceBootstrapTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/HollowDepthsOptionalContentTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/world/HollowBossRoomSpecBindingTest.java`

**Migration note — the legacy chain has eight distinct areas.** The existing `buildAreaGates` in `AuthoringWorldBootstrap` routes through: `area_hollow_depths_camp` → `area_hollow_depths_caves` → `area_weightbound_mines_arena` → `area_hollow_hub_first_sparks` → `area_shatter_moth_nest` → `area_fractured_contact_high_winds` → `area_stone_judge_maze` → `area_abyssal_gate`. The room-spec file must reproduce this complete eight-node traversal chain. Do not collapse `area_fractured_contact_high_winds` (the double-jump restoration area, associated with `beat_fractured_contact_double_jump`) — it is a distinct narrative beat. The mapping is:

| Room ID | Replaces legacy area | Key narrative beat | Ability flag set here |
| --- | --- | --- | --- |
| `hd_abyssal_approach` | `area_hollow_depths_camp` | `beat_hollowing_intro` | — |
| `hd_hollow_caves` | `area_hollow_depths_caves` | `beat_hollow_depths_weight_dialogue` | — |
| `hd_weightbound_mines` | `area_weightbound_mines_arena` | `beat_weightbound_ogre` | dash (verify name) |
| `hd_first_sparks` | `area_hollow_hub_first_sparks` | `beat_dash_restored` | — |
| `hd_shattermoth_grove` | `area_shatter_moth_nest` | `beat_shatter_moth_queen` | — |
| `hd_fractured_contact` | `area_fractured_contact_high_winds` | `beat_fractured_contact_double_jump` | double jump (verify name) |
| `hd_stone_tribunal` | `area_stone_judge_maze` | `beat_stone_judge` | — |
| `hd_echo_gallery_a` | `area_echo_galleries` (split) | `beat_shadow_echo_fragments` | — |
| `hd_abyssal_gate` | `area_abyssal_gate` | `beat_final_cutoff_glide` | glide (verify name) |

Echo galleries (`hd_echo_gallery_a`) cover `area_echo_galleries` which is in the area catalog but not in the legacy gate chain. Add it as a side branch off `hd_hollow_caves` or `hd_first_sparks` — check `narrative_beats.json` for where echo fragment beats sit in relation to the main HOLLOW_DEPTHS route.

- [ ] **Step 1: Write the Hollow boss binding test before migration**

Create `HollowBossRoomSpecBindingTest.java`. This test verifies that boss entities spawned from room-spec `enemy_placements` trigger correct boss pattern dispatch:

```java
@Test
void weightboundBossEntityIsPlacedAndEncounterDefinitionReferencesIt() {
    // This test verifies the room-spec → encounter_definition → enemy_id chain is consistent.
    // It does not run the full GameSimulator; it inspects catalog state.
    RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();

    RoomSpec minesRoom = catalog.room("hd_weightbound_mines")
            .orElseThrow(() -> new AssertionError("hd_weightbound_mines not found in catalog"));

    boolean bossPlaced = minesRoom.enemyPlacements().stream()
            .anyMatch(e -> e.enemyId().contains("weightbound"));
    assertTrue(bossPlaced, "Weightbound boss must be in enemy_placements");

    boolean encounterExists = minesRoom.encounters().stream()
            .flatMap(encId -> catalog.encounter(encId).stream())
            .anyMatch(enc -> enc.enemyIds().stream().anyMatch(id -> id.contains("weightbound")));
    assertTrue(encounterExists, "Encounter definition must reference the weightbound boss entity");
}

@Test
void shimmerMothBossEntityIsPlacedAndEncounterDefinitionReferencesIt() {
    RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
    RoomSpec mothRoom = catalog.room("hd_shattermoth_grove")
            .orElseThrow(() -> new AssertionError("hd_shattermoth_grove not found in catalog"));

    assertTrue(mothRoom.enemyPlacements().stream()
            .anyMatch(e -> e.enemyId().contains("shatter")));
    assertTrue(mothRoom.encounters().stream()
            .flatMap(id -> catalog.encounter(id).stream())
            .anyMatch(enc -> enc.enemyIds().stream().anyMatch(id -> id.contains("shatter"))));
}
```

- [ ] **Step 2: Write the Hollow bootstrap test for the complete eight-node room graph**

Create `HollowDepthsVerticalSliceBootstrapTest.java`:

```java
@Test
void routesToApproachOnFreshHollowEntry() {
    GameState gs = hollowState("act2_unlocked");
    assertEquals("hd_abyssal_approach", bootstrap(gs).id());
}

@Test
void routesToCavesAfterAwokeFlag() {
    GameState gs = hollowState("act2_unlocked", "awoke_in_depths");
    assertEquals("hd_hollow_caves", bootstrap(gs).id());
}

@Test
void routesToWeightboundMinesAfterCavesCleared() {
    GameState gs = hollowState("act2_unlocked", "awoke_in_depths", "hollow_weight_understood");
    RunGameContentProfile profile = bootstrap(gs);
    assertEquals("hd_weightbound_mines", profile.id());
    assertFalse(profile.encounters().isEmpty(), "Boss room must have encounters");
}

@Test
void routesToFracturedContactAfterShatterMothDefeated() {
    GameState gs = hollowState("act2_unlocked", "awoke_in_depths", "hollow_weight_understood",
            "weightbound_ogre_defeated", "dash_restored", "shatter_moth_defeated");
    assertEquals("hd_fractured_contact", bootstrap(gs).id());
}

@Test
void routesToAbyssalGateAfterStoneJudgeDefeated() {
    GameState gs = hollowState("act2_unlocked", "awoke_in_depths", "hollow_weight_understood",
            "weightbound_ogre_defeated", "dash_restored", "shatter_moth_defeated",
            "double_jump_restored", "stone_judge_defeated");
    RunGameContentProfile profile = bootstrap(gs);
    assertEquals("hd_abyssal_gate", profile.id());
    assertFalse(profile.roomTransitions().isEmpty());
}

private static GameState hollowState(String... flags) {
    GameState gs = new GameState();
    gs.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
    for (String flag : flags) gs.getStoryState().setFlag(flag);
    return gs;
}
private static RunGameContentProfile bootstrap(GameState gs) {
    return new AuthoringWorldBootstrap().bootstrap(gs);
}
```

Replace all flag name strings with the verified canonical names from the ability flag table before running this test.

- [ ] **Step 3: Author the complete Hollow Depths room-spec file**

Create `hollow_depths_vertical_slice.json`. Every room must be schema-complete. Geometry must satisfy the traversal spec: HOLLOW_DEPTHS is specifically described in `area_catalog.json` as having "abyssal drops", "dash gaps", and "double-jump rises" — platforms must be spread across multiple elevation bands to reflect this. At least one room must have a platform height spread ≥ 150 units.

Echo gallery rooms (`hd_echo_gallery_a`): add a note in the room spec JSON that this room integrates with `EchoPuzzleEvaluator`. The encounter definition for echo rooms should be verified against how `EchoPuzzleEvaluator` expects to be triggered — if a new `clear_type` is needed, this is a targeted runtime extension requiring a test.

**Do not invent ability flag names.** Use only the verified canonical names from the Required Pre-Work table.

- [ ] **Step 4: Add HOLLOW_DEPTHS routing to `AreaPlacementResolver`**

Add the area resolution case using area IDs that match the `area_id` fields in the authored room specs:

```java
case HOLLOW_DEPTHS -> {
    if (storyState.hasFlag("stone_judge_defeated"))    yield "area_abyssal_gate";
    if (storyState.hasFlag("double_jump_restored"))    yield "area_stone_judge_maze";
    if (storyState.hasFlag("shatter_moth_defeated"))   yield "area_fractured_contact_high_winds";
    if (storyState.hasFlag("dash_restored"))           yield "area_shatter_moth_nest";
    if (storyState.hasFlag("weightbound_ogre_defeated")) yield "area_hollow_hub_first_sparks";
    if (storyState.hasFlag("hollow_weight_understood")) yield "area_weightbound_mines_arena";
    if (storyState.hasFlag("awoke_in_depths"))         yield "area_hollow_depths_caves";
    yield "area_hollow_depths_camp";
}
```

These area IDs must exactly match the `area_id` fields in the authored room specs.

- [ ] **Step 5: Keep legacy Hollow logic as a comparison safety net — do not delete yet**

Do not remove `buildTilesForArea`, `buildNpcPlacements`, or `buildAreaGates` legacy Hollow branches in this commit. They remain only so that if a room-spec miss occurs during migration, the legacy path provides a working fallback for debugging. The room-spec path wins when `resolveRoomSpec` returns non-null. Remove the legacy branches in the gate-closure commit (Task 9) after all tests pass.

- [ ] **Step 6: Add Hollow optional-content coverage with full lifecycle**

Create `HollowDepthsOptionalContentTest.java`. Test start, progress, completion, and persistence for at least one of the four HOLLOW_DEPTHS side-quest chains defined in `quests.json`. Use the same `roundTripSaveLoad` helper pattern as Task 3:

```java
@Test
void hollowOptionalQuestChainStartsProgressesAndPersistsAcrossSaveLoad() throws Exception {
    GameState gameState = new GameState();
    gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
    gameState.getStoryState().setFlag("awoke_in_depths");

    MissionManager manager = new MissionManager(gameState);
    manager.updateAvailableMissions();

    List<Mission> hollowOptional = manager.getAvailableMissions().stream()
            .filter(m -> "HOLLOW_DEPTHS".equals(m.getPlateauId()))
            .toList();
    assertFalse(hollowOptional.isEmpty(), "Expected HOLLOW_DEPTHS optional quests from contracts");

    Mission quest = hollowOptional.get(0);
    String progressFlag = quest.getSteps().get(0).getSetFlag();
    gameState.getStoryState().setFlag(progressFlag);

    GameState loaded = roundTripSaveLoad(gameState, Files.createTempFile("hollow-optional", ".sav"));
    assertTrue(loaded.getStoryState().hasFlag(progressFlag));
}
```

- [ ] **Step 7: Verify Hollow migration**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.HollowBossRoomSpecBindingTest" --tests "com.shadowascent.client.world.HollowDepthsVerticalSliceBootstrapTest" --tests "com.shadowascent.client.HollowDepthsOptionalContentTest"
./gradlew.bat --console=plain runAuthoringDiagnostics
```

Expected: all PASS.

- [ ] **Step 8: Commit the Hollow migration**

```powershell
git add data/room_specs/hollow_depths_vertical_slice.json java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java java/client/src/test/java/com/shadowascent/client/world/HollowDepthsVerticalSliceBootstrapTest.java java/client/src/test/java/com/shadowascent/client/HollowDepthsOptionalContentTest.java java/client/src/test/java/com/shadowascent/client/world/HollowBossRoomSpecBindingTest.java
git commit -m "feat: migrate hollow depths to room spec routing"
```

---

## Task 5: Fix Campaign Continuity Before Staging Later Plateaus

**Prerequisite:** Read `SaveLoadRuntimeStateTest.java` to determine the actual save/load API signature before writing any code in this task.

**Files:**
- Modify: `java/core/src/main/java/com/shadowascent/core/RegressionTest.java`
- Modify: whichever save/load class the passing `SaveLoadRuntimeStateTest` uses (do not assume `SaveLoad.java`)
- Modify: `java/core/src/main/java/com/shadowascent/core/StoryState.java` (only if the root cause requires it)
- Modify: `java/core/src/main/java/com/shadowascent/core/GameState.java` (only if the root cause requires it)
- Create: `java/client/src/test/java/com/shadowascent/client/CampaignContinuitySaveLoadBoundaryTest.java`

- [ ] **Step 1: Extract the failing continuity scenario into a focused test**

Create `CampaignContinuitySaveLoadBoundaryTest.java`. Mirror the boundary conditions that the failing `Campaign Continuity` regression section tests, but as a standalone JUnit test that gives a clear failure message:

```java
@Test
void saveLoadPreservesPlateauBoundaryProgressionAcrossSummitAndHollow() throws Exception {
    GameState gameState = new GameState();
    gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
    gameState.getStoryState().setFlag("yin_yang_taken");
    // Use the flag name for "Aen hollowed" verified against story_flags.json.
    gameState.getStoryState().setFlag("aen_hollowed");

    // Use the same save/load method as SaveLoadRuntimeStateTest.
    GameState loaded = roundTripSaveLoad(gameState,
            Files.createTempFile("campaign-continuity", ".sav"));

    assertEquals("SUMMIT_SHRINE",
            loaded.getStoryState().getCurrentPlateau().name(),
            "Plateau must survive save/load");
    assertTrue(loaded.getStoryState().hasFlag("yin_yang_taken"),
            "Critical transition flag must survive save/load");
    assertTrue(loaded.getStoryState().hasFlag("aen_hollowed"),
            "Hollowing flag must survive save/load");
}
```

- [ ] **Step 2: Run the focused continuity test and capture the actual failure**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.CampaignContinuitySaveLoadBoundaryTest"
./gradlew.bat --console=plain runRegressionTests
```

Capture the exact exception and field that fails. Do not proceed to Step 3 until the failure mode is confirmed.

- [ ] **Step 3: Fix the root cause — apply only the change the failing test proves is needed**

Apply the minimal fix to whichever class the test implicates. The fix pattern will be something like one of:
- A flag not being included in the SAVE_V3 serialized payload
- A plateau enum not being serialized/deserialized correctly
- A field being reset during load that should be preserved

Do not apply speculative fixes. Fix only what the extracted test demonstrates.

- [ ] **Step 4: Re-run and confirm both the focused test and the full regression harness**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.CampaignContinuitySaveLoadBoundaryTest"
./gradlew.bat --console=plain runRegressionTests
```

Expected:
- focused continuity test PASS
- `Campaign Continuity` regression section PASS
- all other sections still PASS

- [ ] **Step 5: Commit the continuity fix**

Include only the files that were actually changed. Do not speculatively add files that might not have needed changes:

```powershell
git add java/client/src/test/java/com/shadowascent/client/CampaignContinuitySaveLoadBoundaryTest.java
# Add whichever source files were actually changed — verify with git diff before adding.
git commit -m "fix: restore campaign continuity across plateau save boundaries"
```

---

## Task 6: Stage Ember Monastery And Ability Unlock Transitions

**Prerequisite:** Ability flag canonical mapping table must be complete before authoring any transitions in this task.

**Files:**
- Create: `data/room_specs/ember_monastery_vertical_slice.json`
- Create: `java/client/src/test/java/com/shadowascent/client/world/EmberMonasteryVerticalSliceBootstrapTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/EmberMonasteryOptionalContentTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/world/EmberAbilityUnlockTransitionTest.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`

- [ ] **Step 1: Write the ability-unlock transition test first**

Create `EmberAbilityUnlockTransitionTest.java`. The test verifies that the specific transitions in the Ember Monastery room graph set the correct ability flags. Use the verified canonical flag names from the Required Pre-Work table:

```java
@Test
void emberHearth_trialTransitionSetsAirDodgeAndGrappleFlags() {
    // Verify the transition INTO the hearth-of-brothers trial room sets ability flags.
    RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
    RoomSpec courtyardRoom = catalog.room("em_courtyard_hub")
            .orElseThrow(() -> new AssertionError("em_courtyard_hub not in catalog"));

    // Find the transition that leads to the ability-unlock room.
    RoomTransitionSpec unlockTransition = courtyardRoom.transitions().stream()
            .filter(t -> "em_hearth_trial".equals(t.targetRoomId()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No transition to em_hearth_trial"));

    // Verify it sets the canonical air-dodge and grapple flags.
    // Replace these string literals with the verified canonical names.
    assertTrue(unlockTransition.setFlags().contains("VERIFIED_AIR_DODGE_FLAG"),
            "Hearth trial transition must set air-dodge flag");
    assertTrue(unlockTransition.setFlags().contains("VERIFIED_GRAPPLE_FLAG"),
            "Hearth trial transition must set grapple flag");
}

@Test
void rogaDojo_transitionSetsWallClingFlag() {
    RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
    // Find the transition into the roga dojo room and assert wall-cling flag.
    boolean wallClingSet = catalog.allRooms().stream()
            .flatMap(r -> r.transitions().stream())
            .filter(t -> "em_roga_dojo".equals(t.targetRoomId()))
            .anyMatch(t -> t.setFlags().contains("VERIFIED_WALL_CLING_FLAG"));
    assertTrue(wallClingSet, "A transition into em_roga_dojo must set wall-cling flag");
}
```

- [ ] **Step 2: Author the complete Ember Monastery room-spec file**

Create `ember_monastery_vertical_slice.json` with five rooms. Ability unlock flags must be on **separate** transitions per ability delivery — do not bundle multiple ability flags onto a single transition, and do not mix state flags (e.g. `entered_ember_monastery`) with ability flags on the same transition.

| Room ID | area_id | Transition into this room sets |
| --- | --- | --- |
| `em_monastery_gate` | `area_old_road_to_ember` | `entered_ember_monastery` |
| `em_courtyard_hub` | `area_hearth_hall` | — |
| `em_hearth_trial` | `area_hearth_of_brothers_trial` | verified air-dodge flag + verified grapple flag |
| `em_roga_dojo` | `area_roga_dojo` | verified wall-cling flag |
| `em_departure_arch` | `area_skyroad_gate` | `skyroad_opened` |

NPC anchors in `em_courtyard_hub`: `BROTHER_KAI`, `BROTHER_LEN`, `BROTHER_ASH`.
NPC anchor in `em_roga_dojo`: `MENTOR_ROGA`. Do not omit `MENTOR_ROGA` — the dojo room without its trainer is a fidelity failure.

Bidirectionality: `em_monastery_gate ↔ em_courtyard_hub` must have transitions in both directions.

- [ ] **Step 3: Add Ember bootstrap and optional-content tests with full lifecycle**

Bootstrap test key assertions:

```java
assertEquals("EMBER_MONASTERY", profile.plateauId());
assertEquals("em_monastery_gate", profile.id());
assertTrue(profile.npcPlacements().stream().anyMatch(n -> "BROTHER_KAI".equals(n.npcId())));

// After ability unlock flags are set, dojo room is accessible.
gs.getStoryState().setFlag("entered_ember_monastery");
gs.getStoryState().setFlag(VERIFIED_AIR_DODGE_FLAG);
gs.getStoryState().setFlag(VERIFIED_GRAPPLE_FLAG);
RunGameContentProfile dojoProfile = new AuthoringWorldBootstrap().bootstrap(gs);
assertTrue(dojoProfile.npcPlacements().stream().anyMatch(n -> "MENTOR_ROGA".equals(n.npcId())));
```

Optional-content test: follow the full lifecycle pattern from Task 3 for at least one EMBER_MONASTERY optional quest found in `quests.json`.

- [ ] **Step 4: Add Ember routing to `AreaPlacementResolver`**

```java
case EMBER_MONASTERY -> {
    if (storyState.hasFlag("skyroad_opened"))             yield "area_skyroad_gate";
    if (storyState.hasFlag(VERIFIED_WALL_CLING_FLAG))    yield "area_roga_dojo";
    if (storyState.hasFlag(VERIFIED_AIR_DODGE_FLAG))     yield "area_hearth_of_brothers_trial";
    if (storyState.hasFlag("entered_ember_monastery"))   yield "area_hearth_hall";
    yield "area_old_road_to_ember";
}
```

- [ ] **Step 5: Verify Ember staging**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.EmberMonasteryVerticalSliceBootstrapTest" --tests "com.shadowascent.client.EmberMonasteryOptionalContentTest" --tests "com.shadowascent.client.world.EmberAbilityUnlockTransitionTest"
./gradlew.bat --console=plain runAuthoringDiagnostics
```

- [ ] **Step 6: Commit Ember Monastery**

```powershell
git add data/room_specs/ember_monastery_vertical_slice.json java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java java/client/src/test/java/com/shadowascent/client/world/EmberMonasteryVerticalSliceBootstrapTest.java java/client/src/test/java/com/shadowascent/client/EmberMonasteryOptionalContentTest.java java/client/src/test/java/com/shadowascent/client/world/EmberAbilityUnlockTransitionTest.java
git commit -m "feat: stage ember monastery runtime slice"
```

---

## Task 7: Stage Winding Skyroad As The Traversal Gauntlet Plateau

**Files:**
- Create: `data/room_specs/winding_skyroad_vertical_slice.json`
- Create: `java/client/src/test/java/com/shadowascent/client/world/WindingSkyRoadVerticalSliceBootstrapTest.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`

**No boss encounter.** WINDING_SKYROAD has no boss in the data contracts (`quests.json`, `narrative_beats.json`, `npc_registry.json`). Do not invent one. The traversal challenge room uses standard enemy types.

**Four rooms, not three.** The area catalog has four named area IDs for WINDING_SKYROAD: `area_skyroad_base`, `area_cloud_bridges` (implied), `area_star_wind_fields`, `area_mirror_summit_gate`. The room `ws_star_wind_fields` is required — it anchors `beat_yin_yang_as_stars`, a named narrative beat.

**No optional content.** Verify that `quests.json` defines no optional quest chains for WINDING_SKYROAD before confirming this. If confirmed empty, document it explicitly in the test file as a comment: "Verified against quests.json — WINDING_SKYROAD has no optional quest chains."

- [ ] **Step 1: Write the Winding Skyroad bootstrap test**

```java
@Test
void skyroadEntryRequiresEmberCompletionAndAllAbilityFlags() {
    GameState gs = new GameState();
    gs.getStoryState().setPlateau(StoryState.Plateau.WINDING_SKYROAD);
    gs.getStoryState().setFlag("entered_ember_monastery");
    gs.getStoryState().setFlag(VERIFIED_AIR_DODGE_FLAG);
    gs.getStoryState().setFlag(VERIFIED_GRAPPLE_FLAG);
    gs.getStoryState().setFlag(VERIFIED_WALL_CLING_FLAG);

    RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gs);

    assertEquals("WINDING_SKYROAD", profile.plateauId());
    assertEquals("ws_skybridge_start", profile.id());
    assertFalse(profile.roomTransitions().isEmpty());
}

@Test
void starWindFieldsRoomIsReachableAndSetsNarrativeBeat() {
    GameState gs = new GameState();
    gs.getStoryState().setPlateau(StoryState.Plateau.WINDING_SKYROAD);
    gs.getStoryState().setFlag("entered_ember_monastery");
    gs.getStoryState().setFlag(VERIFIED_AIR_DODGE_FLAG);
    gs.getStoryState().setFlag(VERIFIED_GRAPPLE_FLAG);
    gs.getStoryState().setFlag(VERIFIED_WALL_CLING_FLAG);
    gs.getStoryState().setFlag("skyroad_ascent_cleared");

    RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gs);
    assertEquals("ws_star_wind_fields", profile.id());
}
```

- [ ] **Step 2: Author the Skyroad room-spec file — four rooms**

| Room ID | area_id | role | required_flags |
| --- | --- | --- | --- |
| `ws_skybridge_start` | `area_skyroad_base` | `entry` | EMBER ability flags |
| `ws_cloud_crossing` | `area_cloud_bridges` | `traversal` | `skyroad_ascent_cleared` (set by skybridge exit) |
| `ws_star_wind_fields` | `area_star_wind_fields` | `narrative` | `skyroad_ascent_cleared` |
| `ws_summit_approach` | `area_mirror_summit_gate` | `exit` | `yin_yang_as_stars_seen` (set by star_wind_fields arrival) |

Geometry for WINDING_SKYROAD must be authentically vertical — the area catalog describes "vertical climbs, cloud bridges, wind tunnels." Every room must have platform spreads exceeding 200 units (screen coordinates). No flat corridors.

Bidirectionality: `ws_skybridge_start ↔ ws_cloud_crossing` at minimum.

The exit transition from `ws_summit_approach` sets `beat_mirror_gate_opened` and routes to the first Mirror Summit room.

- [ ] **Step 3: Add Skyroad routing to `AreaPlacementResolver`**

```java
case WINDING_SKYROAD -> {
    if (storyState.hasFlag("yin_yang_as_stars_seen")) yield "area_mirror_summit_gate";
    if (storyState.hasFlag("skyroad_ascent_cleared")) yield "area_star_wind_fields";
    yield "area_skyroad_base";
}
```

- [ ] **Step 4: Verify Winding Skyroad**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.WindingSkyRoadVerticalSliceBootstrapTest"
./gradlew.bat --console=plain runAuthoringDiagnostics
```

- [ ] **Step 5: Commit Winding Skyroad**

```powershell
git add data/room_specs/winding_skyroad_vertical_slice.json java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java java/client/src/test/java/com/shadowascent/client/world/WindingSkyRoadVerticalSliceBootstrapTest.java
git commit -m "feat: stage winding skyroad runtime slice"
```

---

## Task 8: Stage Mirror Summit, Beacon Cliff, And The Post-Climax Return State

**Files:**
- Create: `data/room_specs/mirror_summit_vertical_slice.json`
- Create: `data/room_specs/beacon_cliff_vertical_slice.json`
- Create: `java/client/src/test/java/com/shadowascent/client/world/MirrorSummitVerticalSliceBootstrapTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/world/BeaconCliffVerticalSliceBootstrapTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/PostClimaxStatePersistenceTest.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- Modify: whichever save/load class Task 5 identified

**Prerequisite — verify HOLLOW_REFLECTION outcome flags.** Before authoring the Mirror Summit encounter, read `data/narrative_beats.json` for `beat_final_fight` and `beat_i_release_you`, and read `data/story_flags.json` for the canonical flags those beats set. The encounter definition must set the OUTCOME flags (e.g. `hollow_reflection_defeated`, `yin_yang_returned`, `beacon_path_opened`) — not `mirror_gate_opened`, which is an ENTRY condition already set before the player arrives.

**Post-climax area_id coordination.** The `bc_epilogue_overlook` room spec must have `"area_id": "area_final_npc_overlook"`. `AreaPlacementResolver` must return `"area_final_npc_overlook"` when `beat_ending_complete` is set. These two values must match exactly or `resolveRoomSpec` Step 2 lookup fails.

- [ ] **Step 1: Write the post-climax persistence test first**

```java
@Test
void postClimaxSaveLoadsIntoEpilogueRoomNotCampaignRoom() throws Exception {
    GameState gameState = new GameState();
    gameState.getStoryState().setPlateau(StoryState.Plateau.BEACON_CLIFF);
    gameState.getStoryState().setFlag("beat_ending_complete");
    gameState.setCurrentRoomId("bc_epilogue_overlook");

    GameState loaded = roundTripSaveLoad(gameState,
            Files.createTempFile("post-climax", ".sav"));

    assertEquals("bc_epilogue_overlook", loaded.getCurrentRoomId(),
            "Post-climax save must reload into epilogue room, not be reset");
    assertTrue(loaded.getStoryState().hasFlag("beat_ending_complete"));
}

@Test
void postClimaxBootstrapRoutesToEpilogueArea() {
    GameState gs = new GameState();
    gs.getStoryState().setPlateau(StoryState.Plateau.BEACON_CLIFF);
    gs.getStoryState().setFlag("beat_ending_complete");

    RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gs);

    assertTrue(profile.id().contains("epilogue"),
            "Post-climax bootstrap must route to epilogue room");
    assertFalse(profile.npcPlacements().isEmpty(),
            "Epilogue room must have at least one NPC staged");
}
```

- [ ] **Step 2: Author the Mirror Summit room specs**

Three rooms with bidirectional navigation:

| Room ID | area_id | required_flags |
| --- | --- | --- |
| `ms_mirror_gate` | `area_mirror_summit_gate` | `beat_mirror_gate_opened` |
| `ms_reflection_chamber` | `area_mirror_summit_peak` | `beat_mirror_gate_opened` |
| `ms_hollow_reflection_arena` | `area_hollow_reflection_arena` | echo puzzle completion flag (verify) |

Bidirectionality: `ms_mirror_gate ↔ ms_reflection_chamber` — both directions required.

HOLLOW_REFLECTION encounter definition: use the outcome flags verified from `narrative_beats.json`. Do NOT include `mirror_gate_opened` as an outcome — it is an entry condition, not an outcome. The encounter sets flags like `hollow_reflection_defeated` that the BEACON_CLIFF entry room uses as a `required_flag`.

Geometry: Mirror Summit's aesthetic is "self-confrontation and release" — use geometric symmetry in the reflection chamber (mirrored platform layout).

- [ ] **Step 3: Author the Beacon Cliff room specs including the epilogue**

Four rooms:

| Room ID | area_id | required_flags | set_flags |
| --- | --- | --- | --- |
| `bc_cliff_ascent` | `area_beacon_cliff_approach` | `hollow_reflection_defeated` | `beacon_walk_started` |
| `bc_final_platform` | `area_ancient_beacon` | `beacon_walk_started` | — |
| `bc_beacon_lit` | `area_ancient_beacon` | `beacon_walk_started` | — |
| `bc_epilogue_overlook` | `area_final_npc_overlook` | — | — |

The transition from `bc_final_platform` or `bc_beacon_lit` into `bc_epilogue_overlook` sets `beat_ending_complete`:

```json
{"id": "gate_to_epilogue", "type": "return_gate",
 "target_room_id": "bc_epilogue_overlook",
 "target_spawn_id": "spawn_from_beacon",
 "min_x": 1420, "max_x": 1540,
 "set_flags": ["beat_ending_complete"]}
```

NPC anchor in `bc_epilogue_overlook`: use a village NPC the player has a positive relationship with (SAMSON, SOPHIA, or OLD_MAN_RIKU — check `dialogue.json` for which has a post-climax line authored). Do NOT use `VEIL_MAIDEN` — she is the antagonist; placing her in the post-climax epilogue without a documented reconciliation scene contradicts the narrative contract.

Return path: `bc_epilogue_overlook` must have a return transition back to `bc_beacon_lit` so the player is not trapped.

- [ ] **Step 4: Add post-climax pre-check and plateau routing to `AreaPlacementResolver`**

The post-climax check must be a **global pre-check at the top of `resolveAreaId()`**, before any plateau-specific branch. Otherwise a loaded post-climax save might route to a different area before the check runs:

```java
public String resolveAreaId(GameState gameState) {
    StoryState storyState = gameState.getStoryState();

    // Global pre-check: post-climax routing takes precedence over all plateau-specific logic.
    if (storyState.hasFlag("beat_ending_complete")) {
        return "area_final_npc_overlook";
    }

    return switch (storyState.getCurrentPlateau()) {
        case LANTERN_HEIGHTS -> { /* existing logic */ }
        case SUMMIT_SHRINE   -> { /* Task 3 logic */ }
        case HOLLOW_DEPTHS   -> { /* Task 4 logic */ }
        case EMBER_MONASTERY -> { /* Task 6 logic */ }
        case WINDING_SKYROAD -> { /* Task 7 logic */ }
        case MIRROR_SUMMIT   -> {
            if (storyState.hasFlag("hollow_reflection_defeated")) yield "area_hollow_reflection_arena";
            yield "area_mirror_summit_gate";
        }
        case BEACON_CLIFF -> {
            if (storyState.hasFlag("beacon_walk_started")) yield "area_ancient_beacon";
            yield "area_beacon_cliff_approach";
        }
        default -> "area_lantern_heights_hub";
    };
}
```

- [ ] **Step 5: Verify Mirror, Beacon, and post-climax persistence**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.MirrorSummitVerticalSliceBootstrapTest" --tests "com.shadowascent.client.world.BeaconCliffVerticalSliceBootstrapTest" --tests "com.shadowascent.client.PostClimaxStatePersistenceTest"
./gradlew.bat --console=plain runAuthoringDiagnostics
```

- [ ] **Step 6: Commit Mirror, Beacon, and epilogue**

```powershell
git add data/room_specs/mirror_summit_vertical_slice.json data/room_specs/beacon_cliff_vertical_slice.json java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java java/client/src/test/java/com/shadowascent/client/world/MirrorSummitVerticalSliceBootstrapTest.java java/client/src/test/java/com/shadowascent/client/world/BeaconCliffVerticalSliceBootstrapTest.java java/client/src/test/java/com/shadowascent/client/PostClimaxStatePersistenceTest.java
git commit -m "feat: stage mirror summit beacon cliff and epilogue"
```

---

## Task 9: Add Plateau Geometry Fidelity Tests And End-To-End Campaign Route Coverage

**Files:**
- Create: `java/client/src/test/java/com/shadowascent/client/world/PlateauGeometryFidelityTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/FullCampaignRouteEndToEndTest.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java` — remove legacy HOLLOW_DEPTHS branches now that room-spec routing covers them
- Modify: `java/core/src/main/java/com/shadowascent/core/RegressionTest.java` — add any milestone-closure sections the harness needs

- [ ] **Step 1: Write the plateau geometry fidelity test**

The test inspects room-spec JSON via `RoomSpecCatalog`. It must assert only the **six newly staged plateaus** — do not include `LANTERN_HEIGHTS` (some Act I rooms have geometry spread < 150 units by design and would fail the check):

```java
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
        assertTrue(count >= 3,
                plateauId + " must have at least 3 rooms (found " + count + ")");
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
    // In screen coordinates: higher Y = lower in world. Floor is at high Y, platforms at lower Y.
    // Spread = floor Y − highest platform Y. A spread ≥ 150 means meaningful vertical range.
    boolean found = catalog.roomsForPlateau(plateauId).stream().anyMatch(room -> {
        if (room.geometry().isEmpty()) return false;
        float minY = room.geometry().stream()
                .map(RoomSpec.GeometrySpec::y).min(Float::compare).orElse(0f);
        float maxY = room.geometry().stream()
                .map(RoomSpec.GeometrySpec::y).max(Float::compare).orElse(0f);
        return (maxY - minY) >= 150f;
    });
    assertTrue(found, plateauId + " must have at least one room with ≥150 units vertical spread");
}

private static void assertPlateauHasBidirectionalPair(RoomSpecCatalog catalog, String plateauId) {
    List<RoomSpec> rooms = catalog.roomsForPlateau(plateauId);
    Set<String> roomIds = rooms.stream().map(RoomSpec::id).collect(Collectors.toSet());
    boolean found = rooms.stream().anyMatch(roomA ->
        roomA.transitions().stream().anyMatch(t -> {
            String targetId = t.targetRoomId();
            if (!roomIds.contains(targetId)) return false;
            return catalog.room(targetId)
                    .map(roomB -> roomB.transitions().stream()
                            .anyMatch(bt -> roomA.id().equals(bt.targetRoomId())))
                    .orElse(false);
        })
    );
    assertTrue(found, plateauId + " must have at least one bidirectional room pair");
}
```

- [ ] **Step 2: Write the complete end-to-end campaign route smoke**

The test exercises bootstrap routing in narrative order with explicit flag advancement at each plateau boundary. Provide the full test body — do not leave comments as placeholders:

```java
@Test
void fullCampaignRouteTraversesAllPlateausAndEndsInStableEpilogue() throws Exception {
    AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();

    // LANTERN_HEIGHTS — fresh start.
    GameState gs = new GameState();
    gs.getStoryState().setPlateau(StoryState.Plateau.LANTERN_HEIGHTS);
    RunGameContentProfile lh = bootstrap.bootstrap(gs);
    assertEquals("LANTERN_HEIGHTS", lh.plateauId());

    // Advance to SUMMIT_SHRINE.
    gs.getStoryState().setFlag("npc_withdrawal_started");
    gs.setCurrentRoomId(null);
    gs.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
    RunGameContentProfile ss = bootstrap.bootstrap(gs);
    assertEquals("SUMMIT_SHRINE", ss.plateauId());
    assertFalse(ss.roomTransitions().isEmpty(), "Summit Shrine rooms must have transitions");

    // Critical beat: Siren confrontation flags set.
    // Use the canonical flag names verified against story_flags.json.
    gs.getStoryState().setFlag("yin_yang_taken");
    gs.getStoryState().setFlag("aen_hollowed"); // verify canonical name

    // Advance to HOLLOW_DEPTHS.
    gs.setCurrentRoomId(null);
    gs.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
    gs.getStoryState().setFlag("act2_unlocked");
    RunGameContentProfile hd = bootstrap.bootstrap(gs);
    assertEquals("HOLLOW_DEPTHS", hd.plateauId());
    assertFalse(hd.roomTransitions().isEmpty(), "Hollow Depths rooms must have transitions");

    // Advance through HOLLOW_DEPTHS — simulate boss progression.
    gs.getStoryState().setFlag("awoke_in_depths");
    gs.getStoryState().setFlag("hollow_weight_understood");
    gs.getStoryState().setFlag("weightbound_ogre_defeated");
    gs.getStoryState().setFlag("dash_restored"); // verify canonical name
    gs.getStoryState().setFlag("shatter_moth_defeated");
    gs.getStoryState().setFlag("double_jump_restored"); // verify canonical name
    gs.getStoryState().setFlag("stone_judge_defeated");
    gs.getStoryState().setFlag("abyssal_gate_cleared");

    // Advance to EMBER_MONASTERY.
    gs.setCurrentRoomId(null);
    gs.getStoryState().setPlateau(StoryState.Plateau.EMBER_MONASTERY);
    RunGameContentProfile em = bootstrap.bootstrap(gs);
    assertEquals("EMBER_MONASTERY", em.plateauId());
    assertTrue(em.npcPlacements().stream().anyMatch(n -> "BROTHER_KAI".equals(n.npcId())));

    // Advance through EMBER_MONASTERY — ability unlocks.
    gs.getStoryState().setFlag("entered_ember_monastery");
    gs.getStoryState().setFlag(VERIFIED_AIR_DODGE_FLAG);
    gs.getStoryState().setFlag(VERIFIED_GRAPPLE_FLAG);
    gs.getStoryState().setFlag(VERIFIED_WALL_CLING_FLAG);
    gs.getStoryState().setFlag("skyroad_opened");

    // Advance to WINDING_SKYROAD.
    gs.setCurrentRoomId(null);
    gs.getStoryState().setPlateau(StoryState.Plateau.WINDING_SKYROAD);
    RunGameContentProfile ws = bootstrap.bootstrap(gs);
    assertEquals("WINDING_SKYROAD", ws.plateauId());

    // Advance through WINDING_SKYROAD.
    gs.getStoryState().setFlag("skyroad_ascent_cleared");
    gs.getStoryState().setFlag("yin_yang_as_stars_seen");
    gs.getStoryState().setFlag("beat_mirror_gate_opened");

    // Advance to MIRROR_SUMMIT.
    gs.setCurrentRoomId(null);
    gs.getStoryState().setPlateau(StoryState.Plateau.MIRROR_SUMMIT);
    RunGameContentProfile ms = bootstrap.bootstrap(gs);
    assertEquals("MIRROR_SUMMIT", ms.plateauId());

    // Advance through MIRROR_SUMMIT — final boss defeated.
    gs.getStoryState().setFlag("hollow_reflection_defeated"); // verify canonical name

    // Advance to BEACON_CLIFF.
    gs.setCurrentRoomId(null);
    gs.getStoryState().setPlateau(StoryState.Plateau.BEACON_CLIFF);
    RunGameContentProfile bc = bootstrap.bootstrap(gs);
    assertEquals("BEACON_CLIFF", bc.plateauId());
    assertFalse(bc.id().contains("epilogue"), "Must be in campaign room, not epilogue, before climax");

    // Trigger climax completion.
    gs.getStoryState().setFlag("beat_ending_complete");
    gs.setCurrentRoomId(null);
    RunGameContentProfile epilogue = bootstrap.bootstrap(gs);
    assertTrue(epilogue.id().contains("epilogue"),
            "Post-climax bootstrap must route to epilogue room");
    assertFalse(epilogue.npcPlacements().isEmpty(),
            "Epilogue must have at least one NPC");

    // Verify post-climax save/load stability.
    GameState loaded = roundTripSaveLoad(gs,
            Files.createTempFile("full-campaign-epilogue", ".sav"));
    assertTrue(loaded.getStoryState().hasFlag("beat_ending_complete"));
}
```

Replace all flag name string literals with the verified canonical names before committing.

- [ ] **Step 3: Remove legacy HOLLOW_DEPTHS branches from `AuthoringWorldBootstrap`**

Now that room-spec routing covers HOLLOW_DEPTHS, remove the legacy branches from `buildTilesForArea`, `buildNpcPlacements`, `buildEnemyPlacements`, `buildAreaGates`, `preferredNpcX`, and `areaNpcFilter` that are specific to HOLLOW_DEPTHS area IDs. Verify after deletion that the HOLLOW_DEPTHS bootstrap tests still pass — if any test regresses, the room spec is missing a room or area that the legacy path covered.

- [ ] **Step 4: Run geometry and full-route verification**

```powershell
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.PlateauGeometryFidelityTest" --tests "com.shadowascent.client.FullCampaignRouteEndToEndTest"
./gradlew.bat --console=plain runAuthoringDiagnostics
./gradlew.bat --console=plain runRegressionTests
```

Expected: all geometry pass conditions green, full route smoke green, regression harness fully green including `Campaign Continuity`.

- [ ] **Step 5: Commit the closure test surface**

```powershell
git add java/client/src/test/java/com/shadowascent/client/world/PlateauGeometryFidelityTest.java java/client/src/test/java/com/shadowascent/client/FullCampaignRouteEndToEndTest.java java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java java/core/src/main/java/com/shadowascent/core/RegressionTest.java
git commit -m "test: add m4 full geometry fidelity and full campaign route coverage"
```

---

## Task 10: Close M4 Full Gate And Sync Project Truth Docs

**Files:**
- Create: `docs/MILESTONE_GATE_M4_FULL.md`
- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/NORTH_STAR_EXECUTION_MATRIX.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Modify: `docs/PLAYABLE_TRUTH.md` — create this file if it does not exist; verify before this step.

- [ ] **Step 1: Write the M4 Full gate doc**

Create `docs/MILESTONE_GATE_M4_FULL.md` with the complete closure checklist:

```markdown
# M4 Full Milestone Gate

## Closure Checklist

- [ ] ResolveRoomSpecMultiPlateauFallbackTest green
- [ ] ActIVerticalSliceBootstrapTest green (regression — no regressions from routing change)
- [ ] SummitShrineVerticalSliceBootstrapTest green
- [ ] SummitShrineOptionalContentTest green
- [ ] HollowBossRoomSpecBindingTest green
- [ ] HollowDepthsVerticalSliceBootstrapTest green
- [ ] HollowDepthsOptionalContentTest green
- [ ] CampaignContinuitySaveLoadBoundaryTest green
- [ ] EmberAbilityUnlockTransitionTest green
- [ ] EmberMonasteryVerticalSliceBootstrapTest green
- [ ] EmberMonasteryOptionalContentTest green
- [ ] WindingSkyRoadVerticalSliceBootstrapTest green
- [ ] MirrorSummitVerticalSliceBootstrapTest green
- [ ] BeaconCliffVerticalSliceBootstrapTest green
- [ ] PostClimaxStatePersistenceTest green
- [ ] PlateauGeometryFidelityTest green (all six new plateaus)
- [ ] FullCampaignRouteEndToEndTest green
- [ ] runAuthoringDiagnostics green (all plateau room specs)
- [ ] Campaign Continuity regression section green
- [ ] Full runRegressionTests green

## Evidence
- Gate run date: [fill in]
- Gradle output: [attach or link]
```

- [ ] **Step 2: Run the full gate — use the complete gate command from CLAUDE.md**

```powershell
./gradlew.bat --console=plain clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runAuthoringDiagnostics runRegressionTests
```

Expected: all commands PASS. Do not sign off the gate doc until this exact command passes.

- [ ] **Step 3: Update project truth docs**

```text
docs/CURRENT_STATE.md
- Promote M4 and M4b from active/partial to complete.
- Note all seven plateau families have room-spec staging in runGame.
- Note legacy HOLLOW_DEPTHS branches removed.

docs/NORTH_STAR_EXECUTION_MATRIX.md
- Mark M4 row: completed, with evidence link to MILESTONE_GATE_M4_FULL.md.
- Mark M4b row: completed.

docs/IMPLEMENTATION_BACKLOG.md
- Move all remaining M4 items to done.
- Promote the next active tranche (M6 elastic generation or campaign fidelity).
```

- [ ] **Step 4: Docs freshness check**

Verify whether `scripts/check_docs_freshness.py` exists before running it. If it does not exist, manually update `docs/reports/docs_freshness_report.md` to reflect the M4 closure date and newly completed docs.

```powershell
if (Test-Path "scripts/check_docs_freshness.py") {
    python scripts/check_docs_freshness.py --emit-report
} else {
    Write-Host "Freshness script not found — update docs/reports/docs_freshness_report.md manually."
}
```

- [ ] **Step 5: Tag the milestone and commit closure**

```powershell
git add docs/MILESTONE_GATE_M4_FULL.md docs/CURRENT_STATE.md docs/NORTH_STAR_EXECUTION_MATRIX.md docs/IMPLEMENTATION_BACKLOG.md docs/PLAYABLE_TRUTH.md docs/reports/docs_freshness_report.md
git commit -m "docs: close m4 full campaign spine milestone"
git tag m4-full
```

---

## Self-Review

### Spec coverage

- Multi-plateau routing unlock and LANTERN_HEIGHTS guard removal: Task 1.
- M4b prerequisite closure and diagnostics reuse: Tasks 1, 2.
- Area catalog family cleanup: Task 1.
- Ability flag canonical mapping pre-work: Required Pre-Work section.
- Siren boss encounter pattern investigation: Required Pre-Work section.
- Save/load API verification: Required Pre-Work section.
- Summit Shrine staging with correct flag semantics: Task 3.
- Hollow Depths lift-and-replace across all eight legacy areas, boss encounter binding, echo gallery noted, optional content: Task 4.
- Campaign Continuity fix with reproduced test: Task 5.
- Ember Monastery with split ability-unlock transitions and MENTOR_ROGA anchor: Task 6.
- Winding Skyroad with four rooms including `beat_yin_yang_as_stars`, no invented boss: Task 7.
- Mirror Summit with correct encounter outcome flags and bidirectional navigation: Task 8.
- Beacon Cliff with correct epilogue NPC (non-antagonist), post-climax pre-check at global level: Task 8.
- Geometry fidelity gate excluding LANTERN_HEIGHTS from assertions: Task 9.
- Complete end-to-end test body with all seven plateaus: Task 9.
- Legacy HOLLOW_DEPTHS branches removed after migration: Task 9.
- Full gate command matching CLAUDE.md (including server compile and worldgen diagnostics): Task 10.
- git tag m4-full: Task 10.

### Placeholder scan

- All `VERIFIED_*_FLAG` string constants in test code must be replaced with canonical flag names from `data/story_flags.json` before committing. These are the only intentional placeholders, and they are bounded to specific test files.
- Task 3 encounter definition for the Siren fight contains one conditional placeholder pending the boss pattern investigation.

### Type consistency

- Room-spec naming follows the existing `RoomSpecCatalog` / `RunGameContentProfile` / `RoomTransitionSpec` surfaces.
- Diagnostics class rename preserves the old Gradle entry point as an alias.
- Save/load API usage mirrors whatever `SaveLoadRuntimeStateTest` uses — no assumptions made.

## Notes For The Implementer

- Do not grow the legacy fallback path for any new plateau. All new plateaus go through room-spec staging exclusively.
- HOLLOW_DEPTHS is the one explicit lift-and-replace: reproduce the full eight-node legacy chain in room specs, then remove the legacy branches in Task 9.
- Treat geometry as a milestone requirement. `PlateauGeometryFidelityTest` is a gate, not a stretch goal.
- Do not declare M4 Full complete while `Campaign Continuity` is red, even if all plateau slices appear playable in isolation.
- Do not sign off `docs/MILESTONE_GATE_M4_FULL.md` against a partial gate run. The full gate command in Task 10 Step 2 is the only acceptable evidence.
