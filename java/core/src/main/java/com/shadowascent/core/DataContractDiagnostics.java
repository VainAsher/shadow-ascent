package com.shadowascent.core;

import com.shadowascent.core.data.BeatDefinition;
import com.shadowascent.core.data.ContractValidationMode;
import com.shadowascent.core.data.GameDataContracts;

/**
 * CLI diagnostics for data contract integrity and progression readiness.
 */
public final class DataContractDiagnostics {
    private DataContractDiagnostics() {
    }

    public static void main(String[] args) {
        GameState gameState = new GameState();
        GameDataContracts contracts = gameState.getDataContracts();

        System.out.println("=== Shadow Ascent Data Contract Diagnostics ===");
        System.out.println("Root: " + contracts.rootDirectory());
        System.out.println("Summary: " + contracts.summaryLine());
        System.out.println("Validation mode: " + ContractValidationMode.resolve()
                + " (configured=`" + ContractValidationMode.configuredValue() + "`)");
        System.out.println(SaveMigrationMatrix.migrationMatrixSummary());

        if (!contracts.validationIssues().isEmpty()) {
            System.out.println("Validation issues:");
            for (String issue : contracts.validationIssues()) {
                System.out.println("- " + issue);
            }
            return;
        }

        System.out.println("Validation issues: none");
        contracts.nextCriticalBeat(gameState.getStoryState())
            .map(BeatDefinition::id)
            .ifPresent(nextBeat -> System.out.println("Next critical beat: " + nextBeat));
    }
}
