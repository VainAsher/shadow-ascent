package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BeaconCliffVerticalSliceBootstrapTest {

    @Test
    void routesIntoCliffAscentWhenBeaconPlateauBegins() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.BEACON_CLIFF);
        gameState.getStoryState().setFlag("beacon_cliff_unlocked");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("bc_cliff_ascent", profile.roomId());
        assertEquals("area_beacon_cliff_approach", profile.areaId());
    }

    @Test
    void routesIntoAncientBeaconAfterFinalWordsAreHeard() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.BEACON_CLIFF);
        gameState.getStoryState().setFlag("beacon_cliff_unlocked");
        gameState.getStoryState().setFlag("final_words_heard");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("bc_ancient_beacon", profile.roomId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "AEN".equals(npc.npcId())));
    }

    @Test
    void routesIntoTwofoldStarsAfterBeaconIsLit() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.BEACON_CLIFF);
        gameState.getStoryState().setFlag("beacon_cliff_unlocked");
        gameState.getStoryState().setFlag("final_words_heard");
        gameState.getStoryState().setFlag("beacon_lit");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("bc_twofold_stars", profile.roomId());
        assertEquals("area_twofold_stars", profile.areaId());
    }
}
