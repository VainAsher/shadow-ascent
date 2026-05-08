---
handover_type: design_doc
milestone: Wave4
topic: sim_player_actor
status: ready-for-implementation
created: 2026-05-08
---
# Design — Wave 4 SimPlayer Actor Slice

Workflow 2 (architecture lane), design record for bounded import of SimPlayer and
its M4 narrative components from `indie-ninja-adventures` into `core.simulation`.

---

## 1. Context

Wave 4 sim entity actor slice (prior) delivered SimEnemy + SimInventory + ItemDatabase.
This slice delivers the player-side counterpart: SimPlayer and the four supporting types
it depends on — InputCommand, YinYangComponent, LanternComponent, EchoRecorder.

SimPlayer is the authoritative server-side player simulation state record. In Phase B
movement is client-authoritative (positions arrive via INPUT messages). Phase C will
drive SimPlayer from the full Java mechanics pipeline (applyPlayerInput in GameSimulator).

---

## 2. Source Files

| Donor File | Lines | Target Package | Notes |
|---|---|---|---|
| `com.indieniinja.network.InputCommand` | 120 | `core.simulation` | Pure value type — boolean input flags + frame. java.util.* only. Placed in core.simulation (not network layer) because it's a data snapshot consumed by SimPlayer/EchoRecorder, not a transport concern in this repo. |
| `com.indieniinja.sim.YinYangComponent` | 117 | `core.simulation` | Drop `extends Component implements SerializableComponent` (ECS base not in this repo). java.util.Map only. |
| `com.indieniinja.sim.LanternComponent` | 121 | `core.simulation` | Drop `extends Component implements SerializableComponent`. java.util.Map only. |
| `com.indieniinja.sim.EchoRecorder` | 55 | `core.simulation` | Depends on InputCommand (same package after rename). Drop explicit import. |
| `com.indieniinja.sim.SimPlayer` | 316 | `core.simulation` | Inline GameConfig constants (no GameConfig class in this repo). Update physics imports. |

Total: 5 files, ~729 lines.

---

## 3. Layer Contract

`core.simulation` may import from:
- `core.physics` (`PhysicsConstants`, `PhysicsState`)
- `java.util.*`

No imports from `client`, `server`, or `network`.

---

## 4. Package Renames

- `package com.indieniinja.network` → `package com.shadowascent.core.simulation` (InputCommand)
- `package com.indieniinja.sim` → `package com.shadowascent.core.simulation` (all others)
- `import com.indieniinja.physics.PhysicsConstants` → `import com.shadowascent.core.physics.PhysicsConstants`
- `import com.indieniinja.physics.PhysicsState` → `import com.shadowascent.core.physics.PhysicsState`
- Same-package types need no explicit imports

---

## 5. Slice Decisions

**InputCommand in core.simulation (not a network package):** InputCommand is a pure data
snapshot of button state. In Shadow Ascent, PlaytestClient drives movement directly — there
is no wire protocol yet. Placing it in core.simulation keeps the layer contract clean and
avoids a premature `core.network` sub-package.

**Drop ECS base classes from YinYangComponent/LanternComponent:** `Component` and
`SerializableComponent` are from `com.indieniinja.core` ECS registry which is not imported
in this wave. The components are pure value types in practice — they hold state and have
decay/query methods. The `toMap()`/`fromMap()` interface contract is retained verbatim.

**Inline GameConfig constants into SimPlayer:** Shadow Ascent uses `PhysicsConstants` for
physics tuning. GameConfig carries gameplay balance. Rather than creating a parallel
`SimPlayerConfig` class, constants are inlined with their values directly in SimPlayer
(the delegation comment is removed). A future `SimPlayerConfig` extraction can split them
if needed.

**`@Override` annotations removed** from YinYangComponent/LanternComponent `toMap()`
methods since `SerializableComponent` interface is dropped.

---

## 6. GameConfig Constants Inlined (SimPlayer)

| SimPlayer field | GameConfig value |
|---|---|
| MELEE_DAMAGE | 1 |
| ARMED_MELEE_DAMAGE | 2 |
| MELEE_ACTIVE_TICKS | 8 |
| MELEE_COOLDOWN | 0.4f |
| MELEE_REACH | 48f |
| MELEE_HEIGHT | 40f |
| SHURIKEN_SPEED | 10f |
| SHURIKEN_COOLDOWN | 0.35f |
| SHURIKEN_DAMAGE | 1 |
| SHURIKEN_MAX_AMMO | 5 |
| PARRY_WINDOW | 0.10f |
| BLOCK_DAMAGE_MULT | 0.35f |
| PLAYER_INVINCIBILITY_TICKS (used in takeDamage) | 30 |

---

## 7. Regression Coverage

### `testSimPlayerActorModel`

```
Scenario: Construct SimPlayer, verify initial state.
Expected: isAlive()=true, health==maxHealth, yinYang neutral, lantern=0.8.

Scenario: takeDamage(1). Expected: health reduced, invincibilityTicks set.
Scenario: takeDamage while invincible. Expected: no effect.

Scenario: addXp to reach level 2. Expected: levelsGained=1, "double_jump" in unlockedAbilities.
Scenario: addXp across level 3. Expected: "dash" unlocked.

Scenario: YinYangComponent — absorbYin/absorbYang, isBalanced, hasYinSight, hasYangSurge.
Scenario: YinYangComponent — decay. Expected: values approach neutral.
Scenario: YinYangComponent toMap/fromMap round-trip.

Scenario: LanternComponent — initial value=0.8, isHigh()=true.
Scenario: LanternComponent.onDamage() reduces value by DAMAGE_DECAY.
Scenario: LanternComponent toMap/fromMap round-trip.

Scenario: EchoRecorder — record 3 inputs, snapshot returns 3 ordered oldest→newest.
Scenario: EchoRecorder.clear() resets size to 0.

Scenario: InputCommand.neutral(5).frame == 5, all booleans false.
Scenario: InputCommand toMap/fromMap round-trip preserves all boolean fields.
```

---

## 8. Migration Map Updates

Add to Wave 4 table in `docs/MIGRATION_MAP.md`:

| `InputCommand.java` | `core.simulation` | input snapshot value type | S | Low | done |
| `YinYangComponent.java` | `core.simulation` | Yin/Yang emotional balance component | S | Low | done |
| `LanternComponent.java` | `core.simulation` | Lantern clarity component | S | Low | done |
| `EchoRecorder.java` | `core.simulation` | 10s input ring buffer for echo playback | S | Low | done |
| `SimPlayer.java` | `core.simulation` | server-side player simulation state | M | Low | done |

---

## 9. Implementation Order

1. Create `InputCommand.java`
2. Create `YinYangComponent.java`
3. Create `LanternComponent.java`
4. Create `EchoRecorder.java`
5. Create `SimPlayer.java`
6. Compile check
7. Add `testSimPlayerActorModel` regression section
8. Run full gate
9. Create `CODEX_WAVE4_SIM_PLAYER_ACTOR_VALIDATE.md`
10. Update `MIGRATION_MAP.md`, `CURRENT_STATE.md`, `IMPLEMENTATION_BACKLOG.md`, `handover/README.md`
