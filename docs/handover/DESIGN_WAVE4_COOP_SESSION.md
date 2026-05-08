---
handover_type: design
milestone: Wave4
topic: coop_session
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 Co-op Session Scaffolding in GameSimulator

Adds the minimal server-side co-op hooks to `GameSimulator`:
- player proximity detection emits `PLAYER_PROXIMITY` each tick for pairs within range,
- `requestRevive` lets an alive player revive a dead teammate who is within range,
- `COOP_REVIVE` event signals successful revival with restored HP.

---

## Scope

### In scope
- `GameSimulator` — `REVIVE_RANGE` constant, `tickCoopProximity()` private method, `requestRevive` public API
- New events: `PLAYER_PROXIMITY`, `COOP_REVIVE`
- `tick(float dt)` — wire `tickCoopProximity()`
- Regression gate: `testCoopSessionScaffolding` (3 sub-tests); 48 → 49 tests

### Out of scope
- Client-side revive UI prompt (client layer)
- Revive animation / timing window (deferred)
- Cross-player damage / friendly fire

---

## Constant

```java
private static final float REVIVE_RANGE = 80f;
```

---

## `tickCoopProximity()`

Emits `PLAYER_PROXIMITY` for each pair of alive players within `REVIVE_RANGE`.
Canonical pair ordering (lower slot first) prevents duplicate events per pair.

```java
private void tickCoopProximity() {
    java.util.List<SimPlayer> alive = players.values().stream()
            .filter(p -> !p.isDead)
            .collect(java.util.stream.Collectors.toList());
    for (int i = 0; i < alive.size(); i++) {
        for (int j = i + 1; j < alive.size(); j++) {
            SimPlayer a = alive.get(i);
            SimPlayer b = alive.get(j);
            float dx = a.physics.x - b.physics.x;
            float dy = a.physics.y - b.physics.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist <= REVIVE_RANGE) {
                emit("PLAYER_PROXIMITY", a.playerId,
                        Map.of("nearPlayerId", b.playerId, "dist", dist));
            }
        }
    }
}
```

Wire into `tick(float dt)`:
```java
tickCoopProximity();
```

---

## `requestRevive`

```java
public void requestRevive(String reviverPlayerId, String targetPlayerId) {
    SimPlayer reviver = players.get(reviverPlayerId);
    SimPlayer target  = players.get(targetPlayerId);
    if (reviver == null || target == null) return;
    if (reviver.isDead || !target.isDead)  return;
    float dx   = reviver.physics.x - target.physics.x;
    float dy   = reviver.physics.y - target.physics.y;
    float dist = (float) Math.sqrt(dx * dx + dy * dy);
    if (dist > REVIVE_RANGE) return;
    target.isDead        = false;
    target.respawnTimer  = -1f;
    target.health        = Math.max(1, target.maxHealth / 2);
    target.invincibilityTicks = SimPlayer.INVINCIBILITY_TICKS;
    emit("COOP_REVIVE", targetPlayerId,
            Map.of("reviverPlayerId", reviverPlayerId, "hp", target.health));
}
```

---

## Files modified

| File | Change |
|---|---|
| `core/simulation/GameSimulator.java` | `REVIVE_RANGE` constant; `tickCoopProximity` private method wired into `tick`; `requestRevive` public API |
| `core/RegressionTest.java` | `testCoopSessionScaffolding` section; dispatch; 48→49 tests |

---

## Regression tests (3 sub-tests)

1. **Revive in range** — add 2 players side-by-side; kill target via `takeDamage`; `requestRevive`; verify `COOP_REVIVE` emitted and target alive at `maxHealth/2`.
2. **Revive out of range** — same setup but target placed 200px away; `requestRevive` should no-op; target still dead.
3. **Proximity event** — two alive players within 40px; `tick`; verify `PLAYER_PROXIMITY` emitted.

---

## Prior test count: 48 → Target: 49/49
