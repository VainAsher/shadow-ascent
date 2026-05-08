package com.shadowascent.core.simulation;

/**
 * A typed quest opportunity generated from world simulation pressure samples.
 * opportunityId is deterministic: "{type}:{plateauId}:{tick}".
 */
public record QuestOpportunity(
        String opportunityId,
        String plateauId,
        String regionId,
        OpportunityType type,
        double signalScore,
        String signalCause,
        String description) {

    public enum OpportunityType {
        CORRUPTION_SURGE,
        PROSPERITY_CRISIS,
        ROUTE_HAZARD,
        FACTION_CONFLICT,
        STABILITY_WINDOW
    }
}
