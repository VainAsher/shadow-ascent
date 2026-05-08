---
handover_type: codex_validate
milestone: Wave4
topic: player_input_controller
status: validated
created: 2026-05-08
---
# Codex Validate — Wave 4 PlayerInputController

Gate evidence for the bounded extraction of `GameSimulator.applyPlayerInput()` into
a standalone `PlayerInputController` class in `com.shadowascent.core.simulation`.

---

## Gate Result

```
BUILD SUCCESSFUL in 43s
[PASS] All regression tests PASSED  (35/35)
```

Full command:
```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava
             runDataContractDiagnostics runWorldgenDiagnostics
             runWorldSimulationDiagnostics runRegressionTests
```

---

## Files Created

| File | Package | Lines | Source |
|---|---|---|---|
| `PlayerInputController.java` | `core.simulation` | 355 | `GameSimulator.applyPlayerInput` (lines 992–1442) |

Total: 1 file.

---

## Regression Section Added

### `testPlayerInputController` — [PASS]

- Neutral command on grounded player → `animState="idle"`, `isDashing=false`
- Jump on ground → `jumpCount=1`, `p.vy < 0`
- Double-jump while airborne (`jumpCount=1`) → `jumpCount=2`
- Wall-jump (on wall, airborne) → `wallJumpLockTimer > 0`, `p.vy < 0`
- Dash press → `isDashing=true`; after DASH_DURATION ticks → `isDashing=false`, `dashCooldown > 0`
- Attack press → `isAttacking=true`; after MELEE_ACTIVE_TICKS → `isAttacking=false`
- Ninjutsu release with 0 mana → no cast; with sufficient mana → `ninjutsuCasting=true`
- Stance switch press → `stanceMode` flips yin→yang
- Teleport press → `teleportPhaseMode=true`, `teleportType` non-null
- Yang out-of-flow teleport → `teleportType="thunder"`

---

## Adaptations

| Dependency | Adaptation |
|---|---|
| `emitNoise(sp, level)` | Inlined to `sp.noiseLevel`/`sp.noiseRadius` mutation |
| `updateTraversalContext` | Stubbed to `false` — ledge/water traversal is Phase 6 scope |
| `syncWeaponStateForStance` / `resolveArmedWeaponState` | Inlined as private statics |
| `stanceDashMult` / `stanceSpeedMult` / `stanceWallJumpXMult` | Inlined as private statics |
| `applyWallSlide` | Inlined as private static |
| `applyWaterTraversalTuning` | No-op stub |
| `applyTeleportArrivalEffects` | Simplified: thunder emits noise; no enemy stun |
| `spatialHash.candidates(...)` | Replaced with `TileQuery` FI (`null` = always unblocked) |
| `GameConfig` constants | Inlined as private static finals |
| `slf4j` log calls | Dropped |

**Test note:** Default `SimPlayer` has balanced `yin==yang==0.5`, so teleport in a fresh
player triggers Flow → "harmonic" type. The yang-path test explicitly unbalances with
`absorbYang(0.3f)` to verify the non-flow code path.

---

## Layer Contract Verification

Imports: `PhysicsConstants`, `PhysicsState` (`core.physics`); same-package types; `java.util.Locale`.
No imports from `client`, `server`, or `network`.

---

## Prior Test Count

34 tests prior to this slice. Now 35/35.
