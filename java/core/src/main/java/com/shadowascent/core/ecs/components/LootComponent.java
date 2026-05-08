package com.shadowascent.core.ecs.components;

import com.shadowascent.core.ecs.Component;

/**
 * Loot component: defines what an entity drops when defeated.
 */
public class LootComponent implements Component {
    public enum LootType {
        HEALTH_ORB,
        AMMO,
        KEY_ITEM,
        ABILITY_SHARD,
        CURRENCY,
        CONSUMABLE
    }

    private final String lootId;
    private final LootType lootType;
    private final int quantity;
    private final float rarityWeight;  // 0.0 to 1.0

    public LootComponent(String lootId, LootType lootType, int quantity, float rarityWeight) {
        this.lootId = lootId;
        this.lootType = lootType;
        this.quantity = quantity;
        this.rarityWeight = Math.max(0f, Math.min(1f, rarityWeight));
    }

    @Override
    public Class<? extends Component> componentType() {
        return LootComponent.class;
    }

    // Getters
    public String lootId() { return lootId; }
    public LootType lootType() { return lootType; }
    public int quantity() { return quantity; }
    public float rarityWeight() { return rarityWeight; }
}
