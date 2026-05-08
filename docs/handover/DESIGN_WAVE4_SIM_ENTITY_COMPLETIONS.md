---
handover_type: design
milestone: Wave4
topic: sim_entity_completions
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 Sim Entity Completions

Bounded import of the remaining sim entity types from `com.indieniinja.sim` into
`com.shadowascent.core.simulation`. Completes the entity layer for NPC patrol,
echo playback, moving platforms, pickups, projectiles, and portals.

---

## Source Files (donor)

| Donor file | Package | Notable deps |
|---|---|---|
| `ReplayPlayer.java` | `com.indieniinja.sim` | `com.indieniinja.network.InputCommand` |
| `SimNPC.java` | `com.indieniinja.sim` | `com.indieniinja.physics.PhysicsState` |
| `SimEcho.java` | `com.indieniinja.sim` | `InputCommand` (network), `ReplayPlayer` (same-pkg) |
| `SimMovingPlatform.java` | `com.indieniinja.sim` | none |
| `SimPickup.java` | `com.indieniinja.sim` | `com.indieniinja.physics.PhysicsConstants` |
| `SimShuriken.java` | `com.indieniinja.sim` | none |
| `SimPortal.java` | `com.indieniinja.sim` | `com.indieniinja.network.PortalState` |

---

## Package Renames

| Donor | Clean-start |
|---|---|
| `com.indieniinja.sim` | `com.shadowascent.core.simulation` |
| `com.indieniinja.physics.PhysicsState` | `com.shadowascent.core.physics.PhysicsState` |
| `com.indieniinja.physics.PhysicsConstants` | `com.shadowascent.core.physics.PhysicsConstants` |
| `com.indieniinja.network.InputCommand` | same-package (no import needed) |

---

## Design Decisions

**PortalState dropped from SimPortal:**
`PortalState` is `com.indieniinja.network.PortalState` — a network wire DTO that violates
the core layer contract. `toState()` is replaced with `toMap()` returning
`Map<String, Object>` using the same field keys as `PortalState.toMap()`. No portal
simulation logic changes.

**ReplayPlayer file I/O:**
`ReplayPlayer.load(Path)` uses only `java.io.*` and `java.nio.file.*` from the standard
library — acceptable in `core.simulation`. No external parser dependencies.

**ECS base class status:**
None of the 7 files extend or implement ECS base classes (no `Component`/`SerializableComponent`
in scope). Donor code is already clean on this front.

**SimPortal.canPlayerEnter:**
References `SimPlayer.unlockedAbilities` — same-package reference, no import needed.

---

## Layer Contract Verification

All 7 files import only:
- `java.util.*`, `java.io.*`, `java.nio.file.*`
- `com.shadowascent.core.physics.PhysicsState`
- `com.shadowascent.core.physics.PhysicsConstants`
- Same-package types (no import needed)

No imports from `client`, `server`, or `network`.

---

## Regression Section

Add `testSimEntityCompletions` to `RegressionTest.java`:

- `ReplayPlayer.fromInputSequence`: 1 command → isDone(0)=false, isDone(1)=true, inputsForTick(0) non-empty
- `SimNPC` patrol: step with edgeAhead=true flips facing and sets wait timer; isInteractable within radius
- `SimEcho` playback: step() advances tickCursor; completed after log exhausted; recall() fails when failed=true
- `SimMovingPlatform`: step() oscillates between bounds; isStandingOn AABB overlap
- `SimPickup`: tick() × ticksRemaining→alive=false; overlaps AABB test; canBeCollectedBy scoping
- `SimShuriken`: fields correct after construction; alive=true; damage clamped to ≥1
- `SimPortal`: step() advances pulseTimer; canInteract radius; toMap() contains portal_id; canPlayerEnter ability gate

---

## Files to Create

All in `java/core/src/main/java/com/shadowascent/core/simulation/`:

1. `ReplayPlayer.java`
2. `SimNPC.java`
3. `SimEcho.java`
4. `SimMovingPlatform.java`
5. `SimPickup.java`
6. `SimShuriken.java`
7. `SimPortal.java`

Prior regression count: 32. After: 33.
