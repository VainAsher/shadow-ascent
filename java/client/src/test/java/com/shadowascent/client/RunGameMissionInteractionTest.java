package com.shadowascent.client;

import com.shadowascent.core.GameState;
import com.shadowascent.core.Mission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

final class RunGameMissionInteractionTest {

    @Test
    void interactingWithVillageNpcStartsVillageBondsAndAdvancesMatchingObjective() {
        GameState gameState = new GameState();

        RunGameMissionInteraction.InteractionResult result =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "SAMSON");

        Mission mission = gameState.getStoryState().getMission("village_bonds");
        String displayName = mission.getDisplayName();
        assertEquals("village_bonds", gameState.getStoryState().getActiveMissionId());
        assertTrue(result.missionStarted());
        assertTrue(result.objectiveAdvanced());
        assertEquals("village_bonds", result.startedMissionId());
        assertTrue(result.feedLines().contains("Mission started: " + displayName));
        assertTrue(result.feedLines().contains("Objective advanced: Talk To Samson"));
        assertTrue(mission.isObjectiveComplete("talk_to_samson"));
    }

    @Test
    void interactingWithWrongNpcDoesNotStartUnrelatedMission() {
        GameState gameState = new GameState();

        RunGameMissionInteraction.InteractionResult result =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "INSTRUCTOR_TAI");

        assertNull(gameState.getStoryState().getActiveMissionId());
        assertFalse(result.missionStarted());
        assertFalse(result.objectiveAdvanced());
        assertTrue(result.feedLines().isEmpty());
    }

    @Test
    void interactionPromptMentionsMissionStartWhenNpcMatchesAvailableMission() {
        GameState gameState = new GameState();
        String displayName = gameState.getStoryState().getMission("village_bonds").getDisplayName();

        String prompt = RunGameMissionInteraction.interactionPrompt(gameState, "SAMSON", "Samson");

        assertEquals("[E] Talk to Samson and start " + displayName, prompt);
    }

    @Test
    void taiInteractionStartsDojoMissionAfterVillageBondsCompletion() {
        GameState gameState = new GameState();
        completeVillageBonds(gameState);

        RunGameMissionInteraction.InteractionResult result =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "INSTRUCTOR_TAI");

        Mission mission = gameState.getStoryState().getMission("dojo_practice");
        assertEquals("dojo_practice", gameState.getStoryState().getActiveMissionId());
        assertTrue(result.missionStarted());
        assertTrue(result.objectiveAdvanced());
        assertTrue(mission.isObjectiveComplete("practice_forms"));
    }

    @Test
    void orderedRelevantNpcIdsFollowAvailableMissionGivers() {
        GameState gameState = new GameState();

        assertIterableEquals(
                java.util.List.of("HAZEL", "MARCEL", "SAMSON", "SOPHIA"),
                RunGameMissionInteraction.orderedRelevantNpcIds(gameState));
    }

    @Test
    void authoredBeatDialogueLinesAreSelectedForMatchingNpcAndArea() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setPlateau(com.shadowascent.core.StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getHubManager().updateHubState();

        java.util.List<String> lines = RunGameMissionInteraction.authoredDialogueLines(
                gameState,
                "SHADE_HERMIT",
                "area_hollow_depths_camp");

        assertFalse(lines.isEmpty());
        String firstLine = lines.getFirst().toLowerCase(java.util.Locale.ROOT);
        Assertions.assertTrue(firstLine.contains("awaken") || firstLine.contains("spirits") || firstLine.contains("dark"));
    }

    private static void completeVillageBonds(GameState gameState) {
        assertTrue(gameState.getMissionManager().startMission("village_bonds"));
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_samson", 1);
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_sophia", 1);
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_marcel", 1);
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_hazel", 1);
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();
    }
}
