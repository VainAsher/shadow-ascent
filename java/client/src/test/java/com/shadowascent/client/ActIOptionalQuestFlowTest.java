package com.shadowascent.client;

import com.shadowascent.core.GameState;
import com.shadowascent.core.Mission;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ActIOptionalQuestFlowTest {

    @Test
    void villageBondsObjectiveProgressRestoresFromStoryFlagsAfterSync() {
        GameState gameState = new GameState();
        // Simulate a partial village_bonds save: samson and sophia talked to, stored as story flags
        gameState.getStoryState().setFlag("talked_to_samson");
        gameState.getStoryState().setFlag("talked_to_sophia");

        // Trigger the sync as GameState.load() does after restoring story state
        gameState.getMissionManager().updateAvailableMissions();

        Mission villageBonds = gameState.getStoryState().getMission("village_bonds");
        assertNotNull(villageBonds, "village_bonds should be in the registry");
        assertTrue(villageBonds.isObjectiveComplete("talk_to_samson"),
                "talk_to_samson objective should be restored from talked_to_samson flag");
        assertTrue(villageBonds.isObjectiveComplete("talk_to_sophia"),
                "talk_to_sophia objective should be restored from talked_to_sophia flag");
        assertFalse(villageBonds.isObjectiveComplete("talk_to_marcel"),
                "talk_to_marcel objective should not be restored — flag not set");
        assertFalse(villageBonds.isObjectiveComplete("talk_to_hazel"),
                "talk_to_hazel objective should not be restored — flag not set");
    }

    @Test
    void optionalSideQuestAvailableAfterVillageBondsCompleted() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("aen_introduced");
        gameState.getStoryState().setFlag("yin_yang_present");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        List<Mission> available = gameState.getMissionManager().getAvailableMissions();
        boolean hasSideQuest = available.stream().anyMatch(m -> m.getId().startsWith("sq_"));
        assertTrue(hasSideQuest,
                "At least one optional side quest should be available in ACT_I LANTERN_HEIGHTS");
        assertTrue(available.stream().anyMatch(m -> "sq_fixture_q1_test_errand".equals(m.getId())),
                "Fixture side quest should prove generic sq_ authoring remains available");
    }

    @Test
    void samsonQuestStartsFromNpcInteractionAfterVillageBondsCompleted() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("aen_introduced");
        gameState.getStoryState().setFlag("yin_yang_present");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        // No active mission before interaction
        assertNull(gameState.getStoryState().getActiveMissionId(),
                "No active mission expected before NPC interaction");

        // Interact with SAMSON — should start his side quest
        RunGameMissionInteraction.InteractionResult result =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "SAMSON");

        assertTrue(result.missionStarted(), "Samson interaction should start his side quest");
        assertNotNull(gameState.getStoryState().getActiveMissionId(),
                "Active mission should be set after Samson interaction");
        assertTrue(gameState.getStoryState().getActiveMissionId().startsWith("sq_"),
                "Started mission should be a side quest (sq_ prefix)");
    }
}
