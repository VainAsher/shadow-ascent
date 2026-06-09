package com.shadowascent.client;

import com.shadowascent.core.GameState;
import com.shadowascent.core.Mission;
import com.shadowascent.core.MissionManager;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.data.SideQuestStepDefinition;
import com.shadowascent.core.simulation.GameSimulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HollowDepthsOptionalContentTest {

    @TempDir
    Path tempDir;

    @Test
    void hollowOptionalQuestStartsCompletesAndPersistsAcrossSaveLoad() throws Exception {
        GameState gameState = new GameState();
        gameState.getStoryState().advanceAct();
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setFlag("awoke_in_depths");
        gameState.getStoryState().setFlag("weightbound_ogre_defeated");

        MissionManager manager = gameState.getMissionManager();
        String missionId = "sq_samson_q2_echoes_in_the_dark";
        Mission mission = gameState.getStoryState().getMission(missionId);
        assertNotNull(mission, "Expected contract-authored Hollow optional mission to be loaded");

        SideQuestStepDefinition step = manager.sideQuestStep(mission.getId()).orElseThrow();
        assertEquals("HOLLOW_DEPTHS", step.plateau());
        mission.setState(Mission.MissionState.AVAILABLE);
        assertTrue(manager.startMission(mission.getId()), "Expected hollow optional quest to start");

        for (String objective : mission.getObjectives()) {
            manager.updateObjectiveProgress(mission.getId(), objective, mission.getRequiredCount(objective));
        }

        String completionFlag = step.setFlags().getFirst();
        assertTrue(gameState.getStoryState().hasFlag(completionFlag));

        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player1", 0, 160f, 280f);
        Path saveFile = tempDir.resolve("hollow_optional.sav");
        SaveLoad saveLoad = new SaveLoad(gameState, saveFile);
        saveLoad.saveRunGame(simulator, "player1");

        GameState loadedGameState = new GameState();
        GameSimulator loadedSimulator = new GameSimulator();
        loadedSimulator.addPlayer("player1", 0, 0f, 0f);
        SaveLoad loadedSaveLoad = new SaveLoad(loadedGameState, saveFile);

        assertTrue(loadedSaveLoad.loadRunGame(loadedSimulator, "player1"));
        assertTrue(loadedGameState.getStoryState().hasFlag(completionFlag));
    }
}
