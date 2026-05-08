package com.shadowascent.server;

import com.shadowascent.core.GameConfig;
import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.HubState;

public final class GameServer {
    public static void main(String[] args) {
        System.out.println(GameConfig.TITLE + " server starting...");
        GameState gameState = new GameState();
        System.out.println("Data contracts: " + gameState.getDataContracts().summaryLine());

        // Server maintains authoritative hub state
        StoryState storyState = gameState.getStoryState();
        storyState.setFlag("server_initialized");
        storyState.setHubState(HubState.VIBRANT); // Start with vibrant hub

        System.out.println("=== Server Hub State Management ===");
        System.out.println("Initial hub state: " + storyState.getCurrentHubState());

        // Simulate story progression on server
        System.out.println("\nSimulating story progression...");

        // Player completes village bonds
        storyState.setFlag("village_bonds");
        gameState.getMissionManager().updateAvailableMissions();
        System.out.println("Village bonds completed - hub remains: " + storyState.getCurrentHubState());

        // Veil influence begins
        storyState.setFlag("npc_withdrawal_started");
        gameState.getHubManager().updateHubState();
        System.out.println("Veil influence begins - hub state: " + storyState.getCurrentHubState());

        // Further progression
        storyState.setFlag("mistwood_beast_defeated");
        gameState.getHubManager().advanceHubState();
        System.out.println("Beast defeated - hub evolves to: " + storyState.getCurrentHubState());

        System.out.println("\nServer authoritative state: " + storyState.serialize());

        // Server would broadcast this state to clients
        System.out.println("Server ready to broadcast hub state to connected clients.");
    }
}
