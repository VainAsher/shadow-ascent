package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SummitShrineVerticalSliceBootstrapTest {

    @Test
    void routesIntoSummitShrineApproachAfterLanternHeightsCompletionFlags() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
        gameState.getStoryState().setFlag("npc_withdrawal_started");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ss_shrine_approach", profile.roomId());
        assertEquals("SUMMIT_SHRINE", profile.plateauId());
    }

    @Test
    void routesIntoEmptyHubAfterEnteringSummit() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
        gameState.getStoryState().setFlag("npc_withdrawal_started");
        gameState.getStoryState().setFlag("entered_summit");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ss_empty_hub", profile.roomId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "AEN".equals(npc.npcId())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "VEIL_MAIDEN".equals(npc.npcId())));
    }

    @Test
    void routesIntoArenaAfterOnlyMaidenState() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
        gameState.getStoryState().setFlag("npc_withdrawal_started");
        gameState.getStoryState().setFlag("entered_summit");
        gameState.getStoryState().setFlag("empty_hub_only_maiden");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ss_summit_arena", profile.roomId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "SIREN_OF_MASKS".equals(npc.npcId())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "AEN".equals(npc.npcId())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "YIN".equals(npc.npcId())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "YANG".equals(npc.npcId())));
    }

    @Test
    void routesIntoFallAfterYinYangAreTaken() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
        gameState.getStoryState().setFlag("npc_withdrawal_started");
        gameState.getStoryState().setFlag("entered_summit");
        gameState.getStoryState().setFlag("empty_hub_only_maiden");
        gameState.getStoryState().setFlag("mask_truth_seen");
        gameState.getStoryState().setFlag("siren_fight_started");
        gameState.getStoryState().setFlag("yin_yang_taken");
        gameState.getStoryState().setFlag("hollowed");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("ss_summit_fall", profile.roomId());
        assertEquals("area_summit_shrine_fall", profile.areaId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "AEN".equals(npc.npcId())));
    }
}
