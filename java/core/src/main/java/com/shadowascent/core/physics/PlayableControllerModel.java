package com.shadowascent.core.physics;

/**
 * Deterministic playable-controller model used by regression checks.
 *
 * This mirrors the high-level movement profile used in the playtest client:
 * run, dash, jump/double-jump, coyote windows, wall jump, and wall stamina/exhaustion.
 */
public final class PlayableControllerModel {
    private static final float RUN_SPEED_MULT = 0.72f;
    private static final float JUMP_POWER_MULT = 0.88f;
    private static final float DOUBLE_JUMP_POWER_MULT = 0.88f;
    private static final float WALL_JUMP_X_MULT = 0.90f;
    private static final float WALL_JUMP_Y_MULT = 1.25f;
    private static final float DASH_SPEED_MULT = 0.92f;
    private static final float DASH_DURATION_MULT = 0.78f;
    private static final float DASH_CONTROL_LOCK_SECONDS = 0.03f;
    private static final float PLAYER_MARGIN_X = 50f;
    private static final float PLAYER_MARGIN_Y_TOP = 70f;
    private static final float PLAYER_MARGIN_Y_BOTTOM = 50f;
    private static final float PLAYER_RADIUS = 14f;
    private static final float WALL_JUMP_INPUT_LOCK_SECONDS = 0.20f;
    private static final float WALL_STAMINA_DRAIN_MULT = 1.6f;
    private static final float WALL_STAMINA_AIR_REGEN_MULT = 0.35f;

    private final float floorY;
    private final float ceilingY;
    private final float worldLeftX;
    private final float worldRightX;

    private final PhysicsState physics;
    private float lastMoveDirX = 1f;
    private boolean isDashing;
    private float dashTimerSeconds;
    private float dashCooldownSeconds;
    private float dashControlLockSeconds;
    private float dashDirectionX = 1f;
    private float coyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
    private float wallCoyoteTimerSeconds;
    private int lastWallDir = 1;
    private float jumpBufferSeconds;
    private int jumpCount;
    private float wallJumpLockTimerSeconds;
    private float wallStaminaSeconds = PhysicsConstants.MAX_WALL_STAMINA;
    private boolean wallExhaustedAwaitGround;

    public PlayableControllerModel() {
        this(1280, 720);
    }

    public PlayableControllerModel(int worldWidth, int worldHeight) {
        this.floorY = worldHeight - PLAYER_MARGIN_Y_BOTTOM;
        this.ceilingY = PLAYER_MARGIN_Y_TOP + PLAYER_RADIUS;
        this.worldLeftX = PLAYER_MARGIN_X;
        this.worldRightX = worldWidth - PLAYER_MARGIN_X;
        this.physics = new PhysicsState(
                worldWidth * 0.5f,
                floorY,
                PhysicsConstants.PLAYER_WIDTH,
                PhysicsConstants.PLAYER_HEIGHT
        );
        this.physics.onGround = true;
    }

    /**
     * Advance movement state by one frame.
     */
    public void step(float dt, boolean left, boolean right, boolean fastFallHeld, boolean jumpPressed, boolean dashPressed) {
        dt = Math.max(0.001f, Math.min(dt, PhysicsConstants.MAX_FRAME_TIME));
        float tickScale = dt * PhysicsConstants.TARGET_FPS;

        float inputX = 0f;
        if (left) {
            inputX -= 1f;
        }
        if (right) {
            inputX += 1f;
        }
        if (inputX != 0f) {
            lastMoveDirX = inputX;
        }

        if (jumpPressed) {
            jumpBufferSeconds = PhysicsConstants.JUMP_BUFFER_TIME;
        }
        if (dashPressed) {
            startDash(inputX);
        }

        if (jumpBufferSeconds > 0f) {
            jumpBufferSeconds -= dt;
        }
        if (dashCooldownSeconds > 0f) {
            dashCooldownSeconds -= dt;
        }
        if (coyoteTimerSeconds > 0f && !physics.onGround) {
            coyoteTimerSeconds -= dt;
        }
        if (wallCoyoteTimerSeconds > 0f && !physics.onWall) {
            wallCoyoteTimerSeconds -= dt;
        }
        if (wallJumpLockTimerSeconds > 0f) {
            wallJumpLockTimerSeconds -= dt;
        }

        updateWallStamina(dt);

        if (isDashing) {
            if (physics.onWall) {
                endDash();
            } else {
                dashTimerSeconds -= dt;
                dashControlLockSeconds = Math.max(0f, dashControlLockSeconds - dt);
                if (dashTimerSeconds <= 0f) {
                    endDash();
                } else {
                    if (dashControlLockSeconds <= 0f && inputX != 0f) {
                        float requestedDir = inputX > 0f ? 1f : -1f;
                        if (requestedDir != dashDirectionX) {
                            endDash();
                        } else {
                            dashDirectionX = requestedDir;
                        }
                    }
                    if (isDashing) {
                        physics.vx = dashDirectionX * PhysicsConstants.DASH_SPEED * DASH_SPEED_MULT;
                    }
                }
            }
        }

        if (!isDashing && wallJumpLockTimerSeconds <= 0f) {
            float maxSpeed = PhysicsConstants.MAX_RUN_SPEED * RUN_SPEED_MULT;
            if (inputX != 0f) {
                physics.vx = inputX * maxSpeed;
            } else {
                float friction = (float) Math.pow(PhysicsConstants.GROUND_FRICTION, tickScale);
                physics.vx *= friction;
                if (Math.abs(physics.vx) < 0.03f) {
                    physics.vx = 0f;
                }
            }
        }

        boolean canGroundJump = physics.onGround || coyoteTimerSeconds > 0f;
        boolean canWallJump = (physics.onWall || wallCoyoteTimerSeconds > 0f)
                && !canGroundJump
                && !wallExhaustedAwaitGround;
        boolean canDoubleJump = !canGroundJump && !canWallJump && jumpCount == 1 && !isDashing;
        if (jumpBufferSeconds > 0f && canGroundJump && jumpCount == 0) {
            physics.vy = -PhysicsConstants.JUMP_POWER * JUMP_POWER_MULT;
            physics.onGround = false;
            coyoteTimerSeconds = 0f;
            jumpBufferSeconds = 0f;
            jumpCount = 1;
        } else if (jumpBufferSeconds > 0f && canWallJump) {
            int wallDir = physics.onWall
                    ? physics.wallDir
                    : (lastWallDir == 0 ? (lastMoveDirX >= 0f ? 1 : -1) : lastWallDir);
            if (wallDir == 0) {
                wallDir = 1;
            }
            physics.vx = -wallDir * PhysicsConstants.WALL_JUMP_POWER_X * WALL_JUMP_X_MULT;
            physics.vy = -PhysicsConstants.WALL_JUMP_POWER_Y * WALL_JUMP_Y_MULT;
            physics.onWall = false;
            physics.wallDir = 0;
            wallCoyoteTimerSeconds = 0f;
            jumpBufferSeconds = 0f;
            lastMoveDirX = -wallDir;
            jumpCount = 0;
            wallJumpLockTimerSeconds = WALL_JUMP_INPUT_LOCK_SECONDS;
        } else if (jumpBufferSeconds > 0f && canDoubleJump) {
            physics.vy = -PhysicsConstants.DOUBLE_JUMP_POWER * DOUBLE_JUMP_POWER_MULT;
            jumpBufferSeconds = 0f;
            jumpCount = 2;
        }

        float gravityMult = physics.vy < 0f ? 1f : PhysicsConstants.FALL_GRAVITY_MULT;
        if (fastFallHeld && physics.vy > 0f) {
            gravityMult *= PhysicsConstants.FAST_FALL_MULT;
        }
        if (!physics.onGround) {
            physics.vy += PhysicsConstants.GRAVITY * gravityMult * tickScale;
            if (physics.vy > PhysicsConstants.MAX_FALL_SPEED) {
                physics.vy = PhysicsConstants.MAX_FALL_SPEED;
            }
        }

        physics.x += physics.vx * tickScale;
        physics.y += physics.vy * tickScale;
        clampToArena();
    }

    private void startDash(float inputX) {
        if (isDashing || dashCooldownSeconds > 0f) {
            return;
        }

        float dashX = inputX;
        if (dashX == 0f) {
            dashX = lastMoveDirX;
        }
        if (dashX == 0f) {
            dashX = 1f;
        }

        isDashing = true;
        dashDirectionX = dashX >= 0f ? 1f : -1f;
        dashTimerSeconds = PhysicsConstants.DASH_DURATION * DASH_DURATION_MULT;
        dashControlLockSeconds = Math.min(DASH_CONTROL_LOCK_SECONDS, dashTimerSeconds);
        physics.vx = dashDirectionX * PhysicsConstants.DASH_SPEED * DASH_SPEED_MULT;
        physics.vy = 0f;
        physics.onGround = false;
    }

    private void endDash() {
        if (!isDashing) {
            return;
        }
        isDashing = false;
        dashTimerSeconds = 0f;
        dashControlLockSeconds = 0f;
        dashCooldownSeconds = PhysicsConstants.DASH_COOLDOWN;
    }

    private void clampToArena() {
        physics.onGround = false;
        physics.onWall = false;
        physics.wallDir = 0;

        if (physics.x < worldLeftX) {
            physics.x = worldLeftX;
            physics.vx = 0f;
            physics.onWall = true;
            physics.wallDir = -1;
            lastWallDir = -1;
        }
        if (physics.x > worldRightX) {
            physics.x = worldRightX;
            physics.vx = 0f;
            physics.onWall = true;
            physics.wallDir = 1;
            lastWallDir = 1;
        }

        if (physics.y > floorY) {
            physics.y = floorY;
            physics.vy = 0f;
            physics.onGround = true;
            coyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
            jumpCount = 0;
        }
        if (physics.y < ceilingY) {
            physics.y = ceilingY;
            if (physics.vy < 0f) {
                physics.vy = 0f;
            }
        }

        if (physics.onWall && !physics.onGround) {
            wallCoyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
            physics.vy = Math.min(physics.vy, PhysicsConstants.WALL_SLIDE_SPEED);
        }
    }

    private void updateWallStamina(float dt) {
        if (physics.onGround) {
            wallStaminaSeconds = PhysicsConstants.MAX_WALL_STAMINA;
            wallExhaustedAwaitGround = false;
            return;
        }

        if (physics.onWall && !physics.onGround) {
            if (!wallExhaustedAwaitGround) {
                wallStaminaSeconds = Math.max(
                        0f,
                        wallStaminaSeconds - (PhysicsConstants.STAMINA_REGEN_RATE * WALL_STAMINA_DRAIN_MULT * dt));
                if (wallStaminaSeconds <= PhysicsConstants.EXHAUST_THRESHOLD) {
                    wallExhaustedAwaitGround = true;
                    wallCoyoteTimerSeconds = 0f;
                }
            } else {
                physics.vy = Math.max(
                        physics.vy,
                        PhysicsConstants.WALL_FRICTION_CLAMP * PhysicsConstants.EXHAUST_PENALTY);
            }
            return;
        }

        if (!wallExhaustedAwaitGround) {
            wallStaminaSeconds = Math.min(
                    PhysicsConstants.MAX_WALL_STAMINA,
                    wallStaminaSeconds + (PhysicsConstants.STAMINA_REGEN_RATE * WALL_STAMINA_AIR_REGEN_MULT * dt));
        }
    }

    // Test hooks
    public void testSetAirborneWithCoyoteWindow(float verticalSpeed) {
        physics.onGround = false;
        physics.vy = verticalSpeed;
        physics.y = floorY - 140f;
        coyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
    }

    public void testSetWallContact(int wallDir) {
        physics.onGround = false;
        physics.onWall = true;
        physics.wallDir = wallDir == 0 ? 1 : (wallDir < 0 ? -1 : 1);
        physics.y = floorY - 140f;
        physics.x = physics.wallDir < 0 ? worldLeftX : worldRightX;
        lastWallDir = physics.wallDir;
        wallCoyoteTimerSeconds = PhysicsConstants.COYOTE_TIME;
    }

    public PhysicsState physics() {
        return physics;
    }

    public float x() {
        return physics.x;
    }

    public float y() {
        return physics.y;
    }

    public float vx() {
        return physics.vx;
    }

    public float vy() {
        return physics.vy;
    }

    public boolean onGround() {
        return physics.onGround;
    }

    public boolean onWall() {
        return physics.onWall;
    }

    public int jumpCount() {
        return jumpCount;
    }

    public boolean isDashing() {
        return isDashing;
    }

    public float wallStaminaSeconds() {
        return wallStaminaSeconds;
    }

    public boolean wallExhaustedAwaitGround() {
        return wallExhaustedAwaitGround;
    }
}
