package com.shadowascent.client.world;

import java.util.List;

public record RoomSpec(
        String id,
        String displayName,
        String plateauId,
        String areaId,
        String sceneRole,
        int routeOrder,
        List<String> requiredFlags,
        List<String> setFlags,
        List<SpawnPoint> spawnPoints,
        List<GeometrySpec> geometry,
        List<NpcAnchor> npcAnchors,
        List<EnemyPlacementSpec> enemyPlacements,
        List<String> encounters,
        List<RoomTransitionSpec> transitions) {

    public RoomSpec {
        requiredFlags = List.copyOf(requiredFlags);
        setFlags = List.copyOf(setFlags);
        spawnPoints = List.copyOf(spawnPoints);
        geometry = List.copyOf(geometry);
        npcAnchors = List.copyOf(npcAnchors);
        enemyPlacements = List.copyOf(enemyPlacements);
        encounters = List.copyOf(encounters);
        transitions = List.copyOf(transitions);
    }

    public record SpawnPoint(String id, float x, float y) {}

    public record GeometrySpec(String type, float x, float y, float w, float h) {}

    public record NpcAnchor(String npcId, String role, float x, float y, float patrolMinX, float patrolMaxX) {}

    public record EnemyPlacementSpec(String enemyId, String enemyType, float x, float y) {}
}
