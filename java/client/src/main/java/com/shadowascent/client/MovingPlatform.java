package com.shadowascent.client;

import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.physics.TileType;

final class MovingPlatform {
    private final String id;
    private final float originX;
    private final float originY;
    private final float width;
    private final float height;
    private final float travelX;
    private final float travelY;
    private final float periodSeconds;
    private float elapsedSeconds;
    private float x;
    private float y;
    private float previousX;
    private float previousY;

    MovingPlatform(
            String id,
            float originX,
            float originY,
            float width,
            float height,
            float travelX,
            float travelY,
            float periodSeconds) {
        this.id = id;
        this.originX = originX;
        this.originY = originY;
        this.width = width;
        this.height = height;
        this.travelX = travelX;
        this.travelY = travelY;
        this.periodSeconds = Math.max(0.75f, periodSeconds);
        this.elapsedSeconds = 0f;
        this.x = originX;
        this.y = originY;
        this.previousX = originX;
        this.previousY = originY;
    }

    void update(float dt) {
        previousX = x;
        previousY = y;
        elapsedSeconds += Math.max(0f, dt);
        float cycle = (elapsedSeconds / periodSeconds) * ((float) Math.PI * 2f);
        float wave = (float) Math.sin(cycle);
        x = originX + (travelX * wave);
        y = originY + (travelY * wave);
    }

    TileRect toTileRect() {
        return new TileRect(x, y, width, height, true, TileType.PLATFORM.id);
    }

    String id() { return id; }
    float x() { return x; }
    float y() { return y; }
    float width() { return width; }
    float height() { return height; }
    float deltaX() { return x - previousX; }
    float deltaY() { return y - previousY; }
}
