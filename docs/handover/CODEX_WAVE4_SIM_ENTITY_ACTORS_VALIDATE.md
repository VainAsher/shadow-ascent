---
handover_type: codex_validate
milestone: Wave4
topic: sim_entity_actors
status: validated
created: 2026-05-08
---
# Codex Validate — Wave 4 Sim Entity Actor Slice

Gate evidence for the bounded Wave 4 import of sim entity types from
`indie-ninja-adventures` into `com.shadowascent.core.simulation`.

---

## Gate Result

```
BUILD SUCCESSFUL in 1m 13s
[PASS] All regression tests PASSED  (30/30)
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
| `EnemyAIState.java` | `core.simulation` | 20 | `com.indieniinja.sim.EnemyAIState` |
| `EnemyAwarenessState.java` | `core.simulation` | 16 | `com.indieniinja.sim.EnemyAwarenessState` |
| `SimEnemy.java` | `core.simulation` | 141 | `com.indieniinja.sim.SimEnemy` |
| `ItemDatabase.java` | `core.simulation` | 153 | `com.indieniinja.sim.ItemDatabase` |
| `SimInventory.java` | `core.simulation` | 196 | `com.indieniinja.sim.SimInventory` |

Total: 5 files.

---

## Regression Sections Added

### `testSimEnemyEntityModel` — [PASS]

- Construct `SimEnemy(goblin)`: `isAlive()=true`, `aiState=PATROL`, `hp==maxHp`, `awarenessState=UNAWARE`
- Lethal `takeDamage(999)`: returns `true`, `isAlive()=false`, `aiState=DEAD`
- Non-lethal `takeDamage(2)` on 20-HP enemy: returns `false`, `aiState=STUNNED`, `hp < maxHp`
- `knockDown(999)`: `knockedOut=true`, `hp` restored to `maxHp`, `aiState=DEAD`
- `EnemyAIState` enum: all 8 values present (IDLE, PATROL, CHASE, ATTACK, FLEE, GUARD, STUNNED, DEAD)
- `EnemyAwarenessState` enum: all 4 values present (UNAWARE, SUSPICIOUS, SEARCHING, ALERTED)

### `testSimInventoryRuntime` — [PASS]

- `addItem("health_potion", 3)`: `countItem==3`, `hasItem(1)=true`
- `removeItem("health_potion", 2)`: `countItem==1`
- `addItem("item_does_not_exist_xyz", 1)`: returns `false`
- `equipItem("weapon_sword")`: `equippedWeapon=="weapon_sword"`; `unequipItem`: `equippedWeapon==null`
- `addCurrency(100)` / `removeCurrency(40)`: balance=60; overdraw returns false
- `ItemDatabase.get("weapon_sword")`: non-null, `type=="weapon"`
- `ItemDatabase.get("__nonexistent__")`: returns null
- `toMap()` / `fromMap()` round-trip: currency=250, potion count=5, equippedWeapon="weapon_dagger" preserved

---

## Package Rename Applied

- `package com.indieniinja.sim` → `package com.shadowascent.core.simulation`
- `import com.indieniinja.physics.PhysicsState` → `import com.shadowascent.core.physics.PhysicsState`
- Same-package types (`EnemyAIState`, `EnemyAwarenessState`, `ItemDatabase`) need no explicit imports

---

## Layer Contract Verification

`SimInventory` and `ItemDatabase` import only `java.util.*`.
`SimEnemy` imports `com.shadowascent.core.physics.PhysicsState` (allowed).
No imports from `client`, `server`, or `network`.

---

## Design Decisions

**SimPlayer deferred:** `SimPlayer` requires `InputCommand` (network dep), `YinYangComponent`,
`LanternComponent`, `EchoRecorder` — deferred to the next "SimPlayer actor + M4 narrative
components" slice.

**ItemDatabase quest items aligned with quests.json:** All `quest_item` entries (`forest_key`,
`relic`, `crystal`, etc.) match existing `quests.json` objective targets — available for
quest ecology engine item-collect opportunity generation.

**`SimInventory.fromMap` `@SuppressWarnings("unchecked")` retained:** Raw `Map<?,?>` cast
required for JSON deserialization compatibility; the cast is safe given the Map structure
guaranteed by `toMap()`.

**`ItemDatabase.reload()` kept:** Allows test/tool overrides of the static catalog without
restart; no callers yet but the hook is useful for data-driven tooling.

---

## Prior Test Count

28 tests prior to this slice. Now 30/30.
