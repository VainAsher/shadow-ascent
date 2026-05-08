package com.shadowascent.core;

import com.shadowascent.core.data.FactionStateDefinition;
import com.shadowascent.core.data.GameDataContracts;
import com.shadowascent.core.data.SettlementStateDefinition;
import com.shadowascent.core.data.WorldRegionStateDefinition;
import com.shadowascent.core.simulation.WorldSimulationTick;
import com.shadowascent.core.simulation.WorldSimulationTickResult;

/**
 * CLI diagnostics for M5 world simulation contract scaffolding.
 */
public final class WorldSimulationDiagnostics {
    private WorldSimulationDiagnostics() {
    }

    public static void main(String[] args) {
        GameDataContracts contracts = GameDataContracts.loadDefault();
        System.out.println("=== Shadow Ascent World Simulation Diagnostics ===");
        System.out.println("Root: " + contracts.rootDirectory());
        System.out.println("World regions: " + contracts.worldRegionStatesById().size());
        System.out.println("Factions: " + contracts.factionStatesById().size());
        System.out.println("Settlements: " + contracts.settlementStatesById().size());

        if (!contracts.validationIssues().isEmpty()) {
            System.out.println("Validation issues (first 30):");
            contracts.validationIssues().stream().limit(30).forEach(issue -> System.out.println("- " + issue));
            return;
        }

        System.out.println("Validation issues: none");
        contracts.worldRegionStatesById().values().stream().limit(3).forEach(WorldSimulationDiagnostics::printRegion);
        contracts.factionStatesById().values().stream().limit(3).forEach(WorldSimulationDiagnostics::printFaction);
        contracts.settlementStatesById().values().stream().limit(3).forEach(WorldSimulationDiagnostics::printSettlement);

        WorldSimulationTick tick = new WorldSimulationTick(1337L);
        WorldSimulationTickResult tick0 = tick.next(contracts);
        WorldSimulationTickResult tick1 = tick.next(contracts);
        int totalEvents = tick0.events().size() + tick1.events().size();
        System.out.println("tick_preview: tick0_regions=" + tick0.regionSamples().size()
                + " tick1_regions=" + tick1.regionSamples().size()
                + " tick_events=" + totalEvents);
    }

    private static void printRegion(WorldRegionStateDefinition region) {
        System.out.println("region: " + region.id()
                + " plateau=" + region.plateauId()
                + " weather=" + region.weatherProfile()
                + " corruption=" + region.corruptionPressure()
                + " route_risk=" + region.routeRisk());
    }

    private static void printFaction(FactionStateDefinition faction) {
        System.out.println("faction: " + faction.id()
                + " influence=" + faction.influence()
                + " tension=" + faction.tension()
                + " territories=" + faction.territoryPlateaus().size());
    }

    private static void printSettlement(SettlementStateDefinition settlement) {
        System.out.println("settlement: " + settlement.id()
                + " plateau=" + settlement.plateauId()
                + " prosperity=" + settlement.prosperity()
                + " morale=" + settlement.morale()
                + " memory_flags=" + settlement.memoryFlags().size());
    }
}
