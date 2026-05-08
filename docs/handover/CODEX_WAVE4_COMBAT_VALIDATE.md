---
handover_type: codex_task
milestone: Wave 4
status: done (2026-05-07)
created: 2026-05-07
---
# Codex Task — Wave 4 CombatSubsystem Extraction Final Validation

## Context

Wave 4 Step 2 (CombatSubsystem extraction + PlaytestClient delegation wiring) and Step 4 (integration review) are complete. All changes are structural — no behaviour changed. This task is a final validation gate before marking the Wave 4 combat slice done.

Files changed in this wave:

- `java/client/src/main/java/com/shadowascent/client/CombatEncounterPhase.java` — new
- `java/client/src/main/java/com/shadowascent/client/EncounterPattern.java` — new
- `java/client/src/main/java/com/shadowascent/client/CombatEncounter.java` — new
- `java/client/src/main/java/com/shadowascent/client/CombatSubsystem.java` — new
- `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` — delegation wiring

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

On success: mark this file `status: done (date)` and update `DESIGN_WAVE4_COMBAT_SUBSYSTEM.md` Step 5 as complete.
