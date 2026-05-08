---
handover_type: codex_validate
milestone: Wave4
topic: crafting_system
status: validated
created: 2026-05-08
---
# Codex Validate — Wave 4 Crafting System

Gate evidence for the bounded Wave 4 import of the crafting and trading system from
`com.indieniinja.sim` into `com.shadowascent.core.simulation`.

---

## Gate Result

```
BUILD SUCCESSFUL in 46s
[PASS] All regression tests PASSED  (34/34)
```

Full command:
```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava
             runDataContractDiagnostics runWorldgenDiagnostics
             runWorldSimulationDiagnostics runRegressionTests
```

---

## Files Created

| File | Package | Lines | Source |
|---|---|---|---|
| `CraftingRecipe.java` | `core.simulation` | 60 | `com.indieniinja.sim.CraftingRecipe` |
| `RecipeBook.java` | `core.simulation` | 90 | `com.indieniinja.sim.RecipeBook` |
| `SimShop.java` | `core.simulation` | 128 | `com.indieniinja.sim.SimShop` |

Total: 3 files.

---

## Regression Section Added

### `testCraftingSystem` — [PASS]

- `RecipeBook.all()` non-empty; `get("craft_dagger")` returns recipe with `outputItemId="weapon_dagger"`
- `RecipeBook.byCategory("weapon")` returns ≥3 recipes
- `CraftingRecipe.canCraft`: false with empty inventory; true after adding 2 `material_iron`
- `CraftingRecipe.craft`: returns true, `weapon_dagger` in inventory, `material_iron` consumed
- Coin ingredient recipe (`craft_iron_from_coin`): canCraft=false with 0 coins, true with 10; after craft currency=0, `material_iron` count=3
- `SimShop` tier-1 seeded generate produces ≥1 item; `buy()` with max currency succeeds, stock decrements; `sell(material_cloth)` returns currency
- `SimShop.toMap()` contains `npc_id` and `items` list

---

## Design Decisions

**No structural changes:** Package rename only. All deps (`SimInventory`, `ItemDatabase`) were
imported in prior Wave 4 slices. `CraftingRecipe.consume()` ignores the boolean return value
of `removeCurrency` — intentional; `canCraft` pre-check guarantees the call will succeed.

---

## Layer Contract Verification

All 3 files import only `java.util.*` and same-package types.
No imports from `client`, `server`, `network`, or `physics`.

---

## Prior Test Count

33 tests prior to this slice. Now 34/34.
