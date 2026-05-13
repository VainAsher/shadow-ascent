package com.shadowascent.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class ShadowAscentGameStructureTest {

    @Test
    void newGameAndContinueShareOneScreenEntryPoint() {
        assertDoesNotThrow(() -> ShadowAscentGame.class.getDeclaredMethod("startNewGame"));
        assertDoesNotThrow(() -> ShadowAscentGame.class.getDeclaredMethod("continueFromSave"));
    }
}
