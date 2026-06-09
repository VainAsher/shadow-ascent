# LibGDX UI Port Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the LibGDX `runGame` UI port by building the missing minimap, inventory, shop, and crafting overlays on top of the already-landed HUD and modal-state foundation.

**Architecture:** Keep `HubScreen` as the single gameplay surface and layer one persistent HUD plus one active modal above it. Reuse the existing `HudOverlayState`, `HudOverlayRenderer`, `ModalOverlayManager`, and `GameInputProcessor` suppression path, but fix the current split-brain overlay-manager wiring before adding new overlays. Route modal open/close/input through `HubScreen`, keep world rendering in `SpriteWorldRenderer`, and use root-project Gradle commands from the repository root.

**Tech Stack:** Java 21, LibGDX 1.12.1, JUnit 5, existing `GameSimulator` runtime, existing Swing-side `InventoryPanel` / `ShopPanel` / `CraftingPanel` as behavior references.

---

## Current Branch Status

Already implemented and passing:

- `java/client/src/main/java/com/shadowascent/client/ui/OverlayType.java`
- `java/client/src/main/java/com/shadowascent/client/ui/ModalOverlayManager.java`
- `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- `java/client/src/main/java/com/shadowascent/client/ui/UiPalette.java`
- `java/client/src/main/java/com/shadowascent/client/ui/UiText.java`
- `java/client/src/test/java/com/shadowascent/client/ui/ModalOverlayManagerTest.java`
- `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`
- `java/client/src/test/java/com/shadowascent/client/input/GameInputProcessorUiRoutingTest.java`

Verified on this branch:

- `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.ModalOverlayManagerTest" --tests "com.shadowascent.client.ui.HudOverlayStateTest" --tests "com.shadowascent.client.input.GameInputProcessorUiRoutingTest"` from repo root: PASS
- `.\gradlew.bat :client:compileJava` from repo root: PASS

Known drift / blockers to resolve first:

- `ShadowAscentGame` creates `GameInputProcessor(simulator, PLAYER_ID)` with its own private `ModalOverlayManager`, then separately creates `overlayManager`. The gameplay input path and HUD are not sharing one modal-state source of truth.
- The original plan’s `.\gradlew.bat` examples assumed running inside `java/`. In this repo, the working entrypoint is the root wrapper and the correct project paths are `:client`, `:core`, and `:server`.
- The following LibGDX UI files do not exist yet:
  - `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
  - `java/client/src/main/java/com/shadowascent/client/ui/InventoryOverlayRenderer.java`
  - `java/client/src/main/java/com/shadowascent/client/ui/ShopOverlayRenderer.java`
  - `java/client/src/main/java/com/shadowascent/client/ui/CraftingOverlayRenderer.java`

## File Structure

Primary files to modify:

- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
  - Own shared UI object creation only.
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
  - Own per-frame HUD state, modal open/close rules, event-feed messages, and overlay rendering order.
- Modify: `java/client/src/main/java/com/shadowascent/client/input/GameInputProcessor.java`
  - Own gameplay suppression plus one-frame UI toggle/cancel signals.
- Modify: `java/client/src/main/java/com/shadowascent/client/rendering/SpriteWorldRenderer.java`
  - No planned changes; keep world/entity rendering isolated.

New UI files to create:

- Create: `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
- Create: `java/client/src/main/java/com/shadowascent/client/ui/InventoryOverlayRenderer.java`
- Create: `java/client/src/main/java/com/shadowascent/client/ui/ShopOverlayRenderer.java`
- Create: `java/client/src/main/java/com/shadowascent/client/ui/CraftingOverlayRenderer.java`

Tests to create:

- Create: `java/client/src/test/java/com/shadowascent/client/ui/InventoryOverlayRendererStateTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/ui/ShopOverlayRendererStateTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/ui/CraftingOverlayRendererStateTest.java`

Tests to modify:

- Modify: `java/client/src/test/java/com/shadowascent/client/ui/ModalOverlayManagerTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/input/GameInputProcessorUiRoutingTest.java`

Docs to modify after code lands:

- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `README.md`

## Completed Foundation Tasks

- [x] Modal overlay state core exists and is test-backed.
- [x] HUD overlay state model exists and is test-backed.
- [x] Game input suppression while a modal is open exists and is test-backed.
- [x] Basic LibGDX HUD shell rendering exists and compiles.

## Task 1: Unify Overlay State and Add UI Toggle Signals

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/input/GameInputProcessor.java`
- Test: `java/client/src/test/java/com/shadowascent/client/input/GameInputProcessorUiRoutingTest.java`

- [ ] **Step 1: Write the failing tests**

```java
@Test
void submitFrameUsesTheSharedOverlayManagerPassedFromGame() {
    GameSimulator simulator = new GameSimulator();
    simulator.addPlayer("player1", 0, 0f, 0f);
    ModalOverlayManager overlays = new ModalOverlayManager();
    GameInputProcessor input = new GameInputProcessor(simulator, "player1", overlays);

    input.keyDown(Keys.RIGHT);
    overlays.open(OverlayType.INVENTORY);
    input.submitFrame();

    assertFalse(input.lastSubmittedCommand().right);
}

@Test
void inventoryCraftingMinimapAndCancelAreExposedAsOneFrameUiSignals() {
    GameInputProcessor input = new GameInputProcessor(new GameSimulator(), "player1", new ModalOverlayManager());

    assertTrue(input.keyDown(Keys.I));
    assertTrue(input.consumeInventoryTogglePressed());
    assertFalse(input.consumeInventoryTogglePressed());

    assertTrue(input.keyDown(Keys.T));
    assertTrue(input.consumeCraftingTogglePressed());

    assertTrue(input.keyDown(Keys.M));
    assertTrue(input.consumeMinimapTogglePressed());

    assertTrue(input.keyDown(Keys.ESCAPE));
    assertTrue(input.consumeCancelPressed());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.input.GameInputProcessorUiRoutingTest"`

Expected: FAIL because the `T` / `M` / `ESCAPE` routing and consume helpers do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```java
// In ShadowAscentGame.create():
overlayManager = new ModalOverlayManager();
inputProcessor = new GameInputProcessor(simulator, PLAYER_ID, overlayManager);
```

```java
// In GameInputProcessor.java:
private boolean inventoryTogglePressed;
private boolean craftingTogglePressed;
private boolean minimapTogglePressed;
private boolean cancelPressed;

public boolean consumeInventoryTogglePressed() {
    boolean pressed = inventoryTogglePressed;
    inventoryTogglePressed = false;
    return pressed;
}

public boolean consumeCraftingTogglePressed() {
    boolean pressed = craftingTogglePressed;
    craftingTogglePressed = false;
    return pressed;
}

public boolean consumeMinimapTogglePressed() {
    boolean pressed = minimapTogglePressed;
    minimapTogglePressed = false;
    return pressed;
}

public boolean consumeCancelPressed() {
    boolean pressed = cancelPressed;
    cancelPressed = false;
    return pressed;
}
```

```java
// In GameInputProcessor.apply(...):
case Keys.I -> {
    if (pressed) inventoryTogglePressed = true;
    cmd.inventory = pressed;
    return true;
}
case Keys.T -> {
    if (pressed) craftingTogglePressed = true;
    return true;
}
case Keys.M -> {
    if (pressed) minimapTogglePressed = true;
    cmd.minimap = pressed;
    return true;
}
case Keys.ESCAPE -> {
    if (pressed) cancelPressed = true;
    cmd.menuBack = pressed;
    return true;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.input.GameInputProcessorUiRoutingTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java java/client/src/main/java/com/shadowascent/client/input/GameInputProcessor.java java/client/src/test/java/com/shadowascent/client/input/GameInputProcessorUiRoutingTest.java
git commit -m "feat: unify libgdx overlay manager and ui toggle input"
```

## Task 2: Add the Minimap Renderer and HUD Toggle

**Files:**
- Create: `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void minimapVisibilityFlagTracksHudToggleState() {
    HudOverlayState shown = new HudOverlayState("A", "P", "M", "O", 3, 3, "H", "S", List.of(), true);
    HudOverlayState hidden = new HudOverlayState("A", "P", "M", "O", 3, 3, "H", "S", List.of(), false);

    assertTrue(shown.showMinimap());
    assertFalse(hidden.showMinimap());
}
```

- [ ] **Step 2: Run test to verify it fails or requires consolidation**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.HudOverlayStateTest"`

Expected: If the test class needs consolidation, update that file to keep all assertions in one class and rerun until PASS/FAIL is meaningful.

- [ ] **Step 3: Write minimal implementation**

```java
// MinimapOverlayRenderer.java
package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.simulation.GameSimulator;

import java.util.List;

public final class MinimapOverlayRenderer {
    private final ShapeRenderer shapes;

    public MinimapOverlayRenderer(ShapeRenderer shapes) {
        this.shapes = shapes;
    }

    public void render(HudOverlayState state, GameSimulator simulator, List<TileRect> worldTiles, float x, float y) {
        if (!state.showMinimap()) {
            return;
        }
        // First slice: draw a docked panel and basic geometry/player dots.
    }
}
```

```java
// In ShadowAscentGame.java:
MinimapOverlayRenderer minimapOverlayRenderer;
minimapOverlayRenderer = new MinimapOverlayRenderer(uiShapes);
```

```java
// In HubScreen.java:
private boolean showMinimap = true;

if (game.inputProcessor.consumeMinimapTogglePressed()) {
    showMinimap = !showMinimap;
}
```

```java
// In buildHudState(...):
showMinimap
```

```java
// In render(...), after HUD render state is built:
HudOverlayState hudState = buildHudState(hudPlayer);
game.hudOverlayRenderer.render(hudState, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
game.minimapOverlayRenderer.render(hudState, game.simulator, game.worldTiles, Gdx.graphics.getWidth() - 220f, 16f);
```

- [ ] **Step 4: Run verification**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.HudOverlayStateTest"`

Expected: PASS.

Run: `.\gradlew.bat :client:compileJava`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java
git commit -m "feat: add libgdx minimap overlay toggle"
```

## Task 3: Add Inventory Overlay State and Renderer

**Files:**
- Create: `java/client/src/main/java/com/shadowascent/client/ui/InventoryOverlayRenderer.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/InventoryOverlayRendererStateTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.shadowascent.client.ui;

import com.shadowascent.core.simulation.SimInventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class InventoryOverlayRendererStateTest {

    @Test
    void inventorySelectionMovesWithinBounds() {
        InventoryOverlayRenderer overlay = new InventoryOverlayRenderer(new SimInventory());

        overlay.moveRight();
        overlay.moveRight();
        overlay.moveLeft();

        assertEquals(1, overlay.selectedIndex());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.InventoryOverlayRendererStateTest"`

Expected: FAIL with missing class.

- [ ] **Step 3: Write minimal implementation**

```java
package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shadowascent.core.simulation.SimInventory;

public final class InventoryOverlayRenderer {
    private final SimInventory inventory;
    private int selectedIndex;

    public InventoryOverlayRenderer(SimInventory inventory) {
        this.inventory = inventory;
    }

    public void moveLeft() { selectedIndex = Math.max(0, selectedIndex - 1); }
    public void moveRight() { selectedIndex = Math.min(SimInventory.MAX_SLOTS - 1, selectedIndex + 1); }
    public void moveUp() { selectedIndex = Math.max(0, selectedIndex - 4); }
    public void moveDown() { selectedIndex = Math.min(SimInventory.MAX_SLOTS - 1, selectedIndex + 4); }
    public int selectedIndex() { return selectedIndex; }

    public String useSelected() {
        SimInventory.Slot slot = inventory.slots[selectedIndex];
        return slot == null ? "Empty slot." : slot.itemId();
    }

    public void render(SpriteBatch batch, BitmapFont font, int screenWidth, int screenHeight) {
        // First slice: centered panel + selected slot summary.
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.InventoryOverlayRendererStateTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/ui/InventoryOverlayRenderer.java java/client/src/test/java/com/shadowascent/client/ui/InventoryOverlayRendererStateTest.java
git commit -m "feat: add libgdx inventory overlay renderer"
```

## Task 4: Wire Inventory Modal Lifecycle and Feedback

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/ModalOverlayManagerTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void toggleReplacesExistingModalInsteadOfStacking() {
    ModalOverlayManager manager = new ModalOverlayManager();

    manager.open(OverlayType.CRAFTING);
    manager.toggle(OverlayType.INVENTORY);

    assertEquals(OverlayType.INVENTORY, manager.activeOverlay());
}
```

- [ ] **Step 2: Run test to verify it fails or exposes coverage gaps**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.ModalOverlayManagerTest"`

Expected: If behavior already passes, keep the test as regression coverage and continue.

- [ ] **Step 3: Write minimal implementation**

```java
// In ShadowAscentGame.java:
InventoryOverlayRenderer inventoryOverlayRenderer;
inventoryOverlayRenderer = new InventoryOverlayRenderer(new SimInventory());
```

```java
// In HubScreen.render(...):
if (game.inputProcessor.consumeInventoryTogglePressed()) {
    game.overlayManager.toggle(OverlayType.INVENTORY);
}

if (game.overlayManager.activeOverlay() == OverlayType.INVENTORY) {
    game.inventoryOverlayRenderer.render(game.batch, game.uiFont, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
}
```

```java
// In HubScreen.render(...), while inventory overlay is active:
if (game.inputProcessor.consumeCancelPressed()) {
    game.overlayManager.close();
}
```

```java
// In HubScreen append returned action strings:
appendEventFeedLine(game.inventoryOverlayRenderer.useSelected());
```

- [ ] **Step 4: Run verification**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.ModalOverlayManagerTest"`

Expected: PASS.

Run: `.\gradlew.bat :client:compileJava`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/test/java/com/shadowascent/client/ui/ModalOverlayManagerTest.java
git commit -m "feat: wire libgdx inventory modal lifecycle"
```

## Task 5: Add Shop Overlay State and Merchant Open Flow

**Files:**
- Create: `java/client/src/main/java/com/shadowascent/client/ui/ShopOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/ShopOverlayRendererStateTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.shadowascent.client.ui;

import com.shadowascent.core.simulation.SimInventory;
import com.shadowascent.core.simulation.SimShop;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ShopOverlayRendererStateTest {

    @Test
    void focusTogglesBetweenShopAndInventoryColumns() {
        ShopOverlayRenderer overlay = new ShopOverlayRenderer();
        overlay.open(new SimShop("merchant_npc", 2, 12345L), new SimInventory());

        assertTrue(overlay.isShopFocus());
        overlay.toggleFocus();
        assertFalse(overlay.isShopFocus());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.ShopOverlayRendererStateTest"`

Expected: FAIL with missing class.

- [ ] **Step 3: Write minimal implementation**

```java
package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shadowascent.core.simulation.SimInventory;
import com.shadowascent.core.simulation.SimShop;

public final class ShopOverlayRenderer {
    private SimShop shop;
    private SimInventory inventory;
    private boolean shopFocus = true;

    public void open(SimShop shop, SimInventory inventory) {
        this.shop = shop;
        this.inventory = inventory;
        this.shopFocus = true;
    }

    public void toggleFocus() { shopFocus = !shopFocus; }
    public boolean isShopFocus() { return shopFocus; }

    public void render(SpriteBatch batch, BitmapFont font, int screenWidth, int screenHeight) {
        // First slice: wide centered panel with focus label.
    }
}
```

```java
// In HubScreen.java:
private boolean nearMerchant(SimPlayer player) {
    float merchantX = 300f;
    float merchantY = 330f;
    float dx = (player.physics.x + player.physics.width * 0.5f) - merchantX;
    float dy = (player.physics.y + player.physics.height * 0.5f) - merchantY;
    return (dx * dx + dy * dy) <= (70f * 70f);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.ShopOverlayRendererStateTest"`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/ui/ShopOverlayRenderer.java java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/test/java/com/shadowascent/client/ui/ShopOverlayRendererStateTest.java
git commit -m "feat: add libgdx shop overlay renderer"
```

## Task 6: Wire Shop Replacement Semantics

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/test/java/com/shadowascent/client/ui/ModalOverlayManagerTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void shopOpenReplacesInventoryModal() {
    ModalOverlayManager manager = new ModalOverlayManager();
    manager.open(OverlayType.INVENTORY);
    manager.open(OverlayType.SHOP);
    assertEquals(OverlayType.SHOP, manager.activeOverlay());
}
```

- [ ] **Step 2: Run test to verify it fails or extends coverage**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.ModalOverlayManagerTest"`

Expected: If behavior already passes, keep the test and continue.

- [ ] **Step 3: Write minimal implementation**

```java
// In HubScreen.render(...):
if (hudPlayer != null && hudPlayer.prevInteract && nearMerchant(hudPlayer)) {
    game.shopOverlayRenderer.open(game.hubShop, game.playerInventory);
    game.overlayManager.open(OverlayType.SHOP);
}

if (game.overlayManager.activeOverlay() == OverlayType.SHOP) {
    game.shopOverlayRenderer.render(game.batch, game.uiFont, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
}
```

- [ ] **Step 4: Run verification**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.ModalOverlayManagerTest"`

Expected: PASS.

Run: `.\gradlew.bat :client:compileJava`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/test/java/com/shadowascent/client/ui/ModalOverlayManagerTest.java
git commit -m "feat: wire libgdx merchant shop modal"
```

## Task 7: Add Crafting Overlay State and Shared Cancel Flow

**Files:**
- Create: `java/client/src/main/java/com/shadowascent/client/ui/CraftingOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/CraftingOverlayRendererStateTest.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/ModalOverlayManagerTest.java`

- [ ] **Step 1: Write the failing tests**

```java
package com.shadowascent.client.ui;

import com.shadowascent.core.simulation.SimInventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CraftingOverlayRendererStateTest {

    @Test
    void selectedRecipeMovesWithinRecipeListBounds() {
        CraftingOverlayRenderer overlay = new CraftingOverlayRenderer(new SimInventory());

        overlay.moveDown();
        overlay.moveDown();
        overlay.moveUp();

        assertEquals(1, overlay.selectedIndex());
    }
}
```

```java
@Test
void closeClearsActiveOverlayRegardlessOfType() {
    ModalOverlayManager manager = new ModalOverlayManager();
    manager.open(OverlayType.SHOP);
    manager.close();
    assertFalse(manager.hasActiveOverlay());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.CraftingOverlayRendererStateTest" --tests "com.shadowascent.client.ui.ModalOverlayManagerTest"`

Expected: FAIL with missing crafting renderer.

- [ ] **Step 3: Write minimal implementation**

```java
package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shadowascent.core.simulation.RecipeBook;
import com.shadowascent.core.simulation.SimInventory;

public final class CraftingOverlayRenderer {
    private final SimInventory inventory;
    private int selectedIndex;

    public CraftingOverlayRenderer(SimInventory inventory) {
        this.inventory = inventory;
    }

    public void moveUp() { selectedIndex = Math.max(0, selectedIndex - 1); }
    public void moveDown() { selectedIndex = Math.min(RecipeBook.all().size() - 1, selectedIndex + 1); }
    public int selectedIndex() { return selectedIndex; }

    public void render(SpriteBatch batch, BitmapFont font, int screenWidth, int screenHeight) {
        // First slice: centered recipe list and selected recipe line.
    }
}
```

```java
// In HubScreen.render(...):
if (game.inputProcessor.consumeCraftingTogglePressed()) {
    game.overlayManager.toggle(OverlayType.CRAFTING);
}

if (game.inputProcessor.consumeCancelPressed()) {
    game.overlayManager.close();
}

if (game.overlayManager.activeOverlay() == OverlayType.CRAFTING) {
    game.craftingOverlayRenderer.render(game.batch, game.uiFont, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
}
```

- [ ] **Step 4: Run verification**

Run: `.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.CraftingOverlayRendererStateTest" --tests "com.shadowascent.client.ui.ModalOverlayManagerTest"`

Expected: PASS.

Run: `.\gradlew.bat :client:compileJava`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add java/client/src/main/java/com/shadowascent/client/ui/CraftingOverlayRenderer.java java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java java/client/src/main/java/com/shadowascent/client/HubScreen.java java/client/src/test/java/com/shadowascent/client/ui/CraftingOverlayRendererStateTest.java java/client/src/test/java/com/shadowascent/client/ui/ModalOverlayManagerTest.java
git commit -m "feat: add libgdx crafting overlay and shared cancel flow"
```

## Task 8: Documentation Sync and Full Verification

**Files:**
- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `README.md`

- [ ] **Step 1: Update docs to match the finished LibGDX UI truth**

```md
- LibGDX `runGame` now includes:
  - persistent HUD
  - docked toggleable minimap
  - modal inventory overlay
  - modal shop overlay
  - modal crafting overlay
  - shared modal suppression and cancel behavior
- `runPlayableClient` remains the main QA route unless that policy is deliberately changed.
```

- [ ] **Step 2: Run docs freshness verification**

Run: `python scripts/check_docs_freshness.py --emit-report`

Expected: PASS with `Status: PASS`.

- [ ] **Step 3: Run client tests**

Run: `.\gradlew.bat :client:test`

Expected: PASS with `BUILD SUCCESSFUL`.

- [ ] **Step 4: Run compile and asset verification**

Run: `.\gradlew.bat :client:compileJava`

Expected: PASS.

Run: `.\gradlew.bat packSprites`

Expected: PASS and regenerated `assets/sprites/packed/sprites.png` plus `assets/sprites/packed/sprites.atlas`.

- [ ] **Step 5: Run regression safety check**

Run: `.\gradlew.bat runRegressionTests`

Expected: PASS / exit code `0`.

- [ ] **Step 6: Commit**

```bash
git add docs/CURRENT_STATE.md docs/ROADMAP.md docs/IMPLEMENTATION_BACKLOG.md docs/PLAYABLE_TRUTH.md README.md docs/reports/docs_freshness_report.md
git commit -m "docs: sync libgdx ui port status"
```

## Self-Review

Spec coverage:

- persistent HUD: already landed and retained
- minimap inside HUD flow: covered by Task 2
- shared modal stack and suppression rules: covered by Task 1, Task 4, Task 6, Task 7
- inventory / shop / crafting overlays: covered by Task 3 through Task 7
- docs and verification: covered by Task 8

Placeholder scan:

- Removed stale “start from zero” steps for already-completed work.
- Replaced broken `java\gradlew.bat` assumptions with repo-root commands.
- Kept code snippets concrete for each remaining task.

Type consistency:

- `OverlayType`, `ModalOverlayManager`, `HudOverlayState`, `HudOverlayRenderer`, `UiPalette`, and `UiText` remain the canonical shared types.
- `GameInputProcessor` becomes the one place that emits one-frame UI toggle signals.
- `HubScreen` remains the owner of modal lifecycle and overlay rendering order.

## Notes for the Implementer

- Do not restart at old Task 1. That work is already present and verified on this branch.
- Fix the shared `ModalOverlayManager` wiring before attempting any new overlay behavior.
- Use the root Gradle wrapper from repository root. Do not use `java\gradlew.bat`.
- Keep `SpriteWorldRenderer` free of HUD or modal drawing.

Plan complete and saved to `docs/superpowers/plans/2026-05-10-libgdx-ui-port-implementation.md`. Two execution options:

1. Subagent-Driven (recommended) - I dispatch a fresh subagent per task, review between tasks, fast iteration
2. Inline Execution - Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?
