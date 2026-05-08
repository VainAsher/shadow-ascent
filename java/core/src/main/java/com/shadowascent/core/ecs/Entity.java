package com.shadowascent.core.ecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a single entity in the ECS system.
 * An entity is a unique object composed of a unique ID and a collection of components.
 */
public class Entity {
    private final long entityId;
    private final Map<Class<? extends Component>, Component> components;

    public Entity(long entityId) {
        this.entityId = entityId;
        this.components = new HashMap<>();
    }

    public long id() {
        return entityId;
    }

    public void addComponent(Component component) {
        if (component != null) {
            components.put(component.componentType(), component);
        }
    }

    public <T extends Component> Optional<T> getComponent(Class<T> componentType) {
        Component component = components.get(componentType);
        if (componentType.isInstance(component)) {
            return Optional.of(componentType.cast(component));
        }
        return Optional.empty();
    }

    public <T extends Component> boolean hasComponent(Class<T> componentType) {
        return components.containsKey(componentType);
    }

    public void removeComponent(Class<? extends Component> componentType) {
        components.remove(componentType);
    }

    public Map<Class<? extends Component>, Component> getAllComponents() {
        return new HashMap<>(components);
    }
}
