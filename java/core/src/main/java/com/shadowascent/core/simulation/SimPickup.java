package com.shadowascent.core.simulation;

import com.shadowascent.core.physics.PhysicsConstants;

/**
 * Server-side pickup entity. Static position; authoritative collection via AABB overlap.
 */
public final class SimPickup {

    public final String  pickupId;
    public final String  pickupType;
    public final float   x, y;
    public final int     width  = PhysicsConstants.TILE_SIZE;
    public final int     height = PhysicsConstants.TILE_SIZE;
    public       boolean alive  = true;

    public final int     slotIdx;
    public final boolean persistent;
    public final int     missionOwnerSlot;

    public int ticksRemaining;

    public SimPickup(String pickupId, String pickupType, float x, float y) {
        this(pickupId, pickupType, x, y, -1, 2700, false, -1);
    }

    public SimPickup(String pickupId, String pickupType, float x, float y,
                     int slotIdx, int ticksRemaining) {
        this(pickupId, pickupType, x, y, slotIdx, ticksRemaining, false, -1);
    }

    public SimPickup(String pickupId, String pickupType, float x, float y,
                     int slotIdx, int ticksRemaining, boolean persistent) {
        this(pickupId, pickupType, x, y, slotIdx, ticksRemaining, persistent, -1);
    }

    public SimPickup(String pickupId, String pickupType, float x, float y,
                     int slotIdx, int ticksRemaining, boolean persistent, int missionOwnerSlot) {
        this.pickupId         = pickupId;
        this.pickupType       = pickupType;
        this.x                = x;
        this.y                = y;
        this.slotIdx          = slotIdx;
        this.ticksRemaining   = ticksRemaining;
        this.persistent       = persistent;
        this.missionOwnerSlot = missionOwnerSlot;
    }

    public boolean overlaps(float px, float py, int pw, int ph) {
        return px < x + width  && px + pw > x
            && py < y + height && py + ph > y;
    }

    public boolean canBeCollectedBy(int playerSlot) {
        return missionOwnerSlot < 0 || missionOwnerSlot == playerSlot;
    }

    public void tick() {
        if (!alive) return;
        if (persistent) return;
        if (--ticksRemaining <= 0) alive = false;
    }
}
