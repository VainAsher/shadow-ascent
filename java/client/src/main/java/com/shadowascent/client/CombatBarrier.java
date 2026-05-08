package com.shadowascent.client;

import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.physics.TileType;

import java.util.Set;

final class CombatBarrier {
    private final String id;
    private final String requiredEncounterId;
    private final String displayName;
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    CombatBarrier(
            String id,
            String requiredEncounterId,
            String displayName,
            float x,
            float y,
            float width,
            float height) {
        this.id = id;
        this.requiredEncounterId = requiredEncounterId;
        this.displayName = displayName;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    boolean isUnlocked(Set<String> clearedEncounters) {
        return clearedEncounters != null && clearedEncounters.contains(requiredEncounterId);
    }

    TileRect toTileRect() {
        return new TileRect(x, y, width, height, false, TileType.DOOR_LOCKED.id);
    }

    String id() { return id; }
    String requiredEncounterId() { return requiredEncounterId; }
    String displayName() { return displayName; }
    float x() { return x; }
    float y() { return y; }
    float width() { return width; }
    float height() { return height; }
}
