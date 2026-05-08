package com.shadowascent.core.ecs;

/**
 * Base interface for ECS systems.
 * Systems contain the logic and operate on entities and their components.
 */
public interface System {
    /**
     * Called each frame to update the system's logic.
     * 
     * @param deltaTime the elapsed time since the last frame, in seconds
     * @param world the entity world containing all entities and systems
     */
    void update(float deltaTime, EntityWorld world);
}
