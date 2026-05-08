package com.shadowascent.core.simulation;

import com.shadowascent.core.physics.PhysicsState;

/**
 * Server-side enemy simulation entity.
 * Imported from indie-ninja-adventures (com.indieniinja.sim.SimEnemy).
 *
 * Simple patrol/chase AI state machine for the five ground enemy types.
 * Flying enemies (bat) skip gravity application in the simulation tick.
 */
public final class SimEnemy {

    // ── Identity ──────────────────────────────────────────────────────────────
    public final String enemyId;
    public final String enemyType;   // "goblin", "bat", "slime", "skeleton", "wolf"
    public final boolean canFly;

    // ── Physics ───────────────────────────────────────────────────────────────
    public final PhysicsState physics;

    // ── Stats ─────────────────────────────────────────────────────────────────
    public int   hp;
    public final int   maxHp;
    public final int   baseDamage;
    public final float moveSpeed;
    public final float detectionRadius;
    public final float attackRange;
    public final float patrolSpeedMult;

    // ── AI state ──────────────────────────────────────────────────────────────
    public EnemyAIState aiState     = EnemyAIState.PATROL;
    public boolean      facingRight = true;

    public float patrolMinX;
    public float patrolMaxX;

    public float attackTimer      = 0f;
    public float stunTimer        = 0f;
    public float fleeTimer        = 0f;
    public float guardTimer       = 0f;

    public float attackWindupTimer    = 0f;
    public float attackActiveTimer    = 0f;
    public float attackRecoveryTimer  = 0f;

    public boolean removed = false;

    public boolean knockedOut     = false;
    public float   knockoutTimer  = 0f;

    // ── Awareness FSM ─────────────────────────────────────────────────────────
    public EnemyAwarenessState awarenessState = EnemyAwarenessState.UNAWARE;
    public float awarenessTimer  = 0f;
    public float lastHeardNoiseX = 0f;
    public float lastHeardNoiseY = 0f;

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final float ATTACK_WINDUP_TIME   = 0.6f;
    public static final float ATTACK_ACTIVE_TIME   = 0.15f;
    public static final float ATTACK_RECOVERY_TIME = 0.4f;
    public static final float STUN_DURATION        = 0.5f;
    public static final float FLEE_DURATION        = 3.0f;
    public static final float GUARD_DURATION       = 2.0f;
    public static final float FLEE_HP_THRESHOLD    = 0.40f;
    public static final float KNOCKOUT_DURATION    = 180f;

    public SimEnemy(
            String enemyId, String enemyType,
            float x, float y, int width, int height,
            int maxHp, int baseDamage, float moveSpeed,
            float detectionRadius, float attackRange,
            float patrolMinX, float patrolMaxX,
            boolean canFly) {

        this.enemyId          = enemyId;
        this.enemyType        = enemyType;
        this.canFly           = canFly;
        this.physics          = new PhysicsState(x, y, width, height);
        this.hp               = maxHp;
        this.maxHp            = maxHp;
        this.baseDamage       = baseDamage;
        this.moveSpeed        = moveSpeed;
        this.detectionRadius  = detectionRadius;
        this.attackRange      = attackRange;
        this.patrolMinX       = patrolMinX;
        this.patrolMaxX       = patrolMaxX;
        this.patrolSpeedMult  = 0.5f;
        this.physics.vx       = 0f;
    }

    public boolean isAlive() { return hp > 0 && !removed && !knockedOut; }

    /**
     * Apply damage and transition to STUNNED.
     * Returns true if the enemy just died.
     */
    public boolean takeDamage(int dmg) {
        if (!isAlive()) return false;
        hp = Math.max(0, hp - dmg);
        if (hp <= 0) {
            aiState = EnemyAIState.DEAD;
            removed = true;
            return true;
        }
        if ((float) hp / maxHp <= FLEE_HP_THRESHOLD) {
            fleeTimer = FLEE_DURATION;
        }
        aiState   = EnemyAIState.STUNNED;
        stunTimer = STUN_DURATION;
        return false;
    }

    /**
     * Unarmed hit: at 0 HP enters KO instead of dying.
     * KO enemies revive after KNOCKOUT_DURATION seconds.
     */
    public void knockDown(int dmg) {
        if (!isAlive()) return;
        hp = Math.max(0, hp - dmg);
        if (hp <= 0) {
            knockedOut    = true;
            knockoutTimer = KNOCKOUT_DURATION;
            hp            = maxHp;
            aiState       = EnemyAIState.DEAD;
            stunTimer     = 0f;
            fleeTimer     = 0f;
        } else {
            if ((float) hp / maxHp <= FLEE_HP_THRESHOLD) fleeTimer = FLEE_DURATION;
            aiState   = EnemyAIState.STUNNED;
            stunTimer = STUN_DURATION;
        }
    }

    public float distanceTo(float tx, float ty) {
        float cx = physics.x + physics.width  * 0.5f;
        float cy = physics.y + physics.height * 0.5f;
        float dx = tx - cx, dy = ty - cy;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
