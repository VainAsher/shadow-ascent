package com.shadowascent.client.ui;

import com.shadowascent.core.simulation.SimInventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CraftingOverlayRendererStateTest {

    @Test
    void selectedRecipeMovesWithinRecipeListBounds() {
        CraftingOverlayRenderer overlay = new CraftingOverlayRenderer(new SimInventory());

        overlay.moveDown();
        overlay.moveDown();
        overlay.moveUp();

        assertEquals(1, overlay.selectedIndex());
    }
}
