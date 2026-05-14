package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ActIVerticalSliceBootstrapTest {

    @Test
    void defaultActIBootUsesBalconyOpeningRoomSpec() {
        GameState gameState = new GameState();

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("lh_balcony_opening", profile.roomId());
        assertEquals("Lantern Heights Balcony", profile.roomDisplayName());
        assertEquals("area_lantern_heights_balcony", profile.areaId());
        assertFalse(profile.worldTiles().isEmpty());
    }

    @Test
    void mistwoodMissionProgressionBootstrapsOutboundRoom() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("mistwood_entry", profile.roomId());
        assertEquals("area_mistwood_path", profile.areaId());
        assertTrue(profile.enemyPlacements().stream().anyMatch(enemy -> "goblin".equals(enemy.enemyType())));
    }

    @Test
    void postMistwoodReturnBootstrapsChangedHubRoom() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.getStoryState().setFlag("mistwood_beast_defeated");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("lh_hub_return_changed", profile.roomId());
        assertEquals("area_lantern_heights_hub_dimming", profile.areaId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "VEIL_MAIDEN".equals(npc.npcId())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "SAMSON".equals(npc.npcId())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "HAZEL".equals(npc.npcId())));
    }

    @Test
    void warningsHeardAdvancesIntoIsolationNightScene() {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("veil_request_accepted");
        gameState.getStoryState().setFlag("mistwood_beast_defeated");
        gameState.getStoryState().setFlag("npc_withdrawal_started");
        gameState.getStoryState().setFlag("warnings_heard");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();

        RunGameContentProfile profile = new AuthoringWorldBootstrap().bootstrap(gameState);

        assertEquals("lh_hub_isolation_night", profile.roomId());
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "VEIL_MAIDEN".equals(npc.npcId())));
        assertTrue(profile.npcPlacements().stream().anyMatch(npc -> "AEN".equals(npc.npcId())));
        assertFalse(profile.npcPlacements().stream().anyMatch(npc -> "SAMSON".equals(npc.npcId())));
        assertFalse(profile.npcPlacements().stream().anyMatch(npc -> "HAZEL".equals(npc.npcId())));
    }
}
