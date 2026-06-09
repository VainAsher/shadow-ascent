package com.shadowascent.client;

import com.shadowascent.client.world.AuthoringWorldBootstrap;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.simulation.GameSimulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PostClimaxStatePersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void postClimaxSaveLoadsIntoEpilogueRoomNotCampaignRoom() throws Exception {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.BEACON_CLIFF);
        gameState.getStoryState().setFlag("ending_complete");
        gameState.setCurrentRoomId("bc_epilogue_overlook");

        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player1", 0, 180f, 220f);
        SaveLoad saveLoad = new SaveLoad(gameState, tempDir.resolve("post_climax.sav"));
        saveLoad.saveRunGame(simulator, "player1");

        gameState.setCurrentRoomId("bc_cliff_ascent");

        assertTrue(saveLoad.loadRunGame(simulator, "player1"));
        assertEquals("bc_epilogue_overlook", gameState.getCurrentRoomId(),
                "Post-climax save must reload into epilogue room, not be reset");
        assertTrue(gameState.getStoryState().hasFlag("ending_complete"));
    }

    @Test
    void postClimaxBootstrapRoutesToEpilogueArea() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.BEACON_CLIFF);
        gameState.getStoryState().setFlag("ending_complete");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("bc_epilogue_overlook", profile.roomId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "OLD_MAN_RIKU".equals(npc.npcId())));
    }
}
