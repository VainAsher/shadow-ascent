package com.shadowascent.client.rendering;

import com.badlogic.gdx.graphics.Color;
import com.shadowascent.core.simulation.EnemyAIState;
import com.shadowascent.core.simulation.EnemyAwarenessState;
import com.shadowascent.core.simulation.SimEnemy;
import com.shadowascent.core.simulation.SimPlayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

final class SpriteWorldRendererSelectionTest {

    @Test
    void playerStatesMapToDistinctSpriteRegions() throws Exception {
        Method method = playerRegionMethod();

        SimPlayer idle = new SimPlayer("idle", 0, 0f, 0f);
        idle.physics.onGround = true;
        assertEquals("player_idle", method.invoke(null, idle));

        SimPlayer running = new SimPlayer("running", 0, 0f, 0f);
        running.physics.vx = 120f;
        running.physics.onGround = true;
        assertEquals("player_run", method.invoke(null, running));

        SimPlayer jumping = new SimPlayer("jumping", 0, 0f, 0f);
        jumping.physics.onGround = false;
        assertEquals("player_jump", method.invoke(null, jumping));

        SimPlayer dashing = new SimPlayer("dashing", 0, 0f, 0f);
        dashing.isDashing = true;
        assertEquals("player_dash", method.invoke(null, dashing));

        SimPlayer attacking = new SimPlayer("attacking", 0, 0f, 0f);
        attacking.isAttacking = true;
        assertEquals("player_attack", method.invoke(null, attacking));

        SimPlayer dead = new SimPlayer("dead", 0, 0f, 0f);
        dead.isDead = true;
        assertEquals("player_dead", method.invoke(null, dead));
    }

    @Test
    void playerCombatAndTraversalStatesMapToDistinctTints() throws Exception {
        Method method = playerTintMethod();

        SimPlayer hurt = new SimPlayer("hurt", 0, 0f, 0f);
        hurt.invincibilityTicks = 10;
        assertEquals(new Color(1f, 0.60f, 0.60f, 1f), method.invoke(null, hurt));

        SimPlayer dashing = new SimPlayer("dashing", 0, 0f, 0f);
        dashing.isDashing = true;
        assertEquals(new Color(0.68f, 0.92f, 1f, 1f), method.invoke(null, dashing));

        SimPlayer attacking = new SimPlayer("attacking", 0, 0f, 0f);
        attacking.isAttacking = true;
        assertEquals(new Color(1f, 0.88f, 0.68f, 1f), method.invoke(null, attacking));

        SimPlayer comboAttacking = new SimPlayer("combo", 0, 0f, 0f);
        comboAttacking.isAttacking = true;
        comboAttacking.comboStep = 2;
        assertEquals(new Color(1f, 0.76f, 0.52f, 1f), method.invoke(null, comboAttacking));

        SimPlayer wall = new SimPlayer("wall", 0, 0f, 0f);
        wall.isWallSliding = true;
        assertEquals(new Color(0.86f, 0.86f, 1f, 1f), method.invoke(null, wall));
    }

    @Test
    void enemyStatesMapToDistinctSpriteRegions() throws Exception {
        Method method = enemyRegionMethod();
        Method baseMethod = enemyBaseRegionMethod();

        SimEnemy patrol = enemy();
        assertEquals("enemy_goblin_patrol", method.invoke(null, patrol));
        assertEquals("enemy_goblin_patrol", baseMethod.invoke(null, patrol));

        SimEnemy bat = enemy("bat");
        assertEquals("enemy_bat_patrol", baseMethod.invoke(null, bat));

        SimEnemy slime = enemy("slime");
        assertEquals("enemy_slime_patrol", baseMethod.invoke(null, slime));

        SimEnemy skeleton = enemy("skeleton");
        assertEquals("enemy_skeleton_patrol", baseMethod.invoke(null, skeleton));

        SimEnemy wolf = enemy("wolf");
        assertEquals("enemy_wolf_patrol", baseMethod.invoke(null, wolf));

        SimEnemy alerted = enemy();
        alerted.awarenessState = EnemyAwarenessState.ALERTED;
        assertEquals("enemy_alerted", method.invoke(null, alerted));

        SimEnemy attacking = enemy();
        attacking.aiState = EnemyAIState.ATTACK;
        assertEquals("enemy_attack", method.invoke(null, attacking));

        SimEnemy stunned = enemy();
        stunned.aiState = EnemyAIState.STUNNED;
        assertEquals("enemy_stunned", method.invoke(null, stunned));
    }

    @Test
    void enemyStatesMapToDistinctTints() throws Exception {
        Method method = enemyTintMethod();

        SimEnemy alerted = enemy();
        alerted.awarenessState = EnemyAwarenessState.ALERTED;
        assertEquals(new Color(1f, 0.88f, 0.56f, 1f), method.invoke(null, alerted));

        SimEnemy attacking = enemy();
        attacking.aiState = EnemyAIState.ATTACK;
        assertEquals(new Color(1f, 0.70f, 0.58f, 1f), method.invoke(null, attacking));

        SimEnemy stunned = enemy();
        stunned.aiState = EnemyAIState.STUNNED;
        assertEquals(new Color(0.70f, 0.82f, 1f, 1f), method.invoke(null, stunned));
    }

    private static Method playerRegionMethod() throws Exception {
        try {
            Method method = SpriteWorldRenderer.class.getDeclaredMethod("selectPlayerRegionName", SimPlayer.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            fail("SpriteWorldRenderer should expose selectPlayerRegionName for state-driven rendering");
            throw e;
        }
    }

    private static Method enemyRegionMethod() throws Exception {
        try {
            Method method = SpriteWorldRenderer.class.getDeclaredMethod("selectEnemyRegionName", SimEnemy.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            fail("SpriteWorldRenderer should expose selectEnemyRegionName for state-driven rendering");
            throw e;
        }
    }

    private static Method enemyBaseRegionMethod() throws Exception {
        try {
            Method method = SpriteWorldRenderer.class.getDeclaredMethod("selectEnemyBaseRegionName", SimEnemy.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            fail("SpriteWorldRenderer should expose selectEnemyBaseRegionName for enemy-type rendering");
            throw e;
        }
    }

    private static Method playerTintMethod() throws Exception {
        try {
            Method method = SpriteWorldRenderer.class.getDeclaredMethod("selectPlayerTint", SimPlayer.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            fail("SpriteWorldRenderer should expose selectPlayerTint for combat readability");
            throw e;
        }
    }

    private static Method enemyTintMethod() throws Exception {
        try {
            Method method = SpriteWorldRenderer.class.getDeclaredMethod("selectEnemyTint", SimEnemy.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            fail("SpriteWorldRenderer should expose selectEnemyTint for combat readability");
            throw e;
        }
    }

    private static SimEnemy enemy() {
        return enemy("goblin");
    }

    private static SimEnemy enemy(String type) {
        boolean canFly = "bat".equals(type);
        return new SimEnemy("enemy", type, 0f, 0f, 32, 40, 3, 1, 60f, 180f, 48f, -100f, 100f, canFly);
    }
}
