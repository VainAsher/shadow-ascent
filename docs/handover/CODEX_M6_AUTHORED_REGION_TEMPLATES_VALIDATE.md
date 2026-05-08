---
handover_type: codex_validate
milestone: M6
topic: authored_region_templates
status: done
created: 2026-05-08
---
# Codex Validate — M6 Authored Region Templates (stub geometry elimination)

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Expected Results

| Check | Expected |
|---|---|
| `:core:compileJava` | BUILD SUCCESSFUL |
| `runWorldgenDiagnostics` | Section templates loaded: 13 (was 10); validation issues: 0 |
| `runRegressionTests` | 49/49 PASS — zero `[WARN] RegionLoader: no template for` lines |

## New Templates

| File | biome | kind | footprint |
|---|---|---|---|
| `data/worldgen/sections/lantern_region_hub.json` | lantern | region_hub | 4×2 |
| `data/worldgen/sections/lantern_hub.json` | lantern | hub | 3×2 |
| `data/worldgen/sections/hollow_dungeon.json` | hollow | dungeon | 3×3 |

## RegressionTest changes

Three test methods that previously constructed `RegionLoader(null/empty, ...)` now use
`SectionTemplateLibrary.loadDefault()` so the authored templates are visible at test time:
- `testMutationPersistenceRoundTrip`
- `testSaveV3OverlayPersistence`
- `testRegionTransitionNeighborhoodReload`

## Evidence

```
BUILD SUCCESSFUL in 45s
runWorldgenDiagnostics: Section templates loaded: 13 — Section template validation issues: 0
runRegressionTests: 49/49 PASS — no [WARN] RegionLoader stub-geometry lines
```
