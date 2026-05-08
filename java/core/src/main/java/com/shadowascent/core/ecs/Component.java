package com.shadowascent.core.ecs;

/**
 * Base interface for all ECS components.
 * Components are data bundles attached to entities to define their properties and behavior.
 */
public interface Component {
    /**
     * Returns the type/class of this component.
     */
    Class<? extends Component> componentType();
}
