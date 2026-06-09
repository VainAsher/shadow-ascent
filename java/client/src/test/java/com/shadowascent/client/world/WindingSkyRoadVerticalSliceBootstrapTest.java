package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WindingSkyRoadVerticalSliceBootstrapTest {

    @Test
    void routesIntoSkybridgeStartAfterEmberCompletionFlags() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.WINDING_SKYROAD);
        gameState.getStoryState().setFlag("skyroad_opened");
        gameState.getStoryState().setFlag("air_dodge_restored");
        gameState.getStoryState().setFlag("grapple_restored");
        gameState.getStoryState().setFlag("wall_cling_mastered");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("WINDING_SKYROAD", profile.plateauId());
        assertEquals("ws_skybridge_start", profile.roomId());
        assertFalse(profile.roomTransitions().isEmpty());
    }

    @Test
    void routesIntoCloudCrossingAfterAscentBegins() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.WINDING_SKYROAD);
        gameState.getStoryState().setFlag("skyroad_opened");
        gameState.getStoryState().setFlag("ascent_begun");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ws_cloud_crossing", profile.roomId());
        assertEquals("area_skyroad_base", profile.areaId());
    }

    @Test
    void routesIntoStarWindFieldsAfterStarsAreAccepted() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.WINDING_SKYROAD);
        gameState.getStoryState().setFlag("skyroad_opened");
        gameState.getStoryState().setFlag("ascent_begun");
        gameState.getStoryState().setFlag("stars_accepted");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ws_star_wind_fields", profile.roomId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "YIN_STAR".equals(npc.npcId())));
    }

    @Test
    void routesIntoSummitApproachAfterMirrorGateOpens() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.WINDING_SKYROAD);
        gameState.getStoryState().setFlag("skyroad_opened");
        gameState.getStoryState().setFlag("ascent_begun");
        gameState.getStoryState().setFlag("stars_accepted");
        gameState.getStoryState().setFlag("mirror_gate_opened");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ws_summit_approach", profile.roomId());
        assertEquals("area_mirror_summit_gate", profile.areaId());
    }
}
