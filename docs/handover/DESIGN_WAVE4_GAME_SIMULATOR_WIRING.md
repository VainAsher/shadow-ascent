---
handover_type: design
milestone: Wave4
topic: game_simulator_wiring
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 GameSimulator Sim-Entity Wiring

Bounded server-authoritative simulation coordinator. Wires together all
`core.simulation` entities already imported in prior Wave 4 slices:
`SimPlayer`, `SimEnemy`, `SimBoss`, `SimNPC`, `SimPickup`, `PlayerInputController`.

NOT a full port of the donor `GameSimulator` monolith (XL / High risk — deferred per
MIGRATION_MAP.md). This slice delivers a clean, testable coordinator that demonstrates
the wiring contract and closes the "authoritative simulation core" gap at bounded scope.

---

## Scope

### In scope

- `SimEvent` record — typed simulation event envelope emitted by the coordinator
- `GameSimulator` — entity registry + tick coordinator + event drain API
- Regression test: `testGameSimulatorEntityWiring` (7 sub-tests; 41/41 target)
- MIGRATION_MAP.md entry updated from `queued` to `done` for GameSimulator slice

### Out of scope

- Full donor `GameSimulator.java` port (must be sliced; this IS the first slice)
- Networked session management, room instancing, anti-cheat
- Full enemy physics (gravity, wall detection) — bounded AI tick only
- `BossPatternLibrary` dispatch — boss tick is phase-tracking only in this slice
- Inventory mutation from pickup collection (pickup event emitted; inventory update deferred to pickup-integration slice)

---

## Layer contract

- `GameSimulator` and `SimEvent` live in `com.shadowascent.core.simulation`
- No `client` or `server` imports
- `core` only

---

## Files created

| File | Package | Purpose |
|---|---|---|
| `SimEvent.java` | `core.simulation` | Typed event record `(String type, String entityId, Map<String,Object> data)` |
| `GameSimulator.java` | `core.simulation` | Entity registry + bounded tick coordinator + event drain |

---

## Files modified

| File | Change |
|---|---|
| `RegressionTest.java` | `testGameSimulatorEntityWiring` section; 40→41 tests |
| `MIGRATION_MAP.md` | GameSimulator row status `queued` → `done (bounded slice)` |

---

## `SimEvent` design

```java
public record SimEvent(String type, String entityId, Map<String, Object> data) {}
```

Event types emitted by `GameSimulator`:

| Type | Entity | When |
|---|---|---|
| `PLAYER_JOINED` | playerId | `addPlayer()` |
| `PLAYER_LEFT` | playerId | `removePlayer()` |
| `ENEMY_AGGRO` | enemyId | enemy enters ALERTED from UNAWARE |
| `ENEMY_DEFEATED` | enemyId | `takeDamage()` returns true |
| `BOSS_INTRO_DONE` | bossId | INTRO timer expires → IDLE |
| `BOSS_PHASE_TRANSITION` | bossId | HP crosses phase ratio threshold |
| `BOSS_DEFEATED` | bossId | boss HP → 0 |
| `PICKUP_COLLECTED` | pickupId | player AABB overlaps pickup |

---

## `GameSimulator` design

### State

```java
private final Map<String, SimPlayer>  players  = new LinkedHashMap<>();
private final List<SimEnemy>          enemies  = new ArrayList<>();
private final List<SimBoss>           bosses   = new ArrayList<>();
private final List<SimNPC>            npcs     = new ArrayList<>();
private final List<SimPickup>         pickups  = new ArrayList<>();
private final PlayerInputController   inputController = new PlayerInputController();
private final List<SimEvent>          eventQueue = new ArrayList<>();
```

### API

```java
// Entity registration
void addPlayer(String playerId, int slot, float x, float y)
void removePlayer(String playerId)
void addEnemy(String id, String type, float x, float y)   // type → default stats
void addBoss(String bossId, BossType type, float x, float y)
void addNpc(String npcId, String npcType, float x, float y, float patrolMin, float patrolMax)
void addPickup(String id, String pickupType, float x, float y)

// Input
void applyInput(String playerId, InputCommand cmd)         // delegates to inputController

// Tick
void tick(float dt)

// Query
SimPlayer getPlayer(String playerId)
List<SimEnemy> getEnemies()
List<SimBoss> getBosses()
List<SimNPC> getNpcs()
List<SimPickup> getPickups()
int playerCount()
int aliveEnemyCount()
int aliveBossCount()

// Events
List<SimEvent> drainEvents()   // returns copy, clears queue

// Diagnostics
Map<String, Object> snapshot() // counts + state summary
```

### Enemy type defaults (addEnemy factory table)

| type | w | h | maxHp | dmg | speed | detect | atk | patrolRange |
|---|---|---|---|---|---|---|---|---|
| `goblin` | 32 | 56 | 3 | 1 | 60 | 180 | 48 | ±128 from spawn |
| `bat` | 28 | 24 | 2 | 1 | 80 | 200 | 40 | ±160 |
| `slime` | 40 | 36 | 5 | 1 | 30 | 120 | 40 | ±80 |
| `skeleton` | 32 | 64 | 4 | 2 | 50 | 200 | 56 | ±128 |
| `wolf` | 48 | 44 | 4 | 2 | 100 | 240 | 72 | ±192 |
| _(default)_ | 32 | 56 | 3 | 1 | 60 | 180 | 48 | ±128 |

### tick() logic

```
1. For each SimPlayer: tick invincibilityTicks (decrement if >0); tick respawnTimer
2. For each alive SimEnemy: tickEnemy(e, dt, players.values())
3. For each alive SimBoss: tickBoss(b, dt, players.values())
4. For each SimNPC: npc.step(nearestPlayerX(npcs), false)
5. For each alive SimPickup: p.tick(); if any player overlaps → p.alive=false + emit PICKUP_COLLECTED
```

### tickEnemy (bounded AI)

```
1. If stunTimer > 0: decrement, keep STUNNED, return
2. If fleeTimer > 0: decrement FLEE, return
3. Find nearest alive player
4. If dist ≤ detectionRadius and awarenessState==UNAWARE: set ALERTED, emit ENEMY_AGGRO
5. If awarenessState != UNAWARE:
   - If dist ≤ attackRange: set ATTACKING (no physics — event-based damage deferred)
   - Else: set CHASING, move physics.x toward player by moveSpeed*dt, update facingRight
6. Else (unaware): PATROL — oscillate physics.x in patrolMin..patrolMax range
```

### tickBoss (bounded phase tracking)

```
1. Tick invincibilityTicks
2. Decrement stateTimer
3. If INTRO and stateTimer ≤ 0: set IDLE, emit BOSS_INTRO_DONE
4. Phase checks (in order, only cross each threshold once):
   - phaseNumber==1 and hpRatio≤0.75 → phaseNumber=2, emit BOSS_PHASE_TRANSITION
   - phaseNumber==2 and hpRatio≤0.50 → phaseNumber=3, emit BOSS_PHASE_TRANSITION
   - phaseNumber==3 and hpRatio≤0.25 → phaseNumber=4, emit BOSS_PHASE_TRANSITION
5. If DEAD: emit BOSS_DEFEATED (once, guard with yielded flag)
```

---

## Regression tests (1 new section — 7 sub-tests)

### `testGameSimulatorEntityWiring`

1. **spawn + playerCount** — `addPlayer("p1", 0, 100, 500)` → `playerCount()==1`; `getPlayer("p1")!=null`; `drainEvents()` contains `PLAYER_JOINED`
2. **removePlayer** — remove "p1" → `playerCount()==0`; drain has `PLAYER_LEFT`
3. **enemy patrol → aggro** — spawn enemy at x=200; spawn player at x=1000 (out of range); tick → enemy stays PATROL; move player to x=210 (within detectionRadius); tick → `drainEvents()` contains `ENEMY_AGGRO`
4. **enemy defeated event** — `takeDamage(maxHp)` on enemy → returns true; drain contains `ENEMY_DEFEATED`
5. **boss INTRO → phase transition** — spawn `BossType.SIREN` at x=500; tick until stateTimer ≤ 0 → INTRO_DONE; manually set boss.hp to 60% of maxHp; tick → drain contains `BOSS_PHASE_TRANSITION` with phaseNumber=2
6. **pickup collection** — spawn pickup at x=300, y=500; spawn player at x=295, y=500; tick → pickup.alive==false; drain contains `PICKUP_COLLECTED`
7. **applyInput delegation** — spawn player; `applyInput("p1", InputCommand.neutral(0))`; `getPlayer("p1").animState` matches neutral output (e.g. "idle")
8. **snapshot keys** — `snapshot()` contains "players", "enemies", "bosses", "npcs", "pickups" keys; values match entity counts

---

## Prior test count

40 tests. Target after this slice: **41/41**.
