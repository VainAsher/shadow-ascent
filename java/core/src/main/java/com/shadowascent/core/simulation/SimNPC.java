package com.shadowascent.core.simulation;

import com.shadowascent.core.physics.PhysicsState;

/**
 * Server-side NPC entity.
 * Patrol movement and player-facing logic; physics updated externally by CollisionSystem.
 */
public final class SimNPC {

    public static final int   DEFAULT_WIDTH       = 48;
    public static final int   DEFAULT_HEIGHT      = 72;

    public static final float PATROL_SPEED        = 30f / 60f;
    public static final float INTERACTION_RADIUS  = 48f;
    private static final float PATROL_WAIT        = 2.0f;
    private static final float TICKS_PER_SEC      = 60f;

    public final String id;
    public final String type;
    public final String characterId;

    public final PhysicsState physics;

    public float   x() { return physics.x; }
    public float   y() { return physics.y; }

    public int     facing         = 1;
    public String  animState      = "idle";
    public boolean isInteractable;

    public final float patrolMinX;
    public final float patrolMaxX;
    private float patrolWaitTimer = 0f;

    public SimNPC(String id, String type, float x, float y,
                  int width, int height,
                  float patrolMinX, float patrolMaxX) {
        this(id, type, "", x, y, width, height, patrolMinX, patrolMaxX);
    }

    public SimNPC(String id, String type, String characterId, float x, float y,
                  int width, int height,
                  float patrolMinX, float patrolMaxX) {
        this.id          = id;
        this.type        = type;
        this.characterId = characterId != null ? characterId : "";
        this.physics     = new PhysicsState(x, y, width, height);
        this.patrolMinX  = patrolMinX;
        this.patrolMaxX  = patrolMaxX;
    }

    public void step(float nearestPlayerX, boolean edgeAhead) {
        float npcCx = physics.x + physics.width * 0.5f;

        isInteractable = false;
        if (!Float.isNaN(nearestPlayerX)) {
            float dist = Math.abs(nearestPlayerX - npcCx);
            if (dist < INTERACTION_RADIUS * 2f) {
                facing = (nearestPlayerX < npcCx) ? -1 : 1;
            }
            isInteractable = dist < INTERACTION_RADIUS;
        }

        if (patrolMinX >= patrolMaxX) {
            animState = "idle";
            return;
        }
        if (patrolWaitTimer > 0f) {
            patrolWaitTimer -= 1f / TICKS_PER_SEC;
            animState = "idle";
            return;
        }

        boolean atRightBound = npcCx >= patrolMaxX;
        boolean atLeftBound  = npcCx <= patrolMinX;
        boolean wallBlocked  = physics.onWall && physics.wallDir == facing;

        if (facing == 1 && (atRightBound || edgeAhead || wallBlocked)) {
            facing = -1;
            patrolWaitTimer = PATROL_WAIT;
        } else if (facing == -1 && (atLeftBound || edgeAhead || wallBlocked)) {
            facing = 1;
            patrolWaitTimer = PATROL_WAIT;
        }

        physics.vx = facing * PATROL_SPEED;
        physics.x += physics.vx;
        animState = "walk";
    }
}
