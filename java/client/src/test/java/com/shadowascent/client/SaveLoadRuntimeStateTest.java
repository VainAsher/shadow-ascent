package com.shadowascent.client;

import com.shadowascent.core.GameState;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.SimEnemy;
import com.shadowascent.core.simulation.SimPlayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SaveLoadRuntimeStateTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadRoundTripRunGameRuntimeState() throws Exception {
        GameState gameState = new GameState();
        gameState.setCurrentRoomId("mistwood_first_encounter");
        gameState.setPendingRoomSpawnId("spawn_from_entry");
        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player1", 0, 100f, 200f);
        simulator.addEnemy("goblin_1", "goblin", 600f, 280f);
        SaveLoad saveLoad = new SaveLoad(gameState, tempDir.resolve("runGame_slot1.sav"));

        SimPlayer player = simulator.getPlayer("player1");
        player.physics.x = 412f;
        player.physics.y = 255f;
        player.health = 3;
        player.isAttacking = true;
        player.attackActiveTicks = 4;
        player.attackCooldown = 0.3f;
        player.meleeHitConsumed = true;
        player.inventory.addCurrency(50);
        player.inventory.addItem("health_potion", 2);

        SimEnemy enemy = simulator.getEnemies().getFirst();
        enemy.physics.x = 640f;
        enemy.hp = 1;

        saveLoad.saveRunGame(simulator, "player1");

        player.physics.x = 64f;
        player.physics.y = 96f;
        player.health = 1;
        player.inventory.removeCurrency(25);
        player.inventory.removeItem("health_potion", 2);
        enemy.physics.x = 700f;
        enemy.hp = 0;
        enemy.removed = true;
        gameState.setCurrentRoomId("lh_balcony_opening");
        gameState.setPendingRoomSpawnId(null);

        assertTrue(saveLoad.loadRunGame(simulator, "player1"));

        assertEquals("mistwood_first_encounter", gameState.getCurrentRoomId());
        assertEquals("spawn_from_entry", gameState.getPendingRoomSpawnId());
        assertEquals(412f, player.physics.x);
        assertEquals(255f, player.physics.y);
        assertEquals(3, player.health);
        assertEquals(50, player.inventory.currency);
        assertEquals(2, player.inventory.countItem("health_potion"));
        assertFalse(player.isAttacking);
        assertEquals(0, player.attackActiveTicks);
        assertEquals(0f, player.attackCooldown);
        assertFalse(player.meleeHitConsumed);
        assertEquals(640f, enemy.physics.x);
        assertEquals(1, enemy.hp);
        assertFalse(enemy.removed);
    }

    @Test
    void saveAndLoadPreservesAuthoredGrowthRoomIdsAndFixtureSideQuestState() throws Exception {
        GameState gameState = new GameState();
        gameState.getStoryState().setFlag("opening_seen");
        gameState.getStoryState().setFlag("aen_introduced");
        gameState.getStoryState().setFlag("yin_yang_present");
        gameState.getStoryState().setFlag("village_bonds");
        gameState.getStoryState().setFlag("fixture_annex_unlocked");
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();
        RunGameMissionInteraction.applyNpcInteraction(gameState, "LANTERN_KID");
        gameState.setCurrentRoomId("lh_fixture_annex");
        gameState.setPendingRoomSpawnId("spawn_main");

        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player1", 0, 160f, 280f);
        SaveLoad saveLoad = new SaveLoad(gameState, tempDir.resolve("runGame_slot2.sav"));

        saveLoad.saveRunGame(simulator, "player1");

        gameState.setCurrentRoomId("lh_balcony_opening");
        gameState.setPendingRoomSpawnId(null);
        gameState.getStoryState().setActiveMission(null);

        assertTrue(saveLoad.loadRunGame(simulator, "player1"));
        assertEquals("lh_fixture_annex", gameState.getCurrentRoomId());
        assertEquals("spawn_main", gameState.getPendingRoomSpawnId());
        assertEquals("sq_fixture_q1_test_errand", gameState.getStoryState().getActiveMissionId());
    }
}
