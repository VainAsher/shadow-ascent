---
handover_type: design
milestone: Wave4
topic: enemy_combat_damage_loop
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 Enemy Combat Damage Loop in GameSimulator

Closes the one-sided combat gap in `GameSimulator`: enemies in ATTACK range now deal damage
to players, and a new `attackEnemy` API lets callers deal player-originated melee damage to
enemies.  Uses the existing `attackTimer` field on `SimEnemy` and the existing
`SimPlayer.takeDamage()` / `SimEnemy.takeDamage()` methods.

---

## Scope

### In scope
- `GameSimulator.tickEnemy` — decrement `attackTimer` per tick; on expiry deal damage, emit events
- `GameSimulator.attackEnemy(playerId, enemyId, damage)` — new public API for player-originated hits
- `GameSimulator` — two new private-field constants + one new private helper
- New event types emitted: `PLAYER_DAMAGED`, `PLAYER_DIED`, `ENEMY_DEFEATED`
- Regression gate: `testEnemyCombatDamageLoop` (4 sub-tests); 43 → 44 tests

### Out of scope
- Boss damage loop (Option 1)
- Shuriken projectile (Option 3)
- Moving-platform / portal integration (Option 4)

---

## Constants (added to GameSimulator)

```java
private static final float ENEMY_ATTACK_COOLDOWN =
        SimEnemy.ATTACK_WINDUP_TIME + SimEnemy.ATTACK_ACTIVE_TIME + SimEnemy.ATTACK_RECOVERY_TIME;
// = 0.6 + 0.15 + 0.4 = 1.15f

private static final float RESPAWN_DELAY = 3.0f;
```

---

## tickEnemy change (ATTACK branch)

Replace:
```java
if (dist <= e.attackRange) {
    e.aiState = EnemyAIState.ATTACK;
}
```

With:
```java
if (dist <= e.attackRange) {
    e.aiState = EnemyAIState.ATTACK;
    e.attackTimer = Math.max(0f, e.attackTimer - dt);
    if (e.attackTimer <= 0f && nearest.isAlive()) {
        int hpBefore = nearest.health;
        nearest.takeDamage(e.baseDamage);
        if (nearest.health < hpBefore) {
            emit("PLAYER_DAMAGED", nearest.playerId,
                    Map.of("hp", nearest.health, "dmg", hpBefore - nearest.health,
                            "byEnemy", e.enemyId));
            if (nearest.isDead) {
                nearest.respawnTimer = RESPAWN_DELAY;
                emit("PLAYER_DIED", nearest.playerId, Map.of("byEnemy", e.enemyId));
            }
        }
        e.attackTimer = ENEMY_ATTACK_COOLDOWN;
    }
}
```

Note: `SimPlayer.takeDamage()` returns void and internally guards on `invincibilityTicks > 0`.
The `hpBefore` comparison detects whether damage actually landed (invincibility frames skip it).
`attackTimer` is reset regardless of whether damage landed (enemy attempted the swing).

---

## New public API

```java
public void attackEnemy(String playerId, String enemyId, int damage) {
    SimPlayer attacker = players.get(playerId);
    if (attacker == null || !attacker.isAlive()) return;
    SimEnemy target = findEnemy(enemyId);
    if (target == null) return;
    boolean died = target.takeDamage(damage);
    if (died) {
        emit("ENEMY_DEFEATED", enemyId, Map.of("killedBy", playerId));
    }
}
```

## New private helper

```java
private SimEnemy findEnemy(String id) {
    for (SimEnemy e : enemies) {
        if (id.equals(e.enemyId)) return e;
    }
    return null;
}
```

---

## Files modified

| File | Change |
|---|---|
| `core/simulation/GameSimulator.java` | Constants + tickEnemy damage branch + `attackEnemy` API + `findEnemy` helper |
| `core/RegressionTest.java` | `testEnemyCombatDamageLoop` section; dispatch; 43→44 tests |

---

## Regression tests (4 sub-tests)

### `testEnemyCombatDamageLoop`

1. **enemy hits player** — place enemy with `attackTimer=0f`, player within `attackRange`, tick; verify `PLAYER_DAMAGED` emitted and `hp` decreased.
2. **player dies** — player starts at 1 HP; same setup; tick → verify `player.isDead == true` and `PLAYER_DIED` emitted and `respawnTimer > 0`.
3. **invincibility suppresses hit** — set `player.invincibilityTicks = 30`, tick; verify no `PLAYER_DAMAGED` event.
4. **player kills enemy** — call `sim.attackEnemy("p1", "e1", 99)` → verify `ENEMY_DEFEATED` emitted and `!enemy.isAlive()`.

---

## Prior test count: 43 → Target: 44/44
