package com.shadowascent.client;

import com.shadowascent.client.world.AuthoringWorldBootstrap;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FullCampaignRouteEndToEndTest {

    @Test
    void fullCampaignRouteTraversesAllPlateausAndEndsInStableEpilogue() {
        AuthoringWorldBootstrap bootstrap = new AuthoringWorldBootstrap();

        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.LANTERN_HEIGHTS);
        RunGameContentProfile lanternHeights = bootstrap.bootstrap(gameState);
        assertEquals("LANTERN_HEIGHTS", lanternHeights.plateauId());

        gameState.getStoryState().setFlag("npc_withdrawal_started");
        gameState.setCurrentRoomId(null);
        gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
        RunGameContentProfile summit = bootstrap.bootstrap(gameState);
        assertEquals("SUMMIT_SHRINE", summit.plateauId());
        assertFalse(summit.roomTransitions().isEmpty());

        gameState.getStoryState().setFlag("entered_summit");
        gameState.getStoryState().setFlag("empty_hub_only_maiden");
        gameState.getStoryState().setFlag("yin_yang_taken");
        gameState.getStoryState().setFlag("hollowed");

        gameState.setCurrentRoomId(null);
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getStoryState().setFlag("act2_unlocked");
        RunGameContentProfile hollow = bootstrap.bootstrap(gameState);
        assertEquals("HOLLOW_DEPTHS", hollow.plateauId());
        assertFalse(hollow.roomTransitions().isEmpty());

        gameState.getStoryState().setFlag("awoke_in_depths");
        gameState.getStoryState().setFlag("hollow_weight_understood");
        gameState.getStoryState().setFlag("weightbound_ogre_defeated");
        gameState.getStoryState().setFlag("dash_restored");
        gameState.getStoryState().setFlag("shatter_moth_defeated");
        gameState.getStoryState().setFlag("double_jump_restored");
        gameState.getStoryState().setFlag("stone_judge_defeated");
        gameState.getStoryState().setFlag("glide_restored");
        gameState.getStoryState().setFlag("old_road_opened");
        gameState.getStoryState().setFlag("hollow_depths_explored");

        gameState.setCurrentRoomId(null);
        gameState.getStoryState().setPlateau(StoryState.Plateau.EMBER_MONASTERY);
        RunGameContentProfile ember = bootstrap.bootstrap(gameState);
        assertEquals("EMBER_MONASTERY", ember.plateauId());
        assertTrue(ember.npcPlacements().stream().anyMatch(n -> "BROTHER_KAI".equals(n.npcId())));

        gameState.getStoryState().setFlag("entered_ember_monastery");
        gameState.getStoryState().setFlag("hearth_joined");
        gameState.getStoryState().setFlag("air_dodge_restored");
        gameState.getStoryState().setFlag("grapple_restored");
        gameState.getStoryState().setFlag("wall_cling_mastered");
        gameState.getStoryState().setFlag("samson_returned");
        gameState.getStoryState().setFlag("marcel_returned");
        gameState.getStoryState().setFlag("skyroad_map_acquired");
        gameState.getStoryState().setFlag("beacon_lantern_prepared");
        gameState.getStoryState().setFlag("skyroad_opened");

        gameState.setCurrentRoomId(null);
        gameState.getStoryState().setPlateau(StoryState.Plateau.WINDING_SKYROAD);
        RunGameContentProfile winding = bootstrap.bootstrap(gameState);
        assertEquals("WINDING_SKYROAD", winding.plateauId());

        gameState.getStoryState().setFlag("ascent_begun");
        gameState.getStoryState().setFlag("stars_accepted");
        gameState.getStoryState().setFlag("mirror_gate_opened");

        gameState.setCurrentRoomId(null);
        gameState.getStoryState().setPlateau(StoryState.Plateau.MIRROR_SUMMIT);
        RunGameContentProfile mirror = bootstrap.bootstrap(gameState);
        assertEquals("MIRROR_SUMMIT", mirror.plateauId());

        gameState.getStoryState().setFlag("mirror_approached");
        gameState.getStoryState().setFlag("reflection_fight_started");
        gameState.getStoryState().setFlag("reflection_released");
        gameState.getStoryState().setFlag("release_accepted");
        gameState.getStoryState().setFlag("beacon_cliff_unlocked");

        gameState.setCurrentRoomId(null);
        gameState.getStoryState().setPlateau(StoryState.Plateau.BEACON_CLIFF);
        RunGameContentProfile beacon = bootstrap.bootstrap(gameState);
        assertEquals("BEACON_CLIFF", beacon.plateauId());
        assertFalse(beacon.roomId().contains("epilogue"));

        gameState.getStoryState().setFlag("final_words_heard");
        gameState.getStoryState().setFlag("beacon_lit");
        gameState.getStoryState().setFlag("ending_complete");
        gameState.setCurrentRoomId(null);
        RunGameContentProfile epilogue = bootstrap.bootstrap(gameState);
        assertEquals("bc_epilogue_overlook", epilogue.roomId());
        assertTrue(epilogue.npcPlacements().stream().anyMatch(npc -> "OLD_MAN_RIKU".equals(npc.npcId())));
    }
}
