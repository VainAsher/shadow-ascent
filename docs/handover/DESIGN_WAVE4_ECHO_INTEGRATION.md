---
handover_type: design
milestone: Wave4
topic: echo_integration
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 EchoRecorder / ReplayPlayer / SimEcho Integration in GameSimulator

Wires the echo subsystem into `GameSimulator`:
- player inputs are recorded into `SimPlayer.echoRecorder` each tick,
- `spawnEcho` creates a `SimEcho` backed by a `ReplayPlayer` built from the player's snapshot,
- `tickEchoes` steps each echo, emits lifecycle events, and removes finished echoes.

---

## Scope

### In scope
- `GameSimulator.tickPlayers` — add `p.echoRecorder.record(p.latestInput)` per tick
- `GameSimulator` — `echoes` list, `spawnEcho` API, `getEchoes` query, `tickEchoes` private method
- New events: `ECHO_STARTED`, `ECHO_COMPLETED`, `ECHO_FAILED`
- Regression gate: `testEchoRecorderIntegration` (3 sub-tests); 47 → 48 tests

### Out of scope
- SimEcho vs enemy/player collision (echo combat — deferred)
- Echo puzzle evaluation logic

---

## `tickPlayers` change

Add per-player input recording after `invincibilityTicks` decrement:
```java
p.echoRecorder.record(p.latestInput);
```

---

## New field

```java
private final List<SimEcho> echoes = new ArrayList<>();
```

---

## New public APIs

```java
public void spawnEcho(String playerId, String echoId, boolean looping) {
    SimPlayer p = players.get(playerId);
    if (p == null) return;
    java.util.List<InputCommand> seq = p.echoRecorder.snapshot();
    if (seq.isEmpty()) return;
    ReplayPlayer replay = ReplayPlayer.fromInputSequence(0L, p.slot, seq);
    SimEcho echo = new SimEcho(echoId, p.slot, p.physics.x, p.physics.y,
                               replay, true, p.weaponState, "player_echo");
    echo.looping = looping;
    echoes.add(echo);
    emit("ECHO_STARTED", echoId,
            Map.of("playerId", playerId, "frames", (long) seq.size()));
}

public List<SimEcho> getEchoes() {
    return Collections.unmodifiableList(echoes);
}
```

---

## `tickEchoes()`

```java
private void tickEchoes() {
    java.util.Iterator<SimEcho> it = echoes.iterator();
    while (it.hasNext()) {
        SimEcho echo = it.next();
        if (!echo.active && (echo.completed || echo.failed)) { it.remove(); continue; }
        if (!echo.active) continue;
        boolean wasDone   = echo.completed;
        boolean wasFailed = echo.failed;
        echo.step();
        if (!wasDone && echo.completed) {
            emit("ECHO_COMPLETED", echo.echoId, Map.of("ownerSlot", echo.ownerSlot));
        }
        if (!wasFailed && echo.failed) {
            emit("ECHO_FAILED", echo.echoId, Map.of("ownerSlot", echo.ownerSlot));
        }
        if (!echo.active && (echo.completed || echo.failed)) it.remove();
    }
}
```

Wire into `tick(float dt)`:
```java
tickEchoes();
```

---

## Files modified

| File | Change |
|---|---|
| `core/simulation/GameSimulator.java` | `tickPlayers` record call + `echoes` field + `spawnEcho` + `getEchoes` + `tickEchoes`; `tick` wired |
| `core/RegressionTest.java` | `testEchoRecorderIntegration` section; dispatch; 47→48 tests |

---

## Regression tests (3 sub-tests)

1. **recorder captures inputs** — add player, apply input, tick, verify `echoRecorder.size() > 0`.
2. **echo spawns and steps** — tick player 3 times to fill recorder (3 frames), `spawnEcho`, tick once; verify echo still active (`ticksPlayed() == 1`).
3. **echo completes** — tick player 1 time (1 frame in recorder), `spawnEcho` (non-looping), tick once → `ECHO_COMPLETED` emitted and echo removed from `getEchoes()`.

---

## Prior test count: 47 → Target: 48/48
