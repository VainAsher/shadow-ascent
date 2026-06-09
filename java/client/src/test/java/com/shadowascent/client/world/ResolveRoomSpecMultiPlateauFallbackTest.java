package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class ResolveRoomSpecMultiPlateauFallbackTest {

    @Test
    void resolvesNonLanternPlateauRoomByFlagsWhenAreaSpecificLookupMisses() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getStoryState().setFlag("act2_unlocked");

        RoomSpecCatalog catalog = RoomSpecCatalog.load(List.of(
                resolveDataPath("room_specs/lantern_heights_vertical_slice.json"),
                resolveDataPath("room_specs/mistwood_vertical_slice.json"),
                resolveDataPath("room_specs/multi_plateau_routing_fixture.json")));
        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap(
                new AreaPlacementResolver(), catalog);

        RunGameContentProfile profile = bootstrap.bootstrap(gameState);

        assertEquals("HOLLOW_DEPTHS", profile.plateauId());
        assertEquals("fixture_hd_fallback", profile.roomId());
        assertNotNull(profile.roomTransitions());
    }

    private static Path resolveDataPath(String relative) {
        Path current = Paths.get("").toAbsolutePath();
        for (Path cursor = current; cursor != null; cursor = cursor.getParent()) {
            Path candidate = cursor.resolve("data").resolve(relative);
            if (Files.exists(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Cannot find data/" + relative + " from " + current);
    }
}
