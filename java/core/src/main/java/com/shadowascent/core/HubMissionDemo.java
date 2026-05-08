package com.shadowascent.core;

import com.shadowascent.core.data.BeatDefinition;

public class HubMissionDemo {
    public static void main(String[] args) {
        System.out.println("=== Shadow Ascent Act I QA Route Demo ===");

        // Create game state
        GameState gameState = new GameState();
        StoryState storyState = gameState.getStoryState();
        System.out.println("Data contracts: " + gameState.getDataContracts().summaryLine());
        gameState.getDataContracts().nextCriticalBeat(storyState)
            .map(BeatDefinition::id)
            .ifPresent(nextBeat -> System.out.println("Next critical beat: " + nextBeat));

        // Initialize Act I
        storyState.setFlag("started_act_i");
        storyState.grantAbility("jump");
        storyState.grantAbility("basic_movement");

        System.out.println("Initial hub state: " + storyState.getCurrentHubState());

        // Show active NPCs
        System.out.println("Active NPCs:");
        storyState.getAllNPCs().values().stream()
            .filter(NPC::isActive)
            .forEach(npc -> {
                String dialogue = gameState.getHubManager().getNPCDialogue(npc.getId());
                System.out.println("  " + npc.getDisplayName() + ": " + dialogue);
            });

        // Show available missions
        System.out.println("\nAvailable missions:");
        gameState.getMissionManager().getAvailableMissions()
            .forEach(mission -> System.out.println("  " + mission.getDisplayName()));

        // Simulate mission progression
        System.out.println("\n=== Completing Village Bonds Mission ===");
        gameState.getMissionManager().startMission("village_bonds");

        // Progress objectives through objective updates (no direct mission-complete flag shortcut).
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_samson", 1);
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_sophia", 1);
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_marcel", 1);
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_hazel", 1);

        System.out.println("Village bonds completed!");
        System.out.println("New available missions:");
        gameState.getMissionManager().getAvailableMissions()
            .forEach(mission -> System.out.println("  " + mission.getDisplayName()));

        // Simulate hub evolution
        System.out.println("\n=== Hub State Evolution ===");
        System.out.println("Before veil influence: " + storyState.getCurrentHubState());

        storyState.setFlag("npc_withdrawal_started");
        gameState.getHubManager().updateHubState();

        System.out.println("After veil influence: " + storyState.getCurrentHubState());
        System.out.println("Remaining active NPCs:");
        storyState.getAllNPCs().values().stream()
            .filter(NPC::isActive)
            .forEach(npc -> System.out.println("  " + npc.getDisplayName()));

        System.out.println("\n=== Campaign Spine Expansion Demo ===");

        // Simulate veil request objective resolution and progression to Act II
        gameState.getMissionManager().startMission("mistwood_beast");
        gameState.getMissionManager().updateObjectiveProgress("mistwood_beast", "defeat_beast", 1);
        gameState.getMissionManager().startMission("veil_request");
        gameState.getMissionManager().updateObjectiveProgress("veil_request", "hear_veil_offer", 1);
        gameState.getMissionManager().updateObjectiveProgress("veil_request", "accept_or_decline", 1);

        System.out.println("Veil accepted - new available missions:");
        java.util.List<Mission> availableMissions = gameState.getMissionManager().getAvailableMissions();
        for (Mission mission : availableMissions) {
            System.out.println("  " + mission.getDisplayName() + " - " + mission.getDescription());
        }

        // Complete hollow descent mission
        System.out.println("\n=== Act II: Hollow Depths ===");
        gameState.getMissionManager().startMission("hollow_descent");
        gameState.getMissionManager().updateObjectiveProgress("hollow_descent", "enter_hollow_depths", 1);
        gameState.getMissionManager().updateObjectiveProgress("hollow_descent", "find_veil_source", 1);

        System.out.println("Act progressed to: " + storyState.getCurrentAct());
        System.out.println("Current plateau: " + storyState.getCurrentPlateau());
        System.out.println("Emotional balance: " + storyState.getEmotionalBalance());

        // Complete lantern restoration
        gameState.getMissionManager().startMission("lantern_restoration");
        gameState.getMissionManager().updateObjectiveProgress("lantern_restoration", "collect_lantern_pieces", 5);
        gameState.getMissionManager().updateObjectiveProgress("lantern_restoration", "restore_lanterns", 1);

        System.out.println("Lanterns collected: " + storyState.getLanternCount());

        // Progress to Act III
        System.out.println("\n=== Act III: Ember Monastery ===");
        gameState.getMissionManager().startMission("monastery_arrival");
        gameState.getMissionManager().updateObjectiveProgress("monastery_arrival", "climb_to_monastery", 1);
        gameState.getMissionManager().updateObjectiveProgress("monastery_arrival", "enter_monastery", 1);

        System.out.println("Act progressed to: " + storyState.getCurrentAct());
        System.out.println("Current plateau: " + storyState.getCurrentPlateau());

        // Complete emotional balance mission
        gameState.getMissionManager().startMission("yin_yang_balance");
        gameState.getMissionManager().updateObjectiveProgress("yin_yang_balance", "understand_yin_yang", 1);
        gameState.getMissionManager().updateObjectiveProgress("yin_yang_balance", "achieve_balance", 1);

        System.out.println("Final abilities: " + storyState.getAbilities());
        System.out.println("Final emotional balance: " + storyState.getEmotionalBalance());
        System.out.println("Emotional dominance: " + storyState.getEmotionalDominance());

        System.out.println("\nCampaign spine expansion completed successfully!");
    }
}
