package com.shadowascent.core.ecs.systems;

import com.shadowascent.core.ecs.EntityWorld;
import com.shadowascent.core.ecs.Entity;
import com.shadowascent.core.ecs.System;
import com.shadowascent.core.ecs.components.TransformComponent;
import java.util.List;

/**
 * Physics system: handles gravity, velocity, and basic movement for entities.
 */
public class PhysicsSystem implements System {
    private static final float GRAVITY = 1200f;  // pixels/second^2
    private static final float MAX_FALL_SPEED = 800f;
    private static final float GROUND_Y = 700f;  // Default ground level (customize as needed)

    @Override
    public void update(float deltaTime, EntityWorld world) {
        List<Entity> movableEntities = world.entitiesWith(TransformComponent.class);

        for (Entity entity : movableEntities) {
            TransformComponent transform = entity.getComponent(TransformComponent.class)
                    .orElse(null);

            if (transform == null) continue;

            // Apply gravity
            float newVelocityY = transform.velocityY() + (GRAVITY * deltaTime);
            newVelocityY = Math.min(newVelocityY, MAX_FALL_SPEED);

            // Update position based on velocity
            float newX = transform.x() + (transform.velocityX() * deltaTime);
            float newY = transform.y() + (newVelocityY * deltaTime);

            // Simple ground collision
            if (newY + transform.height() >= GROUND_Y) {
                newY = GROUND_Y - transform.height();
                newVelocityY = 0f;
                transform.setGrounded(true);
            } else {
                transform.setGrounded(false);
            }

            // Apply bounds checking (basic world boundaries)
            newX = Math.max(0f, Math.min(4000f, newX));  // Assuming 4000px width
            newY = Math.max(0f, newY);

            transform.setPosition(newX, newY);
            transform.setVelocity(transform.velocityX(), newVelocityY);
        }
    }
}
