package com.shadowascent.client.tools;

import com.shadowascent.client.world.RoomSpecCatalog;
import com.shadowascent.core.data.GameDataContracts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class RunGameAuthoringDiagnosticsTest {

    @Test
    void diagnosticsScanAllProductionRoomSpecFiles() {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        GameDataContracts contracts = GameDataContracts.loadDefault();

        RunGameAuthoringDiagnostics.Report report = RunGameAuthoringDiagnostics.validate(catalog, contracts);

        assertTrue(report.errors().isEmpty(), () -> "expected clean diagnostics, got: " + report.errors());
    }
}
