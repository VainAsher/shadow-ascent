# Room Authoring Guide

## Purpose

Use this guide when adding or modifying `runGame` room specs under `data/room_specs/`.

## File Placement

- Keep production plateau room specs in `data/room_specs/<plateau>_vertical_slice.json`.
- Reserve `*fixture*.json` files for tests and proof fixtures only.
- Production scans intentionally exclude any room-spec file whose name contains `fixture`.

## Transition Types

Use only these transition types:

- `free_exit`
- `npc_handoff_gate`
- `mission_gate`
- `encounter_gate`
- `return_gate`

Every transition must target:

- a real `target_room_id`
- a real `target_spawn_id` inside that room
- a valid X band, and Y band where vertical gating matters

## NPC Role Types

Use these existing authoring roles:

- `mission_giver`
- `optional_npc`
- `quest_giver`

Additional established runtime roles such as `merchant`, `story`, `teacher`, `smith`, and `villager` remain valid where already used by production room specs.

## Encounter Authoring

- Put room-level encounter references in the room's `encounters` array.
- Define each encounter in the same file under `encounter_definitions`.
- Keep `enemy_ids` aligned with `enemy_placements.enemy_id` values from the same room graph.

## Geometry Expectations

- Author at least one meaningful vertical route per plateau slice.
- Prefer room shapes that create reasons to move up, down, left, and right.
- Use simpler rooms for hubs or staging, not for every room in the graph.

## Validation Before Push

Run both diagnostics commands before pushing room-spec changes:

```powershell
./gradlew.bat --console=plain runRunGameAuthoringDiagnostics
./gradlew.bat --console=plain runActIAuthoringDiagnostics
```

Run the relevant plateau bootstrap and transition tests for the files you changed.
