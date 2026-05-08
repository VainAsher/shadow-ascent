---
doc_type: codex_handover
milestone: M6
topic: regional_streaming
status: done
completed: 2026-05-08
---
# CODEX M6 Regional Streaming — Validation Handover

## Purpose

Verification record for the M6 regional streaming implementation: package
`com.shadowascent.core.world.streaming`, authored region fragments, constraint
validator, mutation overlay, and diagnostics task.

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegionalStreamingDiagnostics runRegressionTests
```

## Expected Outcomes

| Check | Expected result |
|---|---|
| Compile | All three modules (core, client, server) compile with no errors |
| `runDataContractDiagnostics` | `contracts_loaded=true valid=true` |
| `runWorldgenDiagnostics` | `section templates loaded: 10, validation issues: 0` |
| `runWorldSimulationDiagnostics` | `World regions: 3, Factions: 3, Settlements: 3, Validation issues: none` |
| `runRegionalStreamingDiagnostics` | `Region fragments loaded: 3`, `Constraint validation: PASS` |
| `runRegressionTests` | All tests PASS including M6 sections |

## Actual Gate Output (2026-05-08)

```
Region fragments loaded: 3
Constraint validation: PASS
[PASS] ... (28 total, all PASS)
[PASS] All regression tests PASSED
BUILD SUCCESSFUL in 2m 15s
```

## What Was Implemented

### New package: `com.shadowascent.core.world.streaming`

| File | Role |
|---|---|
| `SocketBinding.java` | Record: `socketId`, `connectedRegionId` — typed region-to-region socket descriptor |
| `ZoneOverride.java` | Record: `zoneRole`, `overlayKind`, `params` — role-addressed overlay entry |
| `RegionManifest.java` | Stable serialization contract for a loaded region: sockets + zone overrides |
| `ResolvedAnchor.java` | Record: `anchorId`, `kind`, `worldX`, `worldY`, `satisfiedRequires`, `active` |
| `RegionFragmentData.java` | Record: authored fragment shape from `data/worldgen/regions/` JSON — public top-level type |
| `RegionInstance.java` | Runtime representation: `staticTiles`, `overlayTiles`, `appliedOverrides`, `SpatialHash`, `anchors`; `setOverlayTiles` stores overrides for save state extraction |
| `RegionLoadException.java` | Wraps `ValidationResult` for hard constraint failures in `loadNeighborhood` |
| `RegionalStreamingConstraintValidator.java` | Four checks: DISCONNECTED, INACCESSIBLE, SOFTLOCK, SOCKET_MISMATCH |
| `RegionLoader.java` | Loads authored fragments from `data/worldgen/regions/*.json`; generates stub tiles from `SectionTemplate` footprint (96px/unit); `loadNeighborhood` BFS + validation |
| `MutationOverlay.java` | Maps `WorldSimulationPressureSample` → `ZoneOverride` list; `apply` mutates `RegionInstance`; `extractSaveState` reads `appliedOverrides()` |

### New entry point
- `com.shadowascent.core.RegionalStreamingDiagnostics` — CLI runner that loads all 3 fragments, builds a 3-node diagnostics graph, runs `RegionLoader` + `RegionalStreamingConstraintValidator`, reports result.

### New Gradle task
- `runRegionalStreamingDiagnostics` in `build.gradle.kts` (group: verification)

### Authored data
- `data/worldgen/regions/hub_lantern_heights.json` — hub_home biome=lantern, 1 socket to forge
- `data/worldgen/regions/dungeon_forge_terrace_a.json` — boss_approach biome=lantern, 2 sockets
- `data/worldgen/regions/region_hollow_shaft.json` — shop_save_loop biome=hollow, 1 socket to forge

### Regression coverage (RegressionTest.java)
- `testRegionalStreamingConstraints` — 3-node graph: clean pass, CORRUPTION_SURGE softlock detection, SOCKET_MISMATCH detection
- `testMutationPersistenceRoundTrip` — apply 2 overrides, `extractSaveState`, restore and reapply, compare overlay tile counts
- `testCoopTraversalRecoveryValidation` — parallel-path graph: partial manifest validates clean, full manifest flags SOFTLOCK on route-blocked non-optional region

## Key Design Decisions

**Softlock immediate-return rule:** `canReachHubUnblocked` returns `false` immediately when the start node is itself route-blocked. A CORRUPTION_SURGE on a traversal zone seals the player inside that node — they cannot use any sockets to exit. Without this rule, BFS would incorrectly find the adjacent hub as reachable through the blocked node's own socket list.

**`appliedOverrides` field:** `RegionManifest` is an immutable record — dynamically-applied overlays cannot be stored there. `RegionInstance` carries a mutable `appliedOverrides` list updated by `MutationOverlay.apply` via `setOverlayTiles`. `extractSaveState` reads this field, not `manifest().zoneOverrides()`.

**`RegionFragmentData` as top-level type:** Originally a nested record inside `RegionLoader`, which made it package-private and inaccessible from `RegionalStreamingDiagnostics` and `RegressionTest`. Extracted to a standalone public record file.

## Layer Contract Status

`core.world.streaming` imports only from:
- `core.physics` (`TileRect`, `TileType`, `SpatialHash`)
- `core.simulation` (`WorldSimulationPressureSample`)
- `core.world.progression` (`WorldProgressionGraph`, `ProgressionNode`)
- `core.world.sections` (`SectionTemplate`, `SectionTemplateLibrary`)

No client or server imports. Layer contract satisfied.

## Deferred Items

Per design doc (`DESIGN_M6_REGIONAL_STREAMING.md`):
- **Step 12**: `PlaytestClient.initializeCollisionLayout` wiring to `RegionLoader` — deferred to next PlaytestClient session.
- **Step 13**: SAVE_V3 envelope + v2→v3 migrator for `region_overlays_b64` persistence — deferred until save schema evolution is confirmed.
