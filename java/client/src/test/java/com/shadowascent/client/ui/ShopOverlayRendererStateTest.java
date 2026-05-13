package com.shadowascent.client.ui;

import com.shadowascent.core.simulation.SimInventory;
import com.shadowascent.core.simulation.SimShop;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ShopOverlayRendererStateTest {

    @Test
    void focusTogglesBetweenShopAndInventoryColumns() {
        ShopOverlayRenderer overlay = new ShopOverlayRenderer();
        overlay.open(new SimShop("merchant_npc", 2, 12345L), new SimInventory());

        assertTrue(overlay.isShopFocus());
        overlay.toggleFocus();
        assertFalse(overlay.isShopFocus());
    }
}
