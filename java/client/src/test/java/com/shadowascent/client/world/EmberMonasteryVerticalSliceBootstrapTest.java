package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmberMonasteryVerticalSliceBootstrapTest {

    @Test
    void routesIntoMonasteryGateAfterOldRoadOpens() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.EMBER_MONASTERY);
        gameState.getStoryState().setFlag("old_road_opened");
        gameState.getStoryState().setFlag("glide_restored");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("EMBER_MONASTERY", profile.plateauId());
        assertEquals("em_monastery_gate", profile.roomId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "BROTHER_KAI".equals(npc.npcId())));
    }

    @Test
    void routesIntoHearthTrialAfterJoiningTheHearth() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.EMBER_MONASTERY);
        gameState.getStoryState().setFlag("old_road_opened");
        gameState.getStoryState().setFlag("glide_restored");
        gameState.getStoryState().setFlag("entered_ember_monastery");
        gameState.getStoryState().setFlag("hearth_joined");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("em_hearth_trial", profile.roomId());
        assertEquals("area_hearth_of_brothers_trial", profile.areaId());
    }

    @Test
    void routesIntoRogaDojoAfterAirAndGrappleUnlocks() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.EMBER_MONASTERY);
        gameState.getStoryState().setFlag("old_road_opened");
        gameState.getStoryState().setFlag("glide_restored");
        gameState.getStoryState().setFlag("entered_ember_monastery");
        gameState.getStoryState().setFlag("hearth_joined");
        gameState.getStoryState().setFlag("air_dodge_restored");
        gameState.getStoryState().setFlag("grapple_restored");

        RunGameContentProfile dojoProfile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("em_roga_dojo", dojoProfile.roomId());
        assertTrue(dojoProfile.npcPlacements().stream().anyMatch(npc -> "MENTOR_ROGA".equals(npc.npcId())));
    }

    @Test
    void routesIntoDepartureArchAfterSkyroadMapIsAcquired() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.EMBER_MONASTERY);
        gameState.getStoryState().setFlag("old_road_opened");
        gameState.getStoryState().setFlag("glide_restored");
        gameState.getStoryState().setFlag("entered_ember_monastery");
        gameState.getStoryState().setFlag("hearth_joined");
        gameState.getStoryState().setFlag("air_dodge_restored");
        gameState.getStoryState().setFlag("grapple_restored");
        gameState.getStoryState().setFlag("wall_cling_mastered");
        gameState.getStoryState().setFlag("samson_returned");
        gameState.getStoryState().setFlag("marcel_returned");
        gameState.getStoryState().setFlag("skyroad_map_acquired");
        gameState.getStoryState().setFlag("beacon_lantern_prepared");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("em_departure_arch", profile.roomId());
        assertEquals("area_skyroad_gate", profile.areaId());
    }
}
