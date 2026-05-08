package com.shadowascent.core.simulation;

import com.shadowascent.core.data.FactionStateDefinition;
import com.shadowascent.core.data.GameDataContracts;
import com.shadowascent.core.data.WorldRegionStateDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Deterministic world simulation tick loop scaffold for M5.
 *
 * This intentionally remains bounded to pressure signals and event emission.
 */
public final class WorldSimulationTick {
    private final long seed;
    private int tickIndex;
    private final Map<String, Double> previousMetricValues;

    public WorldSimulationTick(long seed) {
        this.seed = seed;
        this.tickIndex = 0;
        this.previousMetricValues = new HashMap<>();
    }

    public int currentTick() {
        return tickIndex;
    }

    public WorldSimulationTickResult next(GameDataContracts contracts) {
        List<WorldRegionStateDefinition> regions = contracts.worldRegionStatesById().values().stream()
                .sorted(Comparator.comparing(WorldRegionStateDefinition::id))
                .toList();

        List<WorldSimulationPressureSample> samples = new ArrayList<>();
        List<WorldSimulationEvent> events = new ArrayList<>();
        int regionOrdinal = 0;
        for (WorldRegionStateDefinition region : regions) {
            double factionInfluence = meanFactionInfluenceForPlateau(contracts, region.plateauId());
            double localNoise = normalizedNoise(seed, tickIndex, regionOrdinal);

            double corruption = clamp01(region.corruptionPressure() + (localNoise * 0.03d) + (factionInfluence * 0.01d));
            double prosperity = clamp01(region.prosperityPressure() + (-localNoise * 0.025d) + (factionInfluence * 0.008d));
            double routeRisk = clamp01(region.routeRisk() + (localNoise * 0.02d) + (corruption * 0.015d));
            double maxContractTension = maxFactionTensionForPlateau(contracts, region.plateauId());
            double tension = clamp01(Math.max(region.factionTension(), maxContractTension)
                    + (localNoise * 0.02d) + (factionInfluence * 0.012d));

            WorldSimulationPressureSample sample = new WorldSimulationPressureSample(
                    region.id(),
                    region.plateauId(),
                    corruption,
                    prosperity,
                    routeRisk,
                    tension);
            samples.add(sample);

            collectEvents(events, region.id(), "corruption_pressure", corruption);
            collectEvents(events, region.id(), "prosperity_pressure", prosperity);
            collectEvents(events, region.id(), "route_risk", routeRisk);
            collectEvents(events, region.id(), "faction_tension", tension);
            regionOrdinal++;
        }

        WorldSimulationTickResult result = new WorldSimulationTickResult(
                tickIndex,
                List.copyOf(samples),
                List.copyOf(events));
        tickIndex++;
        return result;
    }

    private double meanFactionInfluenceForPlateau(GameDataContracts contracts, String plateauId) {
        String normalizedPlateau = normalize(plateauId);
        double sum = 0.0d;
        int count = 0;
        for (FactionStateDefinition faction : contracts.factionStatesById().values()) {
            boolean controlsPlateau = faction.territoryPlateaus().stream()
                    .map(WorldSimulationTick::normalize)
                    .anyMatch(normalizedPlateau::equals);
            if (controlsPlateau) {
                sum += faction.influence();
                count++;
            }
        }
        return count == 0 ? 0.0d : sum / count;
    }

    private double maxFactionTensionForPlateau(GameDataContracts contracts, String plateauId) {
        String normalizedPlateau = normalize(plateauId);
        double max = 0.0d;
        for (FactionStateDefinition faction : contracts.factionStatesById().values()) {
            boolean controlsPlateau = faction.territoryPlateaus().stream()
                    .map(WorldSimulationTick::normalize)
                    .anyMatch(normalizedPlateau::equals);
            if (controlsPlateau) {
                max = Math.max(max, faction.tension());
            }
        }
        return max;
    }

    private void collectEvents(List<WorldSimulationEvent> events, String sourceId, String metric, double currentValue) {
        String key = sourceId + "|" + metric;
        Double previous = previousMetricValues.put(key, currentValue);
        if (previous == null) {
            return;
        }

        if (Math.abs(currentValue - previous) >= 0.005d) {
            events.add(new WorldSimulationEvent(
                    tickIndex,
                    "pressure_shift",
                    sourceId,
                    metric,
                    round(previous),
                    round(currentValue)));
        }
        if (previous < 0.70d && currentValue >= 0.70d) {
            events.add(new WorldSimulationEvent(
                    tickIndex,
                    "pressure_spike",
                    sourceId,
                    metric,
                    round(previous),
                    round(currentValue)));
        }
    }

    private static double normalizedNoise(long seed, int tick, int regionOrdinal) {
        double angle = (seed * 0.00017d) + (tick * 0.41d) + (regionOrdinal * 1.27d);
        return Math.sin(angle);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static double clamp01(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }

    private static double round(double value) {
        return Math.round(value * 1000.0d) / 1000.0d;
    }
}
