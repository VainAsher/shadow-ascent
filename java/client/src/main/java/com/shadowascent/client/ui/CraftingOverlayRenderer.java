package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.shadowascent.core.simulation.CraftingRecipe;
import com.shadowascent.core.simulation.RecipeBook;
import com.shadowascent.core.simulation.SimInventory;

import java.util.List;

public final class CraftingOverlayRenderer {
    private final SimInventory inventory;
    private final Matrix4 textProjection = new Matrix4();
    private int selectedIndex;
    private String lastFeedback = "";

    public CraftingOverlayRenderer(SimInventory inventory) {
        this.inventory = inventory;
    }

    public void moveUp() {
        selectedIndex = Math.max(0, selectedIndex - 1);
    }

    public void moveDown() {
        int maxIndex = Math.max(0, RecipeBook.all().size() - 1);
        selectedIndex = Math.min(maxIndex, selectedIndex + 1);
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public String craftSelected() {
        List<CraftingRecipe> recipes = RecipeBook.all();
        if (recipes.isEmpty()) {
            selectedIndex = 0;
            lastFeedback = "No recipes available.";
            return lastFeedback;
        }
        selectedIndex = Math.min(selectedIndex, recipes.size() - 1);
        CraftingRecipe recipe = recipes.get(selectedIndex);
        boolean crafted = recipe.craft(inventory);
        lastFeedback = crafted
                ? "Crafted: " + UiText.itemName(recipe.outputItemId) + " x" + recipe.outputCount
                : "Cannot craft: missing ingredients.";
        return lastFeedback;
    }

    public void render(SpriteBatch batch, BitmapFont font, int screenWidth, int screenHeight) {
        textProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        batch.setProjectionMatrix(textProjection);

        float panelX = (screenWidth - 560f) * 0.5f;
        float panelTop = 120f;
        float textX = panelX + UiPalette.PANEL_PADDING;
        float lineY = screenHeight - (panelTop + 28f);
        List<CraftingRecipe> recipes = RecipeBook.all();
        if (!recipes.isEmpty()) {
            selectedIndex = Math.min(selectedIndex, recipes.size() - 1);
        }

        batch.begin();
        font.setColor(UiPalette.TEXT);
        font.draw(batch, "Crafting", textX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        for (int i = 0; i < Math.min(recipes.size(), 8); i++) {
            CraftingRecipe recipe = recipes.get(i);
            String marker = i == selectedIndex ? "> " : "  ";
            font.setColor(recipe.canCraft(inventory) ? UiPalette.ACCENT : UiPalette.TEXT_MUTED);
            font.draw(batch,
                    marker + String.format("%-18s -> %s x%d",
                            truncate(recipe.description, 18),
                            UiText.itemName(recipe.outputItemId),
                            recipe.outputCount),
                    textX,
                    lineY - i * UiPalette.LINE_HEIGHT);
        }

        if (!recipes.isEmpty()) {
            CraftingRecipe selected = recipes.get(selectedIndex);
            float detailY = lineY - 10f - Math.min(recipes.size(), 8) * UiPalette.LINE_HEIGHT;
            font.setColor(UiPalette.TEXT);
            font.draw(batch, "Needs: " + ingredientLine(selected), textX, detailY);
            if (!lastFeedback.isBlank()) {
                font.setColor(UiPalette.WARNING);
                font.draw(batch, lastFeedback, textX, detailY - UiPalette.LINE_HEIGHT);
            }
        }

        font.setColor(UiPalette.TEXT_MUTED);
        font.draw(batch, "Up/Down select  |  Enter craft  |  Esc close", textX, screenHeight - (panelTop + 250f));
        batch.end();
    }

    private String ingredientLine(CraftingRecipe recipe) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < recipe.ingredients.size(); i++) {
            CraftingRecipe.Ingredient ingredient = recipe.ingredients.get(i);
            if (i > 0) {
                builder.append("  ");
            }
            int have = "coin".equals(ingredient.itemId())
                    ? inventory.currency
                    : inventory.countItem(ingredient.itemId());
            builder.append(UiText.itemName(ingredient.itemId()))
                    .append(" x")
                    .append(ingredient.count())
                    .append(" (")
                    .append(have)
                    .append(")");
        }
        return builder.toString();
    }

    private static String truncate(String value, int maxChars) {
        return value.length() <= maxChars ? value : value.substring(0, maxChars - 2) + "..";
    }
}
