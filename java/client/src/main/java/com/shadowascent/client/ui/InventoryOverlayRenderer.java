package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.shadowascent.core.simulation.ItemDatabase;
import com.shadowascent.core.simulation.SimInventory;

public final class InventoryOverlayRenderer {
    private static final int GRID_COLUMNS = 4;
    private static final int GRID_ROWS = 5;

    private final SimInventory inventory;
    private final Matrix4 textProjection = new Matrix4();
    private int selectedIndex;

    public InventoryOverlayRenderer(SimInventory inventory) {
        this.inventory = inventory;
    }

    public void moveLeft() {
        if (selectedIndex % GRID_COLUMNS > 0) {
            selectedIndex--;
        }
    }

    public void moveRight() {
        if (selectedIndex % GRID_COLUMNS < GRID_COLUMNS - 1) {
            selectedIndex = Math.min(SimInventory.MAX_SLOTS - 1, selectedIndex + 1);
        }
    }

    public void moveUp() {
        selectedIndex = Math.max(0, selectedIndex - GRID_COLUMNS);
    }

    public void moveDown() {
        selectedIndex = Math.min(SimInventory.MAX_SLOTS - 1, selectedIndex + GRID_COLUMNS);
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public String useSelected() {
        SimInventory.Slot slot = inventory.slots[selectedIndex];
        if (slot == null) {
            return "Empty slot.";
        }

        ItemDatabase.ItemDef def = ItemDatabase.get(slot.itemId());
        if (def == null) {
            return "Unknown item.";
        }
        if (def.consumable()) {
            int heal = inventory.useConsumable(slot.itemId());
            return heal > 0 ? "Used " + def.name() + " (+" + heal + " HP)" : "Cannot use " + def.name() + ".";
        }
        if ("weapon".equals(def.type()) || "armor".equals(def.type())) {
            if (slot.equipped()) {
                inventory.unequipItem(slot.itemId());
                return "Unequipped " + def.name();
            }
            inventory.equipItem(slot.itemId());
            return "Equipped " + def.name();
        }
        return def.name() + ": " + def.desc();
    }

    public void render(SpriteBatch batch, BitmapFont font, int screenWidth, int screenHeight) {
        float panelWidth = 420f;
        float panelHeight = 240f;
        float panelX = (screenWidth - panelWidth) * 0.5f;
        float panelTop = 120f;
        float textX = panelX + UiPalette.PANEL_PADDING;
        float lineHeight = UiPalette.LINE_HEIGHT;
        float lineY = screenHeight - (panelTop + 28f);

        textProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        batch.setProjectionMatrix(textProjection);
        batch.begin();
        font.setColor(UiPalette.TEXT);
        font.draw(batch, "Inventory", textX, lineY);

        lineY -= lineHeight;
        font.setColor(UiPalette.TEXT_MUTED);
        for (int row = 0; row < GRID_ROWS; row++) {
            font.draw(batch, buildGridLine(row), textX, lineY);
            lineY -= lineHeight;
        }

        lineY -= lineHeight * 1.5f;
        font.setColor(UiPalette.ACCENT);
        font.draw(batch, "Selected: " + useSelectedPreview(), textX, lineY);

        lineY -= lineHeight;
        font.setColor(UiPalette.TEXT_MUTED);
        font.draw(batch, "Arrows/WASD move  |  Enter use/equip  |  Esc close", textX, lineY);
        batch.end();
    }

    private String buildGridLine(int row) {
        StringBuilder line = new StringBuilder();
        int rowStart = row * GRID_COLUMNS;
        for (int column = 0; column < GRID_COLUMNS; column++) {
            int slotIndex = rowStart + column;
            SimInventory.Slot slot = inventory.slots[slotIndex];
            String label = slot == null ? "Empty" : slot.itemId();
            if (label.length() > 10) {
                label = label.substring(0, 10);
            }
            if (slotIndex == selectedIndex) {
                label = "[" + label + "]";
            }
            if (line.length() > 0) {
                line.append("  ");
            }
            line.append(String.format("%-12s", label));
        }
        return line.toString();
    }

    private String useSelectedPreview() {
        SimInventory.Slot slot = inventory.slots[selectedIndex];
        if (slot == null) {
            return "Empty slot.";
        }

        ItemDatabase.ItemDef def = ItemDatabase.get(slot.itemId());
        return def == null ? slot.itemId() : def.name();
    }
}
