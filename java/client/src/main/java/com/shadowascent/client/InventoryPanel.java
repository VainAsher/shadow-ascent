package com.shadowascent.client;

import com.shadowascent.core.simulation.ItemDatabase;
import com.shadowascent.core.simulation.SimInventory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Inventory overlay — 4×5 slot grid. Toggle with I key. */
public final class InventoryPanel {

    private static final int COLS      = 4;
    private static final int ROWS      = 5;
    private static final int SLOT_SIZE = 52;
    private static final int MARGIN    = 20;
    private static final int HEADER_H  = 36;
    private static final int FOOTER_H  = 48;
    static final int PANEL_W = COLS * SLOT_SIZE + 2 * MARGIN;
    static final int PANEL_H = HEADER_H + ROWS * SLOT_SIZE + FOOTER_H + 2 * MARGIN;

    private final SimInventory inventory;
    private boolean visible     = false;
    private int     selectedIdx = 0;

    public InventoryPanel(SimInventory inventory) {
        this.inventory = inventory;
    }

    public void toggle() {
        visible = !visible;
    }

    public void close() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void moveUp() {
        if (!visible) return;
        selectedIdx = Math.max(0, selectedIdx - COLS);
    }

    public void moveDown() {
        if (!visible) return;
        selectedIdx = Math.min(SimInventory.MAX_SLOTS - 1, selectedIdx + COLS);
    }

    public void moveLeft() {
        if (!visible) return;
        if (selectedIdx % COLS > 0) selectedIdx--;
    }

    public void moveRight() {
        if (!visible) return;
        if (selectedIdx % COLS < COLS - 1) selectedIdx = Math.min(SimInventory.MAX_SLOTS - 1, selectedIdx + 1);
    }

    /** Use or equip the selected slot item. Returns feedback string, or null if panel not visible. */
    public String useSelected() {
        if (!visible || selectedIdx < 0 || selectedIdx >= SimInventory.MAX_SLOTS) return null;
        SimInventory.Slot slot = inventory.slots[selectedIdx];
        if (slot == null) return "Empty slot.";
        ItemDatabase.ItemDef def = ItemDatabase.get(slot.itemId());
        if (def == null) return "Unknown item.";
        if (def.consumable()) {
            int heal = inventory.useConsumable(slot.itemId());
            return heal > 0 ? "Used " + def.name() + " (+" + heal + " HP)" : "Cannot use " + def.name() + ".";
        }
        if ("weapon".equals(def.type()) || "armor".equals(def.type())) {
            if (slot.equipped()) {
                inventory.unequipItem(slot.itemId());
                return "Unequipped " + def.name();
            } else {
                inventory.equipItem(slot.itemId());
                return "Equipped " + def.name();
            }
        }
        return def.name() + ": " + def.desc();
    }

    public void draw(Graphics2D g, int screenW, int screenH) {
        if (!visible) return;

        int px = (screenW - PANEL_W) / 2;
        int py = (screenH - PANEL_H) / 2;

        // Background
        g.setColor(new Color(20, 20, 40, 220));
        g.fillRoundRect(px, py, PANEL_W, PANEL_H, 12, 12);
        g.setColor(new Color(100, 140, 200));
        g.drawRoundRect(px, py, PANEL_W, PANEL_H, 12, 12);

        // Header
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("INVENTORY  [coins: " + inventory.currency + "]", px + MARGIN, py + MARGIN + 14);

        // Slot grid
        int slotStartY = py + MARGIN + HEADER_H;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = row * COLS + col;
                int sx  = px + MARGIN + col * SLOT_SIZE;
                int sy  = slotStartY + row * SLOT_SIZE;
                boolean sel = (idx == selectedIdx);

                g.setColor(sel ? new Color(60, 100, 180) : new Color(35, 35, 60));
                g.fillRect(sx, sy, SLOT_SIZE - 2, SLOT_SIZE - 2);
                g.setColor(sel ? Color.CYAN : new Color(80, 80, 120));
                g.drawRect(sx, sy, SLOT_SIZE - 2, SLOT_SIZE - 2);

                SimInventory.Slot slot = inventory.slots[idx];
                if (slot != null) {
                    ItemDatabase.ItemDef def = ItemDatabase.get(slot.itemId());
                    String label = def != null ? def.name() : slot.itemId();
                    if (label.length() > 7) label = label.substring(0, 6) + "..";
                    g.setFont(new Font("Monospaced", Font.PLAIN, 9));
                    g.setColor(slot.equipped() ? Color.YELLOW : Color.WHITE);
                    g.drawString(label, sx + 3, sy + 24);
                    if (slot.quantity() > 1) {
                        g.setFont(new Font("Monospaced", Font.BOLD, 10));
                        g.setColor(new Color(180, 220, 180));
                        g.drawString("x" + slot.quantity(), sx + 3, sy + 38);
                    }
                }
            }
        }

        // Footer: selected item detail
        int footerY = slotStartY + ROWS * SLOT_SIZE + 6;
        SimInventory.Slot sel = inventory.slots[selectedIdx];
        if (sel != null) {
            ItemDatabase.ItemDef def = ItemDatabase.get(sel.itemId());
            String detail = def != null ? def.name() + " — " + def.desc() : sel.itemId();
            g.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g.setColor(new Color(200, 220, 255));
            g.drawString(detail, px + MARGIN, footerY + 14);
        }
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(new Color(140, 160, 200));
        g.drawString("[Enter] Use/Equip  [Esc] Close  [Arrows] Navigate", px + MARGIN, footerY + 30);
    }
}
