---
handover_type: copilot_task
milestone: Wave 4
status: superseded — no stubs remain in CombatSubsystem.java
created: 2026-05-07
---
# Copilot Task — Wave 4 CombatSubsystem Step 3 Boilerplate

## Status

**Not yet active.** This task activates after Claude Code completes Step 2 (orchestration methods moved + delegation wiring). Check `DESIGN_WAVE4_COMBAT_SUBSYSTEM.md` for step status.

## Context

Claude Code has completed Step 1 of the `CombatSubsystem` extraction:

- `CombatEncounterPhase.java` — package-level enum (extracted from `PlaytestClient`)
- `EncounterPattern.java` — package-level enum (extracted from `PlaytestClient`)
- `CombatEncounter.java` — package-level class with full state machine (extracted from `PlaytestClient`)

Step 2 (Claude Code) will create `CombatSubsystem.java` and wire `PlaytestClient` to delegate to it.

After Step 2 compiles and regression passes, this Copilot task activates.

## What Copilot Should Fill

Open `CombatSubsystem.java`. Claude Code will have created the class with method stubs and field declarations. Copilot should:

1. **Constructor wiring** — fill the body of `CombatSubsystem(Consumer<CombatEncounter> onCleared)` to initialize the encounter list and store the callback.

2. **`addEncounter(...)` body** — instantiate a new `CombatEncounter` with the 12 parameters and add to the internal list. Follow the exact same constructor call that `PlaytestClient.addCombatEncounter()` currently uses.

3. **`allEncounters()` body** — return `Collections.unmodifiableList(encounters)` or `List.copyOf(encounters)`.

4. **`hasActiveEncounterNear(float, float, float)` body** — iterate `encounters`, skip cleared, check `distance(playerX, playerY, e.centerX(), e.centerY()) <= range`.

5. **Any remaining getter/query stubs** Claude Code left as `// TODO` or `throw new UnsupportedOperationException()`.

## Patterns to Follow

- `CombatEncounter` constructor signature is unchanged from what was in `PlaytestClient` — 12 params in the same order.
- `distance(x1, y1, x2, y2)` is now a private static method inside `CombatEncounter` — DO NOT add it to `CombatSubsystem`. Call `encounter.centerX()` / `encounter.centerY()` and compute inline if needed.
- All field types: `List<CombatEncounter>` (ArrayList), `Consumer<CombatEncounter>` (callback stored as field).
- Package: `com.shadowascent.client` — no new imports beyond `java.util.*` and `java.util.function.Consumer`.

## Definition of Done

`./gradlew :client:compileJava runRegressionTests` passes with no errors.
