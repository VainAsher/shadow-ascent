package com.shadowascent.client;

import com.shadowascent.client.world.AuthoringWorldBootstrap;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.SimPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RunGameAreaTransitionTest {

    @Test
    void balconyRoomTransitionAdvancesIntoSocialHub() {
        GameState gameState = new GameState();

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 1500f;
        GameSimulator simulator = new GameSimulator();

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, simulator);

        assertTrue(result.transitioned());
        assertTrue(gameState.getStoryState().hasFlag("opening_seen"));
        assertEquals("lh_hub_social", gameState.getCurrentRoomId());

        RunGameContentProfile transitionedProfile = bootstrap.bootstrap(gameState);
        assertEquals("lh_hub_social", transitionedProfile.roomId());
        assertEquals(120f, transitionedProfile.playerSpawnX());
    }

    @Test
    void socialHubHandoffGateStaysBlockedUntilVillageBondsIsSet() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.setCurrentRoomId("lh_hub_social");

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 1880f;

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, new GameSimulator());

        assertTrue(result.blocked());
        assertEquals("lh_hub_social", gameState.getCurrentRoomId());
    }

    @Test
    void socialHubSupportsBacktrackingToBalcony() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.setCurrentRoomId("lh_hub_social");

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 32f;

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, new GameSimulator());

        assertTrue(result.transitioned());
        assertEquals("lh_balcony_opening", gameState.getCurrentRoomId());

        RunGameContentProfile transitionedProfile = bootstrap.bootstrap(gameState);
        assertEquals("lh_balcony_opening", transitionedProfile.roomId());
    }

    @Test
    void mistwoodEncounterSupportsBacktrackingToEntry() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.setCurrentRoomId("mistwood_first_encounter");

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 24f;

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, new GameSimulator());

        assertTrue(result.transitioned());
        assertEquals("mistwood_entry", gameState.getCurrentRoomId());

        RunGameContentProfile transitionedProfile = bootstrap.bootstrap(gameState);
        assertEquals("mistwood_entry", transitionedProfile.roomId());
    }

    @Test
    void mistwoodEntrySupportsVerticalTraverseIntoEncounterUpperLedge() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.setCurrentRoomId("mistwood_entry");

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 1560f;
        player.physics.y = 120f;

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, new GameSimulator());

        assertTrue(result.transitioned());
        assertEquals("mistwood_first_encounter", gameState.getCurrentRoomId());

        RunGameContentProfile transitionedProfile = bootstrap.bootstrap(gameState);
        assertEquals("mistwood_first_encounter", transitionedProfile.roomId());
        assertEquals(1260f, transitionedProfile.playerSpawnX());
        assertEquals(196f, transitionedProfile.playerSpawnY());
    }

    @Test
    void mistwoodEncounterSupportsVerticalDropBackToEntryUpperShelf() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.setCurrentRoomId("mistwood_first_encounter");

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 860f;
        player.physics.y = 160f;

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, new GameSimulator());

        assertTrue(result.transitioned());
        assertEquals("mistwood_entry", gameState.getCurrentRoomId());

        RunGameContentProfile transitionedProfile = bootstrap.bootstrap(gameState);
        assertEquals("mistwood_entry", transitionedProfile.roomId());
        assertEquals(1540f, transitionedProfile.playerSpawnX());
        assertEquals(146f, transitionedProfile.playerSpawnY());
    }

    @Test
    void verticalRoomTransitionRequiresPlayerWithinYBand() {
        GameState gameState = new GameState();
        RunGameContentProfile profile = new RunGameContentProfile(
                "vertical_room",
                "Vertical Test Room",
                "vertical_test",
                "area_lantern_heights_hub",
                "LANTERN_HEIGHTS",
                200f,
                280f,
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(new com.shadowascent.client.world.RoomTransitionSpec(
                        "gate_upward",
                        "free_exit",
                        "upper_room",
                        "area_lantern_heights_balcony",
                        "spawn_upper",
                        180f,
                        260f,
                        140f,
                        220f,
                        java.util.List.of(),
                        java.util.List.of())),
                null,
                java.util.List.of());

        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 200f;
        player.physics.y = 260f;

        RunGameAreaTransition.TraversalResult outsideBand =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, new GameSimulator());

        assertFalse(outsideBand.transitioned());

        player.physics.y = 150f;

        RunGameAreaTransition.TraversalResult withinBand =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, new GameSimulator());

        assertTrue(withinBand.transitioned());
        assertEquals("upper_room", gameState.getCurrentRoomId());
        assertEquals("spawn_upper", gameState.getPendingRoomSpawnId());
    }

    @Test
    void mistwoodReturnGateUsesTypedEncounterInsteadOfAnyLivingEnemy() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.setCurrentRoomId("mistwood_first_encounter");

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 1680f;

        GameSimulator simulator = new GameSimulator();
        simulator.addEnemy("mistwood_goblin_2", "goblin", 720f, 280f);
        simulator.addEnemy("mistwood_bat_1", "bat", 1040f, 205f);
        simulator.addEnemy("ambient_straggler", "goblin", 240f, 280f);
        simulator.getEnemies().stream()
                .filter(enemy -> "mistwood_goblin_2".equals(enemy.enemyId) || "mistwood_bat_1".equals(enemy.enemyId))
                .forEach(enemy -> {
                    enemy.hp = 0;
                    enemy.removed = true;
                });

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, simulator);

        assertTrue(result.transitioned());
        assertTrue(gameState.getStoryState().hasFlag("mistwood_beast_defeated"));
        assertTrue(gameState.getStoryState().hasFlag("npc_withdrawal_started"));
        assertEquals("lh_hub_return_changed", gameState.getCurrentRoomId());
    }

    @Test
    void clearedMistwoodEncounterCanTraverseIntoDeeperAuthoredRoom() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.setCurrentRoomId("mistwood_first_encounter");

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 1480f;

        GameSimulator simulator = new GameSimulator();
        simulator.addEnemy("mistwood_goblin_2", "goblin", 720f, 280f);
        simulator.addEnemy("mistwood_bat_1", "bat", 1040f, 205f);
        simulator.getEnemies().forEach(enemy -> {
            enemy.hp = 0;
            enemy.removed = true;
        });

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, simulator);

        assertTrue(result.transitioned());
        assertTrue(gameState.getStoryState().hasFlag("mistwood_annex_opened"));
        assertEquals("mistwood_afterglow_pass", gameState.getCurrentRoomId());

        RunGameContentProfile deeperProfile = bootstrap.bootstrap(gameState);
        assertEquals("mistwood_afterglow_pass", deeperProfile.roomId());
    }

    @Test
    void hollowCampGateTraversalAdvancesIntoCaves() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getHubManager().updateHubState();

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 1540f;
        GameSimulator simulator = new GameSimulator();

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, simulator);

        assertTrue(result.transitioned());
        assertTrue(gameState.getStoryState().hasFlag("awoke_in_depths"));
        assertEquals("area_hollow_depths_caves", bootstrap.bootstrap(gameState).areaId());
    }

    @Test
    void gatePromptStaysNullWhenPlayerIsNotNearAnyGate() {
        GameState gameState = new GameState();
        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 80f;

        assertFalse(RunGameAreaTransition.describeNearbyGate(gameState, profile, player, new GameSimulator()).isPresent());
    }

    @Test
    void weightboundGateStaysBlockedUntilArenaEnemiesAreCleared() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setFlag("awoke_in_depths");
        gameState.getStoryState().setFlag("hollow_weight_understood");
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getHubManager().updateHubState();

        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 1360f;
        GameSimulator simulator = new GameSimulator();
        simulator.addEnemy("weightbound_ogre", "goblin", 860f, 270f);

        RunGameAreaTransition.TraversalResult blocked =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, simulator);

        assertTrue(blocked.blocked());
        assertFalse(gameState.getStoryState().hasFlag("weightbound_ogre_defeated"));

        simulator.getEnemies().getFirst().hp = 0;
        simulator.getEnemies().getFirst().removed = true;

        RunGameAreaTransition.TraversalResult cleared =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, simulator);

        assertTrue(cleared.transitioned());
        assertTrue(gameState.getStoryState().hasFlag("weightbound_ogre_defeated"));
    }
}
