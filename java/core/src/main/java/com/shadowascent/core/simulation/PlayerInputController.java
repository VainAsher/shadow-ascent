package com.shadowascent.core.simulation;

import com.shadowascent.core.physics.PhysicsConstants;
import com.shadowascent.core.physics.PhysicsState;
import java.util.Locale;

/**
 * Bounded extraction of GameSimulator.applyPlayerInput() into a standalone unit.
 *
 * Applies one tick of player input to a SimPlayer, mutating movement, combat,
 * stance, ninjutsu, teleport, wall-slide, and animation state.
 *
 * External GameSimulator dependencies are handled as follows:
 *   - emitNoise: inlined to sp.noiseLevel / sp.noiseRadius
 *   - updateTraversalContext: stubbed to false (ledge/water traversal is Phase 6 scope)
 *   - applyWaterTraversalTuning: no-op stub
 *   - applyTeleportArrivalEffects: simplified (thunder noise emit, no enemy stun)
 *   - spatialHash block check: replaced with TileQuery FI (null = always unblocked)
 *   - GameConfig constants: inlined as private static finals
 *   - slf4j log calls: dropped (no slf4j in project)
 */
public final class PlayerInputController {

    // ── Inlined GameConfig constants ──────────────────────────────────────────

    private static final float DT = PhysicsConstants.FIXED_DT;

    private static final float MAX_NOISE_RADIUS         = 320f;
    private static final float NOISE_YIN_CROUCH_WALK    = 0.00f;
    private static final float NOISE_YIN_WALK           = 0.15f;
    private static final float NOISE_NEUTRAL_RUN        = 0.40f;
    private static final float NOISE_YIN_DASH           = 0.15f;
    private static final float NOISE_YANG_DASH          = 0.80f;
    private static final float NOISE_ATTACK_MELEE       = 0.70f;
    private static final float NOISE_ATTACK_SHURIKEN    = 0.20f;
    private static final float NOISE_LAND_YIN           = 0.10f;
    private static final float NOISE_LAND_YANG          = 0.50f;
    private static final float THUNDER_STEP_NOISE       = 0.65f;
    private static final float STANCE_SWITCH_BOOST      = 0.035f;
    private static final float FLOW_RECENCY_WINDOW      = 2.0f;

    private static final float YIN_SPEED_MULT           = 0.88f;
    private static final float YANG_SPEED_MULT          = 1.10f;
    private static final float FLOW_SPEED_MULT          = 1.00f;
    private static final float YIN_DASH_SPEED_MULT      = 0.75f;
    private static final float YANG_DASH_SPEED_MULT     = 1.20f;
    private static final float FLOW_DASH_SPEED_MULT     = 1.00f;
    private static final float YIN_WALL_JUMP_X_MULT     = 0.90f;
    private static final float YANG_WALL_JUMP_X_MULT    = 1.15f;
    private static final float FLOW_WALL_JUMP_X_MULT    = 1.00f;

    private static final float SHADOW_STEP_COOLDOWN_MULT   = 1.10f;
    private static final float THUNDER_STEP_COOLDOWN_MULT  = 1.40f;
    private static final float HARMONIC_STEP_COOLDOWN_MULT = 0.60f;

    // ── Tile query for teleport block checks ──────────────────────────────────

    /**
     * Optional tile-block predicate for teleport destination validation.
     * When null all teleport destinations are treated as unblocked.
     */
    @FunctionalInterface
    public interface TileQuery {
        boolean isBlocked(float x, float y, int width, float height);
    }

    private final TileQuery tileQuery;

    public PlayerInputController() { this(null); }

    public PlayerInputController(TileQuery tileQuery) {
        this.tileQuery = tileQuery;
    }

    // ── Public entry point ────────────────────────────────────────────────────

    public void apply(SimPlayer sp, InputCommand cmd) {
        PhysicsState p = sp.physics;

        // ── Ground-state change detection ──────────────────────────────────────
        boolean justLanded     = !sp.wasOnGround && p.onGround;
        boolean justLeftGround = sp.wasOnGround  && !p.onGround;

        if (justLanded) {
            sp.jumpCount   = 0;
            sp.coyoteTimer = 0f;
            sp.jumpBuffer  = 0f;
            float landNoise = "yang".equals(sp.stanceMode) ? NOISE_LAND_YANG : NOISE_LAND_YIN;
            emitNoise(sp, landNoise);
        }

        // ── Coyote time ────────────────────────────────────────────────────────
        if (justLeftGround && sp.jumpCount == 0) {
            sp.coyoteTimer = PhysicsConstants.COYOTE_TIME;
        }
        if (sp.coyoteTimer > 0f) sp.coyoteTimer -= DT;

        // ── Attack / throw cooldowns ───────────────────────────────────────────
        if (sp.attackActiveTicks > 0) {
            sp.attackActiveTicks--;
            if (sp.attackActiveTicks == 0) {
                if (sp.comboQueued && sp.comboStep < SimPlayer.MAX_COMBO_STEPS) {
                    startMeleeAttack(sp, sp.comboStep + 1, sp.queuedAttackAimX, sp.queuedAttackAimY);
                    sp.comboQueued = false;
                } else {
                    sp.isAttacking = false;
                    sp.comboStep = 0;
                    sp.comboQueued = false;
                }
            }
        }
        if (sp.attackCooldown > 0f) sp.attackCooldown -= DT;
        if (sp.throwCooldown  > 0f) {
            sp.throwCooldown -= DT;
            if (sp.throwCooldown <= 0f) sp.isThrowing = false;
        }
        if (sp.blockHitTimer > 0f) {
            sp.blockHitTimer -= DT;
            if (sp.blockHitTimer <= 0f) {
                sp.blockHitTimer = 0f;
                sp.blockHitAnim  = "";
            }
        }

        // ── Dash timers ────────────────────────────────────────────────────────
        if (sp.isDashing) {
            sp.dashTimer -= DT;
            if (sp.dashTimer <= 0f) {
                sp.isDashing    = false;
                sp.dashTimer    = 0f;
                sp.dashCooldown = PhysicsConstants.DASH_COOLDOWN;
            }
        }
        if (sp.dashCooldown > 0f) sp.dashCooldown -= DT;

        // ── Rising-edge input detection ────────────────────────────────────────
        boolean jumpJustPressed         = cmd.jump          && !sp.prevJump;
        boolean dashJustPressed         = cmd.dash          && !sp.prevDash;
        boolean attackJustPressed       = cmd.attack        && !sp.prevAttack;
        boolean throwJustPressed        = cmd.throwShuriken && !sp.prevThrow;
        boolean stanceSwitchJustPressed = cmd.stanceSwitch  && !sp.prevStanceSwitch;

        // ── Stance switching ───────────────────────────────────────────────────
        if (stanceSwitchJustPressed) {
            sp.stanceMode = "yin".equals(sp.stanceMode) ? "yang" : "yin";
            if ("yang".equals(sp.stanceMode)) {
                sp.yinYang.absorbYang(STANCE_SWITCH_BOOST);
            } else {
                sp.yinYang.absorbYin(STANCE_SWITCH_BOOST);
            }
        }
        if (cmd.selectWeapon1) {
            sp.yangPreferredWeaponState = "unarmed";
        } else if (cmd.selectWeapon2) {
            sp.yangPreferredWeaponState = resolveArmedWeaponState(sp);
        }
        syncWeaponStateForStance(sp);

        // ── Guard / parry ──────────────────────────────────────────────────────
        boolean blockHeld = cmd.block && !sp.isDashing && !sp.teleportPhaseMode && !sp.isAttacking;
        if (blockHeld) {
            sp.blockHeldTime += DT;
            sp.isBlocking = true;
            sp.isParrying = sp.blockHeldTime <= SimPlayer.PARRY_WINDOW;
        } else {
            sp.blockHeldTime = 0f;
            sp.isBlocking    = false;
            sp.isParrying    = false;
        }

        // ── Jump buffer ────────────────────────────────────────────────────────
        if (jumpJustPressed) sp.jumpBuffer = PhysicsConstants.JUMP_BUFFER_TIME;
        if (sp.jumpBuffer > 0f) sp.jumpBuffer -= DT;

        // ── Dash initiation ────────────────────────────────────────────────────
        if (dashJustPressed && sp.dashCooldown <= 0f && !sp.isDashing) {
            sp.isDashing = true;
            sp.lastMeaningfulActionTimer = FLOW_RECENCY_WINDOW;
            sp.dashTimer = PhysicsConstants.DASH_DURATION;
            p.vy = 0f;
            float dashNoise = "yang".equals(sp.stanceMode) ? NOISE_YANG_DASH : NOISE_YIN_DASH;
            emitNoise(sp, dashNoise);
        }

        // ── Horizontal movement ────────────────────────────────────────────────
        if (sp.wallJumpLockTimer > 0f) sp.wallJumpLockTimer -= DT;

        // Cancel dash on wall contact
        if (sp.isDashing && p.onWall) {
            sp.isDashing    = false;
            sp.dashTimer    = 0f;
            sp.dashCooldown = PhysicsConstants.DASH_COOLDOWN;
        }

        if (sp.isDashing) {
            p.vx = PhysicsConstants.DASH_SPEED * stanceDashMult(sp) * sp.facing;
        } else if (sp.wallJumpLockTimer > 0f) {
            if (cmd.right) sp.facing =  1;
            if (cmd.left)  sp.facing = -1;
        } else {
            float speedMult = cmd.slowWalk ? 1.0f : 0.6f;
            float maxSpeed  = PhysicsConstants.MAX_RUN_SPEED * speedMult * stanceSpeedMult(sp);
            if ((cmd.left || cmd.right) && p.onGround) {
                float moveNoise;
                if (cmd.crouch && "yin".equals(sp.stanceMode)) {
                    moveNoise = NOISE_YIN_CROUCH_WALK;
                } else if (!cmd.slowWalk && "yin".equals(sp.stanceMode)) {
                    moveNoise = NOISE_YIN_WALK;
                } else {
                    moveNoise = NOISE_NEUTRAL_RUN;
                }
                emitNoise(sp, moveNoise);
            }
            float targetVx = 0f;
            if (cmd.right) targetVx =  maxSpeed;
            if (cmd.left)  targetVx = -maxSpeed;
            if (cmd.crouch) targetVx *= PhysicsConstants.CROUCH_SPEED_MULT;
            p.vx = targetVx;
            if (cmd.right) sp.facing =  1;
            if (cmd.left)  sp.facing = -1;
        }

        // ── Crouch height ──────────────────────────────────────────────────────
        boolean isCrouching = cmd.crouch && p.onGround;
        if (isCrouching && p.height == PhysicsConstants.PLAYER_HEIGHT) {
            int diff = PhysicsConstants.PLAYER_HEIGHT - PhysicsConstants.PLAYER_CROUCH_HEIGHT;
            p.y     += diff;
            p.height = PhysicsConstants.PLAYER_CROUCH_HEIGHT;
        } else if (!isCrouching && p.height == PhysicsConstants.PLAYER_CROUCH_HEIGHT) {
            int diff = PhysicsConstants.PLAYER_HEIGHT - PhysicsConstants.PLAYER_CROUCH_HEIGHT;
            p.y     -= diff;
            p.height = PhysicsConstants.PLAYER_HEIGHT;
        }

        // ── Jump logic ─────────────────────────────────────────────────────────
        // updateTraversalContext is stubbed: ledge/water traversal is Phase 6 scope
        boolean canGroundJump = p.onGround || sp.coyoteTimer > 0f;
        boolean jumpTriggered = jumpJustPressed || (sp.jumpBuffer > 0f && canGroundJump);

        if (canGroundJump && jumpTriggered && sp.jumpCount == 0) {
            p.vy           = -PhysicsConstants.JUMP_POWER;
            p.onGround     = false;
            sp.jumpCount   = 1;
            sp.coyoteTimer = 0f;
            sp.jumpBuffer  = 0f;
        } else if (!canGroundJump && jumpJustPressed && sp.jumpCount == 1 && !sp.isDashing) {
            p.vy          = -PhysicsConstants.DOUBLE_JUMP_POWER;
            sp.jumpCount  = 2;
            sp.jumpBuffer = 0f;
        }

        // ── Wall jump ──────────────────────────────────────────────────────────
        boolean canWallJump = p.onWall || sp.wallCoyoteTimer > 0f;
        if (jumpJustPressed && canWallJump && !p.onGround) {
            int wallDir = (p.wallDir != 0) ? p.wallDir
                        : (sp.lastWallDir != 0) ? sp.lastWallDir
                        : (sp.facing >= 0 ? -1 : 1);
            p.vy                      = -PhysicsConstants.WALL_JUMP_POWER_Y * 1.6f;
            p.vx                      = -wallDir * PhysicsConstants.WALL_JUMP_POWER_X * stanceWallJumpXMult(sp);
            sp.facing                 = -wallDir;
            sp.jumpCount              = 0;
            sp.jumpBuffer             = 0f;
            sp.isWallSliding          = false;
            sp.wallCoyoteTimer        = 0f;
            sp.wallJumpLockTimer      = SimPlayer.WALL_JUMP_INPUT_LOCK;
            sp.awaitGroundAfterExhaust = false;
            p.onWall                  = false;
            p.wallDir                 = 0;
        }

        // Track last wall direction for wall coyote
        if (p.onWall && p.wallDir != 0) sp.lastWallDir = p.wallDir;

        if (p.onWall && !p.onGround) sp.wallCoyoteTimer = PhysicsConstants.COYOTE_TIME;
        else if (sp.wallCoyoteTimer > 0f) sp.wallCoyoteTimer -= DT;

        // ── Gravity modifier flags ─────────────────────────────────────────────
        p.jumpCutActive  = !cmd.jump && p.vy < 0f;
        p.fastFallActive = cmd.down && !p.onGround;

        // ── Stamina + Mana resources ───────────────────────────────────────────
        boolean isRunning = cmd.slowWalk && (cmd.left || cmd.right) && p.onGround;
        if (isRunning) {
            sp.stamina = Math.max(0f, sp.stamina - SimPlayer.STAMINA_RUN_DRAIN * DT);
        } else {
            float regenRate = p.onGround ? SimPlayer.STAMINA_REGEN_RATE : SimPlayer.STAMINA_REGEN_RATE * 0.5f;
            sp.stamina = Math.min(sp.maxStamina, sp.stamina + regenRate * DT);
        }
        sp.mana = Math.min(sp.maxMana, sp.mana + SimPlayer.MANA_REGEN_RATE * DT);

        // ── Ninjutsu ───────────────────────────────────────────────────────────
        if (sp.ninjutsuCooldown > 0f) sp.ninjutsuCooldown -= DT;
        if (sp.ninjutsuCasting) {
            sp.ninjutsuCastTimer -= DT;
            if (sp.ninjutsuCastTimer <= 0f) sp.ninjutsuCasting = false;
        }
        boolean ninjutsuHeld    = cmd.ninjutsu;
        boolean ninjutsuRelease = sp.ninjutsuHeld && !ninjutsuHeld;
        sp.ninjutsuHeld = ninjutsuHeld;
        if (ninjutsuRelease && sp.ninjutsuCooldown <= 0f && sp.mana >= SimPlayer.NINJUTSU_MANA_COST) {
            sp.mana             = Math.max(0f, sp.mana - SimPlayer.NINJUTSU_MANA_COST);
            sp.ninjutsuCasting  = true;
            sp.ninjutsuCastTimer = SimPlayer.NINJUTSU_CAST_TIME;
            sp.ninjutsuCooldown = SimPlayer.NINJUTSU_COOLDOWN;
        }

        // ── Persist state for next tick ────────────────────────────────────────
        sp.wasOnGround      = p.onGround;
        sp.prevJump         = cmd.jump;
        sp.prevDash         = cmd.dash;
        sp.prevAttack       = cmd.attack;
        sp.prevThrow        = cmd.throwShuriken;
        sp.prevStanceSwitch = cmd.stanceSwitch;

        // ── Teleport ───────────────────────────────────────────────────────────
        if (sp.teleportCooldown > 0f) sp.teleportCooldown -= DT;
        if (sp.isTeleporting) {
            sp.teleportInvulnTimer -= DT;
            if (sp.teleportInvulnTimer <= 0f) sp.isTeleporting = false;
        }
        boolean teleportHeld        = cmd.teleport;
        boolean teleportJustPressed = teleportHeld && !sp.prevTeleport;
        sp.prevTeleport = teleportHeld;

        if (teleportJustPressed && sp.teleportCooldown <= 0f
                && !sp.isTeleporting && !sp.teleportPhaseMode) {
            sp.teleportPhaseMode          = true;
            sp.lastMeaningfulActionTimer  = FLOW_RECENCY_WINDOW;
            sp.teleportPhaseTimer         = SimPlayer.TELEPORT_PHASE_TIME;
            sp.teleportOriginX            = p.x;
            sp.teleportOriginY            = p.y;
            sp.teleportCursorX            = p.x;
            sp.teleportCursorY            = p.y;
            sp.teleportType               = resolveTeleportType(sp);
            p.vx = 0f;
            p.vy = 0f;
        }

        if (sp.teleportPhaseMode) {
            p.vx = 0f;
            p.vy = 0f;

            int dirX = (cmd.right ? 1 : 0) - (cmd.left  ? 1 : 0);
            int dirY = (cmd.down  ? 1 : 0) - ((cmd.up || cmd.jump) ? 1 : 0);
            if (dirX != 0 || dirY != 0) {
                float step = SimPlayer.TELEPORT_CURSOR_SPEED * DT;
                sp.teleportCursorX += dirX * step;
                sp.teleportCursorY += dirY * step;
                float cdx  = sp.teleportCursorX - sp.teleportOriginX;
                float cdy  = sp.teleportCursorY - sp.teleportOriginY;
                float dist = (float) Math.sqrt(cdx * cdx + cdy * cdy);
                if (dist > SimPlayer.TELEPORT_RANGE) {
                    sp.teleportCursorX = sp.teleportOriginX + cdx / dist * SimPlayer.TELEPORT_RANGE;
                    sp.teleportCursorY = sp.teleportOriginY + cdy / dist * SimPlayer.TELEPORT_RANGE;
                }
            }

            sp.teleportPhaseTimer -= DT;
            if (sp.teleportPhaseTimer <= 0f) {
                float cx = sp.teleportCursorX;
                float cy = sp.teleportCursorY;
                float checkH = p.height - 1f;
                boolean blocked = tileQuery != null && tileQuery.isBlocked(cx, cy, p.width, checkH);
                if (!blocked) {
                    p.x  = cx;
                    p.y  = cy;
                    p.vx = 0f;
                    p.vy = 0f;
                    applyTeleportArrival(sp);
                }
                sp.teleportPhaseMode   = false;
                sp.isTeleporting       = true;
                sp.teleportInvulnTimer = SimPlayer.TELEPORT_INVULN;
                sp.teleportCooldown    = SimPlayer.TELEPORT_COOLDOWN * teleportCooldownMult(sp);
                sp.isDashing           = false;
            }
        }

        // ── Melee attack ───────────────────────────────────────────────────────
        if (attackJustPressed) {
            int aimX = resolveAttackAimX(sp, cmd);
            int aimY = resolveAttackAimY(cmd);
            if (sp.isAttacking && sp.comboStep > 0 && sp.comboStep < SimPlayer.MAX_COMBO_STEPS) {
                sp.comboQueued = true;
                sp.queuedAttackAimX = aimX;
                sp.queuedAttackAimY = aimY;
            } else if (sp.attackCooldown <= 0f && !sp.isAttacking) {
                startMeleeAttack(sp, 1, aimX, aimY);
            }
        }

        // ── Shuriken throw ─────────────────────────────────────────────────────
        if (throwJustPressed && sp.throwCooldown <= 0f && sp.shurikenAmmo > 0) {
            sp.shurikenAmmo--;
            sp.isThrowing    = true;
            sp.throwCooldown = SimPlayer.SHURIKEN_COOLDOWN;
            sp.pendingShuriken = true;
            sp.lastMeaningfulActionTimer = FLOW_RECENCY_WINDOW;
            emitNoise(sp, NOISE_ATTACK_SHURIKEN);
        }

        // ── Wall slide ─────────────────────────────────────────────────────────
        if (!sp.isOnLedge && !sp.isLedgeClimbing && !sp.isClimbing) {
            applyWallSlide(sp, p);
        } else {
            sp.isWallSliding = false;
        }

        // ── Hazard tile effects ────────────────────────────────────────────────
        if (p.onIce && p.onGround && !cmd.left && !cmd.right) {
            p.vx *= 0.97f;
        }
        // applyWaterTraversalTuning is stubbed (water traversal is future scope)

        // ── Interaction timer tick ─────────────────────────────────────────────
        if (sp.interactionTimer > 0f) {
            sp.interactionTimer -= DT;
            if (sp.interactionTimer <= 0f) {
                sp.interactionTimer = 0f;
                sp.interactionState = "";
            }
        }

        // ── Animation state ────────────────────────────────────────────────────
        if (sp.ninjutsuCasting) {
            sp.animState = sp.ninjutsuHeld ? "ninjutsu_hand" : "ninjutsu_summon";
        } else if (sp.teleportPhaseMode) {
            sp.animState = "idle";
        } else if (sp.isTeleporting) {
            sp.animState = "teleport";
        } else if (sp.isDashing) {
            sp.animState = "dash";
        } else if (sp.blockHitTimer > 0f && sp.blockHitAnim != null && !sp.blockHitAnim.isBlank()) {
            sp.animState = sp.blockHitAnim;
        } else if (sp.isBlocking) {
            if (!p.onGround) sp.animState = "air_block";
            else sp.animState = cmd.crouch ? "crouch_block" : "block";
        } else if (sp.interactionTimer > 0f && sp.interactionState != null && !sp.interactionState.isBlank()) {
            sp.animState = sp.interactionState;
        } else if (p.inWater) {
            if (sp.atWaterSurface) {
                sp.animState = Math.abs(p.vx) > 0.1f ? "swim_surface" : "swim_surface_idle";
            } else if (Math.abs(p.vy) > 0.15f) {
                sp.animState = p.vy < 0f ? "swim_up" : "swim_down";
            } else {
                sp.animState = Math.abs(p.vx) > 0.1f ? "swim" : "swim_idle";
            }
        } else if (sp.isAttacking) {
            sp.animState = "attack";
        } else if (sp.isThrowing) {
            sp.animState = "throw";
        } else if (sp.isClimbing) {
            sp.animState = Math.abs(p.vy) > 0.1f ? "climb" : "climb_idle";
        } else if (sp.isLedgeClimbing) {
            sp.animState = "ledge_climb";
        } else if (sp.isOnLedge) {
            sp.animState = "ledge_idle";
        } else if (sp.isWallSliding) {
            sp.animState = "wall_slide";
        } else if (!p.onGround) {
            sp.animState = p.vy < 0f ? "jump" : "fall";
        } else if (cmd.crouch) {
            sp.animState = Math.abs(p.vx) > 0.1f ? "crouch_walk" : "crouch";
        } else if (Math.abs(p.vx) > 0.1f) {
            sp.animState = cmd.slowWalk ? "run" : "slow_walk";
        } else {
            sp.animState = "idle";
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static void emitNoise(SimPlayer sp, float level) {
        if (level <= 0f) return;
        sp.noiseLevel  = Math.max(sp.noiseLevel, level);
        sp.noiseRadius = sp.noiseLevel * MAX_NOISE_RADIUS;
    }

    private static void startMeleeAttack(SimPlayer sp, int comboStep, int aimX, int aimY) {
        sp.isAttacking = true;
        sp.attackActiveTicks = SimPlayer.MELEE_ACTIVE_TICKS;
        sp.attackCooldown = SimPlayer.MELEE_COOLDOWN;
        sp.meleeHitConsumed = false;
        sp.comboStep = comboStep;
        sp.attackAimX = aimX;
        sp.attackAimY = aimY;
        sp.comboQueued = false;
        sp.lastMeaningfulActionTimer = FLOW_RECENCY_WINDOW;
        emitNoise(sp, NOISE_ATTACK_MELEE);
    }

    private static int resolveAttackAimX(SimPlayer sp, InputCommand cmd) {
        if (cmd.left && !cmd.right) {
            return -1;
        }
        if (cmd.right && !cmd.left) {
            return 1;
        }
        return sp.facing >= 0 ? 1 : -1;
    }

    private static int resolveAttackAimY(InputCommand cmd) {
        if (cmd.up && !cmd.down) {
            return -1;
        }
        if (cmd.down && !cmd.up) {
            return 1;
        }
        return 0;
    }

    private static boolean isInFlow(SimPlayer sp) {
        return sp.yinYang.isBalanced() && sp.lastMeaningfulActionTimer > 0f;
    }

    private static float stanceSpeedMult(SimPlayer sp) {
        if (isInFlow(sp)) return FLOW_SPEED_MULT;
        return "yang".equals(sp.stanceMode) ? YANG_SPEED_MULT : YIN_SPEED_MULT;
    }

    private static float stanceDashMult(SimPlayer sp) {
        if (isInFlow(sp)) return FLOW_DASH_SPEED_MULT;
        return "yang".equals(sp.stanceMode) ? YANG_DASH_SPEED_MULT : YIN_DASH_SPEED_MULT;
    }

    private static float stanceWallJumpXMult(SimPlayer sp) {
        if (isInFlow(sp)) return FLOW_WALL_JUMP_X_MULT;
        return "yang".equals(sp.stanceMode) ? YANG_WALL_JUMP_X_MULT : YIN_WALL_JUMP_X_MULT;
    }

    private static String resolveTeleportType(SimPlayer sp) {
        if (isInFlow(sp)) return "harmonic";
        return "yang".equals(sp.stanceMode) ? "thunder" : "shadow";
    }

    private static float teleportCooldownMult(SimPlayer sp) {
        return switch (sp.teleportType != null ? sp.teleportType : "shadow") {
            case "thunder"  -> THUNDER_STEP_COOLDOWN_MULT;
            case "harmonic" -> HARMONIC_STEP_COOLDOWN_MULT;
            default         -> SHADOW_STEP_COOLDOWN_MULT;
        };
    }

    private static void applyTeleportArrival(SimPlayer sp) {
        if ("thunder".equals(sp.teleportType)) {
            emitNoise(sp, THUNDER_STEP_NOISE);
        }
        // harmonic: reduced cooldown handled via teleportCooldownMult, no AoE
        // shadow: silent, no effects
    }

    private static void syncWeaponStateForStance(SimPlayer sp) {
        String next = preferredWeaponStateForStance(sp);
        if (!next.equals(sp.weaponState)) sp.weaponState = next;
    }

    private static String preferredWeaponStateForStance(SimPlayer sp) {
        if ("yin".equals(sp.stanceMode)) return "unarmed";
        String override = normalizeWeaponState(sp.yangPreferredWeaponState);
        if (!override.isBlank()) return "unarmed".equals(override) ? "unarmed" : override;
        String equipped = weaponStateFromEquippedItem(sp.inventory.equippedWeapon);
        return "unarmed".equals(equipped) ? "sword" : equipped;
    }

    private static String resolveArmedWeaponState(SimPlayer sp) {
        String equipped = weaponStateFromEquippedItem(sp.inventory.equippedWeapon);
        return "unarmed".equals(equipped) ? "sword" : equipped;
    }

    private static String normalizeWeaponState(String value) {
        if (value == null || value.isBlank()) return "";
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "unarmed", "sword", "pistol" -> value.toLowerCase(Locale.ROOT);
            default -> "";
        };
    }

    private static String weaponStateFromEquippedItem(String equippedWeapon) {
        if (equippedWeapon == null || equippedWeapon.isBlank()) return "unarmed";
        String id = equippedWeapon.toLowerCase(Locale.ROOT);
        return id.contains("pistol") ? "pistol" : "sword";
    }

    private static void applyWallSlide(SimPlayer sp, PhysicsState p) {
        if (!"yang".equals(sp.stanceMode)) {
            sp.isWallSliding           = false;
            sp.awaitGroundAfterExhaust = false;
            sp.exhaustDetachFrames     = 0;
            if (sp.wallSlideStamina < SimPlayer.WALL_SLIDE_MAX_STAMINA) {
                sp.wallSlideStamina = Math.min(
                    SimPlayer.WALL_SLIDE_MAX_STAMINA,
                    sp.wallSlideStamina + SimPlayer.WALL_SLIDE_REGEN_RATE * DT);
            }
            return;
        }

        boolean touchingWall = p.onWall && !p.onGround;

        if (sp.exhaustDetachFrames > 0) {
            if (p.wallDir != 0) p.x += -p.wallDir;
            p.onWall  = false;
            p.wallDir = 0;
            p.vy = Math.max(p.vy, 2.0f);
            sp.exhaustDetachFrames--;
            touchingWall = false;
        }

        if (sp.isWallSliding) {
            touchingWall = p.onWall && !p.onGround;
            if (!touchingWall || sp.wallSlideStamina <= SimPlayer.WALL_SLIDE_EXHAUST_THRESH) {
                sp.wallSlideStamina = Math.max(0f,
                    sp.wallSlideStamina - SimPlayer.WALL_SLIDE_EXHAUST_PENALTY);
                sp.isWallSliding           = false;
                sp.awaitGroundAfterExhaust = true;
                sp.exhaustDetachFrames     = 6;
                p.onWall  = false;
                p.wallDir = 0;
                p.vy = Math.max(p.vy, 2.0f);
            } else {
                sp.wallSlideStamina = Math.max(0f,
                    sp.wallSlideStamina - DT * SimPlayer.WALL_SLIDE_DRAIN_MULT);
                p.vy = Math.min(p.vy + 0.3f, SimPlayer.WALL_SLIDE_SPEED);
            }
            return;
        }

        boolean canSlide = touchingWall
            && sp.wallSlideStamina >= SimPlayer.WALL_SLIDE_MIN_STAMINA
            && !sp.awaitGroundAfterExhaust;

        if (canSlide) {
            sp.isWallSliding = true;
            p.vy = Math.min(p.vy + 0.3f, SimPlayer.WALL_SLIDE_SPEED);
            return;
        }

        if (p.onGround) {
            sp.awaitGroundAfterExhaust = false;
            sp.exhaustDetachFrames     = 0;
        }

        boolean blockRegen = touchingWall || sp.awaitGroundAfterExhaust;
        if (!blockRegen && sp.wallSlideStamina < SimPlayer.WALL_SLIDE_MAX_STAMINA) {
            sp.wallSlideStamina = Math.min(
                SimPlayer.WALL_SLIDE_MAX_STAMINA,
                sp.wallSlideStamina + SimPlayer.WALL_SLIDE_REGEN_RATE * DT);
        }

        if (touchingWall && !sp.awaitGroundAfterExhaust) {
            p.vy = Math.min(p.vy + 0.3f, SimPlayer.WALL_FRICTION_SPEED);
        }
    }
}
