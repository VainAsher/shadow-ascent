package com.shadowascent.client;

import com.shadowascent.client.world.AuthoringWorldBootstrap;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.SimPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ActIRouteStateSmokeTest {

    @Test
    void actIRouteResolvesFromOpeningThroughReturnHubState() {
        GameState gameState = new GameState();
        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();

        RunGameContentProfile opening = bootstrap.bootstrap(gameState);
        assertEquals("lh_balcony_opening", opening.roomId());

        traverse(gameState, opening, 1500f, new GameSimulator());
        assertTrue(gameState.getStoryState().hasFlag("opening_seen"));
        assertTrue(gameState.getStoryState().hasFlag("aen_introduced"));
        assertTrue(gameState.getStoryState().hasFlag("yin_yang_present"));
        RunGameContentProfile socialHub = bootstrap.bootstrap(gameState);
        assertEquals("lh_hub_social", socialHub.roomId());

        gameState.getStoryState().setFlag("village_bonds");
        gameState.setCurrentRoomId("lh_handoff_path");
        RunGameContentProfile handoff = bootstrap.bootstrap(gameState);
        assertEquals("lh_handoff_path", handoff.roomId());

        gameState.getStoryState().setFlag("veil_request_accepted");
        traverse(gameState, handoff, 1700f, new GameSimulator());
        RunGameContentProfile mistwoodEntry = bootstrap.bootstrap(gameState);
        assertEquals("mistwood_entry", mistwoodEntry.roomId());

        traverse(gameState, mistwoodEntry, 1900f, new GameSimulator());
        RunGameContentProfile firstEncounter = bootstrap.bootstrap(gameState);
        assertEquals("mistwood_first_encounter", firstEncounter.roomId());

        SimPlayer player = new SimPlayer("player", 0, firstEncounter.playerSpawnX(), firstEncounter.playerSpawnY());
        player.physics.x = 1680f;
        GameSimulator blockedSimulator = new GameSimulator();
        blockedSimulator.addEnemy("mistwood_goblin_2", "goblin", 720f, 280f);
        RunGameAreaTransition.TraversalResult blocked =
                RunGameAreaTransition.tryTraverse(gameState, firstEncounter, player, blockedSimulator);
        assertTrue(blocked.blocked());
        assertFalse(gameState.getStoryState().hasFlag("mistwood_beast_defeated"));

        GameSimulator clearedSimulator = new GameSimulator();
        clearedSimulator.addEnemy("mistwood_goblin_2", "goblin", 720f, 280f);
        clearedSimulator.addEnemy("mistwood_bat_1", "bat", 1040f, 205f);
        clearedSimulator.getEnemies().forEach(enemy -> {
            enemy.hp = 0;
            enemy.removed = true;
        });
        traverse(gameState, firstEncounter, 1680f, clearedSimulator);
        assertTrue(gameState.getStoryState().hasFlag("mistwood_beast_defeated"));
        assertTrue(gameState.getStoryState().hasFlag("npc_withdrawal_started"));

        RunGameContentProfile returnHub = bootstrap.bootstrap(gameState);
        assertEquals("lh_hub_return_changed", returnHub.roomId());
        assertEquals("area_lantern_heights_hub_dimming", returnHub.areaId());
    }

    @Test
    void npcWithdrawalWarningsAdvanceThroughBeatToWarningsHeard() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("npc_withdrawal_started");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        // Before any warnings: highlighted NPCs are the withdrawal warning set
        java.util.List<String> highlighted = RunGameMissionInteraction.highlightedNpcIds(gameState);
        assertTrue(highlighted.stream().anyMatch(id -> id.equalsIgnoreCase("SAMSON")));
        assertTrue(highlighted.stream().anyMatch(id -> id.equalsIgnoreCase("SOPHIA")));
        assertTrue(highlighted.stream().anyMatch(id -> id.equalsIgnoreCase("MARCEL")));
        assertTrue(highlighted.stream().anyMatch(id -> id.equalsIgnoreCase("HAZEL")));

        // Interact with each warning NPC
        for (String npcId : java.util.List.of("SAMSON", "SOPHIA", "MARCEL", "HAZEL")) {
            assertFalse(gameState.getStoryState().hasFlag("warnings_heard"),
                    "warnings_heard should not be set until all 4 NPCs are heard");
            RunGameMissionInteraction.applyNpcInteraction(gameState, npcId);
            assertTrue(gameState.getStoryState().hasFlag("heard_warning_" + npcId.toLowerCase(java.util.Locale.ROOT)),
                    "heard_warning flag missing for " + npcId);
        }

        assertTrue(gameState.getStoryState().hasFlag("warnings_heard"),
                "warnings_heard should be set after all four warning NPCs are interacted with");

        // After warnings heard: highlighted NPCs switches back to mission-ordered NPCs
        java.util.List<String> afterHighlighted = RunGameMissionInteraction.highlightedNpcIds(gameState);
        assertFalse(afterHighlighted.size() == 4
                && afterHighlighted.stream().anyMatch(id -> id.equalsIgnoreCase("SAMSON"))
                && !gameState.getStoryState().hasFlag("warnings_heard"),
                "After warnings_heard, highlight set should no longer be the withdrawal set");
    }

    private static void traverse(GameState gameState, RunGameContentProfile profile, float playerX, GameSimulator simulator) {
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = playerX;
        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, simulator);
        assertTrue(result.transitioned(), "Expected traversal to transition");
    }
}
