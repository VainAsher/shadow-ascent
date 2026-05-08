---
handover_type: design_doc
milestone: Wave 4
status: done (2026-05-07)
created: 2026-05-07
---
# Wave 4 — CombatSubsystem Extraction Design

## Why

`PlaytestClient.java` is ~120KB. The combat encounter system is a self-contained state machine that has grown to roughly 400 lines (enums + inner class + orchestration methods). Extracting it as `CombatSubsystem` is the first bounded Wave 4 decomposition slice.

**Rule:** Do not import from old repo wholesale. Extract only the identified slice below, compile and pass regression before the next slice begins.

---

## What Gets Extracted

### From `PlaytestClient.java` to new `CombatSubsystem.java`

| Component | Current location | Lines (approx) |
|---|---|---|
| `CombatEncounterPhase` enum | PlaytestClient inner | 2746–2752 |
| `EncounterPattern` enum | PlaytestClient inner | 2754–2759 |
| `CombatEncounter` inner class | PlaytestClient inner | 2761–2991 |
| Combat constants (`ATTACK_RANGE`, `ATTACK_COOLDOWN_SECONDS`, timing defaults, `ENCOUNTER_MISS_DAMAGE`) | PlaytestClient fields | 99–106 |
| `updateCombatEncounters(float dt)` | PlaytestClient method | 995–1007 |
| `performCombatAttack()` | PlaytestClient method | 1009–1044 |
| `nearestAttackableEncounter(float maxRange)` | PlaytestClient method | 1046–1060 |
| `nearestEncounterWithinDistance(float maxRange)` | PlaytestClient method | ~1188–1199 |
| `addCombatEncounter(...)` | PlaytestClient method | 1443–1468 |
| `onCombatEncounterCleared(CombatEncounter)` | PlaytestClient method | 1062–1073 |
| `combatEncounters` field (`List<CombatEncounter>`) | PlaytestClient field | ~130 |

### What stays in `PlaytestClient`

- The `queueAttack` boolean field and F-key input binding (input layer stays in client)
- The `attackCooldownSeconds` field (combat subsystem exposes a method to consume it)
- The `takeDamage(int)` method — this touches player health which stays in PlaytestClient
- All rendering/HUD logic for encounters
- The encounter creation calls (these call into the subsystem)

---

## Target Interface: `CombatSubsystem`

```java
package com.shadowascent.client;

// Owns: encounter list, attack processing, phase state machine
// Does NOT own: player health, rendering, input binding
public class CombatSubsystem {

    // --- Lifecycle ---
    public void addEncounter(String id, String requiredAbility, String displayName,
        float centerX, float centerY, float activationRadius,
        int requiredHits, float telegraphSeconds, float vulnerableSeconds,
        float recoverSeconds, EncounterPattern pattern, List<String> objectiveKeywords)

    public void update(float playerX, float playerY, boolean abilityUnlocked, float dt)

    // --- Attack interface (called by PlaytestClient when queueAttack fires) ---
    // Returns: CombatAttackResult (HIT / MISS / NO_TARGET / ON_COOLDOWN)
    public CombatAttackResult tryAttack(float playerX, float playerY, float attackCooldownSeconds)

    // --- State queries (used by PlaytestClient HUD + minimap) ---
    public List<CombatEncounter> allEncounters()
    public CombatEncounter nearestEncounterWithinDistance(float maxRange, float playerX, float playerY)
    public boolean hasActiveEncounterNear(float playerX, float playerY, float range)

    // --- Completion callback (PlaytestClient hooks mission/objective progression here) ---
    // Set by PlaytestClient during construction:
    public void setOnEncounterCleared(java.util.function.Consumer<CombatEncounter> callback)
}
```

`CombatAttackResult` is a new small record or enum:

```java
public enum CombatAttackResult { HIT, MISS_TIMING, NO_TARGET, ON_COOLDOWN }
```

---

## How PlaytestClient Delegates

After extraction, PlaytestClient:

1. Holds `private final CombatSubsystem combatSubsystem;`
2. Calls `combatSubsystem.update(playerX, playerY, abilityUnlocked, dt)` in its tick
3. On F-key: calls `combatSubsystem.tryAttack(playerX, playerY, attackCooldownSeconds)` and acts on `CombatAttackResult`
4. Sets `combatSubsystem.setOnEncounterCleared(e -> onCombatEncounterCleared(e))` during init
5. Uses `combatSubsystem.allEncounters()` for minimap rendering

---

## Extraction Order (do one step, compile, then next)

```
Step 1  Claude Code   Extract enums + CombatEncounter into CombatSubsystem.java    DONE 2026-05-07
                      Compile only — no delegation wiring yet

Step 2  Claude Code   Move orchestration methods (update, tryAttack, helpers)       DONE 2026-05-07
                      Wire PlaytestClient to delegate                                25/25 tests pass
                      Full compile + runRegressionTests must pass

Step 3  Copilot       [SKIPPED] CombatSubsystem.java was created fully implemented;
                      no stubs or TODOs remain. Copilot boilerplate task not needed.

Step 4  Claude Code   Final integration review — check for coupling creep             DONE 2026-05-07
                      Verify no layer contract violations (combat still in client, not core)

                      Layer contract: PASS — CombatSubsystem, CombatEncounter,
                      CombatEncounterPhase, EncounterPattern all in com.shadowascent.client;
                      no core imports in any of the four new files.

                      Coupling findings (both fixed):
                      - Unused Locale import in CombatSubsystem.java — removed.
                      - Redundant encounter.setCleared() in PlaytestClient.onCombatEncounterCleared()
                        callback — removed (consumeVulnerableHit already clears internally).

                      Bounded debt (noted, not blocking):
                      - restoreClearedCombatEncountersFromFlags() in PlaytestClient still
                        iterates allEncounters() and calls setCleared()/resetState() directly.
                        This bypasses the subsystem for save-state restoration. Acceptable
                        for this slice; candidate for a CombatSubsystem.restoreFromFlags()
                        method in a future refinement pass.

Step 5  Codex CLI     complete (2026-05-07) — `.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runRegressionTests`
```

---

## Regression Gate

After extraction, all existing regression tests must continue to pass — the refactor is purely structural. No new test is required for this slice unless combat behavior changes.

---

## Layer Contract Reminder

`CombatSubsystem` lives in `java/client`. It must not import from `java/core` beyond `PhysicsConstants` and `StoryState`. Combat encounter physics is self-contained in the subsystem — it does not need the `core.physics` collision system.
