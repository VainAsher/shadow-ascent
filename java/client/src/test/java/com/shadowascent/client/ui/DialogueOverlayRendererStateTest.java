package com.shadowascent.client.ui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DialogueOverlayRendererStateTest {

    @Test
    void openAdvanceAndCloseTrackDialogueState() {
        DialogueOverlayRenderer overlay = new DialogueOverlayRenderer();

        overlay.open("Merchant Rilu", List.of("Welcome.", "Need supplies?"));

        assertTrue(overlay.isVisible());
        assertEquals("Merchant Rilu", overlay.speakerName());
        assertEquals("Welcome.", overlay.currentLine());

        assertTrue(overlay.advance());
        assertEquals("Need supplies?", overlay.currentLine());

        assertFalse(overlay.advance());
        assertFalse(overlay.isVisible());
    }

    @Test
    void openNormalizesMissingSpeakerAndDialogue() {
        DialogueOverlayRenderer overlay = new DialogueOverlayRenderer();

        overlay.open(" ", null);

        assertTrue(overlay.isVisible());
        assertEquals("Unknown", overlay.speakerName());
        assertEquals("...", overlay.currentLine());
    }
}
