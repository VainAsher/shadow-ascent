package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MirrorSummitVerticalSliceBootstrapTest {

    @Test
    void routesIntoMirrorGateWhenMirrorPlateauBegins() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.MIRROR_SUMMIT);
        gameState.getStoryState().setFlag("mirror_gate_opened");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ms_mirror_gate", profile.roomId());
        assertEquals("area_mirror_summit_gate", profile.areaId());
    }

    @Test
    void routesIntoReflectionChamberAfterApproachFlag() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.MIRROR_SUMMIT);
        gameState.getStoryState().setFlag("mirror_gate_opened");
        gameState.getStoryState().setFlag("mirror_approached");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ms_reflection_chamber", profile.roomId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "HOLLOW_REFLECTION".equals(npc.npcId())));
    }

    @Test
    void routesIntoArenaAfterFightStarts() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.MIRROR_SUMMIT);
        gameState.getStoryState().setFlag("mirror_gate_opened");
        gameState.getStoryState().setFlag("mirror_approached");
        gameState.getStoryState().setFlag("reflection_fight_started");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ms_hollow_reflection_arena", profile.roomId());
        assertTrue(profile.encounters().stream().anyMatch(encounter -> "ms_hollow_reflection_trial".equals(encounter.id())));
    }

    @Test
    void routesIntoReleaseWalkAfterBossResolution() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.MIRROR_SUMMIT);
        gameState.getStoryState().setFlag("mirror_gate_opened");
        gameState.getStoryState().setFlag("mirror_approached");
        gameState.getStoryState().setFlag("reflection_fight_started");
        gameState.getStoryState().setFlag("release_accepted");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ms_release_walk", profile.roomId());
        assertEquals("area_release_walk", profile.areaId());
    }
}
