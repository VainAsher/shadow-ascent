---
handover_type: codex_validate
milestone: Wave4
topic: sim_entity_completions
status: validated
created: 2026-05-08
---
# Codex Validate — Wave 4 Sim Entity Completions

Gate evidence for the bounded Wave 4 import of remaining sim entity types from
`com.indieniinja.sim` into `com.shadowascent.core.simulation`.

---

## Gate Result

```
BUILD SUCCESSFUL in 45s
[PASS] All regression tests PASSED  (33/33)
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
| `ReplayPlayer.java` | `core.simulation` | 114 | `com.indieniinja.sim.ReplayPlayer` |
| `SimNPC.java` | `core.simulation` | 86 | `com.indieniinja.sim.SimNPC` |
| `SimEcho.java` | `core.simulation` | 107 | `com.indieniinja.sim.SimEcho` |
| `SimMovingPlatform.java` | `core.simulation` | 50 | `com.indieniinja.sim.SimMovingPlatform` |
| `SimPickup.java` | `core.simulation` | 65 | `com.indieniinja.sim.SimPickup` |
| `SimShuriken.java` | `core.simulation` | 48 | `com.indieniinja.sim.SimShuriken` |
| `SimPortal.java` | `core.simulation` | 60 | `com.indieniinja.sim.SimPortal` |

Total: 7 files.

---

## Regression Section Added

### `testSimEntityCompletions` — [PASS]

- `ReplayPlayer.fromInputSequence(42L, 0, [cmd])`: isDone(0)=false, isDone(1)=true, inputsForTick(0) non-empty, seed=42
- `SimNPC` patrol: step() animState="walk"; edgeAhead flips facing; isInteractable=true within radius, false when distant
- `SimEcho` step(): ticksPlayed=1, completed=true, active=false after single-tick replay; recall() returns false when failed=true
- `SimMovingPlatform`: x increases after step(); vx goes negative at right bound; isStandingOn returns true/false correctly
- `SimPickup`: alive=true at start; tick()×3 on ticksRemaining=3 → alive=false; overlaps AABB correct; canBeCollectedBy scoping
- `SimShuriken`: alive=true, damage=1 (clamped); damagesPlayers flag correct
- `SimPortal`: pulseTimer advances after step(); canInteract at center=true, at 1000px=false; toMap() has portal_id/portal_type; canPlayerEnter open portal=true; gated portal denied without ability, allowed after adding "dash"

---

## Design Decisions

**PortalState dropped from SimPortal:** `PortalState` is `com.indieniinja.network.PortalState` — a
network wire DTO incompatible with the core layer contract. `toState()` replaced with `toMap()`
returning `Map<String, Object>` with identical field keys. No simulation logic changed.

**ReplayPlayer file I/O:** `ReplayPlayer.load(Path)` uses only `java.io.*` and `java.nio.file.*`
from the standard library — acceptable in `core.simulation`.

**SimNPC `@deprecated` accessor aliases dropped:** `x()` and `y()` kept (callers use them for
proximity math); the `@deprecated` Javadoc comment removed since it adds no value in this slice.

---

## Layer Contract Verification

All 7 files import only `java.util.*`, `java.io.*`, `java.nio.file.*`, and same-package types.
`SimNPC` imports `PhysicsState`; `SimPickup` imports `PhysicsConstants` (both `core.physics` — allowed).
No imports from `client`, `server`, or `network`.

---

## Prior Test Count

32 tests prior to this slice. Now 33/33.
