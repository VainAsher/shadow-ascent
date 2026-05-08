---
handover_type: codex_validate
milestone: Wave4
topic: sim_player_actor
status: validated
created: 2026-05-08
---
# Codex Validate — Wave 4 SimPlayer Actor Slice

Gate evidence for the bounded Wave 4 import of SimPlayer and its M4 narrative
components from `indie-ninja-adventures` into `com.shadowascent.core.simulation`.

---

## Gate Result

```
BUILD SUCCESSFUL in 2m 11s
[PASS] All regression tests PASSED  (31/31)
```

Full command:
```
./gradlew clean :core:compileJava :client:compileJava :server:compileJava
         runDataContractDiagnostics runWorldgenDiagnostics
         runWorldSimulationDiagnostics runRegressionTests
```

---

## Files Created

| File | Package | Lines | Source |
|---|---|---|---|
| `InputCommand.java` | `core.simulation` | 120 | `com.indieniinja.network.InputCommand` |
| `YinYangComponent.java` | `core.simulation` | 76 | `com.indieniinja.sim.YinYangComponent` |
| `LanternComponent.java` | `core.simulation` | 72 | `com.indieniinja.sim.LanternComponent` |
| `EchoRecorder.java` | `core.simulation` | 48 | `com.indieniinja.sim.EchoRecorder` |
| `SimPlayer.java` | `core.simulation` | 225 | `com.indieniinja.sim.SimPlayer` |

Total: 5 files.

---

## Regression Section Added

### `testSimPlayerActorModel` — [PASS]

- Construct `SimPlayer("player_01", 0, 100f, 200f)`: `isAlive()=true`, `health==maxHealth`, `yinYang` at NEUTRAL, `lantern.value=0.8`
- `takeDamage(1)`: health reduced, `invincibilityTicks > 0`
- `takeDamage` while invincible: no effect
- `addXp(50)` → level 2: `levelsGained=1`, `"double_jump"` in `unlockedAbilities`
- `addXp` → level 3: `"dash"` unlocked
- `YinYangComponent`: `absorbYin(0.4)` → `hasYinSight()=true`; `absorbYang(0.1)` → `hasYangSurge()=false`; `!isBalanced()`; `decay(1.0)` reduces yin toward neutral; `toMap()`/`fromMap()` round-trip
- `LanternComponent`: `isHigh()=true` at init; `onDamage()` reduces value; `toMap()`/`fromMap()` round-trip
- `EchoRecorder`: record 3 inputs → `size()==3`, `snapshot()` ordered frame 1→3; `clear()` → `size()==0`
- `InputCommand.neutral(5)`: `frame==5`, all booleans false; `toMap()`/`fromMap()` round-trip preserves jump+left flags

---

## Package Renames Applied

- `InputCommand`: `com.indieniinja.network` → `com.shadowascent.core.simulation`
- `YinYangComponent`, `LanternComponent`, `EchoRecorder`: `com.indieniijah.sim` → `com.shadowascent.core.simulation`
- `SimPlayer`: `com.indieniinja.sim` → `com.shadowascent.core.simulation`; physics imports updated

---

## Layer Contract Verification

All 5 files import only `java.util.*` and (for SimPlayer/EchoRecorder) same-package types.
`SimPlayer` imports `com.shadowascent.core.physics.PhysicsConstants` + `PhysicsState` (allowed).
No imports from `client`, `server`, or `network`.

---

## Design Decisions

**InputCommand placed in core.simulation:** Pure data snapshot — no transport I/O in this repo.
Keeps the layer contract clean without a premature `core.network` sub-package.

**ECS base classes dropped:** `Component` and `SerializableComponent` from `com.indieniinja.core`
are not imported in this wave. `YinYangComponent`/`LanternComponent` are pure value types —
the `toMap()`/`fromMap()` interface contract is retained verbatim, `@Override` annotations removed.

**GameConfig constants inlined:** 13 gameplay balance constants inlined with their literal values
directly into SimPlayer. A future `SimPlayerConfig` extraction can split them if needed.

---

## Prior Test Count

30 tests prior to this slice. Now 31/31.
