---
handover_type: design
milestone: Wave4
topic: moving_platform_portal
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 SimMovingPlatform + SimPortal Integration in GameSimulator

Adds moving-platform tick (oscillation + player carry) and portal tick (pulse + proximity/ability
activation) to `GameSimulator`. Both entities have their own `step()` methods; the simulator
coordinates them with players.

---

## Scope

### In scope
- `GameSimulator` — `movingPlatforms` / `portals` lists; `addMovingPlatform` / `addPortal` APIs; `tickMovingPlatforms` / `tickPortals`; `getMovingPlatforms` / `getPortals` queries
- New events: `PORTAL_ACTIVATED`
- Portal activated once per session — `isActive = false` set on first activation to prevent repeat events
- Regression gate: `testMovingPlatformAndPortal` (3 sub-tests); 46 → 47 tests

### Out of scope
- Collision push-back for platform sides
- Multi-destination portal routing logic (destination ID is emitted in the event; routing deferred)

---

## New fields

```java
private final List<SimMovingPlatform> movingPlatforms = new ArrayList<>();
private final List<SimPortal>         portals         = new ArrayList<>();
```

---

## New public APIs

```java
public void addMovingPlatform(String id, float x, float y,
                               float width, float height,
                               float leftBound, float rightBound, float speed) {
    movingPlatforms.add(new SimMovingPlatform(id, x, y, width, height,
                                              leftBound, rightBound, speed));
}

public void addPortal(String id, String type, String destId,
                      float x, float y, String requiredAbility) {
    portals.add(new SimPortal(id, type, destId, x, y, requiredAbility));
}

public List<SimMovingPlatform> getMovingPlatforms() {
    return Collections.unmodifiableList(movingPlatforms);
}

public List<SimPortal> getPortals() {
    return Collections.unmodifiableList(portals);
}
```

---

## `tickMovingPlatforms()`

```java
private void tickMovingPlatforms() {
    for (SimMovingPlatform plat : movingPlatforms) {
        float prevX = plat.x;
        plat.step();                           // advances x by vx, bounces at bounds
        float delta = plat.x - prevX;
        if (delta == 0f) continue;
        for (SimPlayer p : players.values()) {
            if (p.isDead) continue;
            if (plat.isStandingOn(p.physics.x, p.physics.y,
                                   p.physics.width, p.physics.height)) {
                p.physics.x += delta;
            }
        }
    }
}
```

No `dt` parameter — `SimMovingPlatform.step()` uses its internal per-frame `vx` directly.

---

## `tickPortals(float dt)`

```java
private void tickPortals(float dt) {
    for (SimPortal portal : portals) {
        portal.step(dt);
        if (!portal.isActive) continue;
        for (SimPlayer p : players.values()) {
            if (p.isDead) continue;
            float px = p.physics.x + p.physics.width  * 0.5f;
            float py = p.physics.y + p.physics.height * 0.5f;
            if (portal.canInteract(px, py) && portal.canPlayerEnter(p)) {
                portal.isActive = false;    // one-shot — prevent repeat events
                emit("PORTAL_ACTIVATED", portal.portalId,
                        Map.of("playerId", p.playerId, "dest", portal.destinationId));
                break;
            }
        }
    }
}
```

---

## `tick` wiring

```java
tickMovingPlatforms();
tickPortals(dt);
```

---

## Files modified

| File | Change |
|---|---|
| `core/simulation/GameSimulator.java` | fields + `addMovingPlatform` + `addPortal` + `tickMovingPlatforms` + `tickPortals` + queries; `tick` wired |
| `core/RegressionTest.java` | `testMovingPlatformAndPortal` section; dispatch; 46→47 tests |

---

## Regression tests (3 sub-tests)

1. **platform carries player** — add platform at x=200 with vx=2, player standing on top; tick once; verify `player.physics.x` advanced by `vx` delta.
2. **portal activates once** — add portal with no required ability, player within 56px radius; tick → `PORTAL_ACTIVATED` emitted, `portal.isActive == false`; tick again → no second event.
3. **portal ability gate blocks** — add portal requiring `"dash"`, player without that ability in range; tick → no `PORTAL_ACTIVATED`.

---

## Prior test count: 46 → Target: 47/47
