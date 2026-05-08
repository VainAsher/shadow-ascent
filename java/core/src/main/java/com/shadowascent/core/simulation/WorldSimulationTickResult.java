package com.shadowascent.core.simulation;

import java.util.List;

/**
 * Output snapshot for one deterministic world simulation tick.
 */
public record WorldSimulationTickResult(
        int tick,
        List<WorldSimulationPressureSample> regionSamples,
        List<WorldSimulationEvent> events) {
}

