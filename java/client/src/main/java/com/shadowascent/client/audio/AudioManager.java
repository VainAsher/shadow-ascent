package com.shadowascent.client.audio;

import com.shadowascent.core.simulation.SimEvent;

import java.util.List;

public final class AudioManager {

    public String resolveSoundKey(List<SimEvent> events) {
        for (SimEvent event : events) {
            switch (event.type()) {
                case "PLAYER_DAMAGED":
                    return "player_hurt";
                case "ENEMY_DEFEATED":
                    return "enemy_defeated";
                case "PORTAL_ACTIVATED":
                    return "portal_activate";
                default:
                    break;
            }
        }
        return null;
    }

    public void processEvents(List<SimEvent> events) {
        String soundKey = resolveSoundKey(events);
        if (soundKey != null) {
            // First slice: safe no-op until playback assets are loaded and bound.
        }
    }
}
