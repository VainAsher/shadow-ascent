package com.shadowascent.client.world;

import java.util.List;

public record RoomTransitionSpec(
        String id,
        String type,
        String targetRoomId,
        String targetAreaId,
        String targetSpawnId,
        float minX,
        float maxX,
        Float minY,
        Float maxY,
        List<String> requiredFlags,
        List<String> setFlags) {

    public RoomTransitionSpec {
        requiredFlags = requiredFlags == null ? List.of() : List.copyOf(requiredFlags);
        setFlags = setFlags == null ? List.of() : List.copyOf(setFlags);
    }
}
