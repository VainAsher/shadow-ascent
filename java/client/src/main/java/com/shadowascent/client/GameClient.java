package com.shadowascent.client;

import com.shadowascent.core.GameConfig;
import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.Mission;
import com.shadowascent.core.NPC;
import com.shadowascent.core.data.BeatDefinition;

import java.nio.file.Path;
import java.util.List;

public final class GameClient {
    public static void main(String[] args) {
        System.out.println(GameConfig.TITLE + " client starting...");
        GameState gameState = new GameState();
        System.out.println("Data contracts: " + gameState.getDataContracts().summaryLine());

        // Initialize Act I starting state
        StoryState storyState = gameState.getStoryState();
        gameState.getDataContracts().nextCriticalBeat(storyState)
            .map(BeatDefinition::id)
            .ifPresent(nextBeat -> System.out.println("Next critical beat: " + nextBeat));
        storyState.setFlag("started_act_i");
        storyState.grantAbility("jump");
        storyState.grantAbility("basic_movement");

        System.out.println("=== Act I QA Route Demo ===");
        System.out.println("Current hub state: " + storyState.getCurrentHubState());
        System.out.println("Active NPCs:");

        // Show active NPCs
        storyState.getAllNPCs().values().stream()
            .filter(NPC::isActive)
            .forEach(npc -> {
                String dialogue = gameState.getHubManager().getNPCDialogue(npc.getId());
                System.out.println("  " + npc.getDisplayName() + ": " + dialogue);
            });

        System.out.println("\nAvailable missions:");
        List<Mission> availableMissions = gameState.getMissionManager().getAvailableMissions();
        for (Mission mission : availableMissions) {
            System.out.println("  " + mission.getDisplayName() + " - " + mission.getDescription());
        }

        // Simulate mission progression
        System.out.println("\n=== Simulating Village Bonds Mission ===");
        gameState.getMissionManager().startMission("village_bonds");

        // Complete mission through objective evidence.
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_samson", 1);
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_sophia", 1);
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_marcel", 1);
        gameState.getMissionManager().updateObjectiveProgress("village_bonds", "talk_to_hazel", 1);

        System.out.println("Completed Village Bonds mission!");
        System.out.println("New available missions:");
        availableMissions = gameState.getMissionManager().getAvailableMissions();
        for (Mission mission : availableMissions) {
            System.out.println("  " + mission.getDisplayName());
        }

        // Simulate hub state change due to story progression
        System.out.println("\n=== Hub State Evolution ===");
        System.out.println("Initial hub state: " + storyState.getCurrentHubState());

        // Start the veil influence
        storyState.setFlag("npc_withdrawal_started");
        gameState.getHubManager().updateHubState();

        System.out.println("After veil influence begins: " + storyState.getCurrentHubState());
        System.out.println("Remaining active NPCs:");
        storyState.getAllNPCs().values().stream()
            .filter(NPC::isActive)
            .forEach(npc -> System.out.println("  " + npc.getDisplayName()));

        System.out.println("\nCurrent story state: " + storyState.serialize());

        // Demonstrate campaign progression
        System.out.println("\n=== Campaign Progression Demo ===");

        gameState.getMissionManager().startMission("dojo_practice");
        gameState.getMissionManager().updateObjectiveProgress("dojo_practice", "practice_forms", 1);
        gameState.getMissionManager().updateObjectiveProgress("dojo_practice", "learn_dash", 1);

        gameState.getMissionManager().startMission("mistwood_beast");
        gameState.getMissionManager().updateObjectiveProgress("mistwood_beast", "defeat_beast", 1);

        gameState.getMissionManager().startMission("veil_request");
        gameState.getMissionManager().updateObjectiveProgress("veil_request", "hear_veil_offer", 1);
        gameState.getMissionManager().updateObjectiveProgress("veil_request", "accept_or_decline", 1);

        System.out.println("After veil acceptance - Act: " + storyState.getCurrentAct());

        // Progression through objectives and mission completion.
        gameState.getMissionManager().startMission("hollow_descent");
        gameState.getMissionManager().updateObjectiveProgress("hollow_descent", "enter_hollow_depths", 1);
        gameState.getMissionManager().updateObjectiveProgress("hollow_descent", "find_veil_source", 1);
        System.out.println("After hollow depths - Act: " + storyState.getCurrentAct() +
                          ", Plateau: " + storyState.getCurrentPlateau());

        gameState.getMissionManager().startMission("monastery_arrival");
        gameState.getMissionManager().updateObjectiveProgress("monastery_arrival", "climb_to_monastery", 1);
        gameState.getMissionManager().updateObjectiveProgress("monastery_arrival", "enter_monastery", 1);
        System.out.println("After monastery arrival - Act: " + storyState.getCurrentAct() +
                          ", Plateau: " + storyState.getCurrentPlateau());

        // Show emotional state
        storyState.addYin(10);
        storyState.addYang(5);
        System.out.println("Emotional balance: " + storyState.getEmotionalBalance() +
                          " (Dominance: " + storyState.getEmotionalDominance() + ")");

        try {
            Path savePath = Path.of("save", "slot1.sav");
            gameState.save(savePath);
            System.out.println("Saved game state to " + savePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to save game state: " + e.getMessage());
        }
    }
}
