---
handover_type: design
milestone: M6
topic: region_transition_wiring
status: complete
created: 2026-05-08
---
# Design — M6 Region Transition Wiring

Wires `PlaytestClient` to call `RegionLoader.loadNeighborhood` dynamically when the player
crosses a room boundary, so the active region set and collision hash always reflect the
current streaming neighborhood.

---

## Problem Statement

After the initial `loadNeighborhood` call in `initializeCollisionLayout`, `currentRegionId`
was never updated and `activeRegions` was never reloaded. The streaming layer existed but
the client was permanently pinned to the startup neighborhood (`hub_lantern_heights` center,
radius=1). Crossing into Forge Terrace or Hollow Shaft produced no overlay refresh.

---

## Room-to-Region Mapping

The playtest world has 4 authored rooms aligned to 3 progression graph nodes:

| X range | Region ID |
|---|---|
| `WORLD_LEFT_X .. HUB_ROOM_END_X` | `hub_lantern_heights` |
| `HUB_ROOM_END_X .. FORGE_ROOM_END_X` | `dungeon_forge_terrace_a` |
| `FORGE_ROOM_END_X .. WORLD_RIGHT_X` | `region_hollow_shaft` |

Room 4 (Summit approach) maps to `region_hollow_shaft` — it is within that region's
authored footprint.

---

## Implementation

### New private methods — `PlaytestClient.java`

**`resolveRegionIdForX(float x) → String`**

Pure mapping from player X to region ID using the three room boundaries.

**`checkAndApplyRegionTransition()`**

Compares `resolveRegionIdForX(playerPhysics.x)` against `currentRegionId`. If they differ,
delegates to `transitionToRegion`.

**`transitionToRegion(String newRegionId)`**

1. Updates `currentRegionId`.
2. Clears `activeRegions`.
3. Calls `RegionLoader.loadNeighborhood(progressionGraph, currentRegionId, 1, WORLD_SEED, savedOverlays)`.
4. Calls `refreshCollisionHashFromRegions()` — rebuilds static + overlay tiles in the hash.
5. Emits `REGION_TRANSITION` evidence line with `from`, `to`, and `playerX`.
6. Logs a readable line: `Region: hub_lantern_heights → dungeon_forge_terrace_a`.

### Wiring in `tick()`

`checkAndApplyRegionTransition()` is called immediately after `updateMovement` and before
`combatSubsystem.update`, ensuring the collision hash is current before any combat or
camera update that frame.

---

## Regression Test Added

### `testRegionTransitionNeighborhoodReload` — [PASS]

Builds the identical 3-node progression graph (`hub_lantern_heights → dungeon_forge_terrace_a → region_hollow_shaft`)
using the same seed (1337L) and empty fragments (no authored JSON overrides).

Four sub-tests:

| # | Center node | Expected IDs in neighborhood |
|---|---|---|
| 1 | `hub_lantern_heights` | `{hub, forge}` (2 nodes, shaft excluded) |
| 2 | `dungeon_forge_terrace_a` | `{hub, forge, shaft}` (3 nodes, full span) |
| 3 | `region_hollow_shaft` | `{forge, shaft}` (2 nodes, hub excluded) |
| 4 | `hub_lantern_heights` (again) | Same as test 1 — reload is stable |

[WARN] lines about missing templates are expected — empty fragments cause stub geometry.
These do not cause test failures.

---

## Layer Contract Verification

`PlaytestClient` already imported `RegionLoader`, `RegionalStreamingConstraintValidator`,
`SectionTemplateLibrary`. No new imports required. All three new methods are private and
confined to the client package. No `core` types were modified.

---

## Prior Test Count

35 tests prior to this slice. Now 36/36.
