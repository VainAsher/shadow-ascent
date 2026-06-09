package com.shadowascent.client;

import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.simulation.GameSimulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CampaignContinuitySaveLoadBoundaryTest {

    @TempDir
    Path tempDir;

    @Test
    void saveLoadPreservesPlateauBoundaryProgressionAcrossSummitAndHollow() throws Exception {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.getStoryState().setFlag("yin_yang_taken");
        gameState.getStoryState().setFlag("hollowed");
        gameState.setCurrentRoomId("ss_summit_fall");
        gameState.setPendingRoomSpawnId("spawn_fall_exit");

        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player1", 0, 180f, 240f);
        SaveLoad saveLoad = new SaveLoad(gameState, tempDir.resolve("campaign_continuity_boundary.sav"));

        saveLoad.saveRunGame(simulator, "player1");

        gameState.getStoryState().setPlateau(StoryState.Plateau.LANTERN_HEIGHTS);
        gameState.getStoryState().removeFlag("yin_yang_taken");
        gameState.getStoryState().removeFlag("hollowed");
        gameState.setCurrentRoomId("lh_balcony_opening");
        gameState.setPendingRoomSpawnId(null);

        assertTrue(saveLoad.loadRunGame(simulator, "player1"));
        assertEquals(StoryState.Plateau.SUMMIT_SHRINE, gameState.getStoryState().getCurrentPlateau(),
                "Plateau must survive save/load at the Summit to Hollow boundary");
        assertTrue(gameState.getStoryState().hasFlag("yin_yang_taken"),
                "Critical Summit transition flag must survive save/load");
        assertTrue(gameState.getStoryState().hasFlag("hollowed"),
                "Hollowing flag must survive save/load");
        assertEquals("ss_summit_fall", gameState.getCurrentRoomId(),
                "Boundary room id must survive save/load");
        assertEquals("spawn_fall_exit", gameState.getPendingRoomSpawnId(),
                "Boundary spawn id must survive save/load");
        assertTrue(gameState.getMissionManager().startMission("hollow_descent"),
                "Save/load must preserve enough mission state for the Summit to Hollow boundary to continue");
    }
}
