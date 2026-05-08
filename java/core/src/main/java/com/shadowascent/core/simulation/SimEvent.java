package com.shadowascent.core.simulation;

import java.util.Map;

/**
 * Typed simulation event emitted by GameSimulator during tick.
 * Drained once per frame by consumers (server broadcast, quest ecology, etc.).
 */
public record SimEvent(String type, String entityId, Map<String, Object> data) {

    public SimEvent(String type, String entityId) {
        this(type, entityId, Map.of());
    }
}
