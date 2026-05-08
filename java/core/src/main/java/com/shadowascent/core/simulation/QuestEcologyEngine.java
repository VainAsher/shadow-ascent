package com.shadowascent.core.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generates typed QuestOpportunity records from WorldSimulationTickResult pressure samples.
 * Stateless — each call to generateOpportunities is independent and deterministic.
 */
public final class QuestEcologyEngine {

    static final double CORRUPTION_SURGE_THRESHOLD  = 0.65d;
    static final double PROSPERITY_CRISIS_THRESHOLD = 0.35d;
    static final double ROUTE_HAZARD_THRESHOLD      = 0.70d;
    static final double FACTION_CONFLICT_THRESHOLD  = 0.75d;
    static final double STABILITY_CEILING           = 0.30d;

    public List<QuestOpportunity> generateOpportunities(WorldSimulationTickResult tickResult) {
        if (tickResult == null || tickResult.regionSamples() == null) {
            return List.of();
        }
        List<QuestOpportunity> opportunities = new ArrayList<>();
        for (WorldSimulationPressureSample sample : tickResult.regionSamples()) {
            String plateauId = normalize(sample.plateauId());
            String regionId  = normalize(sample.regionId());
            if (plateauId.isBlank()) {
                continue;
            }
            int tick = tickResult.tick();

            if (sample.corruptionPressure() >= CORRUPTION_SURGE_THRESHOLD) {
                opportunities.add(new QuestOpportunity(
                        opportunityId(QuestOpportunity.OpportunityType.CORRUPTION_SURGE, plateauId, tick),
                        plateauId, regionId,
                        QuestOpportunity.OpportunityType.CORRUPTION_SURGE,
                        normalize01(sample.corruptionPressure(), CORRUPTION_SURGE_THRESHOLD, 1.0d),
                        "corruption_pressure:" + round3(sample.corruptionPressure()),
                        "Corruption spreading in " + plateauId + " — investigate or cleanse"));
            }

            if (sample.prosperityPressure() <= PROSPERITY_CRISIS_THRESHOLD) {
                double deficit = PROSPERITY_CRISIS_THRESHOLD - sample.prosperityPressure();
                opportunities.add(new QuestOpportunity(
                        opportunityId(QuestOpportunity.OpportunityType.PROSPERITY_CRISIS, plateauId, tick),
                        plateauId, regionId,
                        QuestOpportunity.OpportunityType.PROSPERITY_CRISIS,
                        normalize01(deficit, 0.0d, PROSPERITY_CRISIS_THRESHOLD),
                        "prosperity_pressure:" + round3(sample.prosperityPressure()),
                        "Settlement in " + plateauId + " under strain — supply or aid run"));
            }

            if (sample.routeRisk() >= ROUTE_HAZARD_THRESHOLD) {
                opportunities.add(new QuestOpportunity(
                        opportunityId(QuestOpportunity.OpportunityType.ROUTE_HAZARD, plateauId, tick),
                        plateauId, regionId,
                        QuestOpportunity.OpportunityType.ROUTE_HAZARD,
                        normalize01(sample.routeRisk(), ROUTE_HAZARD_THRESHOLD, 1.0d),
                        "route_risk:" + round3(sample.routeRisk()),
                        "Routes in " + plateauId + " at high risk — escort or clear"));
            }

            if (sample.factionTension() >= FACTION_CONFLICT_THRESHOLD) {
                opportunities.add(new QuestOpportunity(
                        opportunityId(QuestOpportunity.OpportunityType.FACTION_CONFLICT, plateauId, tick),
                        plateauId, regionId,
                        QuestOpportunity.OpportunityType.FACTION_CONFLICT,
                        normalize01(sample.factionTension(), FACTION_CONFLICT_THRESHOLD, 1.0d),
                        "faction_tension:" + round3(sample.factionTension()),
                        "Faction tensions rising in " + plateauId + " — mediate or confront"));
            }

            boolean allStable = sample.corruptionPressure() < STABILITY_CEILING
                    && sample.routeRisk() < STABILITY_CEILING
                    && sample.factionTension() < STABILITY_CEILING;
            if (allStable) {
                double maxHostile = Math.max(
                        Math.max(sample.corruptionPressure(), sample.routeRisk()),
                        sample.factionTension());
                opportunities.add(new QuestOpportunity(
                        opportunityId(QuestOpportunity.OpportunityType.STABILITY_WINDOW, plateauId, tick),
                        plateauId, regionId,
                        QuestOpportunity.OpportunityType.STABILITY_WINDOW,
                        clamp01(1.0d - (maxHostile / STABILITY_CEILING)),
                        "stability:all_below=" + STABILITY_CEILING,
                        "Calm period in " + plateauId + " — trade or exploration window"));
            }
        }
        return opportunities;
    }

    private static String opportunityId(QuestOpportunity.OpportunityType type, String plateauId, int tick) {
        return type.name().toLowerCase(Locale.ROOT) + ":" + plateauId.toLowerCase(Locale.ROOT) + ":" + tick;
    }

    private static double normalize01(double value, double min, double max) {
        if (max <= min) {
            return 0.0d;
        }
        return clamp01((value - min) / (max - min));
    }

    private static double clamp01(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }

    private static double round3(double value) {
        return Math.round(value * 1000.0d) / 1000.0d;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
