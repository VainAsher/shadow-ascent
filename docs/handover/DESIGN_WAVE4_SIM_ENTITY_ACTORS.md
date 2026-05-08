---
handover_type: design_doc
milestone: Wave4
topic: sim_entity_actors
status: ready-for-implementation
created: 2026-05-08
---
# Design — Wave 4 Sim Entity Actor Slice

Workflow 2 (architecture lane), design record for bounded import of sim entity types
from `indie-ninja-adventures` donor repo into `core.simulation`.

---

## 1. Context

Wave 4 imported physics primitives (`TileType`, `TileRect`, `SpatialHash`) and the
`WorldSimulationTick` scaffold (M5). The next Wave 4 slice brings in the entity actor
models that the full simulation tick and regional streaming encounter systems depend on:

- `SimEnemy` — server-side enemy entity (patrol/chase AI state, combat stats, awareness FSM)
- `SimInventory` — 20-slot player inventory runtime with equipment and consumable slots
- Supporting types: `EnemyAIState`, `EnemyAwarenessState` enums; `ItemDatabase` catalog

`SimPlayer` is NOT in this slice — it requires additional deps (`InputCommand`, `YinYangComponent`,
`LanternComponent`, `EchoRecorder`) that warrant their own scoped slice.

---

## 2. Source Files

| Donor File | Lines | Target Package | Notes |
|---|---|---|---|
| `com.indieniinja.sim.EnemyAIState` | 21 | `core.simulation` | Pure enum, no deps |
| `com.indieniinja.sim.EnemyAwarenessState` | 16 | `core.simulation` | Pure enum, no deps |
| `com.indieniinja.sim.SimEnemy` | 138 | `core.simulation` | Deps: `PhysicsState` (done) + 2 enums above |
| `com.indieniinja.sim.ItemDatabase` | 164 | `core.simulation` | Pure static catalog, only `java.util.*` |
| `com.indieniinja.sim.SimInventory` | 203 | `core.simulation` | Deps: `ItemDatabase` only |

Total: 5 files, ~542 lines. All deps are within this slice or already in `core.physics`.

---

## 3. Layer Contract

`core.simulation` may import from:
- `core.physics` (`PhysicsState`)
- `java.util.*`

No imports from `client`, `server`, or `network`. `InputCommand` (network) is explicitly
excluded — it lives in the server module and is out of scope for this slice.

---

## 4. Package Rename

All files: `package com.indieniinja.sim` → `package com.shadowascent.core.simulation`

Import rewrites:
- `import com.indieniinja.physics.PhysicsState` → `import com.shadowascent.core.physics.PhysicsState`
- Same-package types (EnemyAIState, EnemyAwarenessState, ItemDatabase) need no explicit import

---

## 5. Slice Decisions

**SimEnemy included:** Immediately useful for M6 regional streaming encounter anchor
resolution and future `WorldSimulationTick` enemy pressure modeling.

**SimInventory + ItemDatabase included:** Self-contained pair (ItemDatabase is the only
dependency of SimInventory). The `ItemDatabase` quest-item entries (`forest_key`, `relic`,
`crystal`, etc.) align with existing `quests.json` objective targets — useful when
the quest ecology engine generates item-collect opportunities.

**SimPlayer deferred:** Requires `InputCommand` (network dep, server module scope),
`YinYangComponent`, `LanternComponent`, `EchoRecorder` — all safe to import but better
grouped as a single "SimPlayer actor + M4 narrative components" slice next session.

**No `GameConfig` constants substitution needed:** `SimEnemy` and `SimInventory` do not
reference `GameConfig` — those constants are only in `SimPlayer` (deferred).

---

## 6. Regression Coverage

Two new sections in `RegressionTest.java`:

### `testSimEnemyEntityModel`

```
Scenario: Construct SimEnemy, verify initial state.
Expected: isAlive()=true, aiState=PATROL, hp==maxHp.

Scenario: Apply lethal damage. Expected: takeDamage returns true, isAlive()=false, aiState=DEAD.

Scenario: Apply non-lethal damage to high-HP enemy.
Expected: aiState=STUNNED, hp reduced.

Scenario: Apply unarmed knockDown to enemy. Expected: knockedOut=true, hp restored to maxHp, aiState=DEAD.

Scenario: EnemyAIState enum values present: IDLE, PATROL, CHASE, ATTACK, FLEE, GUARD, STUNNED, DEAD.
Scenario: EnemyAwarenessState enum values present: UNAWARE, SUSPICIOUS, SEARCHING, ALERTED.
```

### `testSimInventoryRuntime`

```
Scenario: Add stackable item (health_potion), verify countItem, hasItem.
Expected: countItem == 3, hasItem(1)=true.

Scenario: Remove items, verify stack reduction.
Expected: countItem == 1 after removing 2.

Scenario: Add non-existent itemId. Expected: addItem returns false.

Scenario: Equip weapon, verify equippedWeapon field. Unequip, verify null.

Scenario: Add currency, removeCurrency. Expected: balance tracking correct.

Scenario: ItemDatabase.get() for known item. Expected: non-null ItemDef with correct type.
Scenario: ItemDatabase.get() for unknown item. Expected: null.

Scenario: toMap() / fromMap() round-trip. Expected: currency + slot contents preserved.
```

---

## 7. Migration Map Updates

Add to Wave 4 table in `docs/MIGRATION_MAP.md`:

| `EnemyAIState.java` | `core.simulation` | enemy AI state enum | S | Low | done |
| `EnemyAwarenessState.java` | `core.simulation` | enemy awareness tier enum | S | Low | done |
| `SimEnemy.java` | `core.simulation` | server-side enemy entity model | S | Low | done |
| `ItemDatabase.java` | `core.simulation` | static item definition catalog | S | Low | done |
| `SimInventory.java` | `core.simulation` | 20-slot player inventory runtime | S | Low | done |

---

## 8. Implementation Order

1. Create `EnemyAIState.java`
2. Create `EnemyAwarenessState.java`
3. Create `SimEnemy.java`
4. Create `ItemDatabase.java`
5. Create `SimInventory.java`
6. Compile check
7. Add regression test sections
8. Run full gate
9. Create `CODEX_WAVE4_SIM_ENTITY_ACTORS_VALIDATE.md`
10. Update `MIGRATION_MAP.md`, `CURRENT_STATE.md`, `IMPLEMENTATION_BACKLOG.md`
