---
handover_type: codex_validate
milestone: Wave5
topic: ui_overlays
status: validated
created: 2026-05-08
---
# Codex Validate — Wave 5 UI Overlay Panels

Gate evidence for InventoryPanel, ShopPanel, CraftingPanel and PlaytestClient wiring.

---

## Gate Result

```
BUILD SUCCESSFUL in 2m 9s
[PASS] All regression tests PASSED  (40/40)
```

Full command:
```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava
             runDataContractDiagnostics runWorldgenDiagnostics
             runWorldSimulationDiagnostics runRegressionTests
```

---

## Files Created

| File | Package | Lines | Purpose |
|---|---|---|---|
| `InventoryPanel.java` | `com.shadowascent.client` | ~120 | 4×5 slot grid overlay; use/equip/unequip via SimInventory |
| `ShopPanel.java` | `com.shadowascent.client` | ~145 | Two-column buy/sell overlay; TradeRequest record |
| `CraftingPanel.java` | `com.shadowascent.client` | ~130 | Recipe list from RecipeBook.all(); craft callback |

---

## Files Modified

| File | Change |
|---|---|
| `PlaytestClient.java` | Added `SimInventory playerInventory` (seeded: 100 coins, dagger, 3 potions, 4 iron); `SimShop hubShop` (tier 2, seed 12345); `InventoryPanel`, `ShopPanel`, `CraftingPanel` fields; `SHOP_NPC_X` constant; 8 queue flags; constructor init; key bindings (`I`, `T`, arrows, Esc, Enter contextual); `processPanelInputs()` + `anyPanelOpen()` methods; overlay draw calls in `paintComponent`; shop open in `interactNearestNpc()` |
| `RegressionTest.java` | 3 new test sections + dispatch entries; 37→40 tests |

---

## Key Bindings Added

| Key | Action |
|---|---|
| `I` | Toggle InventoryPanel |
| `T` | Toggle CraftingPanel (opens with playerInventory) |
| `E` near x≈350 (hub) | Open ShopPanel (merchant_npc tier 2) |
| `↑` / `↓` | Navigate selected panel list |
| `←` / `→` | Switch focus in ShopPanel (buy ↔ sell) |
| `Enter` | Panel action (use/equip/craft/buy/sell) when panel open; mission start otherwise |
| `Escape` | Close any open panel |

---

## Regression Sections

### `testInventoryPanelRuntime` — [PASS] (new)
- `useConsumable("health_potion")` returns heal > 0; stack count decremented
- `useConsumable` on absent item returns 0
- `equipItem` sets `equippedWeapon` field and slot `equipped=true`
- Equipping a second weapon auto-unequips the first
- Armor equip/unequip cycle — `equippedArmor` field set/cleared
- `totalAttackBonus()` reflects equipped weapon

### `testShopPanelTradeRequest` — [PASS] (new)
- Buy with sufficient currency → item added, currency reduced
- Buy with 0 currency → returns false, inventory unchanged
- Sell non-protected material → currency granted, stack decremented
- Sell `quest_item` → rejected (returns false)
- Sell from empty inventory → returns false

### `testCraftingPanelRecipeExecution` — [PASS] (new)
- `RecipeBook.all()` has ≥ 8 recipes
- `RecipeBook.get(unknown)` returns null
- `canCraft` with partial ingredients returns false
- Multi-ingredient recipe (`craft_steel_sword`: 5 iron + 1 cloth) — all inputs consumed, output present
- Second craft after consumption returns false
- `craft_health_potion` yields 2 output items

---

## Diagnostics

```
contracts_loaded=true valid=true beats=45 critical_flags=61 plateaus=7
save_migration_matrix=v0→v3, v1→v3, v2→v3, v3 native
Validation issues: none
Section templates loaded: 10 (0 issues)
World Simulation: 3 regions, 3 factions, 3 settlements — tick_events=8
```

---

## Prior Test Count

37 tests prior. Now **40/40**.
