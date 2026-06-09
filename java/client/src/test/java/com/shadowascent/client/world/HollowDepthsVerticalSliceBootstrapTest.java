package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HollowDepthsVerticalSliceBootstrapTest {

    @Test
    void routesIntoAbyssalApproachAtActTwoEntry() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getStoryState().setFlag("act2_unlocked");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("hd_abyssal_approach", profile.roomId());
        assertEquals("area_hollow_depths_camp", profile.areaId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "SHADE_HERMIT".equals(npc.npcId())));
    }

    @Test
    void routesIntoCavernDescentAfterHollowIntro() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setFlag("awoke_in_depths");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("hd_cavern_descent", profile.roomId());
        assertEquals("area_hollow_depths_caves", profile.areaId());
        assertTrue(profile.enemyPlacements().stream().anyMatch(enemy -> "bat".equals(enemy.enemyType())));
    }

    @Test
    void routesIntoWeightboundMinesAfterWeightDialogue() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setFlag("awoke_in_depths");
        gameState.getStoryState().setFlag("hollow_weight_understood");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("hd_weightbound_mines", profile.roomId());
        assertFalse(profile.encounters().isEmpty());
        assertTrue(profile.encounters().stream().anyMatch(encounter -> "hd_weightbound_trial".equals(encounter.id())));
    }

    @Test
    void routesIntoFirstSparksAfterWeightboundVictory() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setFlag("awoke_in_depths");
        gameState.getStoryState().setFlag("hollow_weight_understood");
        gameState.getStoryState().setFlag("weightbound_ogre_defeated");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("hd_first_sparks", profile.roomId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "SMITH_MONK".equals(npc.npcId())));
    }

    @Test
    void routesIntoAbyssalGateAtFinalHollowStage() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setFlag("awoke_in_depths");
        gameState.getStoryState().setFlag("hollow_weight_understood");
        gameState.getStoryState().setFlag("weightbound_ogre_defeated");
        gameState.getStoryState().setFlag("dash_restored");
        gameState.getStoryState().setFlag("shatter_moth_defeated");
        gameState.getStoryState().setFlag("double_jump_restored");
        gameState.getStoryState().setFlag("stone_judge_defeated");

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("hd_abyssal_gate", profile.roomId());
        assertEquals("area_abyssal_gate", profile.areaId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "YIN_MEMORY".equals(npc.npcId())));
        assertTrue(profile.roomTransitions().stream().anyMatch(transition -> "hd_stone_tribunal".equals(transition.targetRoomId())));
    }
}
