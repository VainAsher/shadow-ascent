---
handover_type: codex_task
milestone: M5
status: done (2026-05-07)
created: 2026-05-07
---
# Codex Task — M5 Quest Ecology Validation

## Context

`QuestEcologyEngine` has been added to `core.simulation`. It generates typed `QuestOpportunity` records from `WorldSimulationTickResult` pressure samples. It is wired into `MissionManager.ingestSignalFrame()` and exposed via `MissionManager.currentEcologyOpportunities()`.

Two new regression tests cover it:
- `testQuestEcologyDeterminism` — same seed produces same opportunities across runs
- `testQuestEcologyThresholds` — each opportunity type fires at correct pressure threshold

## Task

Run the full M5 diagnostics and regression gate, then produce a concise summary.

### Commands to run (in order)

```
./gradlew :core:compileJava :client:compileJava runDataContractDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

### Summary format expected

Report back with:

```
BUILD: PASS / FAIL
runDataContractDiagnostics: [key counts line from output]
runWorldSimulationDiagnostics: [key counts line from output]
runRegressionTests: PASS / FAIL
  - testQuestEcologyDeterminism: PASS / FAIL
  - testQuestEcologyThresholds: PASS / FAIL
  - [any other failures if present]
```

If any test fails, paste the `[FAIL]` line and the error message exactly.

## Definition of Done

All tasks pass and summary is captured in `docs/CURRENT_STATE.md` under Verification Evidence.
