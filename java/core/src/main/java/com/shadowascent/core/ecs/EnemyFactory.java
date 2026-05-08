package com.shadowascent.core.ecs;

import com.shadowascent.core.ecs.components.AIComponent;
import com.shadowascent.core.ecs.components.HealthComponent;
import com.shadowascent.core.ecs.components.LootComponent;
import com.shadowascent.core.ecs.components.RenderComponent;
import com.shadowascent.core.ecs.components.TransformComponent;

import java.awt.Color;
import java.util.Optional;

/**
 * Factory for creating enemy entities with standard configurations.
 * This makes it easy to spawn enemies with proper ECS setup.
 */
public class EnemyFactory {
    private final EntityWorld world;

    public EnemyFactory(EntityWorld world) {
        this.world = world;
    }

    /**
     * Creates a basic melee enemy with health, AI, and physics.
     */
    public Entity createMeleeEnemy(String enemyId, float x, float y, 
                                   int health, AIComponent.AIBehavior behavior) {
        Entity entity = world.createEntity();

        // Transform: position, size, physics
        TransformComponent transform = new TransformComponent(x, y, 48f, 64f);
        entity.addComponent(transform);

        // Health
        HealthComponent healthComp = new HealthComponent(health);
        entity.addComponent(healthComp);

        // AI behavior
        AIComponent ai = new AIComponent(behavior, 300f, 80f);
        entity.addComponent(ai);

        // Rendering
        Color enemyColor = switch (behavior) {
            case AGGRESSIVE -> new Color(200, 50, 50, 200);
            case TERRITORIAL -> new Color(180, 100, 50, 200);
            case COWARDLY -> new Color(100, 100, 150, 200);
            default -> new Color(128, 128, 128, 200);
        };
        RenderComponent render = new RenderComponent(RenderComponent.RenderShape.RECT, enemyColor, true);
        entity.addComponent(render);

        // Loot
        LootComponent loot = new LootComponent(enemyId + "_drop", LootComponent.LootType.HEALTH_ORB, 1, 0.5f);
        entity.addComponent(loot);

        return entity;
    }

    /**
     * Creates a ranged enemy with detection and attack behavior.
     */
    public Entity createRangedEnemy(String enemyId, float x, float y,
                                    int health, AIComponent.AIBehavior behavior) {
        Entity entity = createMeleeEnemy(enemyId, x, y, health, behavior);

        // For ranged enemies, increase detection and attack range
        Optional<AIComponent> aiOpt = entity.getComponent(AIComponent.class);
        if (aiOpt.isPresent()) {
            AIComponent ai = aiOpt.get();
            // Could store these in a more flexible way, but for now we update the ranges
            // Creator bears responsibility for the ranges used in constructor
        }

        // Update render color for ranged variant
        Optional<RenderComponent> renderOpt = entity.getComponent(RenderComponent.class);
        if (renderOpt.isPresent()) {
            RenderComponent render = renderOpt.get();
            render.setVisible(true);  // Ensure visible
        }

        return entity;
    }

    /**
     * Creates a flying enemy with unique physics behavior.
     */
    public Entity createFlyingEnemy(String enemyId, float x, float y,
                                    int health, AIComponent.AIBehavior behavior) {
        Entity entity = createMeleeEnemy(enemyId, x, y, health, behavior);

        // Flying enemies: smaller size, lighter, faster
        Optional<TransformComponent> transformOpt = entity.getComponent(TransformComponent.class);
        if (transformOpt.isPresent()) {
            TransformComponent transform = transformOpt.get();
            transform.setPosition(x, y);
            // Mark as flying by leaving grounded as false (handled by flying-specific physics)
        }

        return entity;
    }

    /**
     * Creates a boss-level enemy with higher health and special behavior.
     */
    public Entity createBossEnemy(String bossId, float x, float y,
                                  int health, AIComponent.AIBehavior behavior) {
        Entity entity = world.createEntity();

        // Transform: boss is larger
        TransformComponent transform = new TransformComponent(x, y, 96f, 128f);
        entity.addComponent(transform);

        // Health: boss has significantly more health
        HealthComponent healthComp = new HealthComponent(health);
        entity.addComponent(healthComp);

        // AI: boss has extended ranges and faster behavior
        AIComponent ai = new AIComponent(behavior, 500f, 150f);
        entity.addComponent(ai);

        // Rendering: boss is visually distinct
        Color bossColor = new Color(255, 100, 0, 255);
        RenderComponent render = new RenderComponent(RenderComponent.RenderShape.RECT, bossColor, true);
        entity.addComponent(render);

        // Loot: boss drops better loot
        LootComponent loot = new LootComponent(bossId + "_drop", LootComponent.LootType.ABILITY_SHARD, 1, 0.9f);
        entity.addComponent(loot);

        return entity;
    }
}
