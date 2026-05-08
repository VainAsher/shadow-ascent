package com.shadowascent.core.simulation;

/**
 * Server-side shuriken projectile. Moved and collision-checked each tick by the simulator.
 */
public final class SimShuriken {

    public static final float W = 12f;
    public static final float H = 12f;

    public final String  shurikenId;
    public final int     ownerSlot;
    public final boolean damagesPlayers;
    public final int     damage;

    public float   x, y;
    public float   vx, vy;

    public boolean alive      = true;
    public boolean stuck      = false;
    public float   stuckTimer = 0f;
    public float   ttl        = 2.0f;

    public SimShuriken(String shurikenId, int ownerSlot,
                       float x, float y, float vx, float vy) {
        this(shurikenId, ownerSlot, x, y, vx, vy, false, 1);
    }

    public SimShuriken(String shurikenId, int ownerSlot,
                       float x, float y, float vx, float vy,
                       boolean damagesPlayers, int damage) {
        this.shurikenId     = shurikenId;
        this.ownerSlot      = ownerSlot;
        this.x              = x;
        this.y              = y;
        this.vx             = vx;
        this.vy             = vy;
        this.damagesPlayers = damagesPlayers;
        this.damage         = Math.max(1, damage);
    }
}
