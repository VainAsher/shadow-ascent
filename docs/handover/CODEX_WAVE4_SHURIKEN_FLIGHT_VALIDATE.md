---
handover_type: codex_validate
milestone: Wave4
topic: shuriken_flight
status: done
created: 2026-05-08
---
# Codex Validate — Wave 4 SimShuriken Projectile Flight

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Expected Results

| Check | Expected |
|---|---|
| `:core:compileJava` | BUILD SUCCESSFUL |
| `runRegressionTests` | 46/46 PASS — new `testShurikenProjectileFlight` section (3 sub-tests) |

## New API

- `fireShuriken(String playerId, float vx, float vy)` — spawns shuriken at player centre; emits `SHURIKEN_FIRED`
- `getShurikens()` — unmodifiable view of live shurikens

## New Events

| Event | When |
|---|---|
| `SHURIKEN_FIRED` | `fireShuriken` called for alive player; data: `playerId`, `vx`, `vy` |
| `SHURIKEN_HIT` | Shuriken AABB overlaps alive enemy; data: `enemyId`, `dmg` |
| `ENEMY_DEFEATED` | Hit kills enemy; data: `killedBy=shuriken_<id>` |

## Evidence

```
BUILD SUCCESSFUL in 45s
runRegressionTests: 46/46 PASS
--- Testing Wave4 SimShuriken Projectile Flight ---
[PASS] PASSED
```
