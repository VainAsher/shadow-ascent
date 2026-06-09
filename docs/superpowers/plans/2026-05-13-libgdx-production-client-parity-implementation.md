# LibGDX Production Client Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Advance `runGame` from a rendering-and-overlay validation slice into a broader production-client slice with dialogue interaction, pause/save/load flow, title-to-game flow, audio hooks, and richer authored gameplay feedback.

**Architecture:** Keep `core` authoritative for simulation, mission state, save payloads, and dialogue selection. Keep `HubScreen` as the gameplay screen for now, but add explicit production-client presentation helpers around it instead of growing more ad hoc overlay state into one file. Route all player-facing non-world UI through focused LibGDX-side render/controller classes, and continue using `GameSimulator.drainEvents()` as the only simulation-to-presentation bus.

**Tech Stack:** Java 21, LibGDX 1.12.1, LWJGL3 desktop backend, JUnit 5, existing `GameState`/`HubManager`/`MissionManager`/`GameSimulator`, existing prototype-side `PlaytestClient` / `SaveLoad` / `StoryManager` behavior as reference.

---

## Current Branch Status

Already landed in `runGame` and verified:

- Atlas-backed world/entity rendering through `SpriteWorldRenderer`
- Shared modal stack via `ModalOverlayManager`
- Persistent HUD + minimap
- Inventory, shop, and crafting overlays
- Gameplay input suppression while modals are active
- `:client:test`, `clean :client:compileJava`, `packSprites`, and `runRegressionTests` all pass on this branch

Still missing from the production-client path:

- NPC dialogue presentation comparable to `PlaytestClient.interactNearestNpc()`
- production-client pause/save/load surface
- title/new-game/continue/gameplay flow
- audio manager and event-driven music/SFX hooks
- richer interaction hinting / mission action surfacing already present in the prototype HUD path
- any explicit smoke-test coverage for `runGame`-level screen flow

Do **not** re-implement:

- basic attack, jump, dash, or the game loop itself
- those are already present in `GameSimulator`, `PlayerInputController`, `GameInputProcessor`, `ShadowAscentGame`, and `HubScreen`

## File Structure

Primary gameplay files to modify:

- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
  - Own top-level client object graph, screen switching, save-path configuration, and shared gameplay services.
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
  - Own gameplay-frame orchestration, world rendering order, and integration points for dialogue/pause/audio/hint updates.
- Modify: `java/client/src/main/java/com/shadowascent/client/input/GameInputProcessor.java`
  - Own one-frame UI signals for pause, save/load shortcuts, and any modal-confirm/back behavior.
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
  - Extend to carry contextual interaction hint and optional dialogue/pause status.
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
  - Render the richer status/hint lines without regressing orientation or projection handling.
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/UiText.java`
  - Canonical text formatting helpers for dialogue labels, hints, and save-flow messages.
- Modify: `java/client/src/main/java/com/shadowascent/client/SaveLoad.java`
  - Reuse the existing save/load wrapper from the prototype path rather than re-embedding file I/O in `HubScreen`.

New production-client files to create:

- Create: `java/client/src/main/java/com/shadowascent/client/ui/DialogueOverlayRenderer.java`
- Create: `java/client/src/main/java/com/shadowascent/client/ui/PauseMenuOverlayRenderer.java`
- Create: `java/client/src/main/java/com/shadowascent/client/screens/TitleScreen.java`
- Create: `java/client/src/main/java/com/shadowascent/client/audio/AudioManager.java`
- Create: `java/client/src/main/resources/audio/audio_registry.json`

Tests to create:

- Create: `java/client/src/test/java/com/shadowascent/client/ui/DialogueOverlayRendererStateTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/ui/PauseMenuOverlayRendererStateTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/audio/AudioManagerEventRoutingTest.java`

Tests to modify:

- Modify: `java/client/src/test/java/com/shadowascent/client/input/GameInputProcessorUiRoutingTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`

Docs to update after code lands:

- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `README.md`

## Decomposition Notes

This plan intentionally focuses the **next production-client tranche** on parity and usability, not full asset migration or Tiled replacement. It keeps the scope bounded to five deliverable slices:

1. dialogue interaction in `runGame`
2. pause/save/load flow in `runGame`
3. title/new-game/continue screen flow
4. event-driven audio wiring
5. richer mission/interact hint surfacing

Anything larger than that should become a separate authored-geometry / map-integration plan.

## Task 1: Port NPC Dialogue Interaction Into a Dedicated LibGDX Modal

**Files:**
- Create: `java/client/src/main/java/com/shadowascent/client/ui/DialogueOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/DialogueOverlayRendererStateTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.shadowascent.client.ui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DialogueOverlayRendererStateTest {

    @Test
    void openAdvanceAndCloseTrackDialogueState() {
        DialogueOverlayRenderer overlay = new DialogueOverlayRenderer();

        overlay.open("Merchant Rilu", List.of("Welcome.", "Need supplies?"));

        assertTrue(overlay.isVisible());
        assertEquals("Merchant Rilu", overlay.speakerName());
        assertEquals("Welcome.", overlay.currentLine());

        assertTrue(overlay.advance());
        assertEquals("Need supplies?", overlay.currentLine());

        assertFalse(overlay.advance());
        assertFalse(overlay.isVisible());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.DialogueOverlayRendererStateTest"`

Expected: FAIL with missing class.

- [ ] **Step 3: Write minimal implementation**

```java
package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import java.util.ArrayList;
import java.util.List;

public final class DialogueOverlayRenderer {
    private final Matrix4 textProjection = new Matrix4();
    private final List<String> lines = new ArrayList<>();
    private String speakerName = "";
    private int lineIndex;
    private boolean visible;

    public void open(String speakerName, List<String> dialogueLines) {
        this.speakerName = speakerName == null ? "Unknown" : speakerName;
        this.lines.clear();
        this.lines.addAll(dialogueLines == null || dialogueLines.isEmpty() ? List.of("...") : dialogueLines);
        this.lineIndex = 0;
        this.visible = true;
    }

    public boolean advance() {
        if (!visible) return false;
        if (lineIndex + 1 < lines.size()) {
            lineIndex++;
            return true;
        }
        close();
        return false;
    }

    public void close() {
        visible = false;
        lineIndex = 0;
    }

    public boolean isVisible() { return visible; }
    public String speakerName() { return speakerName; }
    public String currentLine() { return lines.isEmpty() ? "..." : lines.get(lineIndex); }

    public void render(SpriteBatch batch, BitmapFont font, int screenWidth, int screenHeight) {
        if (!visible) return;
        textProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        batch.setProjectionMatrix(textProjection);
        batch.begin();
        font.setColor(UiPalette.TEXT);
        font.draw(batch, speakerName, 120f, screenHeight - 140f);
        font.draw(batch, currentLine(), 120f, screenHeight - 168f);
        font.draw(batch, "Enter continue  |  Esc close", 120f, screenHeight - 196f);
        batch.end();
    }
}
```

```java
// In ShadowAscentGame.java:
DialogueOverlayRenderer dialogueOverlayRenderer;
dialogueOverlayRenderer = new DialogueOverlayRenderer();
```

```java
// In HubScreen.java, near interact handling:
if (interactPressed && hudPlayer != null && !game.overlayManager.hasActiveOverlay()) {
    String speaker = nearestNpcName(hudPlayer);
    String line = nearestNpcDialogue(hudPlayer);
    if (speaker != null) {
        game.dialogueOverlayRenderer.open(speaker, List.of(line));
        game.overlayManager.open(OverlayType.DIALOGUE);
        appendEventFeedLine("Talk: " + speaker);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.DialogueOverlayRendererStateTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/ui/DialogueOverlayRenderer.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java java/client/src/test/java/com/shadowascent/client/ui/DialogueOverlayRendererStateTest.java
git commit -m "feat: add libgdx dialogue modal"
```

## Task 2: Add Pause / Save / Load Flow as a Production-Client Modal

**Files:**
- Create: `java/client/src/main/java/com/shadowascent/client/ui/PauseMenuOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/SaveLoad.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/input/GameInputProcessor.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/PauseMenuOverlayRendererStateTest.java`
- Test: `java/client/src/test/java/com/shadowascent/client/input/GameInputProcessorUiRoutingTest.java`

- [ ] **Step 1: Write the failing tests**

```java
package com.shadowascent.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PauseMenuOverlayRendererStateTest {

    @Test
    void selectionMovesWithinMenuBounds() {
        PauseMenuOverlayRenderer overlay = new PauseMenuOverlayRenderer();

        overlay.moveDown();
        overlay.moveDown();
        overlay.moveUp();

        assertEquals(1, overlay.selectedIndex());
    }
}
```

```java
@Test
void pauseSaveAndLoadAreExposedAsOneFrameUiSignals() {
    GameInputProcessor input = new GameInputProcessor(new GameSimulator(), "player1", new ModalOverlayManager());

    assertTrue(input.keyDown(Keys.ESCAPE));
    assertTrue(input.consumePausePressed());

    assertTrue(input.keyDown(Keys.F5));
    assertTrue(input.consumeSavePressed());

    assertTrue(input.keyDown(Keys.F9));
    assertTrue(input.consumeLoadPressed());
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.PauseMenuOverlayRendererStateTest" --tests "com.shadowascent.client.input.GameInputProcessorUiRoutingTest"`

Expected: FAIL because pause/save/load signals and menu renderer do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```java
package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

import java.util.List;

public final class PauseMenuOverlayRenderer {
    private final Matrix4 textProjection = new Matrix4();
    private final List<String> options = List.of("Resume", "Save", "Load", "Quit To Title");
    private int selectedIndex;

    public void moveUp() { selectedIndex = Math.max(0, selectedIndex - 1); }
    public void moveDown() { selectedIndex = Math.min(options.size() - 1, selectedIndex + 1); }
    public int selectedIndex() { return selectedIndex; }
    public String selectedOption() { return options.get(selectedIndex); }

    public void render(SpriteBatch batch, BitmapFont font, int screenWidth, int screenHeight) {
        textProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        batch.setProjectionMatrix(textProjection);
        batch.begin();
        float lineY = screenHeight - 180f;
        for (int i = 0; i < options.size(); i++) {
            font.setColor(i == selectedIndex ? UiPalette.ACCENT : UiPalette.TEXT);
            font.draw(batch, (i == selectedIndex ? "> " : "  ") + options.get(i), 140f, lineY - i * 24f);
        }
        batch.end();
    }
}
```

```java
// In GameInputProcessor.java:
private boolean pausePressed;
private boolean savePressed;
private boolean loadPressed;

public boolean consumePausePressed() { boolean pressed = pausePressed; pausePressed = false; return pressed; }
public boolean consumeSavePressed() { boolean pressed = savePressed; savePressed = false; return pressed; }
public boolean consumeLoadPressed() { boolean pressed = loadPressed; loadPressed = false; return pressed; }
```

```java
// In GameInputProcessor.apply(...):
case Keys.ESCAPE -> {
    if (pressed) {
        cancelPressed = true;
        pausePressed = true;
    }
    cmd.menuBack = pressed;
    return true;
}
case Keys.F5 -> {
    if (pressed) savePressed = true;
    return true;
}
case Keys.F9 -> {
    if (pressed) loadPressed = true;
    return true;
}
```

```java
// In ShadowAscentGame.java:
SaveLoad saveLoad = new SaveLoad(gameState, Path.of("save", "runGame_slot1.sav"));
PauseMenuOverlayRenderer pauseMenuOverlayRenderer = new PauseMenuOverlayRenderer();
```

```java
// In HubScreen.java:
if (game.inputProcessor.consumePausePressed() && !game.overlayManager.hasActiveOverlay()) {
    game.overlayManager.open(OverlayType.PAUSE);
}
if (game.inputProcessor.consumeSavePressed()) {
    saveCurrentState();
}
if (game.inputProcessor.consumeLoadPressed()) {
    loadCurrentState();
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.PauseMenuOverlayRendererStateTest" --tests "com.shadowascent.client.input.GameInputProcessorUiRoutingTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/ui/PauseMenuOverlayRenderer.java java/client/src/main/java/com/shadowascent/client/SaveLoad.java java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/main/java/com/shadowascent/client/input/GameInputProcessor.java java/client/src/test/java/com/shadowascent/client/ui/PauseMenuOverlayRendererStateTest.java java/client/src/test/java/com/shadowascent/client/input/GameInputProcessorUiRoutingTest.java
git commit -m "feat: add libgdx pause save load flow"
```

## Task 3: Add Title Screen and New Game / Continue Routing

**Files:**
- Create: `java/client/src/main/java/com/shadowascent/client/screens/TitleScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`

- [ ] **Step 1: Write the failing smoke-path test**

```java
@Test
void newGameAndContinueShareOneScreenEntryPoint() {
    // This is a structure test, not a rendering assertion.
    // The game should expose two explicit methods so TitleScreen does not mutate gameplay state directly.
    assertDoesNotThrow(() -> ShadowAscentGame.class.getDeclaredMethod("startNewGame"));
    assertDoesNotThrow(() -> ShadowAscentGame.class.getDeclaredMethod("continueFromSave"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ShadowAscentGameStructureTest"`

Expected: FAIL with missing test class and missing methods.

- [ ] **Step 3: Write minimal implementation**

```java
package com.shadowascent.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.shadowascent.client.ShadowAscentGame;

public final class TitleScreen extends ScreenAdapter {
    private final ShadowAscentGame game;

    public TitleScreen(ShadowAscentGame game) {
        this.game = game;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.justTouched()) {
            game.startNewGame();
        }
    }
}
```

```java
// In ShadowAscentGame.java:
public void startNewGame() {
    gameState = new GameState();
    createGameplaySession();
    setScreen(new HubScreen(this, gameState));
}

public void continueFromSave() {
    createGameplaySession();
    setScreen(new HubScreen(this, gameState));
}
```

```java
// In ShadowAscentGame.create():
setScreen(new TitleScreen(this));
```

- [ ] **Step 4: Run verification**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ShadowAscentGameStructureTest"`

Expected: PASS.

Run: `.\gradlew.bat :client:compileJava`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/screens/TitleScreen.java java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/test/java/com/shadowascent/client/ShadowAscentGameStructureTest.java
git commit -m "feat: add libgdx title and continue flow"
```

## Task 4: Add Event-Driven Audio Wiring

**Files:**
- Create: `java/client/src/main/java/com/shadowascent/client/audio/AudioManager.java`
- Create: `java/client/src/main/resources/audio/audio_registry.json`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Test: `java/client/src/test/java/com/shadowascent/client/audio/AudioManagerEventRoutingTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.shadowascent.client.audio;

import com.shadowascent.core.simulation.SimEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AudioManagerEventRoutingTest {

    @Test
    void eventRoutingSelectsExpectedSoundKey() {
        AudioManager audio = new AudioManager();

        String soundKey = audio.resolveSoundKey(List.of(new SimEvent("PLAYER_DAMAGED", "player1", java.util.Map.of())));

        assertEquals("player_hurt", soundKey);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.audio.AudioManagerEventRoutingTest"`

Expected: FAIL with missing class.

- [ ] **Step 3: Write minimal implementation**

```java
package com.shadowascent.client.audio;

import com.shadowascent.core.simulation.SimEvent;

import java.util.List;

public final class AudioManager {

    public String resolveSoundKey(List<SimEvent> events) {
        for (SimEvent event : events) {
            switch (event.type()) {
                case "PLAYER_DAMAGED": return "player_hurt";
                case "ENEMY_DEFEATED": return "enemy_defeated";
                case "PORTAL_ACTIVATED": return "portal_activate";
                default: break;
            }
        }
        return null;
    }

    public void processEvents(List<SimEvent> events) {
        String soundKey = resolveSoundKey(events);
        if (soundKey != null) {
            // First slice: resolution only, safe no-op playback until assets are loaded.
        }
    }
}
```

```json
{
  "sfx": {
    "player_hurt": "audio/sfx/player_hurt.ogg",
    "enemy_defeated": "audio/sfx/enemy_defeated.ogg",
    "portal_activate": "audio/sfx/portal_activate.ogg"
  },
  "music": {
    "hub": "audio/music/hub.ogg"
  }
}
```

```java
// In ShadowAscentGame.java:
AudioManager audioManager = new AudioManager();
```

```java
// In HubScreen.java, immediately after drainEvents():
game.audioManager.processEvents(events);
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.audio.AudioManagerEventRoutingTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/audio/AudioManager.java java/client/src/main/resources/audio/audio_registry.json java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/test/java/com/shadowascent/client/audio/AudioManagerEventRoutingTest.java
git commit -m "feat: add libgdx audio event routing"
```

## Task 5: Port Richer Interaction Hints and Mission Action Surfacing

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void hudStateCarriesInteractionHintIndependentlyFromContextualHint() {
    HudOverlayState state = new HudOverlayState(
            "Act I", "Lantern Heights", "Mission", "Objective",
            3, 3, "Context", "Overlay: none", List.of(), true,
            "[E] Talk to Merchant Rilu");

    assertEquals("[E] Talk to Merchant Rilu", state.interactionHint());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.HudOverlayStateTest"`

Expected: FAIL because the state record lacks `interactionHint`.

- [ ] **Step 3: Write minimal implementation**

```java
// In HudOverlayState.java:
public record HudOverlayState(
        String actId,
        String plateauId,
        String missionTitle,
        String objectiveLine,
        int playerHealth,
        int playerMaxHealth,
        String contextualHint,
        String overlayStatus,
        List<String> eventFeedLines,
        boolean showMinimap,
        String interactionHint) {
}
```

```java
// In HudOverlayRenderer.java:
font.setColor(UiPalette.TEXT_MUTED);
font.draw(batch, UiText.contextualHint(state.contextualHint()), textX, lineY);

lineY -= UiPalette.LINE_HEIGHT;
font.draw(batch, UiText.contextualHint(state.interactionHint()), textX, lineY);
```

```java
// In HubScreen.buildHudState(...):
String interactionHint = hudPlayer == null
        ? "Explore east through traversal rooms."
        : resolveInteractionHint(hudPlayer);
```

- [ ] **Step 4: Run verification**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.HudOverlayStateTest"`

Expected: PASS.

Run: `.\gradlew.bat :client:compileJava`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java
git commit -m "feat: enrich libgdx hud interaction hints"
```

## Task 6: Documentation Sync and Full Verification

**Files:**
- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `README.md`

- [ ] **Step 1: Update docs to match the production-client parity tranche**

```md
- LibGDX `runGame` now includes:
  - persistent HUD and minimap
  - inventory / shop / crafting overlays
  - dialogue interaction modal
  - pause / save / load flow
  - title/new-game/continue routing
  - event-driven audio wiring
- `runPlayableClient` remains the broader QA/reference harness until authored geometry and screen-flow parity are complete.
```

- [ ] **Step 2: Run docs freshness verification**

Run: `python scripts/check_docs_freshness.py --emit-report`

Expected: PASS with `Status: PASS`.

- [ ] **Step 3: Run client tests**

Run: `.\gradlew.bat :client:test`

Expected: PASS with `BUILD SUCCESSFUL`.

- [ ] **Step 4: Run compile, asset, and regression verification**

Run: `.\gradlew.bat clean :client:compileJava`

Expected: PASS.

Run: `.\gradlew.bat packSprites`

Expected: PASS and regenerated `assets/sprites/packed/sprites.png` plus `assets/sprites/packed/sprites.atlas`.

Run: `.\gradlew.bat runRegressionTests`

Expected: PASS / exit code `0`.

- [ ] **Step 5: Commit**

```bash
git add docs/CURRENT_STATE.md docs/ROADMAP.md docs/IMPLEMENTATION_BACKLOG.md docs/PLAYABLE_TRUTH.md README.md docs/reports/docs_freshness_report.md
git commit -m "docs: sync libgdx production client parity status"
```

## Self-Review

Spec coverage:

- dialogue interaction: covered by Task 1
- pause/save/load: covered by Task 2
- title/new-game/continue flow: covered by Task 3
- audio event wiring: covered by Task 4
- richer gameplay hints: covered by Task 5
- docs and verification: covered by Task 6

Placeholder scan:

- No task says “port later” without naming files and concrete next steps.
- Verification commands all use the root Gradle wrapper, matching the actual repo layout.
- The plan explicitly avoids redoing attack/game-loop work that already exists.

Type consistency:

- `HubScreen` remains the gameplay owner.
- `ShadowAscentGame` remains the object graph / screen router.
- new production-client presentation classes live under `client.ui`, `client.audio`, and `client.screens`.
- save/load reuses `SaveLoad` rather than introducing a second persistence wrapper.

## Notes for the Implementer

- Keep `GameSimulator`, `MissionManager`, `HubManager`, and `GameState` authoritative. Do not fork mission or dialogue logic into the client layer.
- Do not broaden this plan into Tiled map replacement or full art migration. Those are separate plans.
- Keep `SpriteWorldRenderer` responsible only for world/entity drawing.
- Treat `PlaytestClient` as the behavioral reference for dialogue, save/load, and mission feedback, not as a class to copy wholesale.

Plan complete and saved to `docs/superpowers/plans/2026-05-13-libgdx-production-client-parity-implementation.md`. Two execution options:

1. Subagent-Driven (recommended) - I dispatch a fresh subagent per task, review between tasks, fast iteration
2. Inline Execution - Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?
