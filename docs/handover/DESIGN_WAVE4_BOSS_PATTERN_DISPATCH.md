---
handover_type: design
milestone: Wave4
topic: boss_pattern_dispatch
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 BossPatternLibrary Tick Dispatch in GameSimulator

Wires `BossPatternLibrary.tick()` into `GameSimulator.tickBoss` so each boss drives its
psychological pattern AI (movement, cooldowns, scripted-loss, echo-mirror, lantern-drain,
platform-reset, veil-illusion) each tick after INTRO completes.

---

## Scope

### In scope
- `GameSimulator.tickBoss` — pattern dispatch after INTRO/phase-transition logic
- `GameSimulator.buildBossPatternContext()` — private helper that builds `PatternContext`
- New event: `BOSS_SCRIPTED_LOSS` (only SIREN raises this; emitted when `ServerEvent.SCRIPTED_LOSS` returned)
- Regression gate: `testBossPatternDispatch` (3 sub-tests); 44 → 45 tests

### Out of scope
- Full projectile/spawn wiring (null stubs for this slice — both are null-checked in patterns)
- Boss dealing direct damage to players (deferred)

---

## `buildBossPatternContext()` helper

```java
private BossPatternLibrary.PatternContext buildBossPatternContext() {
    Map<Integer, SimPlayer> bySlot = new LinkedHashMap<>();
    for (SimPlayer p : players.values()) bySlot.put(p.slot, p);
    return new BossPatternLibrary.PatternContext(bySlot, enemies, () -> {}, null, null);
}
```

`PatternContext` fields:
- `players` — `Map<Integer, SimPlayer>` by `slot` (required by pattern nearest-player logic)
- `enemies` — `this.enemies` list (TIME_LEECH_LORD / VEIL_MAIDEN scan active enemies)
- `broadcastScriptedLoss` — `() -> {}` no-op (we handle via returned `ServerEvent`)
- `spawnEnemy` — null (null-checked in all patterns that use it)
- `fireProjectile` — null (null-checked in all patterns that use it)

---

## `tickBoss` change

After the existing phase-transition block, add pattern dispatch guarded on INTRO:

```java
// Pattern dispatch — only when boss is past INTRO
if (b.aiState != BossAIState.INTRO) {
    BossPatternLibrary.PatternContext ctx = buildBossPatternContext();
    BossPatternLibrary.ServerEvent ev = BossPatternLibrary.tick(b, ctx, dt);
    if (ev == BossPatternLibrary.ServerEvent.SCRIPTED_LOSS) {
        emit("BOSS_SCRIPTED_LOSS", b.bossId, Map.of("type", b.type.name()));
    }
}
```

Note: `b.tickInvincibility()` is already called at the top of `tickBoss`; ScriptedLossPattern
and VeilMaidenPattern also call it internally. Double-decrement is acceptable for this slice
(invincibility expires ~2× faster for SIREN/VEIL_MAIDEN only, still ~8 ticks protection).

---

## Files modified

| File | Change |
|---|---|
| `core/simulation/GameSimulator.java` | `buildBossPatternContext()` helper + dispatch block in `tickBoss` |
| `core/RegressionTest.java` | `testBossPatternDispatch` section; dispatch; 44→45 tests |

---

## Regression tests (3 sub-tests)

### `testBossPatternDispatch`

1. **dispatch after INTRO** — add ECHO_WARDEN boss + player, force `stateTimer` negative, tick once → `BOSS_INTRO_DONE` emitted, `aiState != INTRO`; tick again → `echoBuffer` non-null (EchoMirrorPattern initialises it on first call).
2. **all 5 boss types dispatch without NPE** — loop `BossType.values()`, add boss + player at same position, force past INTRO, tick once; assert no exception thrown for any type.
3. **SIREN null-projectile safe** — SIREN boss, player present, force past INTRO, set `attackCooldown=0`, phase 1, tick → no NPE; `aiState == ATTACK_RANGED || aiState == MOVE`.

---

## Prior test count: 44 → Target: 45/45
