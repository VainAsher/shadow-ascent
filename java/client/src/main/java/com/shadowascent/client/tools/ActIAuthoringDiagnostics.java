package com.shadowascent.client.tools;

import com.shadowascent.client.world.RoomSpecCatalog;
import com.shadowascent.core.data.GameDataContracts;

import java.util.List;

/**
 * Compatibility wrapper preserved for the existing Gradle task and tests.
 */
public final class ActIAuthoringDiagnostics {

    private ActIAuthoringDiagnostics() {
    }

    public static Report validate(RoomSpecCatalog catalog, GameDataContracts contracts) {
        RunGameAuthoringDiagnostics.Report report = RunGameAuthoringDiagnostics.validate(catalog, contracts);
        return new Report(report.errors());
    }

    public static void main(String[] args) {
        RunGameAuthoringDiagnostics.main(args);
    }

    public record Report(List<String> issues) {
        public Report {
            issues = List.copyOf(issues);
        }

        public boolean isClean() {
            return issues.isEmpty();
        }
    }
}
