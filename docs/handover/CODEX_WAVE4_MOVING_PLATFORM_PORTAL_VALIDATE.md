---
handover_type: codex_validate
milestone: Wave4
topic: moving_platform_portal
status: done
created: 2026-05-08
---
# Codex Validate — Wave 4 SimMovingPlatform + SimPortal

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Expected Results

| Check | Expected |
|---|---|
| `:core:compileJava` | BUILD SUCCESSFUL |
| `runRegressionTests` | 47/47 PASS — new `testMovingPlatformAndPortal` section (3 sub-tests) |

## New APIs

- `addMovingPlatform(id, x, y, w, h, left, right, speed)` — registers oscillating platform
- `addPortal(id, type, destId, x, y, requiredAbility)` — registers portal
- `getMovingPlatforms()` / `getPortals()` — unmodifiable queries

## Behaviour Notes

- Platform carry: `step()` advances x by `vx`, bounces at bounds; players standing on top are carried by the delta each tick
- Portal one-shot: `isActive` set to `false` on first activation — prevents repeat `PORTAL_ACTIVATED` events
- Ability gate: `canPlayerEnter(player)` checks `player.unlockedAbilities`; no event if ability missing

## Evidence

```
BUILD SUCCESSFUL in 45s
runRegressionTests: 47/47 PASS
--- Testing Wave4 SimMovingPlatform + SimPortal ---
[PASS] PASSED
```
