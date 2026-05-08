package com.shadowascent.core.simulation;

/**
 * Per-region pressure sample generated on each deterministic simulation tick.
 */
public record WorldSimulationPressureSample(
        String regionId,
        String plateauId,
        double corruptionPressure,
        double prosperityPressure,
        double routeRisk,
        double factionTension) {
}

