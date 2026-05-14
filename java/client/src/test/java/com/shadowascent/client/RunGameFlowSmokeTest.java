package com.shadowascent.client;

import com.shadowascent.client.world.AuthoringWorldBootstrap;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.Mission;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RunGameFlowSmokeTest {

    @Test
    void productionClientEntryPointsStillExist() {
        assertDoesNotThrow(() -> ShadowAscentGame.class.getDeclaredMethod("startNewGame"));
        assertDoesNotThrow(() -> ShadowAscentGame.class.getDeclaredMethod("continueFromSave"));
        assertDoesNotThrow(() -> ShadowAscentGame.class.getDeclaredMethod("showTitleScreen"));
    }

    @Test
    void authoredBootstrapProducesPlayableProfileForDefaultAndAdvancedPlateaus() {
        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();

        GameState defaultState = new GameState();
        RunGameContentProfile defaultProfile = bootstrap.bootstrap(defaultState);
        assertEquals("LANTERN_HEIGHTS", defaultProfile.plateauId());
        assertFalse(defaultProfile.worldTiles().isEmpty());
        assertEquals("lh_balcony_opening", defaultProfile.roomId());

        GameState advancedState = new GameState();
        advancedState.getStoryState().setFlag("entered_ember_monastery");
        advancedState.getStoryState().setPlateau(StoryState.Plateau.EMBER_MONASTERY);
        advancedState.getHubManager().updateHubState();
        RunGameContentProfile advancedProfile = bootstrap.bootstrap(advancedState);

        assertEquals("EMBER_MONASTERY", advancedProfile.plateauId());
        assertTrue(advancedProfile.areaId().startsWith("area_"));
        assertFalse(advancedProfile.npcPlacements().isEmpty());
        assertFalse(advancedProfile.worldTiles().isEmpty());
    }

    @Test
    void defaultStoryStateStillHasContractBackedAvailableMission() {
        GameState gameState = new GameState();

        Mission availableMission = gameState.getMissionManager().getAvailableMissions().stream().findFirst().orElse(null);

        assertNotNull(availableMission);
        assertEquals("village_bonds", availableMission.getId());
        assertFalse(availableMission.getDescription().isBlank());
    }

    @Test
    void actIMissionOrderingPrefersVeilRequestBeforeMistwoodTraversal() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("aen_introduced");
        gameState.getStoryState().setFlag("yin_yang_present");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        java.util.List<Mission> availableMissions = gameState.getMissionManager().getAvailableMissions();

        assertTrue(availableMissions.stream().anyMatch(mission -> "veil_request".equals(mission.getId())));
        assertFalse(availableMissions.stream().anyMatch(mission -> "mistwood_beast".equals(mission.getId())));
        assertEquals("veil_request", availableMissions.getFirst().getId());
    }

    @Test
    void mistwoodMissionUnlocksAfterVeilRequestAcceptance() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("aen_introduced");
        gameState.getStoryState().setFlag("yin_yang_present");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        java.util.List<Mission> availableMissions = gameState.getMissionManager().getAvailableMissions();

        assertTrue(availableMissions.stream().anyMatch(mission -> "mistwood_beast".equals(mission.getId())));
        assertEquals("mistwood_beast", availableMissions.getFirst().getId());
    }
}
