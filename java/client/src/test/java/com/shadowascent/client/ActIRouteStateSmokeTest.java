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

    private static void traverse(GameState gameState, RunGameContentProfile profile, float playerX, GameSimulator simulator) {
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = playerX;
        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, simulator);
        assertTrue(result.transitioned(), "Expected traversal to transition");
    }
}
