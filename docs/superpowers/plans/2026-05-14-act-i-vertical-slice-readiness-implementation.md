# Act I Vertical Slice Readiness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `runGame` ready to author and tune a real Act I vertical slice that runs from the Lantern Heights opening through the first Mistwood outing and ends after the first return-to-hub emotional change.

**Architecture:** Keep `java/core` authoritative for progression, mission logic, encounter truth, dialogue selection, and save/load state. Keep `java/client` responsible for authored room specs, room transitions, runtime staging, HUD/minimap/dialogue surfacing, and LibGDX presentation. Build a reusable room-spec schema immediately so Lantern Heights and Mistwood are authored through the same path instead of one-off bootstrap code.

**Tech Stack:** Java 21, LibGDX 1.12.1, LWJGL3 desktop backend, JUnit 5, JSON-authored game contracts under `data/`, current `GameSimulator`/`GameState`/`MissionManager`/`HubManager` runtime, and supporting docs/GDD material under `docs/` and `game_design_document/`.

---

## Locked Scope

These are not open questions anymore. Implement the plan around them.

- The first Act I vertical slice ends **after the first return-to-hub emotional change**.
- Lantern Heights authoring starts from a **reusable room-spec schema immediately**.
- Early combat is tuned around the **current bounded 2-hit combo baseline**.
- `Mistwood` is **inside** the first vertical-slice scope.

## Success Criteria

This plan is complete only when all of the following are true:

- `runGame` can boot an authored Lantern Heights room graph through room-spec data.
- The player can move through:
  - balcony opener
  - Lantern Heights social hub
  - mission handoff
  - outbound Mistwood route
  - first bounded combat/mission loop
  - return route
  - changed post-return hub state
- NPC staging, dialogue, mission prompts, and transitions are all scene-aware enough that the route feels authored rather than sandbox-flat.
- Save/load works across outbound and return phases without corrupting room, encounter, or mission truth.
- QA can tell what room they are in, what objective is active, why a gate is blocked, and whether a route failure is content or runtime.

## Canonical Inputs

Use these sources in this order:

1. Runtime truth in repo:
   - `java/core/src/main/java/...`
   - `java/client/src/main/java/...`
   - `data/area_catalog.json`
   - `data/narrative_beats.json`
   - `data/dialogue.json`
   - `data/quests.json`
   - `data/npc_registry.json`
   - `docs/PLAYABLE_TRUTH.md` _(pre-flight: verify this reflects the current `runGame` QA surface before treating as authoritative — it may lag `CURRENT_STATE.md`; reconcile before Task 1 begins)_
   - `docs/CURRENT_STATE.md`
2. Readiness framing:
   - `docs/superpowers/plans/2026-05-14-act-i-vertical-slice-readiness-plan.md`
3. GDD and donor inspiration:
   - `game_design_document/09_level_and_content_plan/...`
   - `game_design_document/15_character_dossiers/...`
   - donor package references for room flow and early mission tone only

## File Map

### New data/runtime files expected

- Create: `data/room_specs/lantern_heights_vertical_slice.json`
- Create: `data/room_specs/mistwood_vertical_slice.json`
- Create: `java/client/src/main/java/com/shadowascent/client/world/RoomSpec.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/RoomTransitionSpec.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/EncounterSpec.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/RoomSpecCatalog.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/RoomRuntimeProfile.java` _(created in Task 3; see that task for ownership)_
- Create: `java/client/src/test/java/com/shadowascent/client/world/RoomSpecCatalogTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java` _(created in Task 15; referenced by Tasks 7, 10, 14, and 16)_
- Create: `java/core/src/test/java/com/shadowascent/core/simulation/GameSimulatorMeleeCombatTest.java` _(created in Task 13)_

### Existing runtime files expected to change

- Modify: `java/client/src/main/java/com/shadowascent/client/world/RunGameContentProfile.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameAreaTransition.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/UiText.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/rendering/SpriteWorldRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/SaveLoad.java`

### Existing core files expected to change

- Modify: `java/core/src/main/java/com/shadowascent/core/GameState.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/MissionManager.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/HubManager.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/data/BeatDefinition.java` _(only if room/scene fields are required by a specific task; no task currently specifies what changes — verify before modifying or omit)_
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/GameSimulator.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/SimPlayer.java`

### Docs expected to change late in the plan

- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `docs/CURRENT_STATE.md`
- Create if absent / Modify: `docs/ACT_I_QA_ROUTE.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`

---

## Pre-flight

Before Task 1 begins, verify the following are true. These are not implementation steps — they are authoring assumptions the plan depends on.

- [ ] `docs/PLAYABLE_TRUTH.md` reflects the current `runGame` QA surface. If it lags `CURRENT_STATE.md` (e.g., still describes `PlaytestClient` as the lead QA surface), update it now. Do not treat a stale PLAYABLE_TRUTH as authoritative input for room authoring.
- [ ] `data/area_catalog.json` is loaded and contains entries for all area IDs that will be referenced in room specs (at minimum: `area_lantern_heights_balcony`, `area_lantern_heights_hub`, and the Mistwood area IDs). Add any missing entries before Task 1 proceeds.

---

## Task 1: Define The Canonical Act I Room Graph

**Files:**
- Create: `data/room_specs/lantern_heights_vertical_slice.json`
- Create: `data/room_specs/mistwood_vertical_slice.json`
- Input: `data/area_catalog.json`
- Input: `data/narrative_beats.json`
- Input: `data/quests.json`

- [ ] Define the exact room order for the slice:
  - `lh_balcony_opening`
  - `lh_hub_social`
  - `lh_handoff_path`
  - `mistwood_entry`
  - `mistwood_first_encounter`
  - `mistwood_objective_return`
  - `lh_hub_return_changed`
- [ ] For each room, record:
  - `room_id`
  - `area_id`
  - `display_name`
  - `plateau_id`
  - `scene_role`
  - `spawn_points`
  - `npc_anchors`
  - `encounter_ids`
  - `transition_ids`
  - `required_flags`
  - `set_flags`
- [ ] Define one room-spec schema shape and use it in both JSON files.
- [ ] Keep the schema deliberately small; do not add decoration-only fields yet.
- [ ] Verify all `area_id` values used in room specs exist in `data/area_catalog.json`; add any missing catalog entries before the spec files are committed.

**Edge cases to cover in schema:**
- room with no enemies
- room with no NPCs
- room with multiple exits
- room whose return-state variant uses same `area_id` but different staging
- room unlocked only after a mission state change

**Acceptance:**
- [ ] The full first-slice route is captured in room data, not just implied by beats.
- [ ] Lantern Heights and Mistwood both use the same schema.
- [ ] All room `area_id` references resolve to known entries in `data/area_catalog.json`.

## Task 2: Add Room Spec Loading And Validation

**Files:**
- Create: `java/client/src/main/java/com/shadowascent/client/world/RoomSpec.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/RoomTransitionSpec.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/EncounterSpec.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/RoomSpecCatalog.java`
- Create: `java/client/src/test/java/com/shadowascent/client/world/RoomSpecCatalogTest.java`

- [ ] Write failing tests for:
  - loading Lantern Heights room spec data
  - loading Mistwood room spec data
  - rejecting duplicate room ids
  - rejecting transitions to unknown rooms
  - rejecting encounters referenced by rooms but missing definitions
- [ ] Implement minimal immutable records/classes for room specs.
- [ ] Implement `RoomSpecCatalog.loadDefault()` using repo-local `data/room_specs/*.json`.
- [ ] Validate the schema at load time with actionable messages.

**Edge cases:**
- malformed numeric coordinates
- missing spawn point for required entry room
- transition target typo
- scene variant with same room id accidentally duplicated

**Acceptance:**
- [ ] Room-spec data loads through tested code.
- [ ] Bad authoring data fails early and clearly.

## Task 3: Extend `RunGameContentProfile` To Carry Room Runtime Identity

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/world/RunGameContentProfile.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/RoomRuntimeProfile.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Test: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`

**AreaGate migration policy:** For Act I room-spec-driven rooms, `AreaGate` entries in `RunGameContentProfile` are superseded by room-scoped `RoomTransitionSpec` records introduced in this task. Non-Act-I plateaus retain `AreaGate`-based transitions until explicitly migrated in a future wave. Do not delete `AreaGate` from the record yet — the two must coexist.

- [ ] Create `RoomRuntimeProfile` as an immutable runtime view assembled from a `RoomSpec`: carries resolved tiles, NPC placements, enemy placements, and `RoomTransitionSpec` records for one resolved room state.
- [ ] Add room-level runtime identity to `RunGameContentProfile`:
  - `roomId`
  - `roomDisplayName`
  - `sceneRole`
  - room-scoped `RoomTransitionSpec` transitions (alongside existing `AreaGate` for non-Act-I plateaus)
  - room-scoped encounter specs
- [ ] Preserve existing area/plateau fields so current systems keep working.
- [ ] Ensure bootstrap can produce room runtime profiles from room specs instead of directly from hardcoded switches.

**Edge cases:**
- room variant shares same area but different scene role
- no merchant present in a room that still belongs to Lantern Heights
- return-state room with same physical shell but changed NPCs

**Acceptance:**
- [ ] Runtime can distinguish “same area, different scene state.”
- [ ] Profile contains enough information to render and transition rooms without re-resolving design data every frame.
- [ ] `AreaGate`-based transitions still resolve correctly for non-Act-I plateaus alongside the new `RoomTransitionSpec` path.

## Task 4: Replace Lantern Heights Hardcoded Bootstrap With Room-Spec Bootstrap

**Dependency note:** Task 4 proves that room-spec routing resolves correctly (the right room is selected from flags). Full geometry assembly from spec data is proved in Task 5. Task 4 acceptance does not require pixel-perfect geometry — only correct room selection and safe fallthrough for non-Act-I plateaus. Do not run Tasks 4 and 5 in parallel; both modify `AuthoringWorldBootstrap.java` and the room-spec JSON files.

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Test: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`

- [ ] Write failing tests for:
  - default Act I boot lands in `lh_balcony_opening`
  - post-social route can resolve to `lh_handoff_path`
  - outbound progression can resolve to Mistwood entry room
  - return-state progression can resolve to changed Lantern Heights room
- [ ] Replace Lantern Heights switch-based room routing with room-spec-driven resolution (geometry assembly is Task 5).
- [ ] Keep Hollow path untouched unless necessary for shared abstraction.
- [ ] Preserve fallback bootstrap behavior for non-migrated plateaus.

**Edge cases:**
- no matching room for current flags
- conflicting room candidates satisfy same flags
- save file from old bootstrap version lands in a now-unknown room

**Acceptance:**

- [ ] Act I boot resolves the correct room from flags via room-spec data.
- [ ] Existing non-Act-I plateaus still boot safely via the existing fallback path.
- [ ] _(Geometry assembly from spec primitives is proved in Task 5, not here.)_

## Task 5: Add Authorable Room Geometry Primitives

**Sequencing note:** Task 5 completes the geometry half of the bootstrap replacement started in Task 4. Both tasks share `AuthoringWorldBootstrap.java` and the room-spec JSON files — begin Task 5 only after Task 4 is committed.

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: room-spec JSON files
- Possibly create: `java/client/src/main/java/com/shadowascent/client/world/RoomGeometryAssembler.java`
- Test: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`

- [ ] Add geometry primitives to room specs:
  - floor segments
  - platforms
  - ledges
  - optional hazard bands
  - spawn markers
- [ ] Assemble `TileRect` geometry from room specs instead of broad area-wide fixed layouts.
- [ ] Keep the primitive vocabulary intentionally small until the first vertical slice is playable.

**Edge cases:**
- zero-width platform
- overlapping floor/hazard tiles
- room with vertical traversal but no valid spawn
- geometry edits that place NPCs or enemies inside solids

**Acceptance:**
- [ ] Lantern Heights and Mistwood room layouts can be iterated by data edits instead of Java-only coordinate surgery.

## Task 6: Add Authorable Room Transitions With Explicit Types

**Note:** `RunGameAreaTransitionTest.java` already exists. Add new test cases for typed transition resolution to the existing file — do not create a replacement.

**AreaGate coexistence:** `RunGameAreaTransition` must continue to resolve `AreaGate`-based transitions for non-Act-I plateaus alongside the new `RoomTransitionSpec` path. Resolution order: room-id-based `RoomTransitionSpec` first; fall back to `AreaGate` for plateaus not yet migrated.

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameAreaTransition.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/RunGameContentProfile.java`
- Modify: room-spec JSON files
- Test: `java/client/src/test/java/com/shadowascent/client/RunGameAreaTransitionTest.java` _(existing file — extend, do not replace)_

- [ ] Add transition types:
  - `free_exit`
  - `mission_gate`
  - `encounter_gate`
  - `return_gate`
  - `npc_handoff_gate`
- [ ] Extend transition resolution to move by room id first, area id second.
- [ ] Add room-entry spawn target support so transitions land at deliberate anchors.
- [ ] Ensure blocked reasons are explicit and stable.

**Edge cases:**
- player overlaps two exits at once
- transition target room exists but target spawn is missing
- return gate active before outbound mission completed
- interaction near both exit and NPC

**Acceptance:**
- [ ] Every slice transition is typed and debuggable.
- [ ] QA can tell why a route is blocked.

## Task 7: Add Encounter Definitions And Encounter-Clear Progression

**JSON sequencing:** Tasks 7 and 8 both modify room-spec JSON files. Run Task 7 to completion and commit before beginning Task 8 JSON edits to avoid conflicts.

**Files:**
- Modify: room-spec JSON files
- Modify: `java/client/src/main/java/com/shadowascent/client/world/RunGameContentProfile.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameAreaTransition.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/GameSimulator.java`
- Possibly create: `java/client/src/main/java/com/shadowascent/client/world/EncounterRuntimeState.java`
- Test: `java/client/src/test/java/com/shadowascent/client/RunGameAreaTransitionTest.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java`

- [ ] Define the first slice encounter(s) in room data.
- [ ] Support clear conditions:
  - all enemies defeated
  - named enemy defeated
- [ ] Emit stable encounter state so transitions and HUD can read it.
- [ ] Ensure encounter clear can set progression flags or satisfy mission progress where needed.

**Edge cases:**
- one enemy removed unexpectedly
- encounter already cleared on load
- player leaves room mid-encounter and returns
- encounter tries to clear twice

**Acceptance:**
- [ ] Mistwood first encounter can intentionally gate return progression.
- [ ] Encounter state is readable outside the raw enemy list.

## Task 8: Make NPC Staging Room-Aware And Scene-Aware

**JSON sequencing:** Begin Task 8 JSON edits only after Task 7 is committed. Both tasks write to room-spec JSON files and must not run in parallel.

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`
- Modify: room-spec JSON files
- Test: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`

- [ ] Move Act I NPC placement from broad area filters to room/scene anchors.
- [ ] Support different NPC rosters in:
  - balcony opener
  - full social hub
  - post-outbound return hub
- [ ] Preserve plateau eligibility, but let room state decide presence/position for the vertical slice.

**Edge cases:**
- mission-eligible NPC omitted from current room
- duplicate anchor assignment
- same NPC accidentally appears twice in return-state variant

**Acceptance:**
- [ ] The social hub can be staged deliberately, and the return-state hub can visibly change.

## Task 9: Add Beat-Aware And Room-Aware Dialogue Selection

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`
- Modify: `data/dialogue.json`
- Modify: `data/narrative_beats.json`
- Test: `java/client/src/test/java/com/shadowascent/client/RunGameMissionInteractionTest.java`

- [ ] Keep authored beat dialogue selection, but extend it for:
  - same NPC in different room states
  - pre-outbound vs post-return lines
  - mission-handoff vs incidental lines
- [ ] Ensure fallback path stays safe when authored room line is missing.
- [ ] Add any missing Act I/Mistwood dialogue refs needed by the vertical slice.

**Edge cases:**
- multiple beats match same NPC and room
- beat line exists but speaker mismatch
- player re-talks after mission step completes
- return-state dialogue incorrectly appears before outbound route

**Acceptance:**
- [ ] Act I vertical slice dialogue feels sequenced, not generic.

## Task 10: Add Mission Triggers For The Full Outbound/Return Loop

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/MissionManager.java`
- Modify: `data/quests.json`
- Modify: `data/narrative_beats.json`
- Test: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java`

- [ ] Add room-aware and encounter-aware mission triggers for:
  - social introductions
  - first mission acceptance
  - Mistwood entry
  - first encounter completion
  - return-to-hub resolution
  - emotional-change hub state
- [ ] Allow objective progression from room arrival and encounter clear, not only NPC talk.

**Edge cases:**
- player reaches Mistwood before talking to intended mission giver
- player returns to hub early
- mission updates twice on repeated room entry
- save/load between objective substeps

**Acceptance:**
- [ ] The full slice route can be authored without hidden code-only progression hacks.

## Task 11: Surface Room, Beat, Gate, And Encounter State In The HUD

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/UiText.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`

- [ ] Add room display name to HUD state.
- [ ] Add current beat title where useful.
- [ ] Add blocked-gate reason and encounter state to contextual hint flow.
- [ ] Keep it concise enough to remain playtest-readable.

**Edge cases:**
- simultaneous NPC prompt and blocked-gate prompt
- stale room label after transition
- encounter state showing after room already cleared

**Acceptance:**
- [ ] QA can tell what the game currently expects from them.

## Task 12: Add Minimap Gate And Route Readability For The Slice

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/MinimapOverlayRendererTest.java`

- [ ] Keep gate markers, but distinguish:
  - normal route exits
  - blocked exits
  - return path exits
- [ ] If feasible in this tranche, surface current room marker emphasis.

**Edge cases:**
- tiny room width compresses gate markers together
- multiple exits near same edge

**Acceptance:**
- [ ] The minimap helps navigation rather than only showing rough position.

## Task 13: Make Combat Feedback Good Enough To Tune The First Encounter

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/rendering/SpriteWorldRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/GameSimulator.java`
- Test: `java/client/src/test/java/com/shadowascent/client/rendering/SpriteWorldRendererSelectionTest.java` _(existing file — extend)_
- Create: `java/core/src/test/java/com/shadowascent/core/simulation/GameSimulatorMeleeCombatTest.java`

- [ ] Keep the 2-hit combo baseline as the authoring target.
- [ ] Create `GameSimulatorMeleeCombatTest` with baseline cases: hit landed, combo follow-up registered, enemy enters hurt state, enemy defeated on lethal hit, player damaged by enemy attack.
- [ ] Improve event feed and tint/state cues enough to judge:
  - hit landed
  - combo follow-up landed
  - enemy hurt
  - enemy defeated
  - player damaged
- [ ] Do not expand into full branching combo design in this tranche.

**Edge cases:**
- combo input near NPC/gate
- two enemies overlapped in first encounter
- combo feed text stale after enemy dies

**Acceptance:**
- [ ] Early combat can be tuned around honest feedback.

## Task 14: Harden Save/Load Across The Full Slice

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/SaveLoad.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Test: `java/client/src/test/java/com/shadowascent/client/SaveLoadRuntimeStateTest.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java`

- [ ] Persist or deterministically rebuild:
  - current room id
  - encounter clear state
  - mission substep state
  - post-return hub variant state
- [ ] Re-verify transient combat state reset rules are still correct.

**Edge cases:**
- save inside Mistwood encounter
- load after return-state unlocked
- load into removed/changed room geometry

**Acceptance:**
- [ ] Iterating on content doesn’t make saves untrustworthy.

## Task 15: Add Smoke Coverage For The Whole Vertical Slice State Machine

**Files:**

- Create: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java`

**Note:** `RunGameFlowSmokeTest.java` already exists and covers general boot/flow. `ActIRouteStateSmokeTest` is additive and focused on Act I route state transitions specifically — do not replace or merge the existing test. If coverage overlaps, consolidate in a follow-up, not during this task.

- [ ] Cover the main route states:
  - initial room
  - social route progression
  - mission start
  - Mistwood room entry
  - first encounter clear
  - return-state room selection
- [ ] Keep the test state-level and deterministic; do not try to simulate full rendering.

**Edge cases:**

- wrong room selected after flag progression
- return-state never selected
- encounter gate never unlocks
- mission trigger sequence breaks if rooms are entered out of intended order

**Acceptance:**

- [ ] The route skeleton is protected by tests before deep content tuning starts.

## Task 16: Write The Canonical Act I QA Route Against `runGame`

**Note:** Do not claim authoring readiness in these docs until Task 15 smoke tests pass. Use passing `ActIRouteStateSmokeTest` results as the evidence gate before updating `PLAYABLE_TRUTH.md`.

**Files:**

- Create if absent / Modify: `docs/ACT_I_QA_ROUTE.md`
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Test evidence: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java` _(created in Task 15 — must pass before docs are updated)_

- [ ] Replace generic route language with the actual first-slice route:
  - opener
  - social hub
  - mission handoff
  - Mistwood route
  - first encounter
  - return
  - changed hub
- [ ] Define pass/fail criteria for each segment.
- [ ] Update playable-truth docs to reflect the new authoring-ready state only after Task 15 tests are green.

**Edge cases:**

- docs claiming authoring readiness before smoke tests pass
- route steps out of sync with current room ids

**Acceptance:**

- [ ] QA and content work are aligned on one route.
- [ ] Doc updates are backed by passing `ActIRouteStateSmokeTest` evidence.

---

## Commit Boundaries

Recommended commit sequence:

1. `feat: add act i room spec schema`
2. `feat: bootstrap lantern heights from room specs`
3. `feat: add authored act i transitions and encounters`
4. `feat: stage act i npcs and dialogue by room`
5. `feat: wire mistwood mission return loop`
6. `feat: harden act i save load and qa surfacing`
7. `test: add act i vertical slice route smoke coverage`
8. `docs: sync act i vertical slice truth`

## Verification Gates

Run these at minimum after each relevant tranche:

- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.RoomSpecCatalogTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.ActIVerticalSliceBootstrapTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.RunGameAreaTransitionTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.RunGameMissionInteractionTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.ActIRouteStateSmokeTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.SaveLoadRuntimeStateTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.ui.HudOverlayStateTest" --tests "com.shadowascent.client.ui.MinimapOverlayRendererTest" --tests "com.shadowascent.client.rendering.SpriteWorldRendererSelectionTest"`
- `./gradlew.bat --console=plain :core:test --tests "com.shadowascent.core.simulation.GameSimulatorMeleeCombatTest"`
- `./gradlew.bat --console=plain :client:compileJava :core:compileJava`
- `python scripts/check_docs_freshness.py --emit-report`

**Cross-tranche compile gate:** After any tranche that modifies both `core` and `client`, run `:client:compileJava :core:compileJava` before moving to the next task — do not wait for the per-test gate to surface a compile failure.

## Review Checklist

Before approving implementation, confirm this plan does all of the following:

- [ ] proves the full Act I loop through return-state change
- [ ] uses a reusable room-spec schema immediately
- [ ] keeps the 2-hit combo baseline instead of expanding scope into larger combat design
- [ ] includes Mistwood inside the first slice
- [ ] distinguishes runtime-readiness work from content-authoring work
- [ ] includes transition, encounter, dialogue, save/load, and QA edge cases
- [ ] gives enough file-level detail that execution can proceed without rediscovery
- [ ] `AreaGate`-to-`RoomTransitionSpec` migration policy is explicit: Act I rooms use `RoomTransitionSpec`; non-Act-I plateaus retain `AreaGate` until migrated; no silent dual-path coexistence without a documented fallback order

## Recommended Execution Mode

Use **Subagent-Driven** execution for this plan.

Reason:
- room schema, transitions/encounters, and dialogue/mission work can be decomposed cleanly
- the write scopes can be kept mostly separate
- review checkpoints matter because this plan changes both data contracts and runtime behavior
