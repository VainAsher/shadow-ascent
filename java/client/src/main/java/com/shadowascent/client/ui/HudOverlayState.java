package com.shadowascent.client.ui;

import java.util.List;

public record HudOverlayState(
        String actId,
        String plateauId,
        String areaId,
        String missionTitle,
        String objectiveLine,
        int playerHealth,
        int playerMaxHealth,
        String contextualHint,
        String overlayStatus,
        List<String> eventFeedLines,
        boolean showMinimap,
        String interactionHint
) {
    public HudOverlayState {
        eventFeedLines = List.copyOf(eventFeedLines);
    }
}
