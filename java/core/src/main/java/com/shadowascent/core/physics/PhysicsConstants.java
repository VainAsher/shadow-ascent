package com.shadowascent.core.physics;

/**
 * Canonical player and world physics tuning values for clean-start runtime lanes.
 *
 * These are imported from the donor profile in indie-ninja-adventures and are the
 * baseline for movement-feel parity work in the playable client.
 */
public final class PhysicsConstants {
    private PhysicsConstants() {}

    // Gravity and falling
    public static final float GRAVITY = 0.4f;
    public static final float FALL_GRAVITY_MULT = 1.5f;
    public static final float FAST_FALL_MULT = 2.0f;
    public static final float JUMP_CUT_MULT = 3.0f;
    public static final float MAX_FALL_SPEED = 12.0f;

    // Horizontal movement
    public static final float MAX_RUN_SPEED = 8.0f;
    public static final float GROUND_ACCEL = 180.0f;
    public static final float GROUND_FRICTION = 0.8f;
    public static final float AIR_ACCEL_MULT = 0.65f;
    public static final float AIR_FRICTION = 0.95f;

    // Jumping
    public static final float JUMP_POWER = 14.5f;
    public static final float DOUBLE_JUMP_POWER = 14.5f;
    public static final float WALL_JUMP_POWER_X = 8.5f;
    public static final float WALL_JUMP_POWER_Y = 14.5f;
    public static final float COYOTE_TIME = 0.12f;
    public static final float JUMP_BUFFER_TIME = 0.14f;
    public static final float CROUCH_JUMP_MULT = 0.7f;

    // Dash
    public static final float DASH_SPEED = 16.0f;
    public static final float DASH_DURATION = 0.16f;
    public static final float DASH_COOLDOWN = 1.0f;

    // Crouch
    public static final float CROUCH_SPEED_MULT = 0.6f;
    public static final float CROUCH_ACCEL_MULT = 0.8f;
    public static final float CROUCH_HEIGHT_RATIO = 0.5f;

    // Wall slide
    public static final float WALL_SLIDE_SPEED = 3.0f;
    public static final float WALL_SLIDE_FRICTION = 0.7f;
    public static final float WALL_FRICTION_CLAMP = 5.0f;
    public static final float MAX_WALL_STAMINA = 3.0f;
    public static final float STAMINA_REGEN_RATE = 2.0f;
    public static final float EXHAUST_THRESHOLD = 0.1f;
    public static final float EXHAUST_PENALTY = 0.5f;

    // Medium effects
    public static final float GAS_DRAG = 0.97f;

    // Ability bitmask flags
    public static final int ABILITY_WATER_WALK = 1 << 0;
    public static final int ABILITY_ICE_GRIP = 1 << 1;
    public static final int ABILITY_GAS_RESIST = 1 << 2;
    public static final int ABILITY_YIN_SIGHT = 1 << 3;

    // Collision
    public static final float SWEPT_COLLISION_THRESHOLD = 8.0f;
    public static final float SWEPT_STEP_SIZE = 12.0f;
    public static final int PLATFORM_GRACE_PIXELS = 4;
    public static final int CORNER_MIN_OVERLAP = 4;
    public static final int CORNER_MAX_OVERLAP = 14;

    // Player dimensions
    public static final int PLAYER_WIDTH = 28;
    public static final int PLAYER_HEIGHT = 56;
    public static final int PLAYER_CROUCH_HEIGHT = 28;

    // World and tiles
    public static final int TILE_SIZE = 32;
    public static final int TILES_PER_ZONE = 8;
    public static final int ROOM_WIDTH_TILES = 128;
    public static final int ROOM_HEIGHT_TILES = 128;

    // Fixed-step clock
    public static final int TARGET_FPS = 60;
    public static final float FIXED_DT = 1.0f / 60.0f;
    public static final float MAX_FRAME_TIME = 0.25f;
}
