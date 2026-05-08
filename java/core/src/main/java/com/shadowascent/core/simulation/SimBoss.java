package com.shadowascent.core.simulation;

import com.shadowascent.core.physics.PhysicsConstants;
import com.shadowascent.core.physics.PhysicsState;

/**
 * Server-side boss simulation entity.
 * Imported from indie-ninja-adventures (com.indieniijah.sim.SimBoss).
 *
 * Four-phase state machine: INTRO → IDLE ↔ MOVE ↔ ATTACK_MELEE/ATTACK_RANGED
 * Phase transitions at 75%/50%/25% HP → PHASE_TRANSITION → VULNERABLE → (resume at higher speed)
 * Boss physics are server-authoritative (not client-authoritative like players).
 */
public final class SimBoss {

    // ── Phase HP thresholds ───────────────────────────────────────────────────
    public static final float PHASE2_RATIO = 0.75f;
    public static final float PHASE3_RATIO = 0.50f;
    public static final float PHASE4_RATIO = 0.25f;

    // ── State durations (seconds) ─────────────────────────────────────────────
    public static final float INTRO_DURATION            = 2.0f;
    public static final float PHASE_TRANSITION_DURATION = 1.5f;
    public static final float VULNERABLE_DURATION       = 3.0f;
    public static final float MELEE_WINDUP              = 0.5f;
    public static final float MELEE_ACTIVE              = 0.25f;
    public static final float MELEE_RECOVERY            = 0.4f;
    public static final float RANGED_WINDUP             = 0.8f;
    public static final float RANGED_ACTIVE             = 0.1f;
    public static final float RANGED_RECOVERY           = 0.6f;
    public static final float BASE_ATTACK_COOLDOWN      = 2.0f;
    public static final float STUN_DURATION             = 1.0f;

    // ── Aggro / range ─────────────────────────────────────────────────────────
    public static final float DETECT_RANGE = 900f;
    public static final float MELEE_RANGE  = 96f;
    public static final float RANGED_RANGE = 400f;
    public static final float INVULN_TICKS = 15;

    // ── Identity ──────────────────────────────────────────────────────────────
    public final String    bossId;
    public final BossType  type;
    public final PhysicsState physics;

    // ── Health ────────────────────────────────────────────────────────────────
    public int    hp;
    public final int maxHp;
    private int   invincibilityTicks = 0;

    // ── Phase state ───────────────────────────────────────────────────────────
    public int  phaseNumber = 1;
    private int pendingPhase = 0;

    // ── AI state ──────────────────────────────────────────────────────────────
    public BossAIState aiState       = BossAIState.INTRO;
    public float       stateTimer    = 0f;
    public float       attackCooldown = 0f;
    public float       attackSubTimer = 0f;
    public int         attackSubPhase = 0;

    // ── Rendering ─────────────────────────────────────────────────────────────
    public boolean facingRight = false;
    public boolean removed     = false;
    public boolean yielded     = false;

    // ── Pattern-specific state fields ─────────────────────────────────────────
    public boolean scriptedLossTriggered       = false;
    public int     sirenAddsPhaseSpawned       = 0;
    public float   sirenTeleportCooldown       = 0f;
    public int     sirenVolleyShotsRemaining   = 0;
    public float   sirenVolleyTimer            = 0f;
    public float   sirenVulnerableTimer        = 0f;
    public java.util.Deque<Float> echoBuffer   = null;
    public float   spawnTimer                  = 0f;
    public boolean speedBurstActive            = false;
    public boolean platformReset               = false;
    public float   veilMaidenIllusionTimer     = 0f;
    public int     veilMaidenIllusionPhaseSpawned = 0;
    public boolean veilMaidenFinalForm         = false;

    // ── Arena bounds ──────────────────────────────────────────────────────────
    public final float arenaMinX;
    public final float arenaMaxX;
    public final float arenaMinY;
    public final float arenaMaxY;

    private float speedMult = 1.0f;

    public SimBoss(String bossId, BossType type, float spawnX, float spawnY) {
        this.bossId  = bossId;
        this.type    = type;
        this.maxHp   = type.maxHp;
        this.hp      = maxHp;
        this.physics = new PhysicsState(spawnX, spawnY, type.width(), type.height());

        float roomW    = PhysicsConstants.ROOM_WIDTH_TILES  * PhysicsConstants.TILE_SIZE;
        float roomH    = PhysicsConstants.ROOM_HEIGHT_TILES * PhysicsConstants.TILE_SIZE;
        float roomLeft = (float) Math.floor(spawnX / roomW) * roomW;
        float roomTop  = (float) Math.floor(spawnY / roomH) * roomH;
        this.arenaMinX = roomLeft + 32f;
        this.arenaMaxX = roomLeft + roomW - type.width() - 32f;
        this.arenaMinY = roomTop  + 24f;
        this.arenaMaxY = roomTop  + roomH - type.height() - 24f;
    }

    public float hpRatio() { return maxHp > 0 ? (float) hp / maxHp : 0f; }

    public float distanceTo(float tx, float ty) {
        float cx = physics.x + physics.width  * 0.5f;
        float cy = physics.y + physics.height * 0.5f;
        float dx = tx - cx, dy = ty - cy;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isAlive() { return hp > 0 && !removed; }

    public String phaseWire() { return "phase_" + phaseNumber; }

    public void clampToArena() {
        physics.x = Math.max(arenaMinX, Math.min(arenaMaxX, physics.x));
        physics.y = Math.max(arenaMinY, Math.min(arenaMaxY, physics.y));
    }

    public boolean takeDamage(int dmg) {
        if (yielded) return false;
        if (invincibilityTicks > 0) return false;
        if (aiState == BossAIState.PHASE_TRANSITION) return false;
        if (aiState == BossAIState.DEAD) return false;

        hp = Math.max(0, hp - dmg);
        invincibilityTicks = (int) INVULN_TICKS;

        if (hp <= 0) {
            aiState = BossAIState.DEAD;
            removed = true;
            return true;
        }
        checkPhaseTransition();
        return false;
    }

    public void forceInvincible(int ticks) {
        if (ticks > invincibilityTicks) invincibilityTicks = ticks;
    }

    public void tickInvincibility() {
        if (invincibilityTicks > 0) invincibilityTicks--;
    }

    public void step(float dt, float nearestX, float nearestY, float dist) {
        if (!isAlive()) return;

        tickInvincibility();
        stateTimer    += dt;
        if (attackCooldown > 0) attackCooldown -= dt;

        float cx = physics.x + physics.width * 0.5f;
        facingRight = nearestX > cx;

        switch (aiState) {
            case INTRO -> {
                physics.vx = 0;
                if (stateTimer >= INTRO_DURATION) enterState(BossAIState.IDLE);
            }
            case IDLE -> {
                physics.vx = 0;
                if (dist <= DETECT_RANGE) enterState(BossAIState.MOVE);
            }
            case MOVE -> {
                float speed = type.moveSpeed * speedMult / 60f;
                physics.x += facingRight ? speed : -speed;
                if (attackCooldown <= 0) {
                    if (dist <= MELEE_RANGE)       startAttack(BossAIState.ATTACK_MELEE);
                    else if (dist <= RANGED_RANGE) startAttack(BossAIState.ATTACK_RANGED);
                }
                if (dist > DETECT_RANGE * 1.5f) enterState(BossAIState.IDLE);
            }
            case ATTACK_SPECIAL -> {
                physics.vx = 0;
                attackSubTimer += dt;
                if (attackSubPhase == 0 && attackSubTimer >= RANGED_WINDUP) {
                    attackSubPhase = 1; attackSubTimer = 0;
                } else if (attackSubPhase == 1 && attackSubTimer >= RANGED_ACTIVE) {
                    attackSubPhase = 2; attackSubTimer = 0;
                } else if (attackSubPhase == 2 && attackSubTimer >= RANGED_RECOVERY) {
                    attackCooldown = BASE_ATTACK_COOLDOWN / speedMult;
                    enterState(BossAIState.MOVE);
                }
            }
            case ATTACK_MELEE, ATTACK_RANGED -> {
                physics.vx = 0;
                attackSubTimer += dt;
                float windupTime = aiState == BossAIState.ATTACK_MELEE ? MELEE_WINDUP  : RANGED_WINDUP;
                float activeTime = aiState == BossAIState.ATTACK_MELEE ? MELEE_ACTIVE  : RANGED_ACTIVE;
                float recovTime  = aiState == BossAIState.ATTACK_MELEE ? MELEE_RECOVERY: RANGED_RECOVERY;
                if (attackSubPhase == 0 && attackSubTimer >= windupTime) {
                    attackSubPhase = 1; attackSubTimer = 0;
                } else if (attackSubPhase == 1 && attackSubTimer >= activeTime) {
                    attackSubPhase = 2; attackSubTimer = 0;
                } else if (attackSubPhase == 2 && attackSubTimer >= recovTime) {
                    attackCooldown = BASE_ATTACK_COOLDOWN / speedMult;
                    enterState(BossAIState.MOVE);
                }
            }
            case VULNERABLE -> {
                physics.vx = 0;
                if (stateTimer >= VULNERABLE_DURATION) enterState(BossAIState.MOVE);
            }
            case PHASE_TRANSITION -> {
                physics.vx = 0;
                if (stateTimer >= PHASE_TRANSITION_DURATION) {
                    phaseNumber  = pendingPhase;
                    speedMult    = 1.0f + (phaseNumber - 1) * 0.2f;
                    pendingPhase = 0;
                    enterState(BossAIState.VULNERABLE);
                }
            }
            case STUNNED -> {
                physics.vx = 0;
                if (stateTimer >= STUN_DURATION) enterState(BossAIState.MOVE);
            }
            case DEAD -> { physics.vx = 0; }
        }
    }

    public boolean isMeleeActive() {
        return aiState == BossAIState.ATTACK_MELEE && attackSubPhase == 1;
    }

    public boolean isRangedFiring() {
        return aiState == BossAIState.ATTACK_RANGED && attackSubPhase == 1
                && attackSubTimer < (1f / 60f) * 2f;
    }

    private void enterState(BossAIState next) {
        aiState    = next;
        stateTimer = 0f;
    }

    private void startAttack(BossAIState attackState) {
        enterState(attackState);
        attackSubPhase = 0;
        attackSubTimer = 0f;
    }

    private void checkPhaseTransition() {
        int targetPhase = phaseNumber;
        if (type == BossType.SIREN) {
            if      (hpRatio() <= 0.34f && phaseNumber < 3) targetPhase = 3;
            else if (hpRatio() <= 0.67f && phaseNumber < 2) targetPhase = 2;
        } else {
            if      (hpRatio() <= PHASE4_RATIO && phaseNumber < 4) targetPhase = 4;
            else if (hpRatio() <= PHASE3_RATIO && phaseNumber < 3) targetPhase = 3;
            else if (hpRatio() <= PHASE2_RATIO && phaseNumber < 2) targetPhase = 2;
        }
        if (targetPhase > phaseNumber) {
            pendingPhase = targetPhase;
            enterState(BossAIState.PHASE_TRANSITION);
        }
    }
}
