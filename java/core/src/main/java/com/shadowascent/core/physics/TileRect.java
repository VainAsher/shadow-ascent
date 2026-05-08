package com.shadowascent.core.physics;

/**
 * Axis-aligned rectangle used for collision candidates in chunk worlds.
 */
public record TileRect(float x, float y, float w, float h, boolean isPlatform, byte tileType) {

    public TileRect(float x, float y, float w, float h, boolean isPlatform) {
        this(x, y, w, h, isPlatform, isPlatform ? TileType.PLATFORM.id : TileType.SOLID.id);
    }

    public boolean overlaps(float ox, float oy, float ow, float oh) {
        return ox < x + w && ox + ow > x
                && oy < y + h && oy + oh > y;
    }

    public float right() {
        return x + w;
    }

    public float bottom() {
        return y + h;
    }

    public TileType tileTypeEnum() {
        return TileType.of(tileType);
    }
}

