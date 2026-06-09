---
doc_type: gate
status: complete
owner: core-team
last_updated: 2026-05-14
milestone: M4a — Act I Vertical Slice Playable Readiness
---
# Milestone A Gate — Act I Vertical Slice

## Purpose

M4a is the first playable-content readiness gate: a human player can run the full Act I campaign spine end-to-end in `runGame` without softlocks, data gaps, or missing feedback, from the opening balcony through the `beat_npc_withdrawal` end state.

## Scope

Act I vertical slice only: Lantern Heights plateau, opening through `npc_withdrawal_started`. Does not cover post-withdrawal content or Act II entry.

## End Condition

- Player reaches `lh_hub_return_changed` (area `area_lantern_heights_hub_dimming`)
- `npc_withdrawal_started` flag is set
- Four warning NPC interactions set `heard_warning_samson/sophia/marcel/hazel`
- `warnings_heard` flag fires

## Exit Criteria

### Data Pre-Flight

- [x] All Act I area IDs registered in `data/area_catalog.json`
- [x] LANTERN_KID and OLD_MAN_RIKU registered in `data/npc_registry.json` (no duplicate entries)
- [x] Room graph in `data/room_specs/lantern_heights_vertical_slice.json` includes dojo and forge rooms

### Room-Spec Routing

- [x] `AuthoringWorldBootstrap` drives NPC placement and transitions from room-spec data (no hardcoded Lantern Heights coordinates)
- [x] LANTERN_HEIGHTS areas that lack a room-spec entry produce a `[AuthoringWorldBootstrap] WARNING` and fall through gracefully
- [x] `lh_balcony_opening → lh_hub_social → lh_handoff_path → mistwood_entry → mistwood_first_encounter → lh_hub_return_changed` route resolves without missing profiles

### Optional Quest Runtime

- [x] ACT_0 optional quests (`sq_samson_q1_unfinished_sparring_match`, `sq_sophia_q1_lantern_cartography`, `sq_marcel_q1_guard_the_forge`, `sq_hazel_q1_gentle_glow`) become available in LANTERN_HEIGHTS ACT_I
- [x] Interacting with SAMSON/SOPHIA/MARCEL/HAZEL with no active mission starts the corresponding side quest
- [x] Encounter clears advance active side-quest objectives via `advanceCombatQuestForEncounter`
- [x] `synchronizeMissionCompletion` covers all four optional quests via completion flags

### NPC Discoverability

- [x] OLD_MAN_RIKU and LANTERN_KID with role `optional_npc` appear in anchored rooms without requiring explicit NPC activation
- [x] `dialogue.json` includes a LANTERN_KID line (`bark_lk_hope_01`) with correct plateau/beat tags

### Encounter Gating

- [x] `encounter_gate`, `return_gate`, `mission_gate`, `npc_handoff_gate` transition types all produce correct player-visible blocked messages
- [x] Clearing an encounter applies `encounter.setFlags()` and calls `updateAvailableMissions()`

### Dialogue Routing

- [x] `authoredDialogueLines` matches beats with empty `npc_ids` as "any NPC in area" (not zero-match)
- [x] Beat types `authored_critical` and `authored_milestone` are included in `isRuntimeBeat` for both `RunGameMissionInteraction` and `HubScreen`

### HUD Clarity

- [x] Active optional quests display `[Side Quest]` prefix in mission title
- [x] Side-quest route hints appear in `missionRoutePrompt` for all four ACT_0 quests
- [x] Beat types `authored_critical`/`authored_milestone` are surfaced in HUD when no mission is active

### Combat Feedback

- [x] 2-hit combo emits `PLAYER_MELEE_HIT` events with `comboStep` 1 and 2 (verified in `GameSimulatorMeleeCombatTest`)
- [x] Enemy kill emits `ENEMY_DEFEATED` and `aliveEnemyCount()` drops to 0 (verified in test)

### Save/Load Hardening

- [x] Story flags (encounter-clear flags, `talked_to_*` NPC flags, `npc_withdrawal_started`) persist through save/load via SAVE_V3 story state envelope
- [x] `village_bonds` partial objective progress restores from `talked_to_*` story flags after `updateAvailableMissions()`

### Smoke Coverage

- [x] `ActIRouteStateSmokeTest.actIRouteResolvesFromOpeningThroughReturnHubState` — full route end-to-end
- [x] `ActIRouteStateSmokeTest.npcWithdrawalWarningsAdvanceThroughBeatToWarningsHeard` — warning NPC flow
- [x] `ActIOptionalQuestFlowTest.villageBondsObjectiveProgressRestoresFromStoryFlagsAfterSync` — save/load fix
- [x] `ActIOptionalQuestFlowTest.optionalSideQuestAvailableAfterVillageBondsCompleted` — sq_ availability
- [x] `ActIOptionalQuestFlowTest.samsonQuestStartsFromNpcInteractionAfterVillageBondsCompleted` — quest start flow

### Regression Gate

- [x] `./gradlew runRegressionTests` — only pre-existing `Campaign Continuity` failure (pre-dates M4a)
- [x] `./gradlew runDataContractDiagnostics` — clean, no validation issues
- [x] `./gradlew :client:compileJava :core:compileJava` — clean compile

## Known Pre-Existing Failures

- `Campaign Continuity` regression test — fails before any M4a change (confirmed by `git stash` + clean run on 2026-05-14). Root cause: upstream data dependency, not M4a scope. Tracked separately.

## Out of Scope for M4a

- Post-withdrawal content (beyond `warnings_heard`)
- Act II entry and Hollow Depths routing
- Final animation/visual polish
- Authored audio binding to assets
- Multiplayer or co-op paths
