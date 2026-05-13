package com.shadowascent.client.ui;

import com.shadowascent.core.simulation.SimInventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class InventoryOverlayRendererStateTest {

    @Test
    void inventorySelectionRespectsRowEdges() {
        InventoryOverlayRenderer overlay = new InventoryOverlayRenderer(new SimInventory());

        overlay.moveRight();
        overlay.moveRight();
        overlay.moveRight();
        overlay.moveLeft();
        overlay.moveRight();
        overlay.moveRight();

        assertEquals(3, overlay.selectedIndex());
    }

    @Test
    void useSelectedConsumesConsumablesAndReturnsPlayerFacingFeedback() {
        SimInventory inventory = new SimInventory();
        inventory.addItem("health_potion", 1);
        InventoryOverlayRenderer overlay = new InventoryOverlayRenderer(inventory);

        assertEquals("Used Health Potion (+2 HP)", overlay.useSelected());
        assertNull(inventory.slots[0]);
    }
}
