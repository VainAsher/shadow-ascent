package com.shadowascent.core.data;

import java.util.List;

/**
 * Contract-backed settlement status snapshot for simulation-driven quest/ecology hooks.
 */
public record SettlementStateDefinition(
        String id,
        String plateauId,
        String populationBand,
        double prosperity,
        double safety,
        double supply,
        double morale,
        List<String> memoryFlags) {
}

