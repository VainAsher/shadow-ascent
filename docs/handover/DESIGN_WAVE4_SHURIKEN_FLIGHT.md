---
handover_type: design
milestone: Wave4
topic: shuriken_flight
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 SimShuriken Projectile Flight in GameSimulator

Adds shuriken projectile lifecycle to `GameSimulator`: fire from player origin, move per tick,
AABB-test against live enemies, emit hit/defeat events, expire on TTL or on first hit.

---

## Scope

### In scope
- `GameSimulator` — `shurikens` list, `shurikenCounter` field, `tickShurikens(dt)`, `fireShuriken` API, `getShurikens()`, `overlaps()` AABB helper
- New events: `SHURIKEN_FIRED`, `SHURIKEN_HIT`, `ENEMY_DEFEATED` (if hit kills)
- Regression gate: `testShurikenProjectileFlight` (3 sub-tests); 45 → 46 tests

### Out of scope
- Shuriken vs player collision (`damagesPlayers=true` path — co-op/boss projectile scope)
- Wall/tile collision (no geometry in GameSimulator at this level)

---

## New fields

```java
private final List<SimShuriken>  shurikens       = new ArrayList<>();
private       int                shurikenCounter = 0;
```

---

## New public API

```java
public void fireShuriken(String playerId, float vx, float vy) {
    SimPlayer p = players.get(playerId);
    if (p == null || !p.isAlive()) return;
    String id = "shr_" + shurikenCounter++;
    float cx = p.physics.x + p.physics.width  * 0.5f;
    float cy = p.physics.y + p.physics.height * 0.5f;
    shurikens.add(new SimShuriken(id, p.slot, cx, cy, vx, vy));
    emit("SHURIKEN_FIRED", id, Map.of("playerId", playerId, "vx", (double) vx, "vy", (double) vy));
}

public List<SimShuriken> getShurikens() {
    return Collections.unmodifiableList(shurikens);
}
```

---

## `tickShurikens(float dt)`

```java
private void tickShurikens(float dt) {
    java.util.Iterator<SimShuriken> it = shurikens.iterator();
    while (it.hasNext()) {
        SimShuriken s = it.next();
        if (!s.alive) { it.remove(); continue; }
        s.ttl -= dt;
        if (s.ttl <= 0f) { s.alive = false; it.remove(); continue; }
        s.x += s.vx * dt;
        s.y += s.vy * dt;
        for (SimEnemy e : enemies) {
            if (!e.isAlive()) continue;
            if (overlaps(s.x, s.y, SimShuriken.W, SimShuriken.H,
                         e.physics.x, e.physics.y, e.physics.width, e.physics.height)) {
                boolean died = e.takeDamage(s.damage);
                emit("SHURIKEN_HIT", s.shurikenId,
                        Map.of("enemyId", e.enemyId, "dmg", s.damage));
                if (died) {
                    emit("ENEMY_DEFEATED", e.enemyId,
                            Map.of("killedBy", "shuriken_" + s.shurikenId));
                }
                s.alive = false;
                break;
            }
        }
        if (!s.alive) it.remove();
    }
}
```

Wire into `tick(float dt)`:
```java
tickShurikens(dt);
```

---

## AABB helper

```java
private static boolean overlaps(float ax, float ay, float aw, float ah,
                                  float bx, float by, float bw, float bh) {
    return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
}
```

---

## Files modified

| File | Change |
|---|---|
| `core/simulation/GameSimulator.java` | fields + `tickShurikens` + `fireShuriken` + `getShurikens` + `overlaps`; `tick` wired |
| `core/RegressionTest.java` | `testShurikenProjectileFlight` section; dispatch; 45→46 tests |

---

## Regression tests (3 sub-tests)

1. **shuriken moves** — fire shuriken with `vx=200f`, tick once, verify `s.x > spawnX`.
2. **shuriken hits enemy** — place enemy at same position as player, fire, tick once → `SHURIKEN_HIT` emitted, enemy HP reduced.
3. **TTL expiry removes shuriken** — set `s.ttl = 0.01f`, tick with `dt=0.05f`, verify shuriken removed from `getShurikens()`.

---

## Prior test count: 45 → Target: 46/46
