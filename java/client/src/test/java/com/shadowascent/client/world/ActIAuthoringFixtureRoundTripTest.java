package com.shadowascent.client;

import com.shadowascent.client.world.AreaPlacementResolver;
import com.shadowascent.client.world.AuthoringWorldBootstrap;
import com.shadowascent.client.world.RoomSpecCatalog;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.SimPlayer;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ActIAuthoringFixtureRoundTripTest {

    @Test
    void fixtureRoomLoadsAndBootstrapsWithoutRuntimeCodeChanges() {
        Path fixturePath = resolveFixturePath();
        List<Path> files = new ArrayList<>(RoomSpecCatalog.defaultRoomSpecFiles());
        files.add(fixturePath);
        RoomSpecCatalog catalog = RoomSpecCatalog.load(files);
        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap(new AreaPlacementResolver(), catalog);

        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("fixture_annex_unlocked");

        // No Java changes were made to runtime code to make this room load — that is the invariant this test protects.
        RunGameContentProfile profile = bootstrap.bootstrap(gameState);

        assertEquals("lh_fixture_annex", profile.roomId());
        assertTrue(catalog.roomsForPlateau("LANTERN_HEIGHTS").stream().anyMatch(room -> "lh_fixture_annex".equals(room.id())));
    }

    @Test
    void fixtureRoomFreeExitTraversesBackIntoProductionRoom() {
        Path fixturePath = resolveFixturePath();
        List<Path> files = new ArrayList<>(RoomSpecCatalog.defaultRoomSpecFiles());
        files.add(fixturePath);
        RoomSpecCatalog catalog = RoomSpecCatalog.load(files);
        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap(new AreaPlacementResolver(), catalog);

        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("fixture_annex_unlocked");

        RunGameContentProfile profile = bootstrap.bootstrap(gameState);
        SimPlayer player = new SimPlayer("player", 0, profile.playerSpawnX(), profile.playerSpawnY());
        player.physics.x = 1080f;

        RunGameAreaTransition.TraversalResult result =
                RunGameAreaTransition.tryTraverse(gameState, profile, player, new GameSimulator());

        assertTrue(result.transitioned());
        assertEquals("lh_hub_social", gameState.getCurrentRoomId());
    }

    private static Path resolveFixturePath() {
        for (Path cursor = Path.of("").toAbsolutePath(); cursor != null; cursor = cursor.getParent()) {
            Path candidate = cursor.resolve("data").resolve("room_specs").resolve("act_i_authoring_fixture.json");
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to resolve act_i_authoring_fixture.json");
    }
}
