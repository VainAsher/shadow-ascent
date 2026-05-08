---
handover_type: design
milestone: Wave5
topic: ui_overlays
status: in_progress
created: 2026-05-08
---
# Design — Wave 5 UI Overlay Panels

Bounded playability slice: Inventory, Shop, and Crafting overlay panels for `PlaytestClient`,
adapted from donor `indie-ninja-adventures` behavioral patterns (LibGDX incompatible;
all rendering rewritten for Java Swing `Graphics2D`).

---

## Scope

### What is in scope

- `InventoryPanel` — full 20-slot grid (4×5); use/equip/unequip; toggled with `I` key
- `ShopPanel` — two-column buy/sell panel; `TradeRequest` record; opens when `E` pressed near hub shop NPC position
- `CraftingPanel` — recipe list from `RecipeBook.all()`; craft action; toggled with `T` key
- `PlaytestClient` wiring: `SimInventory playerInventory` seeded at startup; `SimShop hubShop` (tier 2, seed 12345); key bindings; overlay draw calls; shop open from `interactNearestNpc()`
- Regression tests: `testInventoryPanelRuntime`, `testShopPanelTradeRequest`, `testCraftingPanelRecipeExecution`

### What is NOT in scope

- LibGDX porting (incompatible; all rendering is native Swing `Graphics2D`)
- `DialogueOverlay` / `HudRenderer` / `AudioManager` ports (separate slices)
- Inventory persistence to save file (SAVE_V4 item — not in this slice)
- Full NPC shopkeeper wiring via `npc_registry.json` (deferred; hub shop is a playtest fixture)

---

## Layer contract

- `InventoryPanel`, `ShopPanel`, `CraftingPanel` live in `com.shadowascent.client`
- All three import from `com.shadowascent.core.simulation` only (no `core.world`, no `core.story`)
- `core` is not modified

---

## Data dependencies (already in `core.simulation`)

| Type | Source | Used by |
|---|---|---|
| `SimInventory` | `core.simulation` | `InventoryPanel`, `ShopPanel` |
| `ItemDatabase` | `core.simulation` | All three panels (name/type lookups) |
| `SimShop` | `core.simulation` | `ShopPanel` |
| `CraftingRecipe` | `core.simulation` | `CraftingPanel` |
| `RecipeBook` | `core.simulation` | `CraftingPanel` |

---

## Files created

| File | Package | Purpose |
|---|---|---|
| `InventoryPanel.java` | `com.shadowascent.client` | 4×5 slot grid overlay; use/equip |
| `ShopPanel.java` | `com.shadowascent.client` | Two-column buy/sell overlay; `TradeRequest` |
| `CraftingPanel.java` | `com.shadowascent.client` | Recipe list overlay; craft callback |

---

## Files modified

| File | Changes |
|---|---|
| `PlaytestClient.java` | `SimInventory playerInventory`, `SimShop hubShop`, panel fields, key bindings (`I`, `T`, arrows, Esc), overlay draw calls, shop open in `interactNearestNpc()`, panel input routing in `tick()` |
| `RegressionTest.java` | 3 new test sections; 40/40 target |

---

## Key bindings added to `PlaytestClient`

| Key | Action |
|---|---|
| `I` | Toggle `InventoryPanel` |
| `T` | Toggle `CraftingPanel` (open with seeded `playerInventory`) |
| `E` (near shop) | Open `ShopPanel` (hub shop at x ≈ 300 in hub room) |
| `↑` / `↓` | Navigate selected panel list |
| `←` / `→` | Switch focus between buy/sell columns in `ShopPanel` |
| `Enter` | Panel action (use/equip/craft/buy/sell) — falls through to mission start if no panel open |
| `Escape` | Close any open panel |

---

## `InventoryPanel` design

```
Fields:  SimInventory inv, int selectedIdx, boolean visible
Layout:  COLS=4, ROWS=5, SLOT_SIZE=52, centered on screen
Actions: toggle(), close(), moveUp/Down/Left/Right(), useSelected()
Render:  fillRoundRect background, slot grid, selected item detail footer
```

- `useSelected()` returns feedback string: consumable heal, equip/unequip, or item description
- Yellow text for equipped items; item name truncated to fit slot width

---

## `ShopPanel` design

```
Fields:  SimShop shop, SimInventory playerInv, int shopIdx/invIdx, boolean shopFocus, boolean visible
Layout:  PANEL_W=560, PANEL_H=360; left=BUY list, right=SELL list
Records: TradeRequest(SimShop shop, String itemId, int qty, boolean isBuy)
Actions: open(shop, inv), close(), moveUp/Down(), toggleFocus(), performAction()
```

- `performAction()` calls `shop.buy()` or `shop.sell()` and returns `TradeRequest` if successful
- `TradeRequest` follows donor `ShopOverlay` pattern; enables event log feedback

---

## `CraftingPanel` design

```
Fields:  SimInventory playerInv, int selectedIdx, boolean visible, String lastFeedback
Layout:  PANEL_W=500, PANEL_H=380; recipe list + ingredient detail section
Actions: open(inv), close(), moveUp/Down(), craftSelected()
```

- `craftSelected()` calls `RecipeBook.all().get(selectedIdx).craft(inv)`
- Ingredient availability shown inline (have/need counts)
- Recipes greyed when `canCraft(inv)` is false

---

## `PlaytestClient` wiring

### New fields
```java
private SimInventory playerInventory;
private InventoryPanel inventoryPanel;
private ShopPanel shopPanel;
private CraftingPanel craftingPanel;
private SimShop hubShop;
private static final float SHOP_NPC_X = WORLD_LEFT_X + 300f;
private boolean queueInventoryToggle;
private boolean queueCraftToggle;
private boolean queuePanelUp;
private boolean queuePanelDown;
private boolean queuePanelLeft;
private boolean queuePanelRight;
private boolean queuePanelAction;
private boolean queuePanelClose;
```

### Constructor additions (end of constructor)
```java
this.playerInventory = new SimInventory();
this.playerInventory.addCurrency(100);
this.playerInventory.addItem("weapon_dagger", 1);
this.playerInventory.addItem("health_potion", 3);
this.playerInventory.addItem("material_iron", 4);
this.hubShop = new SimShop("merchant_npc", 2, 12345L);
this.inventoryPanel = new InventoryPanel(playerInventory);
this.shopPanel = new ShopPanel();
this.craftingPanel = new CraftingPanel();
```

### paintComponent additions
```java
inventoryPanel.draw(g, getWidth(), getHeight());
shopPanel.draw(g, getWidth(), getHeight());
craftingPanel.draw(g, getWidth(), getHeight());
```

### tick() additions
```java
processPanelInputs();
```

### `processPanelInputs()` method
Routes queued panel inputs (up/down/left/right/action/close/toggle) to whichever panel
is currently visible. Falls through to `queueStartMission = true` for action when no panel open.

### `interactNearestNpc()` additions
After traversal subsystem check — if player is within `INTERACT_RADIUS` of `SHOP_NPC_X`
and no panel is already open, call `shopPanel.open(hubShop, playerInventory)`.

---

## Regression tests (3 new sections)

### `testInventoryPanelRuntime` (sub-tests)
1. Panel starts invisible; toggle makes it visible; toggle again hides
2. `addItem("health_potion", 3)` → slot[0] has itemId + qty=3
3. `useSelected()` on consumable → returns non-null feedback + qty decremented
4. `equipItem` on weapon slot → slot shows `equipped=true`; `unequipItem` clears it
5. Panel navigation: moveDown wraps within ROWS; moveRight wraps within COLS

### `testShopPanelTradeRequest` (sub-tests)
1. Panel starts invisible; `open(shop, inv)` makes it visible
2. `performAction()` on shop item → returns non-null `TradeRequest(isBuy=true)` + item added to inventory
3. Buy fails when `playerInv.currency < buyPrice` → returns null
4. `toggleFocus()` switches to inventory side; `performAction()` on inventory item with matching shop entry → `TradeRequest(isBuy=false)` returned + item removed + currency added
5. `close()` sets `isVisible()` to false

### `testCraftingPanelRecipeExecution` (sub-tests)
1. Panel starts invisible; `open(inv)` makes it visible
2. `craftSelected()` without ingredients → returns "Cannot craft" feedback
3. Seed ingredients for `craft_dagger` (2× `material_iron`) → `craftSelected()` succeeds, `weapon_dagger` added to inventory
4. Ingredient deduction verified: `material_iron` count reduced by 2
5. `close()` → `isVisible()` false; feedback persists across close/reopen (lastFeedback is instance state)

---

## Prior test count

37 tests. Target after this slice: **40/40**.
