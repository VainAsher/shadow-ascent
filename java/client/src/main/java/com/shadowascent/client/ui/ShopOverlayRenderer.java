package com.shadowascent.client.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.shadowascent.core.simulation.SimInventory;
import com.shadowascent.core.simulation.SimShop;

import java.util.List;

public final class ShopOverlayRenderer {
    public record TradeRequest(SimShop shop, String itemId, int qty, boolean isBuy) {}

    private final Matrix4 textProjection = new Matrix4();
    private SimShop shop;
    private SimInventory inventory;
    private boolean shopFocus = true;
    private int shopIndex;
    private int inventoryIndex;

    public void open(SimShop shop, SimInventory inventory) {
        this.shop = shop;
        this.inventory = inventory;
        this.shopFocus = true;
        this.shopIndex = 0;
        this.inventoryIndex = 0;
    }

    public boolean isShopFocus() {
        return shopFocus;
    }

    public void toggleFocus() {
        shopFocus = !shopFocus;
    }

    public void moveUp() {
        if (shopFocus) {
            shopIndex = Math.max(0, shopIndex - 1);
        } else {
            inventoryIndex = Math.max(0, inventoryIndex - 1);
        }
    }

    public void moveDown() {
        if (shopFocus) {
            int maxIndex = shop == null ? 0 : Math.max(0, shop.getItems().size() - 1);
            shopIndex = Math.min(maxIndex, shopIndex + 1);
        } else {
            int maxIndex = Math.max(0, occupiedInventoryCount() - 1);
            inventoryIndex = Math.min(maxIndex, inventoryIndex + 1);
        }
    }

    public TradeRequest performAction() {
        if (shop == null || inventory == null) {
            return null;
        }
        if (shopFocus) {
            List<SimShop.Entry> items = shop.getItems();
            if (shopIndex >= items.size()) {
                return null;
            }
            SimShop.Entry entry = items.get(shopIndex);
            return shop.buy(entry.itemId(), 1, inventory) ? new TradeRequest(shop, entry.itemId(), 1, true) : null;
        }

        int occupied = 0;
        for (SimInventory.Slot slot : inventory.slots) {
            if (slot == null) {
                continue;
            }
            if (occupied == inventoryIndex) {
                return shop.sell(slot.itemId(), 1, inventory) ? new TradeRequest(shop, slot.itemId(), 1, false) : null;
            }
            occupied++;
        }
        return null;
    }

    public void render(SpriteBatch batch, BitmapFont font, ShapeRenderer shapes, int screenWidth, int screenHeight) {
        textProjection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        batch.setProjectionMatrix(textProjection);

        float panelWidth = Math.min(720f, screenWidth - 120f);
        float panelHeight = 280f;
        float panelX = (screenWidth - panelWidth) * 0.5f;
        float panelTop = 88f;
        float leftX = panelX + UiPalette.PANEL_PADDING;
        float rightX = panelX + panelWidth * 0.52f;
        float lineY = screenHeight - (panelTop + 28f);

        Matrix4 shapeProjection = new Matrix4().setToOrtho(0f, screenWidth, screenHeight, 0f, 0f, 1f);
        UiPanelRenderer.drawPanel(shapes, shapeProjection, panelX, panelTop, panelWidth, panelHeight);

        batch.begin();
        font.setColor(UiPalette.TEXT);
        font.draw(batch, "Shop: " + (shop == null ? "Unavailable" : shop.npcId), leftX, lineY);
        font.draw(batch, "Coins: " + (inventory == null ? "?" : inventory.currency), rightX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(shopFocus ? UiPalette.ACCENT : UiPalette.TEXT_MUTED);
        font.draw(batch, "Buy", leftX, lineY);
        font.setColor(!shopFocus ? UiPalette.ACCENT : UiPalette.TEXT_MUTED);
        font.draw(batch, "Sell", rightX, lineY);

        lineY -= UiPalette.LINE_HEIGHT;
        font.setColor(UiPalette.TEXT_MUTED);
        drawShopColumn(batch, font, leftX, lineY);
        drawInventoryColumn(batch, font, rightX, lineY);

        font.setColor(UiPalette.TEXT_MUTED);
        font.draw(batch, "Up/Down select  |  Left/Right switch  |  Enter trade  |  Esc close",
                leftX, screenHeight - (panelTop + panelHeight - 24f));
        batch.end();
    }

    private void drawShopColumn(SpriteBatch batch, BitmapFont font, float x, float lineY) {
        if (shop == null) {
            font.draw(batch, "No shop open.", x, lineY);
            return;
        }
        List<SimShop.Entry> items = shop.getItems();
        if (items.isEmpty()) {
            font.draw(batch, "No items for sale.", x, lineY);
            return;
        }
        for (int i = 0; i < Math.min(items.size(), 7); i++) {
            SimShop.Entry entry = items.get(i);
            String marker = shopFocus && i == shopIndex ? "> " : "  ";
            font.draw(batch,
                    marker + String.format("%-16s %3dg x%d", UiText.itemName(entry.itemId()), entry.buyPrice(), entry.stock()),
                    x,
                    lineY - i * UiPalette.LINE_HEIGHT);
        }
    }

    private void drawInventoryColumn(SpriteBatch batch, BitmapFont font, float x, float lineY) {
        if (inventory == null) {
            font.draw(batch, "No inventory.", x, lineY);
            return;
        }
        int drawn = 0;
        for (SimInventory.Slot slot : inventory.slots) {
            if (slot == null || drawn >= 7) {
                if (drawn >= 7) {
                    break;
                }
                continue;
            }
            String marker = !shopFocus && drawn == inventoryIndex ? "> " : "  ";
            font.draw(batch,
                    marker + String.format("%-16s x%d", UiText.itemName(slot.itemId()), slot.quantity()),
                    x,
                    lineY - drawn * UiPalette.LINE_HEIGHT);
            drawn++;
        }
        if (drawn == 0) {
            font.draw(batch, "Inventory empty.", x, lineY);
        }
    }

    private int occupiedInventoryCount() {
        if (inventory == null) {
            return 0;
        }
        int occupied = 0;
        for (SimInventory.Slot slot : inventory.slots) {
            if (slot != null) {
                occupied++;
            }
        }
        return occupied;
    }
}
