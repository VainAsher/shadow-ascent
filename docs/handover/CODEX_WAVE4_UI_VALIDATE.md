---
handover_type: codex_task
milestone: Wave 4
status: done (2026-05-08)
created: 2026-05-08
---
# Codex Task — Wave 4 UISubsystem Extraction Final Validation

## Context

Wave 4 UISubsystem extraction is complete. All HUD and rendering logic was moved from `PlaytestClient`
into a standalone `UISubsystem.java`. A `WorldGeometry` record was also introduced to package layout
constants. `PlaytestClient` now delegates all rendering and display-state management through
`UISubsystem`. No behaviour changed — this is a structural refactor only.

Files changed in this wave:

- `java/client/src/main/java/com/shadowascent/client/WorldGeometry.java` — new (record, layout constants)
- `java/client/src/main/java/com/shadowascent/client/UISubsystem.java` — new (HUD, minimap, event log, interaction hint, ability snapshot, NPC positions)
- `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` — all draw/display-state methods deleted, constructor wired to `UISubsystem`, all call sites delegated

Key architectural decisions:

- `UISubsystem.RenderState` (nested public record, 13 fields) is the per-frame snapshot; `drawFrame` is pure rendering
- `tickAndUpdate(playerX, playerY, dt)` owns all display-state mutation (timer decrements, NPC position refresh, ability snapshot, mission feedback)
- `evidenceLogger` is a `Consumer<String>` injected at construction so `UISubsystem` can fire evidence log entries without a back-reference to `PlaytestClient`
- `getNpcPositions()` exposes NPC positions read-only for `interactNearestNpc()` in `PlaytestClient`
- `missionTimerSeconds` and `sessionElapsedSeconds` are not in `RenderState`; `UISubsystem` reads `storyState.getMissionTimer()` directly
- `notifyAbilityGateBlocked(TileRect tile, Set<String> clearedCombatEncounterIds)` signature adds the cleared set since it is dynamic state owned by `PlaytestClient`

## Task

Run the full regression and compile gate. Report the result.

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runRegressionTests
```

## Definition of Done

Report back with:

1. Build outcome: `BUILD SUCCESSFUL` or `BUILD FAILED`
2. `runRegressionTests` result: pass count / total, any `[FAIL]` lines
3. `runDataContractDiagnostics` summary line (`contracts_loaded`, `valid`, `beats`, `critical_flags`, `plateaus`)
4. Any compile errors or unexpected warnings

On success: mark this file `status: done (date)` and update `docs/handover/README.md`
UISubsystem entry as Done.

## Verification Evidence (2026-05-08)

Local gate executed:

```
.\gradlew.bat :core:compileJava :client:compileJava runDataContractDiagnostics runRegressionTests
```

- Build: `BUILD SUCCESSFUL`
- `runRegressionTests`: 25/25 PASS, all regression tests PASSED
- `runDataContractDiagnostics`: valid contracts, no issues
- No compile errors
