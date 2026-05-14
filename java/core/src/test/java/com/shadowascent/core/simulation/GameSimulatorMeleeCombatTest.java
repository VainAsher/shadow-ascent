package com.shadowascent.core.simulation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GameSimulatorMeleeCombatTest {

    @Test
    void meleeAttackHitsEnemyInFrontButNotBehind() {
        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player", 0, 100f, 300f);
        simulator.addEnemy("front_enemy", "goblin", 132f, 300f);
        simulator.addEnemy("rear_enemy", "goblin", 32f, 300f);

        InputCommand attack = InputCommand.neutral(1);
        attack.attack = true;
        simulator.applyInput("player", attack);
        simulator.tick(1f / 60f);

        SimEnemy frontEnemy = enemy(simulator, "front_enemy");
        SimEnemy rearEnemy = enemy(simulator, "rear_enemy");
        List<SimEvent> events = simulator.drainEvents();

        assertEquals(frontEnemy.maxHp - SimPlayer.MELEE_DAMAGE, frontEnemy.hp);
        assertEquals(rearEnemy.maxHp, rearEnemy.hp);
        assertTrue(events.stream().anyMatch(event -> "ENEMY_DAMAGED".equals(event.type())
                && "front_enemy".equals(event.entityId())));
        assertFalse(events.stream().anyMatch(event -> "ENEMY_DAMAGED".equals(event.type())
                && "rear_enemy".equals(event.entityId())));
    }

    @Test
    void oneSwingAppliesDamageOnlyOnceAcrossActiveFrames() {
        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player", 0, 100f, 300f);
        simulator.addEnemy("front_enemy", "slime", 132f, 300f);

        InputCommand attack = InputCommand.neutral(1);
        attack.attack = true;
        simulator.applyInput("player", attack);
        simulator.tick(1f / 60f);

        SimEnemy enemy = enemy(simulator, "front_enemy");
        int hpAfterFirstTick = enemy.hp;

        for (int i = 0; i < SimPlayer.MELEE_ACTIVE_TICKS + 2; i++) {
            simulator.applyInput("player", InputCommand.neutral(2 + i));
            simulator.tick(1f / 60f);
        }

        assertEquals(hpAfterFirstTick, enemy.hp);
    }

    private static SimEnemy enemy(GameSimulator simulator, String enemyId) {
        return simulator.getEnemies().stream()
                .filter(enemy -> enemyId.equals(enemy.enemyId))
                .findFirst()
                .orElseThrow();
    }
}
