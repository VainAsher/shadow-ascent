package com.shadowascent.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModalOverlayManagerTest {

    @Test
    void opensReplacesAndTogglesSingleActiveModal() {
        ModalOverlayManager manager = new ModalOverlayManager();

        assertFalse(manager.hasActiveOverlay());

        manager.open(OverlayType.INVENTORY);
        assertTrue(manager.hasActiveOverlay());
        assertEquals(OverlayType.INVENTORY, manager.activeOverlay());

        manager.open(OverlayType.SHOP);
        assertEquals(OverlayType.SHOP, manager.activeOverlay());

        manager.toggle(OverlayType.SHOP);
        assertFalse(manager.hasActiveOverlay());

        manager.toggle(OverlayType.CRAFTING);
        assertTrue(manager.hasActiveOverlay());
        assertEquals(OverlayType.CRAFTING, manager.activeOverlay());
    }

    @Test
    void closeIsSafeWhenNothingIsOpen() {
        ModalOverlayManager manager = new ModalOverlayManager();

        manager.close();

        assertFalse(manager.hasActiveOverlay());
        assertEquals(null, manager.activeOverlay());
    }

    @Test
    void toggleReplacesExistingModalInsteadOfStacking() {
        ModalOverlayManager manager = new ModalOverlayManager();

        manager.open(OverlayType.CRAFTING);
        manager.toggle(OverlayType.INVENTORY);

        assertEquals(OverlayType.INVENTORY, manager.activeOverlay());
    }

    @Test
    void rejectsNullOverlayTypeForOpenAndToggle() {
        ModalOverlayManager manager = new ModalOverlayManager();

        assertThrows(NullPointerException.class, () -> manager.open(null));
        assertThrows(NullPointerException.class, () -> manager.toggle(null));
        assertFalse(manager.hasActiveOverlay());
    }
}
