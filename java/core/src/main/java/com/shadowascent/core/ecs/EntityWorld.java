package com.shadowascent.core.ecs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Manages all entities and systems in the ECS world.
 * Responsible for entity creation, destruction, and orchestrating system updates.
 */
public class EntityWorld {
    private final Map<Long, Entity> entities;
    private final List<System> systems;
    private final AtomicLong nextEntityId;

    public EntityWorld() {
        this.entities = new HashMap<>();
        this.systems = new java.util.ArrayList<>();
        this.nextEntityId = new AtomicLong(1L);
    }

    /**
     * Creates a new entity with a unique ID.
     */
    public Entity createEntity() {
        long id = nextEntityId.getAndIncrement();
        Entity entity = new Entity(id);
        entities.put(id, entity);
        return entity;
    }

    /**
     * Destroys an entity by removing it from the world.
     */
    public void destroyEntity(long entityId) {
        entities.remove(entityId);
    }

    /**
     * Retrieves an entity by ID.
     */
    public Optional<Entity> entity(long entityId) {
        return Optional.ofNullable(entities.get(entityId));
    }

    /**
     * Gets all entities with a specific component type.
     */
    public <T extends Component> List<Entity> entitiesWith(Class<T> componentType) {
        return entities.values().stream()
                .filter(e -> e.hasComponent(componentType))
                .collect(Collectors.toList());
    }

    /**
     * Gets all entities with all specified component types (query).
     */
    public List<Entity> entitiesWith(Class<? extends Component>... componentTypes) {
        return entities.values().stream()
                .filter(e -> {
                    for (Class<? extends Component> type : componentTypes) {
                        if (!e.hasComponent(type)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Registers a system to be updated each frame.
     */
    public void addSystem(System system) {
        if (system != null) {
            systems.add(system);
        }
    }

    /**
     * Updates all systems with the given delta time.
     */
    public void update(float deltaTime) {
        for (System system : systems) {
            system.update(deltaTime, this);
        }
    }

    /**
     * Returns the total number of entities in the world.
     */
    public int entityCount() {
        return entities.size();
    }
}
