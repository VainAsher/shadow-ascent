package com.shadowascent.client.ui;

import java.util.Objects;

public final class ModalOverlayManager {

    private OverlayType activeOverlay;

    public void open(OverlayType overlayType) {
        activeOverlay = Objects.requireNonNull(overlayType, "overlayType");
    }

    public void toggle(OverlayType overlayType) {
        Objects.requireNonNull(overlayType, "overlayType");
        if (overlayType == activeOverlay) {
            activeOverlay = null;
            return;
        }
        activeOverlay = overlayType;
    }

    public void close() {
        activeOverlay = null;
    }

    public boolean hasActiveOverlay() {
        return activeOverlay != null;
    }

    public OverlayType activeOverlay() {
        return activeOverlay;
    }
}
