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

final class EmberMonasteryOptionalContentTest {

    @TempDir
    Path tempDir;

    @Test
    void emberOptionalQuestStartsCompletesAndPersistsAcrossSaveLoad() throws Exception {
        GameState gameState = new GameState();
        gameState.getStoryState().advanceAct();
        gameState.getStoryState().advanceAct();
        gameState.getStoryState().setPlateau(StoryState.Plateau.EMBER_MONASTERY);
        gameState.getStoryState().setFlag("old_road_opened");
        gameState.getStoryState().setFlag("glide_restored");
        gameState.getStoryState().setFlag("entered_ember_monastery");
        gameState.getStoryState().setFlag("hearth_joined");
        gameState.getStoryState().setFlag("air_dodge_restored");
        gameState.getStoryState().setFlag("grapple_restored");
        gameState.getStoryState().setFlag("wall_cling_mastered");

        MissionManager manager = gameState.getMissionManager();
        String missionId = "sq_sophia_q3_map_to_skyroad";
        Mission mission = gameState.getStoryState().getMission(missionId);
        assertNotNull(mission, "Expected contract-authored Ember optional mission to be loaded");

        SideQuestStepDefinition step = manager.sideQuestStep(mission.getId()).orElseThrow();
        assertEquals("EMBER_MONASTERY", step.plateau());
        mission.setState(Mission.MissionState.AVAILABLE);
        assertTrue(manager.startMission(mission.getId()), "Expected Ember optional quest to start");

        for (String objective : mission.getObjectives()) {
            manager.updateObjectiveProgress(mission.getId(), objective, mission.getRequiredCount(objective));
        }

        assertTrue(gameState.getStoryState().hasFlag("skyroad_map_acquired"));
        assertTrue(gameState.getStoryState().hasFlag("sophia_q3_complete"));

        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player1", 0, 160f, 280f);
        Path saveFile = tempDir.resolve("ember_optional.sav");
        SaveLoad saveLoad = new SaveLoad(gameState, saveFile);
        saveLoad.saveRunGame(simulator, "player1");

        GameState loadedGameState = new GameState();
        GameSimulator loadedSimulator = new GameSimulator();
        loadedSimulator.addPlayer("player1", 0, 0f, 0f);
        SaveLoad loadedSaveLoad = new SaveLoad(loadedGameState, saveFile);

        assertTrue(loadedSaveLoad.loadRunGame(loadedSimulator, "player1"));
        assertTrue(loadedGameState.getStoryState().hasFlag("skyroad_map_acquired"));
        assertTrue(loadedGameState.getStoryState().hasFlag("sophia_q3_complete"));
    }
}
