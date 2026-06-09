# M4b Act I Authoring Velocity And Fidelity Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete Milestone B so Act I content in `runGame` can be extended and tuned through the supported authoring path with fast failure on bad content, minimal Java touchpoints for ordinary room or beat additions, and stronger readability driven by better authored structure.

**Architecture:** Preserve the M4a runtime boundaries: `java/core` remains the authority for progression, mission truth, and save/load state; `java/client` remains responsible for room-spec-driven authored runtime assembly and presentation. M4b should reduce the amount of Java code that must change for ordinary Act I growth by moving more variation into validated room-spec, encounter, NPC-staging, and dialogue authoring paths. Readability improvements should come primarily from richer authored metadata and stronger validators, not from piling on one-off UI logic.

**Tech Stack:** Java 21, LibGDX 1.12.1, LWJGL3 desktop backend, JUnit 5, JSON content contracts under `data/`, current `GameSimulator` / `GameState` / `MissionManager` / `HubManager`, Gradle task surface, and docs under `docs/`.

---

## Scope

This plan implements **M4b** from [2026-05-14-act-i-milestone-stack-design.md](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/docs/superpowers/specs/2026-05-14-act-i-milestone-stack-design.md:1).

M4b starts only after M4a is functionally closed. This plan assumes the runtime slice is already truthful enough to play through the first Act I loop and focuses on:

- authoring velocity first
- validation and diagnostics second
- readability improvements that follow from stronger content structure

This plan does **not** broaden into `EMBER_MONASTERY`, does **not** redesign combat, and does **not** turn into a general editor initiative.

**Hollow Depths hardcoding is explicitly deferred.** `AuthoringWorldBootstrap` contains a hardcoded fallback path (the `legacy_bootstrap` branch) that covers 9 Hollow Depths areas via inline switch statements for player spawn coordinates, NPC placement maps (`preferredNpcX()`), enemy placements (`buildEnemyPlacements()`), and gate definitions (`buildAreaGates()`). This is a real authoring-velocity problem but it is M6 scope — Hollow Depths authored area deepening belongs to the open-world runtime expansion work. M4b authoring velocity proofs must operate entirely within the Act I room-spec path (`data/room_specs/lantern_heights_vertical_slice.json`, `data/room_specs/mistwood_vertical_slice.json`) where room-spec loading already works end-to-end. Do not migrate Hollow Depths to room-spec as part of M4b.

## Inputs

Use these sources in this order:

1. Approved milestone spec:
   - `docs/superpowers/specs/2026-05-14-act-i-milestone-stack-design.md`
2. M4a closure artifacts:
   - `docs/MILESTONE_A_GATE.md`
   - `docs/ACT_I_QA_ROUTE.md`
   - `docs/CURRENT_STATE.md`
   - `docs/NORTH_STAR_EXECUTION_MATRIX.md`
3. Runtime truth:
   - `data/room_specs/*.json`
   - `data/dialogue.json`
   - `data/narrative_beats.json`
   - `data/quests.json`
   - `data/npc_registry.json`
   - `java/client/src/main/java/...`
   - `java/core/src/main/java/...`
4. Existing M4a test surface:
   - `RoomSpecCatalogTest`
   - `ActIVerticalSliceBootstrapTest`
   - `RunGameAreaTransitionTest`
   - `ActIRouteStateSmokeTest`
   - `ActIOptionalQuestFlowTest`
   - `SaveLoadRuntimeStateTest`

## Success Criteria

This plan is complete only when all of the following are true:

- a new Act I room or optional side beat can be added through JSON authoring alone with zero Java changes, and this is proved by a test
- content-authoring mistakes fail fast with actionable diagnostics
- common content-growth operations are bounded tasks rather than rediscovery work
- readability improvements are achieved by authored metadata and reusable patterns, not by one-off hardcoded branching
- `docs/MILESTONE_B_AUTHORING_PATTERNS.md` and `docs/MILESTONE_B_GATE.md` exist and match the implementation truth

## File Structure

### Runtime and validation files expected to change

- Modify: `data/room_specs/lantern_heights_vertical_slice.json`
- Modify: `data/room_specs/mistwood_vertical_slice.json`
- Create (Task 2 skeleton; Task 5 extends with proof room): `data/room_specs/act_i_authoring_fixture.json`
- Modify: `data/dialogue.json`
- Modify: `data/narrative_beats.json`
- Modify (Task 8 — add `route_hint` and `mainline` fields to quest schema): `data/quests.json`
- Modify (Task 6 — add optional NPC anchor entries for room-spec-driven staging): `data/npc_registry.json`
- Modify: `data/area_catalog.json`

- Modify: `java/client/src/main/java/com/shadowascent/client/world/RoomSpecCatalog.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/RunGameContentProfile.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
- Modify (Task 8 — add `areaLabel(roomSpec)` or equivalent so area display names come from room-spec `display_name` rather than hardcoded switches): `java/client/src/main/java/com/shadowascent/client/ui/UiText.java`

- Modify: `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/MissionManager.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/HubManager.java`

### Tooling and diagnostics surface expected to change

- Modify: `build.gradle.kts`
- Create (Task 3 — validation logic): `java/client/src/main/java/com/shadowascent/client/tools/ActIAuthoringDiagnostics.java`
- Create (Task 3 — tests for passing fixture and intentionally bad samples): `java/client/src/test/java/com/shadowascent/client/tools/ActIAuthoringDiagnosticsTest.java`

### Tests expected to change or be added

- Modify: `java/client/src/test/java/com/shadowascent/client/world/RoomSpecCatalogTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ActIOptionalQuestFlowTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameMissionInteractionTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameAreaTransitionTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/MinimapOverlayRendererTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/world/ActIAuthoringFixtureRoundTripTest.java`

### Docs expected to change

- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/NORTH_STAR_EXECUTION_MATRIX.md`
- Modify: `docs/MILESTONE_A_GATE.md`
- Create: `docs/MILESTONE_B_AUTHORING_PATTERNS.md`
- Create: `docs/MILESTONE_B_GATE.md`

---

### Task 1: Reconcile Milestone Closure Truth Before M4b Starts

**Files:**
- Modify: `docs/MILESTONE_A_GATE.md`
- Modify: `docs/NORTH_STAR_EXECUTION_MATRIX.md`
- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/superpowers/specs/2026-05-14-act-i-milestone-stack-design.md`

- [ ] Change `docs/MILESTONE_A_GATE.md` frontmatter status from `active` to `complete` if M4a is being treated as closed.
- [ ] Change `docs/NORTH_STAR_EXECUTION_MATRIX.md` so M4a is no longer marked `active` once the closure claim is official.
- [ ] Confirm `docs/CURRENT_STATE.md` and the milestone matrix agree on M4a/M4b state labels.
- [ ] Update the Milestone A spec to reference the current M4a plan path:
  - replace the outdated `2026-05-14-act-i-vertical-slice-readiness-implementation.md` reference
  - point at `2026-05-14-m4a-act-i-vertical-slice-playable-readiness-implementation.md`
- [ ] Commit this truth-sync as its own docs-only tranche before changing runtime or diagnostics code.

**Acceptance:**
- [ ] The milestone control docs all describe the same state for M4a and M4b.

### Task 2: Define The Supported M4b Authoring Operations

**Files:**
- Create: `docs/MILESTONE_B_AUTHORING_PATTERNS.md` (skeleton — filled out in Task 11)
- Create: `data/room_specs/act_i_authoring_fixture.json` (skeleton; Task 5 adds the proof room)
- Input: `data/room_specs/lantern_heights_vertical_slice.json`
- Input: `data/room_specs/mistwood_vertical_slice.json`

- [ ] Write down the exact operations M4b promises to support without Java changes:
  - add one new room
  - add one optional side beat
  - add one room-state variant
  - add one bounded encounter
  - add one mission-critical readability cue through authored metadata
- [ ] Create the skeleton of `act_i_authoring_fixture.json` with valid schema header, an empty `rooms` array, and an empty `encounter_definitions` array. This file is consumed only by tests — it must not be added to `RoomSpecCatalog.loadDefault()` scan path. Task 5 will add the actual proof room entry.
- [ ] Keep the fixture structurally consistent with production room-spec files (`lantern_heights_vertical_slice.json` is the reference) so it exercises the real parsing path.

**Acceptance:**
- [ ] There is an explicit, testable authoring contract for what M4b is supposed to make easy.
- [ ] `act_i_authoring_fixture.json` parses without error under `RoomSpecCatalog.load(List.of(fixturePath))`.

### Task 3: Expand Room-Spec Validation To Catch Authoring Errors Early

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/world/RoomSpecCatalog.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/RoomSpecCatalogTest.java`
- Create: `java/client/src/main/java/com/shadowascent/client/tools/ActIAuthoringDiagnostics.java`
- Create: `java/client/src/test/java/com/shadowascent/client/tools/ActIAuthoringDiagnosticsTest.java`

**What `RoomSpecCatalog` already validates (do not duplicate):**

- duplicate room IDs across all loaded files
- transition targets that reference a room ID not present in the catalog
- encounter IDs referenced by a room that are not defined in `encounter_definitions`

**New validation to add (what currently fails silently):**

- [ ] Stale room IDs referenced by `dialogue.json` or `narrative_beats.json` (beat `areas` field) that no longer exist in the room-spec catalog.
- [ ] Spawn IDs referenced by a transition's `target_spawn_id` that are not present in the target room's `spawn_points` list.
- [ ] NPC IDs referenced in a room's `npc_anchors` that are not registered in `data/npc_registry.json`.
- [ ] Room-state variants within the same area whose `required_flags` sets are identical (overlap risk — first-wins resolution becomes non-deterministic under small edits).
- [ ] Optional-content transitions (`free_exit` type) that share a trigger band with a `mission_gate` or `encounter_gate` on the same room edge, creating an ambiguous block.

- [ ] Create `ActIAuthoringDiagnostics.java` with a `validate(RoomSpecCatalog, GameDataContracts)` method that aggregates all check results into a typed report, and a `main()` entry point for Gradle invocation (used in Task 4).
- [ ] Create `ActIAuthoringDiagnosticsTest.java` with tests for both a passing fixture and intentionally broken authoring samples covering each new check.
- [ ] Make every diagnostic message name the exact room ID, transition ID, NPC ID, or beat ID causing the failure — no generic messages.
- [ ] Keep scope bounded to Act I authoring surfaces (Lantern Heights + Mistwood room specs); do not broaden into full-campaign contract validation here.

**Acceptance:**
- [ ] Each new authoring mistake type above produces a named, actionable diagnostic message.
- [ ] A clean fixture passes all checks without warnings.

### Task 4: Add A Dedicated Act I Authoring Diagnostics Gradle Command

**Files:**
- Modify: `build.gradle.kts`
- Modify (add `main()` Gradle entry point if not already present from Task 3): `java/client/src/main/java/com/shadowascent/client/tools/ActIAuthoringDiagnostics.java`

`ActIAuthoringDiagnosticsTest.java` is created in Task 3 — do not create it again here.

- [ ] Register `runActIAuthoringDiagnostics` in `build.gradle.kts` following the same pattern as `runDataContractDiagnostics` — `JavaExec` task targeting `com.shadowascent.client.tools.ActIAuthoringDiagnostics`, classpath `:client:runtimeClasspath`, working directory project root.
- [ ] The command must exit non-zero when any validation check fails, so CI can gate on it.
- [ ] Confirm the task is wired to load production data from `data/room_specs/`, `data/dialogue.json`, and `data/npc_registry.json` — not the test fixture path.

**Acceptance:**
- [ ] `./gradlew.bat runActIAuthoringDiagnostics` exits 0 on the clean repo.
- [ ] `./gradlew.bat runActIAuthoringDiagnostics` exits non-zero when a known bad authoring sample is injected.

### Task 5: Prove Zero-Java Room Addition Through A Fixture-Based Test

**Files:**
- Modify (extend skeleton from Task 2 with proof room): `data/room_specs/act_i_authoring_fixture.json`
- Create: `java/client/src/test/java/com/shadowascent/client/world/ActIAuthoringFixtureRoundTripTest.java`

`RoomSpecCatalog.java` must NOT be modified to make this test pass — if a change is needed there, that change belongs in Task 3 and must be completed first.

**Current baseline (confirmed by code review):** `RoomSpecCatalog.loadDefault()` already scans all `.json` files in `data/room_specs/` alphabetically. A new JSON file placed there is automatically ingested. For LANTERN_HEIGHTS, `AuthoringWorldBootstrap.resolveRoomSpec()` selects rooms by plateau ID and flag predicates — no hardcoded room ID list. This means a new Lantern Heights room is already addable without Java changes, which is the condition this test must verify and lock in.

- [ ] Add a new room entry to `act_i_authoring_fixture.json` with:
  - a unique room ID (`lh_fixture_annex` or similar),
  - `plateau_id: "LANTERN_HEIGHTS"`,
  - a `required_flags` entry that is distinct from any production room's predicate,
  - one `free_exit` transition back to an existing room (e.g., `lh_hub_social`),
  - at least one spawn point and a floor tile in `geometry`.
- [ ] Write `ActIAuthoringFixtureRoundTripTest` loading the catalog from both the production room-spec files AND the fixture file via `RoomSpecCatalog.load(List.of(productionFiles..., fixturePath))`.
- [ ] Assert: the fixture room appears in `catalog.roomsForPlateau("LANTERN_HEIGHTS")`.
- [ ] Set the matching required flags on a `GameState` and assert: `AuthoringWorldBootstrap.bootstrap(gameState)` returns the fixture room — not a production fallback.
- [ ] Assert: `RunGameAreaTransition.tryTraverse()` resolves the fixture room's free-exit transition without exception.
- [ ] The test must contain a comment: `// No Java changes were made to runtime code to make this room load — that is the invariant this test protects.`

**Acceptance:**
- [ ] The test passes on an unmodified runtime.
- [ ] The test would fail if `AuthoringWorldBootstrap` were to hardcode a room ID whitelist or require a Java switch case for the new room.

### Task 6: Generalize Room-State Variant Authoring

**Files:**
- Modify: `data/room_specs/lantern_heights_vertical_slice.json`
- Modify (Task 6 — optional NPC anchor entries for room-spec-driven staging): `data/npc_registry.json`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java`

**Specific brittle code to address (confirmed by code review):**

`AuthoringWorldBootstrap.shouldStageNpcAnchor()` contains a hardcoded NPC whitelist per scene role — NPCs allowed in `return_changed` and `isolation_night` variants are named explicitly in Java. Adding a new NPC to those scenes requires a Java edit.

`AuthoringWorldBootstrap.preferredNpcX()` contains a hardcoded map of NPC-ID-to-X-position per area. Any NPC position change or new NPC placement requires a Java edit.

**Do not touch:** The `legacy_bootstrap` fallback path (the `buildTilesForArea`, `buildNpcPlacements`, `buildEnemyPlacements`, `buildAreaGates` methods covering Hollow Depths areas) is explicitly out of M4b scope — see Scope section.

- [ ] Move the `shouldStageNpcAnchor()` NPC whitelist logic into room-spec `npc_anchors` scene-role metadata: if a room spec's `npc_anchors` entry declares an NPC with a matching scene role, that is the authority. The hardcoded Java whitelist becomes redundant and can be removed for room-spec-covered rooms.
- [ ] Move NPC X-positions from `preferredNpcX()` into room-spec `npc_anchors[].x` fields in `lantern_heights_vertical_slice.json`. For room-spec-driven rooms, read positions from the spec; remove the inline switch for those rooms.
- [ ] Ensure `npc_registry.json` has entries for any optional NPC (`OLD_MAN_RIKU`, `LANTERN_KID`) that are staged through room-spec anchors so the diagnostic from Task 3 passes cleanly.
- [ ] Make room-state variant precedence explicit in code comments or schema: when multiple rooms share the same `area_id` and `plateau_id`, the first whose `required_flags` are all set wins. Document this rule.
- [ ] Add tests for:
  - one Lantern Heights area with multiple room-state variants verifying correct selection per flag state
  - a new room-state variant added through JSON only (using the fixture from Task 5)
  - no accidental flag-predicate overlap between variants

**Acceptance:**
- [ ] Adding a new NPC to a Lantern Heights room-spec scene, or repositioning one, requires only a JSON edit.
- [ ] Adding a new room-state variant for an existing Lantern Heights area requires only a JSON edit and a new `required_flags` predicate — no Java.

### Task 7: Generalize Optional Side-Beat Authoring

**Files:**
- Modify: `data/quests.json`
- Modify: `data/dialogue.json`
- Modify: `data/narrative_beats.json`
- Modify: `java/core/src/main/java/com/shadowascent/core/MissionManager.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ActIOptionalQuestFlowTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameMissionInteractionTest.java`

**Specific hardcoding to address (confirmed by code review):**

`RunGameMissionInteraction.STARTER_NPCS_BY_MISSION` is a hardcoded Java map associating 5 mainline mission IDs with their starter NPC ID lists. Adding a new mainline mission requires a Java edit to this constant.

`RunGameMissionInteraction.objectiveIdForNpc()` contains a hardcoded switch on mission ID for `village_bonds`, `dojo_practice`, and `veil_request`, mapping NPC IDs to objective IDs. The `default` branch already handles `sq_` quests via `sideQuestStep()` contract lookup — the mainline branches are the brittle ones for growth.

**Scope boundary:** Do NOT rewrite `MissionManager.updateAvailableMissions()` or `onMissionCompleted()` — those 8 mainline mission gating rules and completion side-effects involve act advancement, plateau transitions, and ability grants that are not safe to table-drive without a separate schema design effort. This task is scoped only to the `sq_` quest authoring path.

- [ ] Verify that `STARTER_NPCS_BY_MISSION` is not consulted for `sq_` quests — the `firstAvailableMissionForNpc()` contract path must already bypass it. If it does not, fix the routing so `sq_` quests are entirely contract-driven.
- [ ] Verify that `objectiveIdForNpc()` already falls through to the `sideQuestStep()` contract lookup for any quest ID that starts with `sq_` and that this path has no hardcoded assumption about the four current ACT_0 quest IDs.
- [ ] Add a fifth `sq_` quest entry to `data/quests.json` as a fixture beat (e.g., `sq_fixture_q1_test_errand` for NPC `LANTERN_KID`, `act: "ACT_0"`, plateau `LANTERN_HEIGHTS`, one objective). This quest should be structurally identical to the production `sq_` quests but clearly labelled as a test fixture.
- [ ] Add a test to `RunGameMissionInteractionTest`: set the `village_bonds` completion flag, call `updateAvailableMissions()`, then call `applyNpcInteraction(gameState, "LANTERN_KID")` — assert the fixture quest starts and the active mission ID starts with `sq_fixture_`. This test must pass with no new Java switch branch for the fixture quest ID.
- [ ] Confirm `ActIOptionalQuestFlowTest` still passes after adding the fixture quest (it should, since it tests a generic `sq_` prefix, not a specific ID).

**Acceptance:**
- [ ] A new `sq_` quest added to `data/quests.json` with the correct `act`, `plateau`, `npc`, and `steps` fields becomes startable from NPC interaction with zero Java changes.
- [ ] This is proved by the `RunGameMissionInteractionTest` fixture-quest test described above.

### Task 8: Push Readability Improvements Into Authored Metadata

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
- Modify (add `areaLabel(roomSpec)` reading `display_name` from room-spec; remove hardcoded area-ID switch): `java/client/src/main/java/com/shadowascent/client/ui/UiText.java`
- Modify: `data/room_specs/lantern_heights_vertical_slice.json`
- Modify: `data/room_specs/mistwood_vertical_slice.json`
- Modify: `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/MinimapOverlayRendererTest.java`

**Specific hardcoding to move into contract data (confirmed by code review):**

`HubScreen.missionRoutePrompt()` has 11 hardcoded switch cases mapping mission IDs to route hint strings. Every new mission requires a new Java case.

`HubScreen.isMainlineMission()` lists 7 hardcoded mission IDs. New mainline missions are invisible to it without a Java edit.

`HubScreen.plateauForMission()` hardcodes a region-string-to-plateau-enum map, redundant with the `plateau` field already present in the quest contract.

- [ ] Add a `route_hint` string field to the quest schema in `data/quests.json`. Populate it for all 8 mainline missions and all 4 ACT_0 side quests with the same text currently in `missionRoutePrompt()`. Expose via `GameDataContracts.routeHintForMission(missionId)`.
- [ ] Replace `HubScreen.missionRoutePrompt()` with a contract lookup returning `contracts.routeHintForMission(missionId)`, falling back to `null`. Delete the switch body.
- [ ] Add a `mainline: true/false` boolean field to the quest schema. Set it on the 8 existing mainline missions. Replace `HubScreen.isMainlineMission()` with a `GameDataContracts` lookup so new missions need only a JSON field, not a Java edit.
- [ ] Replace `HubScreen.plateauForMission()` with a direct read of the quest contract's existing `plateau` field — delete the Java switch.
- [ ] Add `UiText.areaLabel(RoomSpec roomSpec)` returning `roomSpec.displayName()`. Update room entry message call sites to use it instead of the existing area-ID switch where a `RoomSpec` is available.
- [ ] Update `HudOverlayStateTest` to assert `missionRoutePrompt` resolves correctly for a mainline mission and the Task 7 fixture side quest via the contract `route_hint` field — not from a Java constant.

**Acceptance:**
- [ ] Adding a new mission's route hint requires only a `data/quests.json` `route_hint` field — no Java.
- [ ] `HubScreen.missionRoutePrompt()` contains no hardcoded mission ID strings after this task.
- [ ] `HubScreen.isMainlineMission()` contains no hardcoded mission ID strings after this task.

### Task 9: Strengthen Encounter And Transition Authoring Patterns

**Files:**
- Modify: `data/room_specs/mistwood_vertical_slice.json`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameAreaTransition.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameAreaTransitionTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/ActIAuthoringFixtureRoundTripTest.java`

- [ ] Make bounded encounter authoring easier to extend to more Mistwood depth:
  - additional encounter-gated rooms
  - alternate return-state transitions
  - richer outbound route without new hardcoded gate semantics
- [ ] Add tests proving the generic transition and encounter patterns scale beyond the current single-clearing shape.

**Acceptance:**
- [ ] Mistwood can deepen structurally through the existing authored path rather than bespoke runtime logic.

### Task 10: Harden Save/Load Expectations For Content Growth

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/SaveLoad.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/SaveLoadRuntimeStateTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ActIAuthoringFixtureRoundTripTest.java`

- [ ] Ensure content growth does not silently break save assumptions for:
  - new room IDs
  - new room-state variants
  - new optional side beats
  - new encounter IDs
- [ ] Add tests proving save/load remains deterministic when the authoring fixture includes growth cases supported by M4b.

**Acceptance:**
- [ ] Content iteration does not undermine persistence trust.

### Task 11: Write The Authoring Pattern Guide And Gate Doc

**Files:**
- Create: `docs/MILESTONE_B_AUTHORING_PATTERNS.md`
- Create: `docs/MILESTONE_B_GATE.md`
- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/NORTH_STAR_EXECUTION_MATRIX.md`

- [ ] Write `docs/MILESTONE_B_AUTHORING_PATTERNS.md` covering:
  - adding a new room
  - adding a new optional side beat
  - adding a new room-state variant
  - adding a bounded encounter
- [ ] Write `docs/MILESTONE_B_GATE.md` in the same style as the other milestone gate docs.
- [ ] Record the exact evidence required by the M4b spec:
  - diagnostics command exists and fails fast
  - zero-Java room or beat addition is proved by test
  - readability improvements are grounded in stronger authored structure
- [ ] Update milestone truth docs only after the validation command and zero-Java test are green.

**Acceptance:**
- [ ] M4b has explicit operating instructions and a formal closure artifact.

---

## Recommended Commit Boundaries

1. `docs: reconcile m4a closure truth before m4b`
2. `feat: add act i authoring diagnostics`
3. `test: prove zero-java room addition for act i`
4. `feat: generalize act i room-state and side-beat authoring`
5. `feat: move act i readability toward authored metadata`
6. `feat: harden act i content growth persistence`
7. `docs: add milestone b patterns and gate`

## Verification Gates

Run these after each relevant tranche:

- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.RoomSpecCatalogTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.ActIVerticalSliceBootstrapTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.ActIRouteStateSmokeTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.ActIOptionalQuestFlowTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.RunGameAreaTransitionTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.RunGameMissionInteractionTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.SaveLoadRuntimeStateTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.ActIAuthoringFixtureRoundTripTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.tools.ActIAuthoringDiagnosticsTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.ui.HudOverlayStateTest" --tests "com.shadowascent.client.ui.MinimapOverlayRendererTest"`
- `./gradlew.bat --console=plain runActIAuthoringDiagnostics`
- `./gradlew.bat --console=plain :client:compileJava :core:compileJava`
- `./gradlew.bat --console=plain runRegressionTests`
- `python scripts/check_docs_freshness.py --emit-report`

## Self-Review

- [ ] The first task reconciles milestone-truth drift before assuming M4a closed.
- [ ] The plan addresses the exact M4b acceptance requirement of zero-Java room or beat addition.
- [ ] The diagnostics command is explicit, not implied.
- [ ] Readability work follows authoring-structure improvements rather than leading them.
- [ ] The plan does not broaden into next-plateau scope.
- [ ] The Hollow Depths legacy fallback path (`buildTilesForArea`, `buildNpcPlacements`, `buildEnemyPlacements`, `buildAreaGates` covering 9 areas) is explicitly called out as M6 scope and is not touched by any M4b task.
- [ ] `act_i_authoring_fixture.json` is created once (Task 2 skeleton, Task 5 extends) — not created twice.
- [ ] `ActIAuthoringDiagnostics.java` is created once (Task 3) — Task 4 only adds the Gradle wiring.
- [ ] Task 5 zero-Java proof is grounded in the confirmed behavior of `RoomSpecCatalog.loadDefault()` (scans all `.json` files) and `AuthoringWorldBootstrap.resolveRoomSpec()` (flag-predicate selection, no hardcoded room ID list for Lantern Heights).
- [ ] Task 7 acceptance has a binary-testable form matching Task 5: a fixture `sq_` quest starts from NPC interaction via a `RunGameMissionInteractionTest` assertion, with no new Java switch branch.
- [ ] Task 8 acceptance is concrete: `missionRoutePrompt()` and `isMainlineMission()` contain no hardcoded mission ID strings after the task — verified by code inspection, not just by test pass.
