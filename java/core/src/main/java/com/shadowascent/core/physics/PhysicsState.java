package com.shadowascent.core.physics;

/**
 * Mutable per-entity physics state used by runtime movement controllers.
 */
public final class PhysicsState {
    // Position (pixels)
    public float x;
    public float y;

    // Velocity (pixels per tick at 60 Hz)
    public float vx;
    public float vy;

    // Collision flags (reset each tick)
    public boolean onGround;
    public boolean onWall;
    public int wallDir; // -1 left, +1 right, 0 none

    // Gravity/airborne modifiers
    public boolean jumpCutActive;
    public boolean fastFallActive;
    public boolean gravityFrozen;

    // Medium contact flags
    public boolean inWater;
    public boolean onIce;
    public boolean onLava;
    public boolean inGas;

    // Ability bitmask (PhysicsConstants.ABILITY_*)
    public int abilityFlags;

    // Entity dimensions
    public int width;
    public int height;

    public PhysicsState(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void resetCollisionFlags() {
        onGround = false;
        onWall = false;
        wallDir = 0;
        inWater = false;
        onIce = false;
        onLava = false;
        inGas = false;
    }
}
