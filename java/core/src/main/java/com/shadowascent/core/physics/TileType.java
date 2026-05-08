package com.shadowascent.core.physics;

/**
 * Physics-side tile type constants.
 */
public enum TileType {
    AIR((byte) 0),
    SOLID((byte) 1),
    PLATFORM((byte) 2),
    ICE((byte) 3),
    WATER((byte) 4),
    LAVA((byte) 5),
    DOOR_LOCKED((byte) 6),
    GAS((byte) 7),
    CLIMBABLE((byte) 8);

    public final byte id;

    TileType(byte id) {
        this.id = id;
    }

    private static final TileType[] BY_ID;

    static {
        TileType[] values = values();
        int max = 0;
        for (TileType type : values) {
            if (type.id > max) {
                max = type.id;
            }
        }
        BY_ID = new TileType[max + 1];
        for (TileType type : values) {
            BY_ID[type.id] = type;
        }
    }

    public static TileType of(byte id) {
        return (id >= 0 && id < BY_ID.length && BY_ID[id] != null) ? BY_ID[id] : AIR;
    }
}

