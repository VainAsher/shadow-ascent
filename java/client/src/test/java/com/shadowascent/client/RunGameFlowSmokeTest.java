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
        assertNotNull(defaultProfile.merchantNpcId());

        GameState advancedState = new GameState();
        advancedState.getStoryState().setFlag("entered_ember_monastery");
        advancedState.getStoryState().setPlateau(StoryState.Plateau.EMBER_MONASTERY);
        advancedState.getHubManager().updateHubState();
        RunGameContentProfile advancedProfile = bootstrap.bootstrap(advancedState);

        assertEquals("EMBER_MONASTERY", advancedProfile.plateauId());
        assertTrue(advancedProfile.areaId().startsWith("area_"));
        assertFalse(advancedProfile.npcPlacements().isEmpty());
    }

    @Test
    void defaultStoryStateStillHasContractBackedAvailableMission() {
        GameState gameState = new GameState();

        Mission availableMission = gameState.getMissionManager().getAvailableMissions().stream().findFirst().orElse(null);

        assertNotNull(availableMission);
        assertEquals("village_bonds", availableMission.getId());
        assertFalse(availableMission.getDescription().isBlank());
    }
}
