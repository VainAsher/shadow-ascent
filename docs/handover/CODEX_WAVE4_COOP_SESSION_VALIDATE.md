---
handover_type: codex_validate
milestone: Wave4
topic: coop_session
status: done
created: 2026-05-08
---
# Codex Validate — Wave 4 Co-op Session Scaffolding

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Expected Results

| Check | Expected |
|---|---|
| `:core:compileJava` | BUILD SUCCESSFUL |
| `runRegressionTests` | 49/49 PASS — new `testCoopSessionScaffolding` section (3 sub-tests) |

## New APIs

- `requestRevive(reviverPlayerId, targetPlayerId)` — revives dead target if reviver is alive and within `REVIVE_RANGE` (80px); emits `COOP_REVIVE`
- `tickCoopProximity()` — called each tick; emits `PLAYER_PROXIMITY` for each alive player pair within range

## Behaviour Notes

- `requestRevive` is a no-op if either player is missing, reviver is dead, target is not dead, or distance > 80px
- Revived player restores to `max(1, maxHealth/2)` HP and gains `INVINCIBILITY_TICKS` frames
- `PLAYER_PROXIMITY` uses canonical pair ordering (lower-slot player as entity ID) to avoid duplicate events per pair per tick

## Evidence

```
BUILD SUCCESSFUL in 2m 9s
runRegressionTests: 49/49 PASS
--- Testing Wave4 Co-op Session Scaffolding ---
[PASS] PASSED
```
