---
handover_type: codex_validate
milestone: Wave4
topic: boss_pattern_dispatch
status: done
created: 2026-05-08
---
# Codex Validate — Wave 4 BossPatternLibrary Dispatch

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Expected Results

| Check | Expected |
|---|---|
| `:core:compileJava` | BUILD SUCCESSFUL |
| `runRegressionTests` | 45/45 PASS — new `testBossPatternDispatch` section (3 sub-tests) |

## Wiring Notes

- `BossPatternLibrary.tick()` called in `tickBoss` only when `aiState != INTRO`
- `PatternContext` built with `Map<Integer,SimPlayer>` by slot; null for spawn/projectile stubs (both null-checked inside patterns)
- `BOSS_SCRIPTED_LOSS` event emitted if pattern returns `ServerEvent.SCRIPTED_LOSS`
- Double `tickInvincibility` for SIREN/VEIL_MAIDEN is accepted — ~2× faster expiry, still meaningful protection

## Evidence

```
BUILD SUCCESSFUL in 45s
runRegressionTests: 45/45 PASS
--- Testing Wave4 BossPatternLibrary Dispatch ---
[PASS] PASSED
```
