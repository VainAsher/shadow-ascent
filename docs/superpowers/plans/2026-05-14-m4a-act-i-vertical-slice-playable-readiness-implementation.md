# M4a Act I Vertical Slice Playable Readiness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete Milestone A so `runGame` is a truthful, playable, testable, and authorable Act I vertical slice host through `beat_npc_withdrawal` / `npc_withdrawal_started`, including the intended ACT_0 optional side-content layer.

**Architecture:** Keep `java/core` authoritative for mission progression, story flags, encounter truth, dialogue selection, and save/load state. Keep `java/client` responsible for room-spec-driven authored bootstrap, room transitions, NPC staging, UI surfacing, and LibGDX presentation. Treat the room-spec schema as a hard gate: room selection, scene-state variants, and optional content routing must be data-driven rather than fallback switch logic.

**Tech Stack:** Java 21, LibGDX 1.12.1, LWJGL3 desktop backend, JUnit 5, JSON contracts under `data/`, current `GameSimulator` / `GameState` / `MissionManager` / `HubManager`, and docs/GDD inputs under `docs/` and `game_design_document/`.

---

## Scope

This plan implements **M4a** from [2026-05-14-act-i-milestone-stack-design.md](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/docs/superpowers/specs/2026-05-14-act-i-milestone-stack-design.md:1).

This plan supersedes [2026-05-14-act-i-vertical-slice-readiness-implementation.md](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/docs/superpowers/plans/2026-05-14-act-i-vertical-slice-readiness-implementation.md:1) for Milestone A execution because the reviewed spec now requires:

- the exact M4a end condition: `beat_npc_withdrawal` / `npc_withdrawal_started`
- explicit ACT_0 side-quest support in-slice
- explicit optional NPC discoverability (`old_man_riku`, `lantern_kid`)
- a formal `MILESTONE_A_GATE.md` sign-off document
- a hard acceptance requirement that room selection be room-spec-driven

Milestone B is intentionally **not** planned here. The spec says M4b planning should start only after M4a closes.

## Locked Inputs

Use these sources in this order:

1. Runtime truth:
   - `java/core/src/main/java/...`
   - `java/client/src/main/java/...`
   - `data/quests.json`
   - `data/dialogue.json`
   - `data/narrative_beats.json`
   - `data/npc_registry.json`
   - `data/story_flags.json`
   - `data/area_catalog.json`
2. Approved milestone spec:
   - `docs/superpowers/specs/2026-05-14-act-i-milestone-stack-design.md`
3. Current repo truth:
   - `docs/CURRENT_STATE.md`
   - `docs/PLAYABLE_TRUTH.md`
   - `docs/ACT_I_QA_ROUTE.md`
4. Supporting content design:
   - `game_design_document/09_level_and_content_plan/...`
   - `game_design_document/15_character_dossiers/...`

## Explicit In-Scope Content

### Mainline route

- `lh_balcony_opening`
- `lh_hub_social`
- `lh_handoff_path`
- `mistwood_entry`
- `mistwood_first_encounter`
- return path to Lantern Heights
- `area_lantern_heights_hub_dimming`

### Optional ACT_0 side content

From `data/quests.json`, all of the following are in scope for M4a:

- `samson_q1_unfinished_sparring_match`
- `sophia_q1_lantern_cartography`
- `marcel_q1_guard_the_forge`
- `hazel_q1_gentle_glow`

### Optional discoverable NPC interactions

- `old_man_riku`
- `lantern_kid`

## Success Criteria

This plan is complete only when:

- `runGame` reaches `npc_withdrawal_started` through real route play
- all four ACT_0 side-quest steps can be started, progressed, saved, loaded, and completed without corrupting mainline truth
- `old_man_riku` and `lantern_kid` are discoverable in the slice where intended
- room selection is driven by validated room specs, not hardcoded Act I routing
- all Milestone A tests and gate docs required by the spec exist and pass

## File Structure

### Runtime and data files expected to change

- Modify: `data/room_specs/lantern_heights_vertical_slice.json`
- Modify: `data/room_specs/mistwood_vertical_slice.json`
- Modify: `data/area_catalog.json`
- Modify: `data/dialogue.json`
- Modify: `data/narrative_beats.json`
- Modify: `data/npc_registry.json`
- Modify: `data/quests.json`
- Modify: `data/story_flags.json`

- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameAreaTransition.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/SaveLoad.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/rendering/SpriteWorldRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/UiText.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/RoomSpecCatalog.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/RunGameContentProfile.java`

- Modify: `java/core/src/main/java/com/shadowascent/core/GameState.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/HubManager.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/MissionManager.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/GameSimulator.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/SimPlayer.java`

### Tests expected to change or be added

- Modify: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameAreaTransitionTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameMissionInteractionTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/SaveLoadRuntimeStateTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/rendering/SpriteWorldRendererSelectionTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/MinimapOverlayRendererTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/RoomSpecCatalogTest.java`
- Modify: `java/core/src/test/java/com/shadowascent/core/simulation/GameSimulatorMeleeCombatTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/ActIOptionalQuestFlowTest.java`

### Docs expected to change

- Modify: `docs/ACT_I_QA_ROUTE.md`
- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Create: `docs/MILESTONE_A_GATE.md`

---

### Task 1: Reconcile Milestone A Inputs And ACT_0 Scope

**Files:**
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `data/area_catalog.json`
- Input: `data/quests.json`
- Input: `docs/superpowers/specs/2026-05-14-act-i-milestone-stack-design.md`

- [ ] Confirm `docs/PLAYABLE_TRUTH.md` already names `runGame` as the primary runtime and does not imply `runPlayableClient` is a substitute for M4a acceptance.
- [ ] Verify `data/area_catalog.json` contains all M4a areas:
  - `area_lantern_heights_balcony`
  - `area_lantern_heights_hub`
  - `area_lantern_heights_hub_dimming`
  - `area_mistwood_path`
  - `area_training_dojo`
  - `area_lantern_forge`
- [ ] Identify any missing area catalog entries needed by:
  - `samson_q1_unfinished_sparring_match`
  - `sophia_q1_lantern_cartography`
  - `marcel_q1_guard_the_forge`
  - `hazel_q1_gentle_glow`
  - `old_man_riku`
  - `lantern_kid`
- [ ] Add missing area catalog entries before room-spec or quest work proceeds.
- [ ] Commit pre-flight truth fixes separately.

**Acceptance:**
- [ ] All M4a areas exist as catalog truth before route authoring expands.
- [ ] Runtime truth docs do not conflict with Milestone A acceptance.

### Task 2: Expand The Canonical Room Graph To Cover The Full M4a Slice

**Files:**
- Modify: `data/room_specs/lantern_heights_vertical_slice.json`
- Modify: `data/room_specs/mistwood_vertical_slice.json`
- Test: `java/client/src/test/java/com/shadowascent/client/world/RoomSpecCatalogTest.java`

- [ ] Extend the Lantern Heights room graph to explicitly cover optional-content-supporting spaces where needed:
  - social hub
  - dojo/training support room or room-state
  - forge support room or room-state
  - return-hub dimming state
- [ ] Decide whether each ACT_0 quest uses:
  - an existing room with room-state staging, or
  - a distinct room entry in the schema
- [ ] Ensure all transitions are bidirectional where design intent requires return traversal.
- [ ] Ensure all room IDs remain stable and unique.
- [ ] Add or update tests to reject:
  - duplicate room IDs
  - unknown transition targets
  - required spawn IDs missing from target rooms

**Edge cases:**
- room used by both mainline and optional side content
- same area with multiple room-state variants
- optional room reachable before mainline eligibility

**Acceptance:**
- [ ] The room graph expresses all spaces needed by the first-loop route plus ACT_0 optional content.

### Task 3: Make Room-Spec Routing A Hard Runtime Requirement

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/RoomSpecCatalog.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`

- [ ] Remove any remaining Act I room-selection dependency on hand-authored switch logic where room-spec data should decide the answer.
- [ ] Ensure bootstrap resolves:
  - opening
  - social hub
  - handoff path
  - Mistwood
  - dimming hub
  from room data and flags alone.
- [ ] Ensure optional-content-supporting rooms/variants resolve from route and quest flags without side effects leaking into unrelated scenes.
- [ ] Add bootstrap tests for:
  - first launch
  - ACT_0 side-quest-eligible social state
  - Mistwood outbound
  - dimming-hub return

**Acceptance:**
- [ ] Room-spec-driven routing is the authoritative path for M4a.

### Task 4: Extend Optional Quest Runtime Truth For ACT_0

**Files:**
- Modify: `data/quests.json`
- Modify: `java/core/src/main/java/com/shadowascent/core/MissionManager.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- Create: `java/client/src/test/java/com/shadowascent/client/ActIOptionalQuestFlowTest.java`

- [ ] Normalize ACT_0 side-quest triggers so the four named quests can:
  - start intentionally
  - progress on their intended actions
  - complete without stepping on the mainline route
- [ ] Ensure optional quest routing remains optional in all HUD and mission-selection surfaces.
- [ ] Add focused tests for each quest:
  - Samson training combat challenge starts and completes
  - Sophia map-shard route starts and records bounded progress
  - Marcel forge-defense step starts and resolves
  - Hazel gathering errand starts and resolves
- [ ] Ensure mission reconciliation from story flags does not re-open completed ACT_0 optional steps incorrectly.

**Acceptance:**
- [ ] Each named ACT_0 quest step is a working, state-safe optional path in `runGame`.

### Task 5: Stage Optional NPC Discoverability In The Slice

**Files:**
- Modify: `data/npc_registry.json`
- Modify: `data/dialogue.json`
- Modify: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameMissionInteractionTest.java`

- [ ] Stage `old_man_riku` and `lantern_kid` in their intended Lantern Heights slice contexts.
- [ ] Ensure they are discoverable but not route-blocking.
- [ ] Add authored dialogue/ref selection for their interactions.
- [ ] Add tests that:
  - they can be surfaced by proximity interaction when staged
  - they do not masquerade as mainline-critical NPCs

**Acceptance:**
- [ ] Optional NPC interactions required by the Milestone A spec are discoverable and harmless to mainline truth.

### Task 6: Strengthen Typed Transitions And Encounter Gating For Mixed Mainline/Optional Flow

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameAreaTransition.java`
- Modify: `data/room_specs/lantern_heights_vertical_slice.json`
- Modify: `data/room_specs/mistwood_vertical_slice.json`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameAreaTransitionTest.java`

- [ ] Keep the five existing transition classes stable:
  - `free_exit`
  - `mission_gate`
  - `encounter_gate`
  - `return_gate`
  - `npc_handoff_gate`
- [ ] Ensure optional side-content rooms or variants can be reached without weakening mainline gate semantics.
- [ ] Ensure blocked reasons explicitly report:
  - missing flag
  - missing mission handoff
  - uncleared encounter
  - missing target spawn
- [ ] Add tests for:
  - mainline gate precedence
  - optional room access when eligible
  - return gate blocked until Mistwood clear

**Acceptance:**
- [ ] Mainline and optional traversal paths are both explicit and debuggable.

### Task 7: Deepen Scene-Aware Dialogue And Story Beat Routing

**Files:**
- Modify: `data/dialogue.json`
- Modify: `data/narrative_beats.json`
- Modify: `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameMissionInteractionTest.java`

- [ ] Add or normalize room-aware dialogue for:
  - pre-social mainline interactions
  - ACT_0 optional quest handoffs
  - Mistwood handoff
  - dimming-hub return
- [ ] Ensure beat selection resolves correctly when:
  - NPC appears in multiple room states
  - optional quest state and mainline state coexist
  - return-state dialogue replaces pre-return dialogue
- [ ] Keep fallback dialogue safe when authoring is incomplete.

**Acceptance:**
- [ ] Dialogue sequencing feels authored rather than generic across both mainline and optional slice content.

### Task 8: Make HUD And Minimap Truth Clearer For Mainline Vs Optional Work

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/UiText.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/MinimapOverlayRendererTest.java`

- [ ] Make mainline mission surfacing clearly preferred when both mainline and optional quests are available.
- [ ] Make optional quest surfacing discoverable without stealing route-critical priority.
- [ ] Surface room, beat, blocked-gate, and encounter state concisely.
- [ ] Ensure mission-critical NPC emphasis differs from optional NPC emphasis.
- [ ] Ensure minimap and event feed reflect:
  - dimming-hub state
  - Mistwood encounter blocked/cleared state
  - optional route availability without ambiguity

**Acceptance:**
- [ ] A tester can tell what is required, what is optional, and what is blocked.

### Task 9: Harden Combat And Encounter Feedback For Samson And Mistwood

**Files:**
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/GameSimulator.java`
- Modify: `java/core/src/test/java/com/shadowascent/core/simulation/GameSimulatorMeleeCombatTest.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/rendering/SpriteWorldRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/rendering/SpriteWorldRendererSelectionTest.java`

- [ ] Keep the 2-hit combo baseline fixed.
- [ ] Ensure combat feedback is strong enough for:
  - Mistwood first encounter tuning
  - Samson optional sparring challenge readability
- [ ] Add or extend tests for:
  - hit landed
  - follow-up combo landed
  - enemy or training target damage state visible
  - player damage visible
  - combat completion event routed to quest/encounter logic

**Acceptance:**
- [ ] Both critical-path and ACT_0 training combat are readable enough to judge honestly.

### Task 10: Harden Save/Load For Mixed Mainline And Optional Progress

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/SaveLoad.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/SaveLoadRuntimeStateTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ActIOptionalQuestFlowTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java`

- [ ] Persist or deterministically restore:
  - current room
  - current spawn target
  - encounter clear state
  - side-quest progress
  - dimming-hub return-state truth
- [ ] Add save/load coverage for:
  - ACT_0 optional quest mid-progress
  - Mistwood encounter pre-clear
  - post-return dimming state

**Acceptance:**
- [ ] Save/load is trustworthy across all M4a content, not just the critical route.

### Task 11: Expand Smoke Coverage To Match The Approved Milestone Spec

**Files:**
- Modify: `java/client/src/test/java/com/shadowascent/client/ActIRouteStateSmokeTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/ActIVerticalSliceBootstrapTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/world/RoomSpecCatalogTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/RunGameAreaTransitionTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/SaveLoadRuntimeStateTest.java`

- [ ] Ensure the exact Milestone A tests named by the spec exist and match the approved acceptance language.
- [ ] Extend route smoke coverage through the true end condition:
  - Mistwood clear
  - return
  - `beat_npc_withdrawal`
  - `npc_withdrawal_started`
  - `area_lantern_heights_hub_dimming`
- [ ] Ensure side-content tests remain separate from mainline route smoke so failures are attributable.

**Acceptance:**
- [ ] The codebase contains the exact test-backed acceptance surface the spec requires.

### Task 12: Write The Formal Milestone A Gate Docs

**Files:**
- Create: `docs/MILESTONE_A_GATE.md`
- Modify: `docs/ACT_I_QA_ROUTE.md`
- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`

- [ ] Write `docs/MILESTONE_A_GATE.md` using the same style as `docs/M3_RELEASE_GATE.md`.
- [ ] Record explicit sign-off items:
  - mainline route completion through `npc_withdrawal_started`
  - all four ACT_0 optional quest steps started and resolved
  - `old_man_riku` discoverability
  - `lantern_kid` discoverability
  - Mistwood first encounter completion
  - dimming-hub readability in `runGame`
- [ ] Update `docs/ACT_I_QA_ROUTE.md` so the canonical route matches actual room IDs and state transitions.
- [ ] Update project truth docs only after the required tests are green.

**Acceptance:**
- [ ] Milestone A has a formal sign-off artifact instead of only implied completion.

---

## Recommended Commit Boundaries

1. `feat: align act i room graph to m4a scope`
2. `feat: wire act i optional quest runtime`
3. `feat: stage optional npc interactions for m4a`
4. `feat: harden act i transitions and dialogue truth`
5. `feat: improve act i hud minimap and combat readability`
6. `feat: harden act i save load and smoke coverage`
7. `docs: add milestone a gate and sync act i truth`

## Verification Gates

Run these after each relevant tranche:

- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.RoomSpecCatalogTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.ActIVerticalSliceBootstrapTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.RunGameAreaTransitionTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.RunGameMissionInteractionTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.ActIRouteStateSmokeTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.ActIOptionalQuestFlowTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.SaveLoadRuntimeStateTest"`
- `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.ui.HudOverlayStateTest" --tests "com.shadowascent.client.ui.MinimapOverlayRendererTest" --tests "com.shadowascent.client.rendering.SpriteWorldRendererSelectionTest"`
- `./gradlew.bat --console=plain :core:test --tests "com.shadowascent.core.simulation.GameSimulatorMeleeCombatTest"`
- `./gradlew.bat --console=plain :client:compileJava :core:compileJava`
- `./gradlew.bat --console=plain runRegressionTests`
- `python scripts/check_docs_freshness.py --emit-report`

## Self-Review

- [ ] The plan maps directly to M4a, not the broader M4 or M4b scope.
- [ ] The ACT_0 optional side-content layer is explicitly represented, not implied.
- [ ] The plan ends at `npc_withdrawal_started` / dimming hub, matching the reviewed spec.
- [ ] Room-spec schema truth remains a hard runtime criterion.
- [ ] Formal gate docs are part of the plan, not left as an afterthought.
