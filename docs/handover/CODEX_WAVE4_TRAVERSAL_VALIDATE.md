---
handover_type: codex_task
milestone: Wave 4
status: done (2026-05-08)
created: 2026-05-08
---
# Codex Task — Wave 4 TraversalSubsystem Extraction Final Validation

## Context

Wave 4 TraversalSubsystem extraction is complete. All six inner types were moved to standalone
files, `TraversalSubsystem.java` was created, and PlaytestClient was fully rewired to delegate.
No behaviour changed — this is a structural refactor only.

Files changed in this wave:

- `java/client/src/main/java/com/shadowascent/client/TriggerMode.java` — new
- `java/client/src/main/java/com/shadowascent/client/MovingPlatform.java` — new
- `java/client/src/main/java/com/shadowascent/client/AbilityGate.java` — new
- `java/client/src/main/java/com/shadowascent/client/AbilityTrigger.java` — new
- `java/client/src/main/java/com/shadowascent/client/CombatBarrier.java` — new
- `java/client/src/main/java/com/shadowascent/client/TriggeredPlatform.java` — new (added `requiredTriggerId()` getter)
- `java/client/src/main/java/com/shadowascent/client/TraversalSubsystem.java` — new
- `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` — full delegation wiring, inner classes deleted

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
TraversalSubsystem entry as Done.
