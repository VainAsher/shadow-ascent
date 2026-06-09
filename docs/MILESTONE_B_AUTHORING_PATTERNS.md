---
doc_type: guide
status: draft
owner: core-team
last_updated: 2026-05-14
milestone: M4b - Act I Authoring Velocity And Fidelity Hardening
---
# Milestone B Authoring Patterns

## Purpose

This guide records the bounded authoring operations M4b supports on the Act I `runGame` path without ordinary Java edits.

## Supported Zero-Java Authoring Operations

1. Add one new Act I room through `data/room_specs/*.json`.
2. Add one optional side beat through `data/quests.json` plus supporting authored data.
3. Add one room-state variant through room-spec `required_flags`.
4. Add one bounded encounter through room-spec `encounter_definitions`.
5. Add one mission-critical readability cue through authored metadata.

## Preconditions

- Run `./gradlew.bat --console=plain runActIAuthoringDiagnostics` before and after authoring changes.
- Keep `data/room_specs/act_i_authoring_fixture.json` reserved for proof fixtures and tests. It is intentionally excluded from `RoomSpecCatalog.loadDefault()`.
- For Act I room-spec-driven scenes, `AuthoringWorldBootstrap` treats `npc_anchors` as the source of truth for staged cast and positions.

## Pattern 1: Add A New Room

Edit a production room-spec file under `data/room_specs/`.

Required fields:
- `id`
- `display_name`
- `plateau_id`
- `area_id`
- `scene_role`
- `route_order`
- `spawn_points`
- `geometry`
- `transitions`

Rules:
- New room IDs must be globally unique across all loaded room-spec files.
- Transition `target_room_id` and `target_spawn_id` must resolve to a loaded room and spawn.
- If the room should participate in an existing Act I area, author a distinct `required_flags` predicate so the room-selection precedence stays intentional.

Proof point:
- `ActIAuthoringFixtureRoundTripTest` locks the zero-Java room-addition invariant with `lh_fixture_annex`.

## Pattern 2: Add A Room-State Variant

Use the same `area_id` as the room you are varying, and give the new room:
- a distinct `scene_role`
- a distinct `required_flags` set
- a `route_order` that places it intentionally relative to the other matching rooms

Selection rule:
- For room-spec-driven Act I areas, the first room whose `required_flags` are all satisfied wins by `route_order`, then `id`.

Diagnostics guard:
- `ActIAuthoringDiagnostics` flags same-area, same-scene-role variants that share identical `required_flags`.

## Pattern 3: Add An Optional Side Beat

Edit `data/quests.json`.

Required step fields:
- `id`
- `act`
- `plateau`
- `hook`
- `objective`
- `route_hint`
- `area_pool`
- `reward_effects`
- `sets_flags`

Rules:
- Side-beat mission IDs become `sq_` + step `id`.
- The starter NPC comes from the quest chain's `npc` field.
- `RunGameMissionInteraction` already resolves `sq_` mission starts and objective routing through the side-quest contract path.

Proof point:
- `sq_fixture_q1_test_errand` proves a new `LANTERN_KID` side beat starts without a Java switch branch.

## Pattern 4: Add A Bounded Encounter

Add an `encounter_definitions` entry and reference it from the room's `encounters` list.

Rules:
- `enemy_ids` should match authored `enemy_placements` IDs that are meant to gate progression.
- Use `encounter_gate` or `return_gate` transitions when the route must wait on encounter clear.
- Use transition `set_flags` to open downstream room-state or route progression.

Proof point:
- `mistwood_afterglow_pass` is a deeper post-clear Mistwood room added through room-spec JSON and the existing encounter-gate semantics.

## Pattern 5: Add A Readability Cue

Prefer authored metadata over new HUD-only switch logic.

Supported metadata surfaces:
- `mainline_missions[].route_hint`
- `mainline_missions[].mainline`
- side-quest step `route_hint`
- room `display_name`
- room `scene_role`

Rules:
- Mainline route hints belong in `quests.json` `mainline_missions`.
- Optional side-beat route hints belong on the step itself.
- `HubScreen` consumes the contract metadata rather than a mission-ID switch for Act I route hints.

## Verification

Minimum M4b authoring validation:

```bash
./gradlew.bat --console=plain runActIAuthoringDiagnostics
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.ActIAuthoringFixtureRoundTripTest"
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.RunGameMissionInteractionTest"
./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.SaveLoadRuntimeStateTest"
```
