package com.shadowascent.core.data;

/**
 * Contract-backed world simulation pressure snapshot per region/plateau.
 */
public record WorldRegionStateDefinition(
        String id,
        String plateauId,
        String weatherProfile,
        double corruptionPressure,
        double prosperityPressure,
        double routeRisk,
        double factionTension) {
}

