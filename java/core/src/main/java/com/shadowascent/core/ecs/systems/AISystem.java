package com.shadowascent.core.ecs.systems;

import com.shadowascent.core.ecs.EntityWorld;
import com.shadowascent.core.ecs.Entity;
import com.shadowascent.core.ecs.System;
import com.shadowascent.core.ecs.components.AIComponent;
import com.shadowascent.core.ecs.components.TransformComponent;
import com.shadowascent.core.ecs.components.HealthComponent;

import java.util.List;

/**
 * AI system: handles enemy decision-making and behavior state transitions.
 * This is a foundational AI system that can be extended with more sophisticated behaviors.
 */
public class AISystem implements System {
    private float playerX;
    private float playerY;
    private boolean playerPresent;

    public AISystem() {
        this.playerX = 0f;
        this.playerY = 0f;
        this.playerPresent = false;
    }

    /**
     * Sets the current player position for AI detection.
     */
    public void setPlayerPosition(float x, float y) {
        this.playerX = x;
        this.playerY = y;
        this.playerPresent = true;
    }

    @Override
    public void update(float deltaTime, EntityWorld world) {
        List<Entity> aiEntities = world.entitiesWith(AIComponent.class, TransformComponent.class);

        for (Entity entity : aiEntities) {
            AIComponent ai = entity.getComponent(AIComponent.class).orElse(null);
            TransformComponent transform = entity.getComponent(TransformComponent.class).orElse(null);
            HealthComponent health = entity.getComponent(HealthComponent.class).orElse(null);

            if (ai == null || transform == null) continue;

            // Update state time
            ai.updateStateTime(deltaTime);

            // Don't process if dead or stunned
            if (!ai.canAct()) {
                if (health != null && health.isDead()) {
                    ai.setState(AIComponent.AIState.DEAD);
                }
                continue;
            }

            updateAIBehavior(ai, transform, health);
            moveTowardTarget(ai, transform, deltaTime);
        }
    }

    private void updateAIBehavior(AIComponent ai, TransformComponent transform, HealthComponent health) {
        if (!playerPresent) {
            // No player awareness - go idle or patrol
            if (ai.currentState() != AIComponent.AIState.IDLE && 
                ai.currentState() != AIComponent.AIState.PATROLLING) {
                ai.setState(AIComponent.AIState.IDLE);
            }
            return;
        }

        float distToPlayer = distance(transform.x(), transform.y(), playerX, playerY);

        // Behavior-dependent state transitions
        switch (ai.behavior()) {
            case AGGRESSIVE -> {
                if (distToPlayer <= ai.attackRange()) {
                    ai.setState(AIComponent.AIState.ATTACKING);
                    ai.setTarget(playerX, playerY);
                } else if (distToPlayer <= ai.detectionRange()) {
                    ai.setState(AIComponent.AIState.PURSUING);
                    ai.setTarget(playerX, playerY);
                } else {
                    ai.setState(AIComponent.AIState.IDLE);
                }
            }
            case TERRITORIAL -> {
                if (distToPlayer <= ai.attackRange()) {
                    ai.setState(AIComponent.AIState.ATTACKING);
                } else if (distToPlayer <= ai.detectionRange()) {
                    ai.setState(AIComponent.AIState.PURSUING);
                }
            }
            case PASSIVE -> {
                if (distToPlayer <= ai.attackRange()) {
                    ai.setState(AIComponent.AIState.ALERTED);
                }
            }
            case COWARDLY -> {
                // Flee if health is low or threat is detected
                if (health != null && health.healthPercent() < 0.3f) {
                    ai.setState(AIComponent.AIState.RETREATING);
                    ai.setTarget(transform.x() - 200, transform.y());
                } else if (distToPlayer <= ai.attackRange()) {
                    ai.setState(AIComponent.AIState.RETREATING);
                    ai.setTarget(transform.x() - 200, transform.y());
                }
            }
        }
    }

    private void moveTowardTarget(AIComponent ai, TransformComponent transform, float deltaTime) {
        float targetX = ai.targetX();
        float targetY = ai.targetY();
        float currentX = transform.x();
        float currentY = transform.y();

        switch (ai.currentState()) {
            case PURSUING, ATTACKING, RETREATING -> {
                // Move toward target
                float dirX = targetX - currentX;
                float distance = Math.abs(dirX);

                if (distance > 5f) {
                    float moveSpeed = 150f;  // pixels per second
                    float moveDirection = dirX > 0 ? 1f : -1f;
                    transform.setVelocity(moveDirection * moveSpeed, transform.velocityY());
                } else {
                    transform.setVelocity(0f, transform.velocityY());
                }
            }
            default -> {
                // Idle or alerted - maintain position or apply friction
                transform.setVelocity(transform.velocityX() * 0.95f, transform.velocityY());
            }
        }
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
