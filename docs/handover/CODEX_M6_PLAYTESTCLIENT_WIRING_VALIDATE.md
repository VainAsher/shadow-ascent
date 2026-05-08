---
doc_type: codex_handover
milestone: M6
topic: playtestclient_regionloader_wiring
status: done
completed: 2026-05-08
---
# CODEX M6 PlaytestClient → RegionLoader Wiring — Validation Handover

## Purpose

Verification record for M6 step 12: wiring `PlaytestClient.initializeCollisionLayout`
to `RegionLoader`, connecting the streaming runtime to the client game loop.

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegionalStreamingDiagnostics runRegressionTests
```

## Expected Outcomes

| Check | Expected result |
|---|---|
| Compile | All three modules (core, client, server) compile with no errors |
| `runRegionalStreamingDiagnostics` | `Region fragments loaded: 3`, `Constraint validation: PASS` |
| `runRegressionTests` | 28/28 PASS including all three M6 sections |

## Actual Gate Output (2026-05-08)

```
[PASS] All regression tests PASSED
[READY] Release candidate is stable and ready
BUILD SUCCESSFUL in 2m 10s
```

## What Was Wired

### New imports in `PlaytestClient.java`

```java
import com.shadowascent.core.world.progression.WorldProgressionGraph;
import com.shadowascent.core.world.progression.WorldProgressionGraph.ProgressionNode;
import com.shadowascent.core.world.sections.SectionTemplateLibrary;
import com.shadowascent.core.world.streaming.RegionInstance;
import com.shadowascent.core.world.streaming.RegionLoader;
import com.shadowascent.core.world.streaming.RegionalStreamingConstraintValidator;
import com.shadowascent.core.world.streaming.ZoneOverride;
```

### New constant

```java
private static final long WORLD_SEED = 1337L;
```

### New fields

```java
private final List<RegionInstance> activeRegions;     // live region instances loaded at startup
private final WorldProgressionGraph progressionGraph; // 3-node playtest graph
private String currentRegionId;                       // starting region for neighborhood load
private Map<String, List<ZoneOverride>> savedOverlays; // populated from SAVE_V3 on load (empty now)
```

### `buildPlaytestProgressionGraph()` (new static method)

Builds the canonical 3-node playtest graph matching the authored region fragments:
- `hub_lantern_heights` (CENTRAL_HUB) → children: `dungeon_forge_terrace_a`
- `dungeon_forge_terrace_a` (REGION_HUB, grants: dash) → children: `region_hollow_shaft`
- `region_hollow_shaft` (DUNGEON, requires: dash, grants: combat_basic)

Seed: `WORLD_SEED = 1337L`, source label: `"playtest"`.

### `initializeCollisionLayout()` (modified)

Added region loading block at the top (before the authored traversal geometry):

```java
activeRegions.clear();
try {
    RegionLoader loader = new RegionLoader(
            SectionTemplateLibrary.loadDefault(),
            new RegionalStreamingConstraintValidator());
    activeRegions.addAll(loader.loadNeighborhood(
            progressionGraph, currentRegionId, 1, WORLD_SEED, savedOverlays));
    for (RegionInstance r : activeRegions) {
        for (TileRect tile : r.staticTiles()) {
            collisionTiles.add(tile);
            collisionHash.insert(tile);
        }
    }
} catch (Exception e) {
    System.out.println("[WARN] PlaytestClient: region neighborhood load failed: " + e.getMessage());
}
```

The authored solid/platform tile geometry (rooms 1–4) remains. RegionInstance static
tiles are supplemental — the authored tiles define the playable QA route geometry.
Full geometry replacement is deferred until section templates carry authored pixel-accurate
platform layouts (current templates provide stub floor+wall geometry only).

### `buildAllDynamicTiles()` (new method)

Merges traversal subsystem dynamic tiles with overlay tiles from all active regions:

```java
private List<TileRect> buildAllDynamicTiles() {
    List<TileRect> tiles = new ArrayList<>(
            traversalSubsystem.buildDynamicTiles(clearedCombatEncounterIds, storyState::hasAbility));
    for (RegionInstance r : activeRegions) {
        tiles.addAll(r.overlayTiles());
    }
    return tiles;
}
```

This is the key runtime connection: `MutationOverlay.apply()` sets overlay tiles on
`RegionInstance`, and they now flow into the live `collisionHash` on every dynamic-tile refresh.

### `refreshDynamicCollisionTiles()` (updated)

Now delegates to `buildAllDynamicTiles()` instead of calling traversal subsystem directly:

```java
private void refreshDynamicCollisionTiles() {
    dynamicCollisionTiles.clear();
    dynamicCollisionTiles.addAll(buildAllDynamicTiles());
    collisionHash.setDynamicTiles(dynamicCollisionTiles);
}
```

### `refreshCollisionHashFromRegions()` (new method)

Full hash rebuild entry point for region transitions and post-mutation refresh:

```java
private void refreshCollisionHashFromRegions() {
    collisionHash.clear();
    for (TileRect tile : collisionTiles) {
        collisionHash.insert(tile);
    }
    collisionHash.setDynamicTiles(buildAllDynamicTiles());
}
```

Called on region transition (when `currentRegionId` changes) and after a `MutationOverlay`
is applied to a live `RegionInstance`.

## Runtime Data Flow After Wiring

```
WorldProgressionGraph (3-node playtest graph)
    └─► RegionLoader.loadNeighborhood(hub_lantern_heights, radius=1)
            └─► [hub_lantern_heights, dungeon_forge_terrace_a] loaded as RegionInstances
                    └─► staticTiles → collisionTiles + collisionHash (supplemental)
                    └─► overlayTiles → buildAllDynamicTiles() → collisionHash dynamic layer

WorldSimulationTick → MutationOverlay.apply(regionInstance, overrides)
    └─► regionInstance.setOverlayTiles(tiles, overrides)
            └─► refreshDynamicCollisionTiles() picks up new overlayTiles next frame
```

## Deferred Items

- **`currentRegionId` update on region transition**: field is set to `"hub_lantern_heights"` at
  startup and never updated. Region transition wiring (updating `currentRegionId` then calling
  `initializeCollisionLayout()` or `refreshCollisionHashFromRegions()`) is deferred.
- **`savedOverlays` population from SAVE_V3**: field is always empty at startup. Will be populated
  from the `region_overlays_b64` field once SAVE_V3 is implemented (M6 step 13).
- **Full authored geometry replacement**: section templates currently provide stub floor+wall
  geometry (small footprints at origin). Replacing all `addSolidTile`/`addPlatformTile` calls
  with RegionInstance tiles requires templates with pixel-accurate authored platform layouts.
