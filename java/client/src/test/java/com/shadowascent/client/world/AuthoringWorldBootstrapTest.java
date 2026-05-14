package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AuthoringWorldBootstrapTest {

    @Test
    void bootstrapUsesAuthoredBeatAreaForDefaultStoryState() {
        GameState gameState = new GameState();

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("area_lantern_heights_balcony", profile.areaId());
        assertEquals("LANTERN_HEIGHTS", profile.plateauId());
        assertFalse(profile.worldTiles().isEmpty());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "INSTRUCTOR_TAI".equals(npc.npcId())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "MERCHANT_RILU".equals(npc.npcId()) && npc.x() == 350f));
    }

    @Test
    void bootstrapFollowsPlateauSpecificAreaWhenStoryAdvances() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setPlateau(com.shadowascent.core.StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getHubManager().updateHubState();

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("area_hollow_depths_camp", profile.areaId());
        assertEquals("HOLLOW_DEPTHS", profile.plateauId());
        assertNotNull(profile.worldTiles());
        assertTrue(profile.enemyPlacements().stream().anyMatch(enemy -> "goblin".equals(enemy.enemyType())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "SHADE_HERMIT".equals(npc.npcId()) && npc.x() == 360f));
    }

    @Test
    void bootstrapAdvancesIntoHollowDepthsCavesAfterCampIntroFlag() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setFlag("awoke_in_depths");
        gameState.getStoryState().setPlateau(com.shadowascent.core.StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getHubManager().updateHubState();

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("area_hollow_depths_caves", profile.areaId());
        assertTrue(profile.enemyPlacements().stream().anyMatch(enemy -> "bat".equals(enemy.enemyType())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "SHADE_HERMIT".equals(npc.npcId())));
        assertFalse(profile.npcPlacements().stream().anyMatch(npc -> "ADVOCATE".equals(npc.npcId())));
    }

    @Test
    void bootstrapUsesWeightboundArenaAfterWeightDialogueProgression() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setFlag("awoke_in_depths");
        gameState.getStoryState().setFlag("hollow_weight_understood");
        gameState.getStoryState().setPlateau(com.shadowascent.core.StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getHubManager().updateHubState();

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("area_weightbound_mines_arena", profile.areaId());
        assertTrue(profile.enemyPlacements().stream().anyMatch(enemy -> "ogre".equals(enemy.enemyType())));
    }

    @Test
    void firstSparksAreaFocusesOnSmithMonkInsteadOfWholePlateauRoster() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("act2_unlocked");
        gameState.getStoryState().setFlag("awoke_in_depths");
        gameState.getStoryState().setFlag("hollow_weight_understood");
        gameState.getStoryState().setFlag("weightbound_ogre_defeated");
        gameState.getStoryState().setPlateau(com.shadowascent.core.StoryState.Plateau.HOLLOW_DEPTHS);
        gameState.getHubManager().updateHubState();

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("area_hollow_hub_first_sparks", profile.areaId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "SMITH_MONK".equals(npc.npcId())));
        assertFalse(profile.npcPlacements().stream().anyMatch(npc -> "LISTENING_ELDER".equals(npc.npcId())));
        assertFalse(profile.npcPlacements().stream().anyMatch(npc -> "ADVOCATE".equals(npc.npcId())));
    }
}
