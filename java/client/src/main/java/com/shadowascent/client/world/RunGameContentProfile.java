package com.shadowascent.client.world;

import com.shadowascent.core.physics.TileRect;

import java.util.List;

public record RunGameContentProfile(
        String areaId,
        String plateauId,
        float playerSpawnX,
        float playerSpawnY,
        List<TileRect> worldTiles,
        List<NpcPlacement> npcPlacements,
        List<EnemyPlacement> enemyPlacements,
        String merchantNpcId) {

    public record NpcPlacement(String npcId, String role, float x, float y, float patrolMinX, float patrolMaxX) {
    }

    public record EnemyPlacement(String enemyId, String enemyType, float x, float y) {
    }
}
