---
handover_type: design_doc
milestone: Wave 4
status: ready-for-extraction
created: 2026-05-08
---
# Design — Wave 4 UISubsystem Extraction

Workflow 2, Step 1 output. Defines the decomposition boundary, interface contracts, and extraction
order for pulling all HUD, rendering, and display-update logic out of `PlaytestClient.java`.

---

## 1. Context

After CombatSubsystem and TraversalSubsystem extractions, `PlaytestClient.java` is ~100KB /
2349 lines. Roughly 600 lines are rendering methods (`draw*`). Another ~130 lines are
display-state fields, display-update logic, and helper methods that exist solely to feed the
renderer. UISubsystem extraction removes all of that.

Target post-extraction size: ~1500 lines (~60KB).

---

## 2. What UISubsystem Owns

### 2a. State Fields (move from PlaytestClient)

| Field | Type | Rationale |
|---|---|---|
| `eventLog` | `Deque<String>` | Pure display — shown in the event log panel |
| `npcPositions` | `Map<String, Point2D.Float>` | Computed for display only |
| `showMinimap` | `boolean` | Display preference toggled by `M` key |
| `missionFeedbackLine` | `String` | Flash message shown in HUD |
| `missionFeedbackSeconds` | `float` | Countdown for the flash message |
| `interactionHint` | `String` | Per-frame hint shown at bottom of HUD |
| `surfacedAbilitySnapshot` | `Set<String>` | Tracks which unlocks have been surfaced to player |
| `gateFeedbackCooldownSeconds` | `float` | Rate-limits gate-blocked log/feedback spam |

### 2b. Methods (move from PlaytestClient)

**Rendering (all take `Graphics2D g`):**
- `drawBackground(g)`
- `drawHubRing(g)`
- `drawRoomFrame(g, roomStartX, roomWidth, label)` — helper for drawHubRing
- `drawWorldGeometry(g)` — reads traversalSubsystem + combatSubsystem
- `drawMinimap(g, RenderState state)`
- `drawNpcs(g, RenderState state)`
- `drawPlayer(g, RenderState state)`
- `drawHud(g, RenderState state)`
- `drawEventLog(g)`

**Display updates (called from PlaytestClient's tick loop):**
- `updateNpcPositions()` — reads storyState.getAllNPCs(), uses held refs
- `updateInteractionHint(float playerX, float playerY)` — queries subsystems
- `updateMissionFeedbackLine()` — reads storyState/missionManager
- `detectNewAbilityUnlocks()` — compares storyState.getAbilities() to snapshot

**Feedback:**
- `notifyAbilityGateBlocked(TileRect tile)` — checks gateFeedbackCooldown, looks up
  gate/barrier via traversalSubsystem, sets missionFeedback + logs

**Static helpers (stay as private static in UISubsystem):**
- `missionCompletionPercent(Mission m)`
- `firstPendingObjective(Mission m)`
- `activeNpcsSorted()` — reads held storyState ref

---

## 3. Interface Design

### 3a. Constructor

```java
UISubsystem(
    StoryState storyState,
    MissionManager missionManager,
    TraversalSubsystem traversalSubsystem,
    CombatSubsystem combatSubsystem,
    List<TileRect> collisionTiles,
    WorldGeometry geometry,
    Consumer<String> evidenceLogger      // for addEventLogLine to also write evidence
)
```

`WorldGeometry` is a new record holding all the layout constants UISubsystem needs for rendering:

```java
record WorldGeometry(
    float worldLeftX, float worldRightX,
    float floorY, float ceilingY,
    float windowWidth, float windowHeight,
    float playerRadius,
    float interactRadius,
    float hubRoomEndX, float forgeRoomEndX, float shaftRoomEndX, float summitRoomEndX
)
```

The `evidenceLogger` callback lets UISubsystem call `writeEvidenceLine("EVENT", message)` from
inside `addEventLogLine` without holding a reference back to PlaytestClient. Pass
`(msg) -> writeEvidenceLine("EVENT", msg)` in the constructor call.

### 3b. RenderState Record

All mutable per-frame state PlaytestClient holds that the renderer needs:

```java
record RenderState(
    float playerX,
    float playerY,
    float cameraX,
    boolean isDashing,
    float dashTimerSeconds,
    float dashCooldownSeconds,
    int playerHealth,
    boolean playerDead,
    int jumpCount,
    float wallStaminaSeconds,
    boolean wallExhaustedAwaitGround,
    float attackCooldownSeconds,
    Set<String> clearedCombatEncounterIds,
    float sessionElapsedSeconds,
    float missionTimerSeconds
) {}
```

`missionTimerSeconds` is `storyState.getMissionTimer()` — avoids UISubsystem needing to call
through to storyState for that one field during drawHud.

### 3c. Public API

```java
// Main rendering entry point — called from PlayPanel.paintComponent
void drawFrame(Graphics2D g, RenderState state)

// Tick — timer countdown + display updates. Call once per game tick before drawFrame.
void tickAndUpdate(float playerX, float playerY, float dt)

// Called from PlaytestClient.log() — queues line for display + fires evidenceLogger
void addEventLogLine(String message)

// Set the flash feedback line (from callbacks, dashFeedback, takeDamage, etc.)
void setMissionFeedback(String line, float seconds)

// Called from resolveHorizontalCollisions when a DOOR_LOCKED tile is hit
void notifyAbilityGateBlocked(TileRect tile, StoryState storyState, Set<String> clearedEncounterIds)

// Toggle minimap (bound to M key)
void toggleMinimap()

// Seed ability snapshot on startup / load
void seedAbilitySnapshot(Set<String> currentAbilities)
```

Note: `notifyAbilityGateBlocked` needs `storyState` and `clearedEncounterIds` because
it checks `gate.isUnlocked(storyState)` and `barrier.isUnlocked(clearedIds)` after the
traversalSubsystem lookup. These could also be injected at construction — choose at
implementation time based on what's cleaner.

---

## 4. PlaytestClient After Extraction

### Fields removed
All 8 fields listed in section 2a.

### Methods removed / replaced
All 18+ methods listed in section 2b. 

### `paintComponent` after extraction

```java
@Override
protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    Graphics2D g = (Graphics2D) graphics.create();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    uiSubsystem.drawFrame(g, buildRenderState());
    g.dispose();
}
```

### `tick()` delta
Remove per-tick timer decrements for `gateFeedbackCooldownSeconds` and
`missionFeedbackSeconds` — UISubsystem ticks those internally.

Replace `updateInteractionHint()`, `updateMissionFeedbackLine()`, `detectNewAbilityUnlocks()`,
`updateNpcPositions()` calls with:

```java
uiSubsystem.tickAndUpdate(playerPhysics.x, playerPhysics.y, dt);
```

### Callbacks that set missionFeedback
Every call site that currently does:
```java
missionFeedbackLine = "...";
missionFeedbackSeconds = FEEDBACK_FLASH_SECONDS;
```
becomes:
```java
uiSubsystem.setMissionFeedback("...", FEEDBACK_FLASH_SECONDS);
```

Sites: `onCombatEncounterCleared`, `onAbilityTriggerActivated`, `dashFeedback`,
`startDash`, `takeDamage`, `onPlayerDeath`, `resetPlayerAfterDeath`,
`startNextAvailableMission`, `interactNearestNpc`, `advanceObjective`,
`saveState`, `loadState`, `detectNewAbilityUnlocks`.

### `log()` after extraction
```java
private void log(String message) {
    if (message == null || message.isBlank()) return;
    uiSubsystem.addEventLogLine(message);
    // evidenceLogger callback inside UISubsystem fires automatically
}
```

Remove direct `writeEvidenceLine` call — it fires via the `evidenceLogger` Consumer
injected at construction.

Actually: keep `writeEvidenceLine` calls in PlaytestClient for non-EVENT kinds
(SESSION_START, SESSION_END, SNAPSHOT, MOVEMENT_SIGNOFF). Only the inline EVENT write
moves to UISubsystem via the callback.

---

## 5. Layer Contract

`UISubsystem` stays in `com.shadowascent.client`. It may import:
- `com.shadowascent.core.StoryState`
- `com.shadowascent.core.Mission`
- `com.shadowascent.core.MissionManager`
- `com.shadowascent.core.NPC`
- `com.shadowascent.core.GameConfig`
- `com.shadowascent.core.physics.TileRect`
- `com.shadowascent.core.physics.TileType`
- Swing/AWT (`javax.swing.*`, `java.awt.*`)
- `TraversalSubsystem`, `CombatSubsystem` (same package)

It must NOT import from `core.physics.PhysicsState` except via the `RenderState` record
(float fields only — no PhysicsState reference). No back-reference to `PlaytestClient`.

---

## 6. Extraction Order

Do each step, compile-check, then proceed:

1. Create `WorldGeometry.java` record in `com.shadowascent.client`
2. Create `UISubsystem.java` stub — constructor + `RenderState` record + all public methods
   as empty stubs (this lets PlaytestClient wire up immediately)
3. Wire PlaytestClient constructor to create `uiSubsystem`; replace field declarations
4. Redirect all `missionFeedbackLine/Seconds` set-sites to `uiSubsystem.setMissionFeedback()`
5. Replace `log()` body to call `uiSubsystem.addEventLogLine()`
6. Replace `tick()` display-update calls with `uiSubsystem.tickAndUpdate()`
7. Replace `paintComponent` with `uiSubsystem.drawFrame()` + `buildRenderState()` helper
8. Move all `draw*` method bodies into UISubsystem
9. Move all display-update method bodies into UISubsystem
10. Delete 8 state fields from PlaytestClient
11. Delete moved methods from PlaytestClient
12. Compile + run regression gate

---

## 7. Codex CLI Validation

After implementation, create `CODEX_WAVE4_UI_VALIDATE.md` with:

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runRegressionTests
```

Expected: BUILD SUCCESSFUL, 25/25 pass, no new compile errors.
