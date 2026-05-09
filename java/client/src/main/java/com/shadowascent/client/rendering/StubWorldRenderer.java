package com.shadowascent.client.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.SimEnemy;
import com.shadowascent.core.simulation.SimNPC;
import com.shadowascent.core.simulation.SimPlayer;

/**
 * P1 placeholder renderer — draws all simulation entities as coloured rectangles.
 * No art assets required. Replaced by SpriteWorldRenderer in Phase P3.
 */
public final class StubWorldRenderer {

    private static final Color PLAYER_COLOUR = new Color(0.2f, 0.8f, 0.3f, 1f);
    private static final Color ENEMY_COLOUR  = new Color(0.9f, 0.2f, 0.2f, 1f);
    private static final Color NPC_COLOUR    = new Color(0.3f, 0.6f, 0.9f, 1f);
    private static final Color DEAD_COLOUR   = new Color(0.4f, 0.4f, 0.4f, 0.5f);

    private final ShapeRenderer shapes;

    public StubWorldRenderer() {
        shapes = new ShapeRenderer();
    }

    public void render(GameSimulator simulator) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (SimPlayer p : simulator.getPlayers()) {
            shapes.setColor(p.isDead ? DEAD_COLOUR : PLAYER_COLOUR);
            shapes.rect(p.physics.x, p.physics.y, p.physics.width, p.physics.height);
        }

        for (SimEnemy e : simulator.getEnemies()) {
            if (!e.isAlive()) continue;
            shapes.setColor(ENEMY_COLOUR);
            shapes.rect(e.physics.x, e.physics.y, e.physics.width, e.physics.height);
        }

        for (SimNPC npc : simulator.getNpcs()) {
            shapes.setColor(NPC_COLOUR);
            shapes.rect(npc.physics.x, npc.physics.y, npc.physics.width, npc.physics.height);
        }

        shapes.end();
    }

    public void dispose() {
        shapes.dispose();
    }
}
