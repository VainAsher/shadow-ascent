package com.shadowascent.client;

import com.shadowascent.core.simulation.ItemDatabase;
import com.shadowascent.core.simulation.SimInventory;
import com.shadowascent.core.simulation.SimShop;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

/** Two-column buy/sell shop overlay. Opens via interactNearestNpc() near shop position. */
public final class ShopPanel {

    private static final int PANEL_W = 560;
    private static final int PANEL_H = 360;
    private static final int MARGIN  = 16;
    private static final int COL_W   = (PANEL_W - 3 * MARGIN) / 2;

    /** A completed trade action from the shop. */
    public record TradeRequest(SimShop shop, String itemId, int qty, boolean isBuy) {}

    private SimShop      shop;
    private SimInventory playerInv;
    private boolean      visible   = false;
    private boolean      shopFocus = true;
    private int          shopIdx   = 0;
    private int          invIdx    = 0;

    public void open(SimShop shop, SimInventory inv) {
        this.shop      = shop;
        this.playerInv = inv;
        this.visible   = true;
        this.shopFocus = true;
        this.shopIdx   = 0;
        this.invIdx    = 0;
    }

    public void close() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void moveUp() {
        if (!visible) return;
        if (shopFocus) shopIdx = Math.max(0, shopIdx - 1);
        else           invIdx  = Math.max(0, invIdx  - 1);
    }

    public void moveDown() {
        if (!visible) return;
        if (shopFocus) {
            int max = (shop != null && !shop.getItems().isEmpty()) ? shop.getItems().size() - 1 : 0;
            shopIdx = Math.min(max, shopIdx + 1);
        } else {
            int occupiedCount = countOccupiedSlots();
            invIdx = Math.min(Math.max(0, occupiedCount - 1), invIdx + 1);
        }
    }

    /** Switch between buy (shop) and sell (inventory) focus. */
    public void toggleFocus() {
        if (!visible) return;
        shopFocus = !shopFocus;
    }

    /**
     * Buy the selected shop item (if shopFocus) or sell the selected inventory item.
     * Returns a TradeRequest on success, null on failure or when not visible.
     */
    public TradeRequest performAction() {
        if (!visible || shop == null || playerInv == null) return null;
        if (shopFocus) {
            List<SimShop.Entry> items = shop.getItems();
            if (shopIdx >= items.size()) return null;
            SimShop.Entry entry = items.get(shopIdx);
            boolean ok = shop.buy(entry.itemId(), 1, playerInv);
            return ok ? new TradeRequest(shop, entry.itemId(), 1, true) : null;
        } else {
            int count = 0;
            for (int i = 0; i < SimInventory.MAX_SLOTS; i++) {
                if (playerInv.slots[i] == null) continue;
                if (count == invIdx) {
                    String itemId = playerInv.slots[i].itemId();
                    boolean ok = shop.sell(itemId, 1, playerInv);
                    return ok ? new TradeRequest(shop, itemId, 1, false) : null;
                }
                count++;
            }
            return null;
        }
    }

    public void draw(Graphics2D g, int screenW, int screenH) {
        if (!visible || shop == null) return;

        int px = (screenW - PANEL_W) / 2;
        int py = (screenH - PANEL_H) / 2;

        g.setColor(new Color(20, 30, 20, 230));
        g.fillRoundRect(px, py, PANEL_W, PANEL_H, 12, 12);
        g.setColor(new Color(80, 160, 80));
        g.drawRoundRect(px, py, PANEL_W, PANEL_H, 12, 12);

        // Header
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        String coins = playerInv != null ? String.valueOf(playerInv.currency) : "?";
        g.drawString("SHOP [" + shop.npcId + " Tier " + shop.tier + "]  Coins: " + coins,
                px + MARGIN, py + MARGIN + 14);

        int leftX = px + MARGIN;
        int rightX = px + MARGIN * 2 + COL_W;
        int colY  = py + MARGIN + 28;

        // Left column header
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(shopFocus ? Color.CYAN : new Color(160, 200, 160));
        g.drawString("BUY" + (shopFocus ? " ◄" : ""), leftX, colY);

        List<SimShop.Entry> shopItems = shop.getItems();
        for (int i = 0; i < shopItems.size(); i++) {
            SimShop.Entry e   = shopItems.get(i);
            ItemDatabase.ItemDef def = ItemDatabase.get(e.itemId());
            String name = def != null ? def.name() : e.itemId();
            boolean sel = shopFocus && i == shopIdx;
            int rowY = colY + 14 + i * 20;
            if (rowY > py + PANEL_H - 50) break;
            g.setColor(sel ? new Color(60, 130, 60) : new Color(30, 50, 30));
            g.fillRect(leftX, rowY - 13, COL_W, 18);
            g.setColor(sel ? Color.GREEN : new Color(160, 220, 160));
            g.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g.drawString(String.format("%-16s %4dg x%d", truncate(name, 16), e.buyPrice(), e.stock()),
                    leftX + 2, rowY);
        }

        // Right column header
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(!shopFocus ? Color.CYAN : new Color(160, 200, 160));
        g.drawString("SELL" + (!shopFocus ? " ◄" : ""), rightX, colY);

        if (playerInv != null) {
            int count = 0;
            for (int i = 0; i < SimInventory.MAX_SLOTS; i++) {
                SimInventory.Slot slot = playerInv.slots[i];
                if (slot == null) continue;
                ItemDatabase.ItemDef def = ItemDatabase.get(slot.itemId());
                String name = def != null ? def.name() : slot.itemId();
                boolean sel = !shopFocus && count == invIdx;
                int rowY = colY + 14 + count * 20;
                if (rowY > py + PANEL_H - 50) break;
                g.setColor(sel ? new Color(60, 60, 130) : new Color(30, 30, 50));
                g.fillRect(rightX, rowY - 13, COL_W, 18);
                g.setColor(sel ? Color.CYAN : new Color(160, 200, 255));
                g.setFont(new Font("Monospaced", Font.PLAIN, 11));
                g.drawString(String.format("%-15s x%d", truncate(name, 15), slot.quantity()),
                        rightX + 2, rowY);
                count++;
            }
        }

        // Footer hints
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(new Color(140, 180, 140));
        g.drawString("[Up/Down] Select  [Left/Right] Switch  [Enter] Buy/Sell  [Esc] Close",
                px + MARGIN, py + PANEL_H - 10);
    }

    private int countOccupiedSlots() {
        if (playerInv == null) return 0;
        int c = 0;
        for (SimInventory.Slot s : playerInv.slots) if (s != null) c++;
        return c;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }
}
