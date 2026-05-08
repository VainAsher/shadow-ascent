package com.shadowascent.core.ecs.components;

import com.shadowascent.core.ecs.Component;

/**
 * AI component: manages enemy behavior state and decision-making.
 */
public class AIComponent implements Component {
    public enum AIState {
        IDLE,           // Waiting, not aware of player
        ALERTED,        // Noticed player but not attacking
        PURSUING,       // Moving toward player
        ATTACKING,      // In combat/attack range
        PATROLLING,     // Moving in predefined patrol pattern
        RETREATING,     // Fleeing from player
        STUNNED,        // Incapacitated temporarily
        DEAD            // No longer acts
    }

    public enum AIBehavior {
        PASSIVE,        // Won't attack unless provoked
        AGGRESSIVE,     // Attacks on sight
        TERRITORIAL,    // Attacks within patrol area
        COWARDLY        // Flees when low health
    }

    private AIState currentState;
    private AIBehavior behavior;
    private float detectionRange;
    private float attackRange;
    private float targetX;
    private float targetY;
    private float stateTime;
    private String patrolPointId;

    public AIComponent(AIBehavior behavior, float detectionRange, float attackRange) {
        this.currentState = AIState.IDLE;
        this.behavior = behavior;
        this.detectionRange = detectionRange;
        this.attackRange = attackRange;
        this.targetX = 0f;
        this.targetY = 0f;
        this.stateTime = 0f;
        this.patrolPointId = "";
    }

    @Override
    public Class<? extends Component> componentType() {
        return AIComponent.class;
    }

    // Getters
    public AIState currentState() { return currentState; }
    public AIBehavior behavior() { return behavior; }
    public float detectionRange() { return detectionRange; }
    public float attackRange() { return attackRange; }
    public float targetX() { return targetX; }
    public float targetY() { return targetY; }
    public float stateTime() { return stateTime; }
    public String patrolPointId() { return patrolPointId; }

    // Setters
    public void setState(AIState state) {
        this.currentState = state;
        this.stateTime = 0f;
    }

    public void setTarget(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }

    public void setPatrolPoint(String pointId) {
        this.patrolPointId = pointId;
    }

    public void updateStateTime(float deltaTime) {
        this.stateTime += deltaTime;
    }

    public boolean isInCombat() {
        return currentState == AIState.ATTACKING || currentState == AIState.PURSUING;
    }

    public boolean canAct() {
        return currentState != AIState.STUNNED && currentState != AIState.DEAD;
    }
}
