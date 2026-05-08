---
handover_type: design_doc
milestone: Wave 4
status: in_progress
created: 2026-05-07
---
# Wave 4 — TraversalSubsystem Extraction Design

## Why

Second bounded Wave 4 decomposition slice of `PlaytestClient.java`. The traversal system — moving platforms, ability gates, triggered platforms, ability triggers, and combat barriers — is a cohesive block of ~350 lines of inner classes plus ~200 lines of orchestration methods. Extracting it removes the second major responsibility from the monolith.

---

## What Gets Extracted

### Inner types (Step 1 — new standalone files)

| Type | Current location | Description |
| --- | --- | --- |
| `TriggerMode` enum | PlaytestClient inner | `DASH_PASS` / `INTERACT` |
| `MovingPlatform` class | PlaytestClient inner | Sinusoidal platform animation, collision tile |
| `AbilityGate` class | PlaytestClient inner | Static traversal blocker unlocked by story ability |
| `CombatBarrier` class | PlaytestClient inner | Static traversal blocker unlocked by encounter clear |
| `TriggeredPlatform` class | PlaytestClient inner | Platform unlocked by ability trigger activation |
| `AbilityTrigger` class | PlaytestClient inner | Interactable world object (dash-pass sigil / interact altar) |

### Methods moved to `TraversalSubsystem` (Step 2)

| Method | Current location |
| --- | --- |
| `updateMovingPlatforms(float dt)` | PlaytestClient |
| `applyMovingPlatformCarry()` | PlaytestClient |
| `movingPlatformIdForTile(TileRect tile)` | PlaytestClient |
| `updateAbilityExecutionTriggers()` | PlaytestClient |
| `overlapsTrigger(AbilityTrigger trigger)` | PlaytestClient |
| `nearestTriggerWithinDistance(float radius)` | PlaytestClient |
| `tryInteractAbilityTrigger()` | PlaytestClient |
| `activateAbilityTrigger(AbilityTrigger trigger)` | PlaytestClient |
| `restoreActivatedAbilityTriggersFromFlags()` | PlaytestClient |
| `abilityGateForTile(TileRect tile)` | PlaytestClient |
| `combatBarrierForTile(TileRect tile)` | PlaytestClient |
| `addMovingPlatform(...)` | PlaytestClient |
| `addAbilityGate(...)` | PlaytestClient |
| `addAbilityTrigger(...)` | PlaytestClient |
| `addTriggeredPlatform(...)` | PlaytestClient |
| `addCombatBarrier(...)` | PlaytestClient |

### Fields moved

| Field | Type |
| --- | --- |
| `movingPlatforms` | `List<MovingPlatform>` |
| `movingPlatformsById` | `Map<String, MovingPlatform>` |
| `abilityGates` | `List<AbilityGate>` |
| `abilityTriggers` | `List<AbilityTrigger>` |
| `activatedAbilityTriggers` | `Set<String>` |
| `triggeredPlatforms` | `List<TriggeredPlatform>` |
| `combatBarriers` | `List<CombatBarrier>` |

### What stays in `PlaytestClient`

- `groundedMovingPlatformId` field — set during collision resolution, used for carry
- `clearedCombatEncounterIds` set — combat state (bounded debt from CombatSubsystem slice)
- `notifyAbilityGateBlocked(TileRect tile)` — UI feedback (stays in client)
- `collisionHash` and `dynamicCollisionTiles` — collision infrastructure stays in PlaytestClient
- All rendering/HUD loops for traversal elements
- `applyAbilityTriggerObjectiveProgress(trigger)` — mission logic (delegated via callback)
- `almostEquals(float, float)` — shared static helper, stays in PlaytestClient

---

## Target Interface: `TraversalSubsystem`

```java
final class TraversalSubsystem {

    record PlatformCarry(float dx, float dy) {}

    record InteractOutcome(OutcomeType type, String triggerName, String requiredAbility) {
        enum OutcomeType { ACTIVATED, ALREADY_ACTIVE, ABILITY_LOCKED, NO_TRIGGER }
        static InteractOutcome noTrigger() { ... }
        static InteractOutcome alreadyActive(String name) { ... }
        static InteractOutcome abilityLocked(String name, String ability) { ... }
        static InteractOutcome activated(String name) { ... }
    }

    TraversalSubsystem(Consumer<AbilityTrigger> onTriggerActivated)

    // Lifecycle
    void addMovingPlatform(String id, float x, float y, float w, float h, float tx, float ty, float period)
    void addAbilityGate(String id, String ability, String name, float x, float y, float w, float h)
    void addAbilityTrigger(String id, String ability, String name, TriggerMode mode,
                           float x, float y, float w, float h, List<String> keywords)
    void addTriggeredPlatform(String id, String triggerId, float x, float y, float w, float h)
    void addCombatBarrier(String id, String encounterId, String name, float x, float y, float w, float h)
    void clearAll()

    // Tick — updates platform positions + detects dash-pass triggers
    void update(float playerX, float playerY, boolean isDashing,
                Predicate<String> abilityCheck, float dt)

    // Interact (E key) — handles interact-mode triggers
    InteractOutcome tryInteract(float playerX, float playerY, float radius,
                                Predicate<String> abilityCheck)

    // Platform carry (called after vertical collision resolution)
    PlatformCarry carryFor(String groundedPlatformId)

    // Dynamic collision tile list (fed to SpatialHash by PlaytestClient)
    List<TileRect> buildDynamicTiles(Set<String> clearedEncounterIds,
                                     Predicate<String> abilityCheck)

    // State restore (save/load)
    void restoreFromFlags(Predicate<String> hasFlag)

    // Tile lookups (for gate-blocked feedback in PlaytestClient)
    AbilityGate gateForTile(TileRect tile)
    CombatBarrier barrierForTile(TileRect tile)
    String platformIdForTile(TileRect tile)

    // Nearest queries (HUD / hints)
    AbilityTrigger nearestTriggerWithinRange(float playerX, float playerY, float radius)

    // State queries (rendering)
    List<MovingPlatform> allMovingPlatforms()
    List<AbilityGate> allGates()
    List<AbilityTrigger> allTriggers()
    List<TriggeredPlatform> allTriggeredPlatforms()
    List<CombatBarrier> allCombatBarriers()
    boolean isTriggerActivated(String id)
}
```

### Callback contract

`onTriggerActivated` fires when a trigger activates (dash-pass OR interact). PlaytestClient's handler:
1. Sets story flag: `storyState.setFlag(ABILITY_TRIGGER_FLAG_PREFIX + trigger.id())`
2. Rebuilds dynamic tiles: `collisionHash.setDynamicTiles(traversalSubsystem.buildDynamicTiles(...))`
3. Logs + sets mission feedback
4. Calls `applyAbilityTriggerObjectiveProgress(trigger)`

The callback fires AFTER the subsystem has added the trigger to `activatedAbilityTriggers` internally.

---

## How PlaytestClient Delegates

After extraction, PlaytestClient:

1. Holds `private final TraversalSubsystem traversalSubsystem;`
2. Constructs: `new TraversalSubsystem(this::onAbilityTriggerActivated)`
3. Game loop: `traversalSubsystem.update(playerX, playerY, isDashing, storyState::hasAbility, dt)` then `collisionHash.setDynamicTiles(traversalSubsystem.buildDynamicTiles(clearedCombatEncounterIds, storyState::hasAbility))`
4. Post-collision carry: `TraversalSubsystem.PlatformCarry carry = traversalSubsystem.carryFor(groundedMovingPlatformId); if (carry != null) { playerPhysics.x += carry.dx(); playerPhysics.y += carry.dy(); }`
5. E key: `traversalSubsystem.tryInteract(playerX, playerY, INTERACT_RADIUS + 18f, storyState::hasAbility)` replaces `tryInteractAbilityTrigger()`
6. `notifyAbilityGateBlocked(tile)` calls `traversalSubsystem.gateForTile(tile)` and `traversalSubsystem.barrierForTile(tile)`
7. Collision resolution: `groundedMovingPlatformId = traversalSubsystem.platformIdForTile(tile)` replaces `movingPlatformIdForTile(tile)`
8. Save/load: `traversalSubsystem.restoreFromFlags(storyState::hasFlag)` replaces `restoreActivatedAbilityTriggersFromFlags()`
9. Rendering: uses `traversalSubsystem.allMovingPlatforms()`, `.allGates()`, `.allTriggers()` etc.

---

## Extraction Order

```
Step 1  Claude Code   Extract 6 inner types to standalone package files          DONE 2026-05-07
                      TriggerMode, MovingPlatform, AbilityGate, CombatBarrier,
                      TriggeredPlatform, AbilityTrigger
                      Compile only — no delegation wiring yet

Step 2  Claude Code   Create TraversalSubsystem.java + wire PlaytestClient        DONE 2026-05-07
                      Full compile + runRegressionTests must pass

Step 3  Copilot       [SKIPPED if no stubs remain]

Step 4  Claude Code   Integration review — coupling creep + layer contracts       DONE 2026-05-07

Step 5  Codex CLI     Final validation gate
```

---

## Layer Contract

`TraversalSubsystem` lives in `java/client`. It may import `core.physics` types (`TileRect`, `TileType`, `SpatialHash`) and `core.StoryState` (only via Predicate<String> abstractions — do not import StoryState directly into TraversalSubsystem). All traversal logic is client-owned.
