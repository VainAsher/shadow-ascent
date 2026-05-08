---
handover_type: design
milestone: Wave4
topic: crafting_system
status: in_progress
created: 2026-05-08
---
# Design — Wave 4 Crafting System

Bounded import of the crafting and trading system from `com.indieniinja.sim` into
`com.shadowascent.core.simulation`. Delivers recipe definition, recipe registry,
and NPC shop simulation.

---

## Source Files (donor)

| Donor file | Package | Notable deps |
|---|---|---|
| `CraftingRecipe.java` | `com.indieniinja.sim` | `SimInventory` (same-pkg) |
| `RecipeBook.java` | `com.indieniinja.sim` | `CraftingRecipe` (same-pkg) |
| `SimShop.java` | `com.indieniinja.sim` | `SimInventory`, `ItemDatabase` (same-pkg) |

---

## Package Renames

| Donor | Clean-start |
|---|---|
| `com.indieniinja.sim` | `com.shadowascent.core.simulation` |

---

## Design Decisions

**All deps already in core.simulation:** `SimInventory` and `ItemDatabase` were imported in prior
Wave 4 slices. All three files compile with same-package references and `java.util.*` only.

**No structural changes:** Logic is identical to donor — package rename only.

**CraftingRecipe.consume():** Ignores the boolean return value of `removeCurrency` — intentional;
the ingredient-presence pre-check in `canCraft` guarantees the operation will succeed.

---

## Layer Contract Verification

All 3 files import only `java.util.*` and same-package types.
No imports from `client`, `server`, `network`, or `physics`.

---

## Regression Section

Add `testCraftingSystem` to `RegressionTest.java`:

- `RecipeBook.all()` non-empty; `RecipeBook.get("craft_dagger")` returns recipe with `outputItemId="weapon_dagger"`
- `RecipeBook.byCategory("weapon")` returns at least 3 recipes
- `CraftingRecipe.canCraft`: false when ingredients missing; true after adding them
- `CraftingRecipe.craft`: returns true, output in inventory, ingredients consumed
- `SimShop`: tier-1 seeded generate produces ≥1 item; `buy` succeeds with sufficient currency; stock decrements; `sell` succeeds and returns currency
- `SimShop.toMap()` contains "npc_id" and "items"

---

## Files to Create

All in `java/core/src/main/java/com/shadowascent/core/simulation/`:

1. `CraftingRecipe.java`
2. `RecipeBook.java`
3. `SimShop.java`

Prior regression count: 33. After: 34.
