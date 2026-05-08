---
handover_type: design
milestone: Wave4
topic: player_input_controller
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 PlayerInputController

Bounded extraction of `GameSimulator.applyPlayerInput()` (~450 lines) into a
standalone `PlayerInputController` class in `core.simulation`.

---

## Source

`com.indieniinja.sim.GameSimulator.applyPlayerInput(SimPlayer, InputCommand)` — lines 992–1442.

---

## Scope

All player movement state transitions, combat timers, stance logic, ninjutsu,
teleport, wall-slide, and animation-state derivation — portable as a pure
`SimPlayer` + `InputCommand` → state mutation with no GameSimulator dependency.

---

## Adaptations

| Dependency | Adaptation |
|---|---|
| `emitNoise(sp, level)` | Inlined as private static: sets `sp.noiseLevel` and `sp.noiseRadius` |
| `updateTraversalContext(sp, cmd, jp)` | Stubbed to return `false` — ledge/water traversal is Phase 6 scope |
| `syncWeaponStateForStance(sp)` | Inlined as private static |
| `resolveArmedWeaponState(sp)` | Inlined as private static |
| `stanceDashMult / stanceSpeedMult / stanceWallJumpXMult` | Inlined as private static methods |
| `applyWallSlide(sp, p)` | Inlined as private static |
| `applyWaterTraversalTuning(sp, cmd, p)` | No-op stub — water traversal is future scope |
| `applyTeleportArrivalEffects(sp, cx, cy)` | Simplified: thunder step emits noise; no enemy stun (no enemy list access) |
| `resolveTeleportType / teleportCooldownMult` | Inlined as private static |
| `spatialHash.candidates(...)` | Replaced with `TileQuery` FI: `isBlocked(x,y,w,h)→boolean`; `null` → always unblocked |
| `GameConfig` constants | Inlined as private static final fields |
| `slf4j` log calls | Dropped — no slf4j dep in project |

---

## Layer Contract

- Imports: `PhysicsConstants`, `PhysicsState` (`core.physics`); same-package types; `java.util.Locale`
- No imports from `client`, `server`, `network`

---

## Regression Section

Add `testPlayerInputController` to `RegressionTest.java`:

- Neutral command: no state mutation (idle anim, no dash)
- Ground jump: `jumpCount=1`, `p.vy < 0`
- Double-jump (airborne, `jumpCount=1`): `jumpCount=2`
- Wall-jump (on wall, airborne): `p.vy < 0`, `wallJumpLockTimer > 0`
- Dash: `isDashing=true`; after `DASH_DURATION` ticks → cooldown set, `isDashing=false`
- Attack: `isAttacking=true`; after `MELEE_ACTIVE_TICKS` ticks → `isAttacking=false`
- Ninjutsu release: fires only when mana >= `NINJUTSU_MANA_COST`
- Stance switch: `stanceMode` flips; `yinYang` component updated
- Teleport phase entry: `teleportPhaseMode=true` on first press

---

## Files to Create

In `java/core/src/main/java/com/shadowascent/core/simulation/`:

1. `PlayerInputController.java`

Prior regression count: 34. After: 35.
