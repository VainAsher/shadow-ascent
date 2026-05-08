package com.shadowascent.core.simulation;

import java.util.List;

/**
 * A single crafting recipe. At a crafting NPC the player combines ingredients to produce output.
 * All item IDs match ItemDatabase entries.
 */
public final class CraftingRecipe {

    public record Ingredient(String itemId, int count) {}

    public final String           recipeId;
    public final String           outputItemId;
    public final int              outputCount;
    public final List<Ingredient> ingredients;
    public final String           category;
    public final String           description;

    public CraftingRecipe(String recipeId, String outputItemId, int outputCount,
                          List<Ingredient> ingredients, String category, String description) {
        this.recipeId     = recipeId;
        this.outputItemId = outputItemId;
        this.outputCount  = outputCount;
        this.ingredients  = ingredients;
        this.category     = category;
        this.description  = description;
    }

    public boolean canCraft(SimInventory inv) {
        for (Ingredient ing : ingredients) {
            if (!hasEnough(inv, ing)) return false;
        }
        return true;
    }

    public boolean craft(SimInventory inv) {
        if (!canCraft(inv)) return false;
        for (Ingredient ing : ingredients) consume(inv, ing);
        inv.addItem(outputItemId, outputCount);
        return true;
    }

    private static boolean hasEnough(SimInventory inv, Ingredient ing) {
        if ("coin".equals(ing.itemId())) {
            return inv.currency >= ing.count();
        }
        return inv.countItem(ing.itemId()) >= ing.count();
    }

    private static void consume(SimInventory inv, Ingredient ing) {
        if ("coin".equals(ing.itemId())) {
            inv.removeCurrency(ing.count());
        } else {
            inv.removeItem(ing.itemId(), ing.count());
        }
    }
}
