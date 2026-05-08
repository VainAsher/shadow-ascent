---
handover_type: codex_validate
milestone: Wave5
topic: minimap_renderer
status: done
created: 2026-05-08
---
# Codex Validate — Wave 5 MinimapRenderer Extraction

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Expected Results

| Check | Expected |
|---|---|
| `:core:compileJava` | BUILD SUCCESSFUL |
| `:client:compileJava` | BUILD SUCCESSFUL — `MinimapRenderer.java` and updated `UISubsystem.java` compile cleanly |
| `:server:compileJava` | BUILD SUCCESSFUL |
| `runDataContractDiagnostics` | no new issues |
| `runWorldgenDiagnostics` | PASS |
| `runWorldSimulationDiagnostics` | PASS |
| `runRegressionTests` | 43/43 PASS — existing tests unchanged |

## Validation Notes

- `MinimapRenderer` lives in `com.shadowascent.client` — no `core` regression section added (layer contract: `core` cannot import `client`).
- Validation evidence is `client:compileJava` success + all 43 prior regression tests still passing.
- `activeNpcsSorted()` retained in `UISubsystem` (used by NPC drawing at lines 174/412 beyond minimap); `MinimapRenderer` carries its own copy.
- `drawMinimap` method body deleted from `UISubsystem`; `minimapRenderer.draw(g, state)` delegation wired in `drawFrame`.

## Evidence

```
BUILD SUCCESSFUL in 45s
runRegressionTests: 43/43 PASS
```
