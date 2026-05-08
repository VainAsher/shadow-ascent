package com.shadowascent.client;

import com.shadowascent.core.StoryState;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.physics.TileType;

final class AbilityGate {
    private final String id;
    private final String requiredAbility;
    private final String displayName;
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    AbilityGate(
            String id,
            String requiredAbility,
            String displayName,
            float x,
            float y,
            float width,
            float height) {
        this.id = id;
        this.requiredAbility = requiredAbility;
        this.displayName = displayName;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    boolean isUnlocked(StoryState state) {
        return state != null && state.hasAbility(requiredAbility);
    }

    TileRect toTileRect() {
        return new TileRect(x, y, width, height, false, TileType.DOOR_LOCKED.id);
    }

    String id() { return id; }
    String requiredAbility() { return requiredAbility; }
    String displayName() { return displayName; }
    float x() { return x; }
    float y() { return y; }
    float width() { return width; }
    float height() { return height; }
}
