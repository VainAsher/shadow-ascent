---
doc_type: spec
status: draft
owner: core-team
last_updated: 2026-05-15
milestone: M4 Full
---
# M4 Full: Complete Campaign Spine Runtime Playability

## Purpose

Define the closure bar for `M4 Full` so the clean-start repository stops being "contract-complete but runtime-incomplete" for the campaign spine.

The milestone closes when `runGame` becomes the truthful runtime host for all currently contract-authored campaign plateau content:

- the critical path is playable across all staged plateaus,
- each plateau's already-authored optional side content is playable,
- authored beats, NPC staging, dialogue, encounters, and gate logic are surfaced in runtime,
- geometry is strong enough to support meaningful traversal rather than flat room-to-room routing,
- the player can reach the `BEACON_CLIFF` climax and return into a bounded post-climax free-roam state inside existing staged rooms,
- that post-climax state survives save/load.

This is a runtime playability milestone with placeholder presentation allowed. It is not a final-art milestone.

## Context And Corrections

The current repo state matters:

- `M4a` is complete: Act I vertical slice playable readiness for `LANTERN_HEIGHTS` and the Mistwood zone is already landed.
- `M4b` is active and substantially implemented: authoring diagnostics, zero-Java room addition proof, zero-Java side-beat proof, contract-driven route hints, and authoring-pattern docs already exist.
- The largest remaining M4 gap is not data design. It is runtime staging breadth and fidelity across the unstaged plateau families.

**Important — Mistwood is not a separate staging task.** All Mistwood room specs (`mistwood_entry`, `mistwood_first_encounter`, `mistwood_afterglow_pass`) carry `plateau_id: LANTERN_HEIGHTS`. They are served by the existing `LANTERN_HEIGHTS` branch in `resolveRoomSpec` and are already complete. Mistwood does not appear in the staging scope below.

**Important — narrative plateau order.** The correct campaign sequence, as established by `area_catalog.json` beat anchors, is:

```text
LANTERN_HEIGHTS (+ Mistwood zone) → SUMMIT_SHRINE → HOLLOW_DEPTHS → EMBER_MONASTERY → WINDING_SKYROAD → MIRROR_SUMMIT → BEACON_CLIFF
```

SUMMIT_SHRINE precedes HOLLOW_DEPTHS. The Siren reveals herself at the shrine, takes Yin and Yang, and Aen falls into the Hollow Depths. Implementation must respect this order: each plateau's entry flag depends on the preceding plateau's completion.

Because of that, `M4 Full` must not treat M4b authoring proof as hypothetical new work. It must treat M4b as a prerequisite truth surface that is finalized and then reused for multi-plateau delivery.

## Central Implementation Constraint

The single code bottleneck blocking multi-plateau room-spec routing is the guard in `AuthoringWorldBootstrap.resolveRoomSpec` at line 180:

```java
if (gameState == null || !"LANTERN_HEIGHTS".equals(plateauId)) {
    return null;
}
```

When this guard fires, `bootstrap()` falls to the legacy path, which passes `List.of()` for both `encounters` (line 83) and `roomTransitions` (line 84). This means every non-`LANTERN_HEIGHTS` plateau currently lacks:

- `RoomTransitionSpec`-driven room-to-room routing (the typed transition system)
- `EncounterSpec` gating (no encounter-clear barriers)

Lifting this guard is the single change that unlocks the entire multi-plateau room-spec path. Everything else in this milestone — room-spec authoring, NPC placement, beat surfacing, encounter gating — depends on that guard being removed or replaced with a general multi-plateau routing decision.

The `AreaPlacementResolver` class also participates in routing. It resolves the `areaId` passed into `resolveRoomSpec` before the room spec lookup runs. For each newly staged plateau, `AreaPlacementResolver` must correctly resolve to area IDs that match the `area_id` fields in the authored room specs. Verify its routing logic for each plateau before authoring room specs that depend on correct area selection.

## Multi-Plateau Routing Design Decision

**Decision: Option A — lift the LANTERN_HEIGHTS guard, extend to flag-based fallback.**

The `resolveRoomSpec` algorithm is already general. The only plateau-specific part is the guard. The implementation change is:

1. Remove `!"LANTERN_HEIGHTS".equals(plateauId)` from the guard.
2. Add a fallback path inside `resolveRoomSpec`: if no room spec matches the current `areaId`, attempt a pure flag-based lookup across all rooms for the plateau (dropping the `areaId` filter). This handles plateaus where `AreaPlacementResolver` resolves area IDs that do not exactly match room spec `area_id` values.
3. If no room spec is found after both lookups, return `null` and fall to the legacy path. This preserves backward compatibility for plateaus not yet authored.

The resulting selection order per plateau is:

```text
1. Pinned room by currentRoomId (if valid and flags satisfied)
2. Room matching plateau_id + area_id + flags (existing area-id-specific match)
3. Room matching plateau_id + flags only (new fallback, area_id-agnostic)
4. null → legacy fallback path
```

This is backward compatible: `LANTERN_HEIGHTS` and Mistwood still use path 1 or 2 as before. Plateaus with no room specs still fall to legacy. No plateau-local helper objects or routing-table JSONs are required at this stage.

**Sprawl rule:** Do not grow the legacy fallback path. When adding a new plateau, if the legacy path needs changes to support it, that is a signal to author room specs instead, not to extend the legacy switch ladder.

## Milestone Definition

`M4 Full` should be treated as:

**Complete Campaign Spine With Authored Traversal-Ready Runtime Staging**

The milestone is complete only when:

1. `runGame` can carry the campaign through all currently contract-authored plateau families.
2. All already-authored optional content for newly staged plateaus is surfaced and playable, not just the mainline route.
3. Each plateau, taken as a whole, supports meaningful up/down/left/right navigation even if some individual rooms remain simple.
4. The player can move from climax into a bounded epilogue/free-roam state in existing staged rooms.
5. Campaign continuity, including save/load across plateau boundaries and post-climax return state, is stable and test-backed.

## In Scope

- M4b closure (defined below) and its reuse for broader room-spec authoring.
- Multi-plateau routing design change (lifting the `resolveRoomSpec` LANTERN_HEIGHTS guard).
- Phase 0 data contract cleanup (area catalog family inconsistencies; defined below).
- Runtime staging for:
  - `SUMMIT_SHRINE`
  - `HOLLOW_DEPTHS` (lift-and-replace from legacy path; not a build from scratch — see migration note)
  - `EMBER_MONASTERY`
  - `WINDING_SKYROAD`
  - `MIRROR_SUMMIT`
  - `BEACON_CLIFF`
- Continued support and preservation of already-landed `LANTERN_HEIGHTS` and Mistwood slices.
- All already-authored beats and optional side quests for the above plateaus, as represented in:
  - [narrative_beats.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/narrative_beats.json)
  - [quests.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/quests.json)
  - [dialogue.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/dialogue.json)
  - [npc_registry.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/npc_registry.json)
  - [plateaus.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/plateaus.json)
- Room-spec authoring, plateau routing, NPC anchor placement, beat-trigger surfacing, mission/UI surfacing, encounter gating, and save/load continuity needed to make that content real in `runGame`.
- Ability unlock surfacing in room-spec-driven rooms (mechanism defined below).
- Small targeted runtime extensions where an authored beat or quest exposes a real gap that cannot be expressed through current room-spec, mission, dialogue, transition, or encounter systems.
- Bounded post-climax free-roam state (specification below).

## Out Of Scope

- New plateau families or new campaign acts beyond the already-authored contract set.
- New narrative design invented solely for M4 closure.
- Final-art geometry polish, final sprite pass, or cinematic presentation work.
- Large subsystem redesigns not forced by an authored runtime requirement.
- Full M6 elastic opportunity-stream integration as a milestone requirement.
- Broad procgen-port scope for its own sake.

## Phase 0 Prerequisites

Phase 0 must close before multi-plateau room-spec authoring begins. No new plateau room specs are written until all Phase 0 items are green.

### M4b Closure Checklist

- [ ] `./gradlew runActIAuthoringDiagnostics` exits clean on all current room specs with zero errors.
- [ ] A named test (e.g. `ActIAuthoringFixtureRoundTripTest`) demonstrates a new room addable via JSON alone with zero Java changes, and that test passes.
- [ ] `docs/MILESTONE_B_GATE.md` exists and all checklist items are green.
- [ ] `docs/MILESTONE_B_AUTHORING_PATTERNS.md` documents all five transition types (`free_exit`, `npc_handoff_gate`, `mission_gate`, `encounter_gate`, `return_gate`) and all three NPC role types (`mission_giver`, `optional_npc`, `quest_giver`).

### Multi-Plateau Routing Change

- [ ] `resolveRoomSpec` LANTERN_HEIGHTS guard removed.
- [ ] Flag-based area-agnostic fallback path added (Step 3 in routing decision above).
- [ ] Existing `LANTERN_HEIGHTS` and Mistwood test suite still passes with no regressions.
- [ ] New test: `ResolveRoomSpecMultiPlateauFallbackTest` — verifies that a room spec with a non-LANTERN_HEIGHTS `plateau_id` is resolved correctly by flag state when no area_id match is found.

### Area Catalog Family Cleanup

The following entries in `data/area_catalog.json` have incorrect family assignments that will cause authoring confusion when writing room specs for later plateaus:

| Area ID | Current family | Correct family |
| --- | --- | --- |
| `area_mirror_summit_gate` | `summit_shrine` | `winding_skyroad` |
| `area_mirror_summit_peak` | `summit_shrine` | `mirror_summit` |
| `area_hollow_reflection_arena` | `hollow_depths` | `mirror_summit` |
| `area_lantern_silk_workshop` | `lantern_village` | `ember_monastery` |

- [ ] Correct all four entries in `area_catalog.json`.
- [ ] Run `./gradlew runDataContractDiagnostics` — zero issues after correction.

## Plateau Runtime Requirements

Each plateau in scope must be runtime-complete as an authored slice, not merely reachable.

For every staged plateau, `runGame` must provide:

- a bounded authored room graph backed by room-spec data,
- valid entry conditions from the preceding plateau or return state,
- valid exits into the next plateau or bounded return state,
- NPC staging anchored to authored rooms instead of generic fallback injection,
- beat surfacing through room arrival, interaction, encounter, or transition logic,
- optional side content that can start, progress, complete, and survive save/load,
- mission and HUD surfacing that distinguishes mainline from optional content,
- encounter and gate semantics tied to authored state rather than generic broad shortcuts,
- world-state changes after major beats where the contracts imply them.

Plateau closure must be judged plateau-by-plateau, not only by a final campaign smoke.

### Per-Plateau Entry Requirements

Each plateau's entry room spec must declare the correct `required_flags`. These are the canonical flag dependencies between plateaus. The exact flag names must be verified against `data/story_flags.json` before authoring room specs.

| Plateau | Entry flag dependency | Narrative reason |
| --- | --- | --- |
| `SUMMIT_SHRINE` | A LANTERN_HEIGHTS completion flag (e.g. `beat_final_departures` or `npc_withdrawal_started`) | Village has changed; Veil Maiden calls Aen to the shrine |
| `HOLLOW_DEPTHS` | A SUMMIT_SHRINE defeat flag (e.g. `yin_yang_taken` or `aen_hollowed`) | Aen falls after losing Yin and Yang at the shrine |
| `EMBER_MONASTERY` | A HOLLOW_DEPTHS completion flag (e.g. `abyssal_gate_cleared`) | Aen escapes the depths, road to the monastery opens |
| `WINDING_SKYROAD` | EMBER_MONASTERY completion + ability flags for glide and wall-cling | Full-kit required; Roga must have trained Aen |
| `MIRROR_SUMMIT` | A WINDING_SKYROAD completion flag (e.g. `beat_mirror_gate_opened`) | Mirror gate opened by ascending the skyroad |
| `BEACON_CLIFF` | A MIRROR_SUMMIT completion flag (e.g. `beat_i_release_you` or `hollow_reflection_defeated`) | Final boss defeated; Aen walks to the beacon |

### HOLLOW_DEPTHS Migration Note

HOLLOW_DEPTHS is **not starting from zero**. `AuthoringWorldBootstrap` already has extensive legacy staging for it:

- `buildTilesForArea`: specific geometry for all eight HOLLOW_DEPTHS area IDs
- `buildEnemyPlacements`: per-area enemy lists including `weightbound_ogre` boss placement
- `buildNpcPlacements` / `preferredNpcX` / `areaNpcFilter`: area-specific NPC positioning for six area IDs
- `buildAreaGates`: a full seven-gate traversal chain (camp → caves → weightbound → sparks → shatter_moth → fractured → stone_judge → abyssal_gate)

What the legacy path currently lacks for HOLLOW_DEPTHS:
- `RoomTransitionSpec` routing (uses `AreaGate` instead, which is a separate mechanism)
- `EncounterSpec` gating (no encounter-clear barriers in the legacy path)

The implementation task for HOLLOW_DEPTHS is a **lift-and-replace**: author room specs that reproduce the existing seven-gate traversal chain under the `RoomTransitionSpec` system, with proper encounter definitions for the boss areas, then verify that routing through the room-spec path matches the legacy area chain. Do not delete the legacy path until room-spec routing for HOLLOW_DEPTHS passes its bootstrap and optional-content tests.

### Boss Encounter Binding

HOLLOW_DEPTHS has three boss encounters: `WEIGHTBOUND_OGRE`, `SHATTER_MOTH_QUEEN`, and `STONE_JUDGE`. MIRROR_SUMMIT has the `HOLLOW_REFLECTION` final boss.

When authoring room specs for these areas, boss entity placement must appear in the room spec `enemy_placements` array with the correct `enemy_type` string (e.g. `"ogre"`, `"boss_shatter_moth"`, `"stone_judge"`, `"hollow_reflection"`). The `encounters` array must reference an encounter definition with that boss's entity ID in its `enemy_ids` list.

Before writing boss room specs, verify: does `GameSimulator.tickBoss()` dispatch correctly when the boss entity is spawned from a room-spec `enemy_placements` entry rather than a legacy hardcoded placement? If dispatch relies on entity type strings set during legacy bootstrap, confirm those strings survive the room-spec path unchanged. This is a concrete integration risk that must be tested before HOLLOW_DEPTHS room-spec work is marked complete.

### Ability Unlock Delivery In Room-Spec Rooms

Ability unlocks (glide, wall-cling, aerial dodge, grapple) are awarded at specific EMBER_MONASTERY rooms. In room-spec-driven staging, the mechanism for ability unlock delivery is the `set_flags` field on the **incoming transition** that delivers the player into the unlock room.

Example: the transition from the EMBER_MONASTERY hub room into the `area_hearth_of_brothers_trial` room sets `set_flags: ["airdodge_grapple_unlocked"]`. When the player traverses that transition, the flags are set. On next frame, the game state has the ability flags and any ability-gate checks in later rooms will pass.

This means **no new room-spec attribute is needed**. Ability unlocks are expressed entirely through existing transition `set_flags`. Verify flag names against `data/story_flags.json` before authoring the relevant transitions.

`WINDING_SKYROAD` entry room spec must gate on all ability flags that `area_catalog.json` implies are required for full-kit traversal. Do not author WINDING_SKYROAD room specs before EMBER_MONASTERY ability-unlock transitions are defined and tested.

## Geometry And Traversal Fidelity Requirements

`M4 Full` requires authored room geometry that is good enough to support meaningful play with placeholder presentation.

The fidelity bar is plateau-level, not per-room uniformity:

- some rooms may remain simple hubs, dialogue rooms, or transition spaces,
- not every room needs a traversal puzzle,
- but each plateau as a whole must support meaningful navigation across up, down, left, and right.

Each plateau slice must include, across its room graph:

- at least one meaningful vertical route,
- at least one return or backtrack path where returning changes interpretation or function,
- at least one encounter, objective, or interaction that requires moving through more than one elevation band or subspace,
- transitions that feel spatially motivated rather than like arbitrary edge bands,
- spawn anchors, NPC anchors, and gate placement that reinforce room purpose.

### Geometry Fidelity Pass Conditions

The following are machine-verifiable minimum standards. These apply per plateau, not per room. Terminal rooms (rooms with no transitions by narrative design, such as post-return dead-end scenes) are exempt from the transition requirement but not from the geometry requirement.

- **Vertical range:** At least one room in the plateau graph must have a platform height spread of ≥ 150 units (measured from the lowest platform `y` to the highest platform `y` in the room's geometry array). This ensures at least one room requires non-floor traversal.
- **Multi-room depth:** The plateau room graph must include at least three distinct rooms reachable via authored transitions, not counting terminal dead-end rooms.
- **Two-directional navigation:** At least one pair of rooms in the plateau graph must have transitions in both directions (A→B and B→A), confirming backtrack is authored and not just implied.
- **Encounter-gated progression:** At least one transition in the plateau graph must be of type `encounter_gate`, confirming combat gates are real.

### Geometry Failure Conditions

`M4 Full` must not be promoted complete if any newly staged plateau:

- has a room graph that is effectively a flat corridor (all geometry heights within 50 units of the floor),
- has vertical traversal that is technically possible but never required by any transition or encounter,
- has mainline or optional content that only works because invisible triggers bridge shallow geometry,
- has optional content placed into spaces with no navigation identity.

## Donor Layout Generation As A Resource

At least one donor repo includes a useful layered procedural room-carving approach. That technique is explicitly in scope as a reference or helper for room-shape authoring.

Boundary:

- the milestone does not require end-to-end procedural generation parity,
- but donor carving logic may be adapted if it materially improves authored room shells, ledges, elevation bands, or reusable layout patterns,
- any such use should serve authored plateau fidelity and authoring velocity rather than expanding procgen scope for its own sake.

## Post-Climax State Specification

`M4 Full` does not close when the player merely reaches the climax.

### Climax Completion Flag

The completion flag set when the player reaches the end of `BEACON_CLIFF` is **`beat_ending_complete`**, as anchored in `area_catalog.json` at `area_sunrise_end_card`. This is the save-load discriminator for post-climax state. Any room routing logic that needs to distinguish "in-campaign" from "post-climax" checks for this flag.

### Post-Climax Accessible Rooms

After the climax resolves, the player must be placeable in a bounded free-roam state. The minimum acceptable set of post-climax rooms is:

- At least one `BEACON_CLIFF` room that is reachable post-climax (e.g. `area_final_npc_overlook` or `area_ancient_beacon`) with updated NPC presence reflecting the completion state.
- At least one authored return path back to a prior plateau hub room, confirming the player is not locked in an end-card with no traversal.

Terminal rooms that have `"transitions": []` by design (such as `lh_hub_isolation_night`) must **not** be made the post-climax landing zone. If the post-climax landing zone needs to be a hub-like room, author a new post-climax room spec variant (e.g. `bc_epilogue_overlook`) rather than adding transitions to a room whose sealed state is intentional.

### Post-Climax Routing

`AreaPlacementResolver` must route to a post-climax area when `beat_ending_complete` is set. Define the routing rule before authoring the post-climax room specs. The simplest correct rule: if `beat_ending_complete` is set, resolve to the epilogue landing area regardless of other flag state.

### Post-Climax NPC And Dialogue Truth

At minimum, the post-climax state must have at least one NPC present in the epilogue landing room with a post-climax dialogue line. The rest of NPC state changes are not required for M4 Full closure but must not be contradicted by broken flag state.

### Post-Climax Save/Load

Loading a post-climax save must land the player in the post-climax area, not in an in-campaign room. This is a required regression test assertion.

## Testing And Gate Requirements

`M4 Full` needs a stronger gate than M4a or M4b because the risk is cross-plateau drift.

### Required Test Layers

**Layer 1 — Plateau bootstrap tests**

One runtime bootstrap test per newly staged plateau. Must verify room selection by flag state, entry routing, NPC staging at correct anchors, beat-triggered state changes on room arrival, and plateau exits.

| Plateau | Test class |
| --- | --- |
| `SUMMIT_SHRINE` | `SummitShrineVerticalSliceBootstrapTest` |
| `HOLLOW_DEPTHS` | `HollowDepthsVerticalSliceBootstrapTest` |
| `EMBER_MONASTERY` | `EmberMonasteryVerticalSliceBootstrapTest` |
| `WINDING_SKYROAD` | `WindingSkyRoadVerticalSliceBootstrapTest` |
| `MIRROR_SUMMIT` | `MirrorSummitVerticalSliceBootstrapTest` |
| `BEACON_CLIFF` | `BeaconCliffVerticalSliceBootstrapTest` |

**Layer 2 — Plateau optional-content tests**

One focused test surface per newly staged plateau for already-authored optional content. Must verify start, progress, completion, and flag persistence across save/load.

| Plateau | Test class |
| --- | --- |
| `SUMMIT_SHRINE` | `SummitShrineOptionalContentTest` |
| `HOLLOW_DEPTHS` | `HollowDepthsOptionalContentTest` |
| `EMBER_MONASTERY` | `EmberMonasteryOptionalContentTest` |

Optional content for `WINDING_SKYROAD`, `MIRROR_SUMMIT`, and `BEACON_CLIFF` may be deferred to a single combined test if those plateaus have no authored optional quests in `quests.json`. Confirm against contract before deciding.

**Layer 3 — Traversal and geometry truth tests**

One test per plateau that asserts the geometry pass conditions defined above (vertical range, multi-room depth, two-directional navigation, encounter-gated progression). These tests inspect the room spec JSON directly via `RoomSpecCatalog` — they do not run the full simulator.

| Check | Test method |
| --- | --- |
| Vertical range ≥ 150 units | `assertPlateauHasVerticalDepth(plateauId)` |
| ≥ 3 rooms in graph | `assertPlateauRoomCount(plateauId, 3)` |
| Bidirectional pair exists | `assertPlateauHasBidirectionalTransition(plateauId)` |
| Encounter-gate exists | `assertPlateauHasEncounterGate(plateauId)` |

Collect these into a single `PlateauGeometryFidelityTest` class covering all six newly staged plateaus.

**Layer 4 — Campaign continuity tests**

- The existing `Campaign Continuity` regression failure must be fixed, not documented away.
- Save/load continuity must hold across plateau boundaries, pre-boss states, post-boss states, and post-climax epilogue state.
- Specific required cases: HOLLOW_DEPTHS → SUMMIT_SHRINE boundary save/load; BEACON_CLIFF post-climax save/load (loads into epilogue area, not campaign area).

**Layer 5 — End-to-end campaign route test**

Test class: `FullCampaignRouteEndToEndTest`.

Must validate in a single deterministic run:
- fresh start,
- all plateau traversals in narrative order (LANTERN_HEIGHTS → SUMMIT_SHRINE → HOLLOW_DEPTHS → EMBER_MONASTERY → WINDING_SKYROAD → MIRROR_SUMMIT → BEACON_CLIFF),
- critical beat flag progression at each plateau,
- at least one optional-content progression (one side quest started and completed during the run),
- climax reached and `beat_ending_complete` set,
- post-climax state entered with NPC presence and stable save/load.

**Layer 6 — Authoring diagnostics gate**

The existing Act I authoring diagnostics gate must be generalized to cover multi-plateau room specs. The `runActIAuthoringDiagnostics` Gradle task (or its renamed successor) must fail fast on:

- stale room IDs in transitions,
- broken transition targets (target room not in catalog),
- missing spawn IDs referenced by transitions,
- missing NPC anchor IDs not in `npc_registry.json`,
- missing dialogue refs not in `dialogue.json`,
- broken encounter links (encounter ID not in catalog).

The diagnostics task must scan all room spec files in `data/room_specs/`, not only Act I files.

### Required Closure Evidence

- all plateau bootstrap tests green,
- all plateau optional-content tests green,
- `PlateauGeometryFidelityTest` green for all six newly staged plateaus,
- `FullCampaignRouteEndToEndTest` green,
- `Campaign Continuity` regression section green,
- multi-plateau authoring diagnostics gate green on all room spec files,
- full regression gate green (`./gradlew runRegressionTests`),
- `docs/MILESTONE_GATE_M4_FULL.md` created with explicit evidence checklist, all items green.

## Risks

### Central Routing Constraint (New — High Priority)

Lifting the `resolveRoomSpec` LANTERN_HEIGHTS guard is the prerequisite for all room-spec work on new plateaus. If this change introduces regressions in the existing Act I / Mistwood routing, all multi-plateau work is blocked.

Mitigation:

- Make this change first, in isolation, before authoring any new room specs.
- Run the full `LANTERN_HEIGHTS` and Mistwood bootstrap test suite immediately after.
- The `ResolveRoomSpecMultiPlateauFallbackTest` (Phase 0) must pass before proceeding.

### Boss Encounter Binding (New — Medium Priority)

Boss entities (`weightbound_ogre`, `shatter_moth_queen`, `stone_judge`, `hollow_reflection`) are placed and dispatched differently in the legacy path vs. the room-spec path. If `GameSimulator.tickBoss()` dispatch relies on entity state set during legacy bootstrap, boss encounters may silently fail when entities are spawned from room-spec placements.

Mitigation:

- Write a targeted integration test for the WEIGHTBOUND_OGRE encounter before authoring the full HOLLOW_DEPTHS room-spec suite. If boss dispatch fails, fix the dispatch mechanism before proceeding to other boss rooms.
- Require that boss entity placement in room-spec `enemy_placements` uses the same `enemy_type` strings as the legacy path.

### Ability Unlock Delivery (New — Medium Priority)

Ability flags set via transition `set_flags` at EMBER_MONASTERY room boundaries must propagate correctly into ability-gate checks in WINDING_SKYROAD room specs. If flag propagation has any timing issue (flags not visible until next bootstrap call), WINDING_SKYROAD entry will block falsely.

Mitigation:

- Write an integration test for the EMBER_MONASTERY glide-unlock transition before authoring WINDING_SKYROAD room specs.
- Confirm that ability flags set in a transition are visible to `resolveRoomSpec` on the same bootstrap call.

### Authoring / Runtime Mismatch

Some already-authored beats or side quests may assume spatial or encounter semantics that the current runtime still expresses weakly.

Mitigation:

- allow only small targeted runtime extensions,
- tie each extension to a concrete authored runtime requirement,
- require tests and diagnostics for every new extension path.

### Bootstrap Sprawl

`AuthoringWorldBootstrap` may become an unmaintainable plateau-switch monolith if multi-plateau staging is handled entirely through hardcoded branching.

Mitigation:

- the multi-plateau routing change (lifting the guard, adding the area-agnostic fallback) is the correct structural move,
- do not grow the legacy switch ladder in `buildTilesForArea`, `buildNpcPlacements`, or `buildAreaGates` for new plateaus,
- new plateaus must go through the room-spec path exclusively.

### Geometry Quality Drift

It is easy to satisfy staging with shallow room graphs that technically route but do not feel intentional.

Mitigation:

- enforce geometry pass conditions via `PlateauGeometryFidelityTest`,
- treat geometry truth as a milestone requirement, not polish.

### Optional Content Dishonesty

Optional quests may appear in HUD/contracts but remain unplayable or route-breaking.

Mitigation:

- plateau-specific optional-content tests are required,
- optional content is part of closure, not a stretch goal.

### Continuity Fragility

Cross-plateau save/load and post-climax state are likely places for hidden state bugs.

Mitigation:

- continuity is a first-class gate,
- not a final cleanup pass.

### Donor Over-Import

The donor layered room-carving approach is useful, but a wholesale procgen port would create avoidable scope drift.

Mitigation:

- use donor techniques only where they improve authored layout production or reusable shape patterns,
- keep acceptance focused on authored runtime quality.

## Failure Conditions

`M4 Full` must not be promoted complete if any of the following are still true:

- one or more plateau families remain contract-authored but lack runtime room-spec staging,
- plateau-local optional content exists in contracts but is not playable in `runGame`,
- the player can reach the climax but not persist a stable post-climax state,
- post-climax save/load routes the player to an in-campaign room rather than the epilogue area,
- geometry remains broadly flat and transition-driven rather than traversal-driven,
- `Campaign Continuity` still fails,
- authoring diagnostics remain Act-I-only in practice and do not protect multi-plateau room growth,
- any new plateau's room-spec routing depends on changes to the legacy `buildTilesForArea`, `buildNpcPlacements`, or `buildAreaGates` switch ladders rather than going through the room-spec path.

## Milestone Handoff

If `M4 Full` closes successfully, the project should no longer have a campaign-existence problem.

At that point:

- `runGame` is the real campaign host rather than a partial slice host,
- all currently-authored campaign plateau content is runtime-playable,
- room-spec authoring is proven beyond Act I across six additional plateau families,
- the authoring diagnostics gate protects the full room-spec corpus,
- future work can focus on fidelity, scale, authoring acceleration, or M6 opportunity integration instead of basic campaign staging.

The natural next milestone after `M4 Full` is not "make the campaign exist." It is one of:

- deeper campaign fidelity and readability,
- broader authoring acceleration across all plateaus,
- or selective M6 runtime/world opportunity integration on top of a now-complete campaign host.
