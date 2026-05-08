package com.shadowascent.client;

import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.physics.TileType;

import java.util.Set;

final class TriggeredPlatform {
    private final String id;
    private final String requiredTriggerId;
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    TriggeredPlatform(
            String id,
            String requiredTriggerId,
            float x,
            float y,
            float width,
            float height) {
        this.id = id;
        this.requiredTriggerId = requiredTriggerId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    boolean isUnlocked(Set<String> activatedTriggerIds) {
        return activatedTriggerIds != null && activatedTriggerIds.contains(requiredTriggerId);
    }

    TileRect toTileRect() {
        return new TileRect(x, y, width, height, true, TileType.PLATFORM.id);
    }

    String requiredTriggerId() { return requiredTriggerId; }
    float x() { return x; }
    float y() { return y; }
    float width() { return width; }
    float height() { return height; }
}
