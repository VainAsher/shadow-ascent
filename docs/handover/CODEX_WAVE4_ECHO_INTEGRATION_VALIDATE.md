---
handover_type: codex_validate
milestone: Wave4
topic: echo_integration
status: done
created: 2026-05-08
---
# Codex Validate — Wave 4 EchoRecorder / ReplayPlayer / SimEcho Integration

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Expected Results

| Check | Expected |
|---|---|
| `:core:compileJava` | BUILD SUCCESSFUL |
| `runRegressionTests` | 48/48 PASS — new `testEchoRecorderIntegration` section (3 sub-tests) |

## New APIs

- `spawnEcho(playerId, echoId, looping)` — snapshots recorder, builds ReplayPlayer, creates SimEcho
- `getEchoes()` — unmodifiable view of active echoes
- `tickPlayers` now calls `p.echoRecorder.record(p.latestInput)` each tick

## Behaviour Notes

- EchoRecorder is a 600-frame ring buffer (10s at 60 Hz) — already on SimPlayer
- `spawnEcho` is a no-op if the recorder is empty or the player does not exist
- Non-looping echo marks `completed=true` / `active=false` after exhausting its frame sequence; removed from list on same tick
- `ECHO_STARTED` fires on spawn; `ECHO_COMPLETED` / `ECHO_FAILED` fire on terminal transitions

## Evidence

```
BUILD SUCCESSFUL in 2m 9s
runRegressionTests: 48/48 PASS
--- Testing Wave4 EchoRecorder / ReplayPlayer / SimEcho ---
[PASS] PASSED
```
