package com.shadowascent.client;

import com.shadowascent.client.world.AuthoringWorldBootstrap;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.simulation.SimPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RunGameAreaTransitionTest {

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

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player);

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

        assertFalse(RunGameAreaTransition.describeNearbyGate(gameState, profile, player).isPresent());
    }
}
