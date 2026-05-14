package com.shadowascent.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MinimapOverlayRendererTest {

    @Test
    void mapMarkerCoordinateClampsInsideMinimapContent() {
        assertEquals(10f, MinimapOverlayRenderer.mapMarkerCoordinate(-20f, 0f, 2f, 10f, 10f, 100f, 100f, 4f));
        assertEquals(106f, MinimapOverlayRenderer.mapMarkerCoordinate(80f, 0f, 2f, 10f, 10f, 100f, 100f, 4f));
    }

    @Test
    void mapMarkerCoordinateUsesCenteredDrawBoundsWhenMapIsLetterboxed() {
        assertEquals(20f, MinimapOverlayRenderer.mapMarkerCoordinate(-5f, 0f, 1f, 10f, 20f, 100f, 60f, 4f));
        assertEquals(76f, MinimapOverlayRenderer.mapMarkerCoordinate(90f, 0f, 1f, 10f, 20f, 100f, 60f, 4f));
    }

    @Test
    void gateCoordinateUsesGateCenterAndClampsInsideMap() {
        assertEquals(56f, MinimapOverlayRenderer.mapGateCoordinate(24f, 72f, 0f, 1f, 10f, 10f, 100f, 100f, 4f));
        assertEquals(106f, MinimapOverlayRenderer.mapGateCoordinate(140f, 180f, 0f, 1f, 10f, 10f, 100f, 100f, 4f));
    }

    @Test
    void gateVerticalCoordinateDefaultsToBottomBandWhenNoYRangeExists() {
        assertEquals(100f, MinimapOverlayRenderer.mapGateVerticalCoordinate(null, null, 0f, 1f, 10f, 10f, 100f, 100f, 4f));
    }

    @Test
    void gateVerticalCoordinateUsesCenteredYBandWhenPresent() {
        assertEquals(58f, MinimapOverlayRenderer.mapGateVerticalCoordinate(30f, 70f, 0f, 1f, 10f, 10f, 100f, 100f, 4f));
    }
}
