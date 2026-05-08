package com.shadowascent.client;

import com.shadowascent.core.simulation.CraftingRecipe;
import com.shadowascent.core.simulation.ItemDatabase;
import com.shadowascent.core.simulation.RecipeBook;
import com.shadowascent.core.simulation.SimInventory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

/** Crafting overlay. Displays all RecipeBook recipes; craft action on Enter. Toggle with T key. */
public final class CraftingPanel {

    private static final int PANEL_W = 500;
    private static final int PANEL_H = 380;
    private static final int MARGIN  = 18;
    private static final int ROW_H   = 20;

    private SimInventory playerInv;
    private boolean      visible      = false;
    private int          selectedIdx  = 0;
    private String       lastFeedback = "";

    public void open(SimInventory inv) {
        this.playerInv   = inv;
        this.visible     = true;
        this.selectedIdx = 0;
    }

    public void close() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getLastFeedback() {
        return lastFeedback;
    }

    public void moveUp() {
        if (!visible) return;
        selectedIdx = Math.max(0, selectedIdx - 1);
    }

    public void moveDown() {
        if (!visible) return;
        int max = RecipeBook.all().size() - 1;
        selectedIdx = Math.min(max, selectedIdx + 1);
    }

    /**
     * Attempt to craft the currently selected recipe.
     * Returns a feedback string; never null when visible.
     */
    public String craftSelected() {
        if (!visible || playerInv == null) return null;
        List<CraftingRecipe> recipes = RecipeBook.all();
        if (selectedIdx >= recipes.size()) return "No recipe selected.";
        CraftingRecipe recipe = recipes.get(selectedIdx);
        boolean ok = recipe.craft(playerInv);
        lastFeedback = ok
                ? "Crafted: " + itemName(recipe.outputItemId) + " x" + recipe.outputCount
                : "Cannot craft: missing ingredients.";
        return lastFeedback;
    }

    public void draw(Graphics2D g, int screenW, int screenH) {
        if (!visible) return;

        int px = (screenW - PANEL_W) / 2;
        int py = (screenH - PANEL_H) / 2;

        g.setColor(new Color(30, 20, 40, 230));
        g.fillRoundRect(px, py, PANEL_W, PANEL_H, 12, 12);
        g.setColor(new Color(160, 80, 200));
        g.drawRoundRect(px, py, PANEL_W, PANEL_H, 12, 12);

        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("CRAFTING", px + MARGIN, py + MARGIN + 14);

        List<CraftingRecipe> recipes = RecipeBook.all();
        int listY = py + MARGIN + 32;
        for (int i = 0; i < recipes.size(); i++) {
            CraftingRecipe r    = recipes.get(i);
            boolean sel         = (i == selectedIdx);
            boolean canCraft    = playerInv != null && r.canCraft(playerInv);
            int ry = listY + i * ROW_H;
            if (ry > py + PANEL_H - 90) break;

            g.setColor(sel ? new Color(80, 40, 100) : new Color(40, 25, 55));
            g.fillRect(px + MARGIN, ry - ROW_H + 4, PANEL_W - 2 * MARGIN, ROW_H - 1);

            g.setFont(new Font("Monospaced", sel ? Font.BOLD : Font.PLAIN, 12));
            g.setColor(canCraft ? (sel ? Color.MAGENTA : new Color(200, 150, 255))
                                : new Color(100, 80, 100));
            String row = String.format("%-20s → %-14s x%d",
                    truncate(r.description, 20),
                    truncate(itemName(r.outputItemId), 14),
                    r.outputCount);
            g.drawString(row, px + MARGIN + 4, ry);
        }

        // Selected recipe ingredient detail
        if (selectedIdx < recipes.size()) {
            CraftingRecipe r = recipes.get(selectedIdx);
            int detailY = py + PANEL_H - 80;
            g.setColor(new Color(50, 35, 65));
            g.fillRect(px + MARGIN, detailY, PANEL_W - 2 * MARGIN, 50);

            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.setColor(new Color(220, 180, 255));
            g.drawString(r.description, px + MARGIN + 4, detailY + 14);

            StringBuilder ingLine = new StringBuilder("Needs: ");
            for (CraftingRecipe.Ingredient ing : r.ingredients) {
                ItemDatabase.ItemDef def = ItemDatabase.get(ing.itemId());
                String name = def != null ? def.name() : ing.itemId();
                int have = playerInv != null
                        ? ("coin".equals(ing.itemId()) ? playerInv.currency : playerInv.countItem(ing.itemId()))
                        : 0;
                ingLine.append(name).append(" x").append(ing.count())
                        .append("(have ").append(have).append(")  ");
            }
            g.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g.setColor(new Color(180, 160, 210));
            g.drawString(ingLine.toString(), px + MARGIN + 4, detailY + 28);
        }

        // Feedback line
        if (!lastFeedback.isEmpty()) {
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.setColor(new Color(255, 230, 100));
            g.drawString(lastFeedback, px + MARGIN, py + PANEL_H - 22);
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(new Color(160, 130, 200));
        g.drawString("[Up/Down] Select  [Enter] Craft  [Esc] Close", px + MARGIN, py + PANEL_H - 8);
    }

    private static String itemName(String itemId) {
        ItemDatabase.ItemDef def = ItemDatabase.get(itemId);
        return def != null ? def.name() : itemId;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }
}
