package com.shadowascent.core.simulation;

/**
 * Deterministic world-simulation event emitted from pressure changes.
 */
public record WorldSimulationEvent(
        int tick,
        String eventType,
        String sourceId,
        String metric,
        double previousValue,
        double currentValue) {
}

