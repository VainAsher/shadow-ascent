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

    @Test
    void villageBondsCanProgressAcrossAllRequiredVillagersWithAuthoredDialogue() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        RunGameMissionInteraction.InteractionResult samson =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "SAMSON");
        assertTrue(samson.missionStarted());
        assertTrue(samson.objectiveAdvanced());

        assertTrue(gameState.getStoryState().getNPC("SOPHIA").isActive());
        assertTrue(gameState.getStoryState().getNPC("MARCEL").isActive());
        assertTrue(gameState.getStoryState().getNPC("HAZEL").isActive());
        assertFalse(RunGameMissionInteraction.authoredDialogueLines(gameState, "SOPHIA", "area_lantern_heights_hub").isEmpty());
        assertFalse(RunGameMissionInteraction.authoredDialogueLines(gameState, "MARCEL", "area_lantern_heights_hub").isEmpty());
        assertFalse(RunGameMissionInteraction.authoredDialogueLines(gameState, "HAZEL", "area_lantern_heights_hub").isEmpty());

        RunGameMissionInteraction.InteractionResult sophia =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "SOPHIA");
        RunGameMissionInteraction.InteractionResult marcel =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "MARCEL");
        RunGameMissionInteraction.InteractionResult hazel =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "HAZEL");

        assertTrue(sophia.objectiveAdvanced());
        assertTrue(marcel.objectiveAdvanced());
        assertTrue(hazel.objectiveAdvanced());
        assertTrue(gameState.getStoryState().hasFlag("village_bonds"));
        assertEquals(Mission.MissionState.COMPLETED, gameState.getStoryState().getMission("village_bonds").getState());
    }

    @Test
    void returnHubWarningsCanProgressAcrossRemainingVillagers() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("aen_introduced");
        gameState.getStoryState().setFlag("yin_yang_present");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.getStoryState().setFlag("mistwood_beast_defeated");
        gameState.getStoryState().setFlag("npc_withdrawal_started");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        assertTrue(gameState.getStoryState().getNPC("SAMSON").isActive());
        assertTrue(gameState.getStoryState().getNPC("SOPHIA").isActive());
        assertTrue(gameState.getStoryState().getNPC("MARCEL").isActive());
        assertTrue(gameState.getStoryState().getNPC("HAZEL").isActive());
        assertFalse(RunGameMissionInteraction.authoredDialogueLines(gameState, "SAMSON", "area_lantern_heights_hub_dimming").isEmpty());

        RunGameMissionInteraction.InteractionResult samson =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "SAMSON");
        RunGameMissionInteraction.InteractionResult sophia =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "SOPHIA");
        RunGameMissionInteraction.InteractionResult marcel =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "MARCEL");
        RunGameMissionInteraction.InteractionResult hazel =
                RunGameMissionInteraction.applyNpcInteraction(gameState, "HAZEL");

        assertTrue(samson.feedLines().stream().anyMatch(line -> line.contains("Warning heard")));
        assertTrue(gameState.getStoryState().hasFlag("heard_warning_samson"));
        assertTrue(gameState.getStoryState().hasFlag("heard_warning_sophia"));
        assertTrue(gameState.getStoryState().hasFlag("heard_warning_marcel"));
        assertTrue(gameState.getStoryState().hasFlag("heard_warning_hazel"));
        assertTrue(hazel.feedLines().stream().anyMatch(line -> line.contains("Beat advanced")));
        assertTrue(gameState.getStoryState().hasFlag("warnings_heard"));
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
