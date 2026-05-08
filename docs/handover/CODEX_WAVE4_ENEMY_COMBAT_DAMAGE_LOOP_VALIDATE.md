---
handover_type: codex_validate
milestone: Wave4
topic: enemy_combat_damage_loop
status: done
created: 2026-05-08
---
# Codex Validate — Wave 4 Enemy Combat Damage Loop

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Expected Results

| Check | Expected |
|---|---|
| `:core:compileJava` | BUILD SUCCESSFUL |
| `:client:compileJava` | BUILD SUCCESSFUL |
| `:server:compileJava` | BUILD SUCCESSFUL |
| `runRegressionTests` | 44/44 PASS — new `testEnemyCombatDamageLoop` section (4 sub-tests) |

## New Events Emitted

| Event | When |
|---|---|
| `PLAYER_DAMAGED` | Enemy attack lands (invincibility check passes); data: `hp`, `dmg`, `byEnemy` |
| `PLAYER_DIED` | Player HP reaches 0 from enemy hit; data: `byEnemy`; `respawnTimer` set to 3.0f |
| `ENEMY_DEFEATED` | `attackEnemy()` kills enemy; data: `killedBy` |

## New API

`GameSimulator.attackEnemy(String playerId, String enemyId, int damage)` — player-originated
hit on named enemy; emits `ENEMY_DEFEATED` if the enemy dies.

## Evidence

```
BUILD SUCCESSFUL in 2m 9s
runRegressionTests: 44/44 PASS
--- Testing Wave4 Enemy Combat Damage Loop ---
[PASS] PASSED
```
