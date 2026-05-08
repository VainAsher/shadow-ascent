---
handover_type: design_doc
milestone: Wave4
topic: boss_system
status: ready-for-implementation
created: 2026-05-08
---
# Design — Wave 4 Boss System Slice

Workflow 2 (architecture lane), design record for bounded import of the boss
simulation system from `indie-ninja-adventures` into `core.simulation`.

---

## 1. Context

Wave 4 prior slices delivered all sim entity actors (SimEnemy, SimPlayer, SimInventory,
YinYangComponent, LanternComponent, EchoRecorder, InputCommand). This slice brings
in the boss simulation layer: BossAIState, BossType, SimBoss, and BossPatternLibrary.

BossPatternLibrary depends on SimPlayer (Slice 2) and SimEnemy (Slice 1) — both already
in core.simulation. The HubStateMachine and slf4j dependencies are cleanly dropped (see §5).

---

## 2. Source Files

| Donor File | Lines | Target Package | Notes |
|---|---|---|---|
| `com.indieniinja.sim.BossAIState` | 20 | `core.simulation` | Pure enum, no deps |
| `com.indieniinja.sim.BossType` | 75 | `core.simulation` | Pure enum, no deps |
| `com.indieniinja.sim.SimBoss` | 337 | `core.simulation` | Deps: PhysicsConstants + PhysicsState (done), BossType + BossAIState (same package) |
| `com.indieniinja.sim.BossPatternLibrary` | 569 | `core.simulation` | Drop HubStateMachine + slf4j (see §5). SimPlayer + SimEnemy already in core.simulation. |

Total: 4 files, ~1001 lines.

---

## 3. Layer Contract

`core.simulation` may import from:
- `core.physics` (`PhysicsConstants`, `PhysicsState`)
- `java.util.*`

No imports from `client`, `server`, or `network`.

---

## 4. Package Renames

- All files: `package com.indieniinja.sim` → `package com.shadowascent.core.simulation`
- `SimBoss`: `import com.indieniinja.physics.PhysicsConstants` → `import com.shadowascent.core.physics.PhysicsConstants`
- `SimBoss`: `import com.indieniinja.physics.PhysicsState` → `import com.shadowascent.core.physics.PhysicsState`
- `BossPatternLibrary`: remove `import com.indieniinja.world.HubStateMachine`
- `BossPatternLibrary`: remove `import org.slf4j.Logger`, `import org.slf4j.LoggerFactory`
- Same-package types (`SimPlayer`, `SimEnemy`, `BossAIState`, `BossType`, `SimBoss`) need no explicit imports

---

## 5. Slice Decisions

**HubStateMachine dropped from PatternContext:** `ctx.hub` (type `HubStateMachine`) is declared
in PatternContext but never accessed by any of the 5 pattern implementations. The field and
its constructor parameter are removed entirely. No pattern behavior is affected.

**slf4j Logger dropped:** `BossPatternLibrary` uses `log.info/debug` for 7 diagnostic lines.
No slf4j dependency exists in the project. The log statements are removed (pattern behavior
is unchanged — they were debug/info only). Diagnostic output is surfaced through the gate
instead.

**PatternContext constructor signature change:** Original 6-param constructor (players, enemies,
hub, broadcastScriptedLoss, spawnEnemy, fireProjectile) becomes 5-param (hub removed). All
other fields and interfaces retain their signatures.

---

## 6. Regression Coverage

### `testBossSystemModel`

```
Scenario: Construct SimBoss(SIREN, 0, 0). Expected: isAlive()=true, hp==maxHp, aiState=INTRO.

Scenario: takeDamage(lethal). Expected: returns true, isAlive()=false, aiState=DEAD.

Scenario: takeDamage while aiState==PHASE_TRANSITION. Expected: returns false (immune).

Scenario: SimBoss.step() advances from INTRO → IDLE after stateTimer >= INTRO_DURATION.

Scenario: BossAIState enum: all 10 values present.

Scenario: BossType enum: SIREN, ECHO_WARDEN, TIME_LEECH_LORD, MEMORY_EATER, VEIL_MAIDEN present.
Expected: BossType.fromWire("siren") == BossType.SIREN.

Scenario: BossPatternLibrary.tick with ECHO_WARDEN boss and single-player PatternContext.
Expected: returns null (no SCRIPTED_LOSS event).

Scenario: BossPatternLibrary.tick with TIME_LEECH_LORD. Expected: player lantern.value decreases.
```

---

## 7. Migration Map Updates

Add to Wave 4 table in `docs/MIGRATION_MAP.md`:

| `BossAIState.java` | `core.simulation` | boss AI state enum | S | Low | done |
| `BossType.java` | `core.simulation` | boss type definitions | S | Low | done |
| `SimBoss.java` | `core.simulation` | server-side boss simulation entity | M | Low | done |
| `BossPatternLibrary.java` | `core.simulation` | boss psychological pattern dispatch | M | Low | done |

---

## 8. Implementation Order

1. Create `BossAIState.java`
2. Create `BossType.java`
3. Create `SimBoss.java`
4. Create `BossPatternLibrary.java`
5. Compile check
6. Add `testBossSystemModel` regression section
7. Run full gate
8. Create `CODEX_WAVE4_BOSS_SYSTEM_VALIDATE.md`
9. Update `MIGRATION_MAP.md`, `CURRENT_STATE.md`, `IMPLEMENTATION_BACKLOG.md`, `handover/README.md`
