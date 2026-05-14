package com.shadowascent.client.world;

import com.shadowascent.client.RunGameMissionInteraction;
import com.shadowascent.core.GameState;
import com.shadowascent.core.NPC;
import com.shadowascent.core.physics.TileRect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AuthoringWorldBootstrap {
    private static final float FLOOR_Y = 360f;

    private final AreaPlacementResolver areaPlacementResolver;

    public AuthoringWorldBootstrap() {
        this(new AreaPlacementResolver());
    }

    AuthoringWorldBootstrap(AreaPlacementResolver areaPlacementResolver) {
        this.areaPlacementResolver = areaPlacementResolver;
    }

    public RunGameContentProfile bootstrap(GameState gameState) {
        String areaId = areaPlacementResolver.resolveAreaId(gameState);
        String plateauId = gameState.getStoryState().getCurrentPlateau().name();

        List<TileRect> worldTiles = buildTilesForArea(areaId, plateauId);
        List<RunGameContentProfile.NpcPlacement> npcPlacements = buildNpcPlacements(gameState, areaId, plateauId);
        List<RunGameContentProfile.EnemyPlacement> enemyPlacements = buildEnemyPlacements(areaId, plateauId);
        String merchantNpcId = npcPlacements.stream()
                .map(RunGameContentProfile.NpcPlacement::npcId)
                .filter("MERCHANT_RILU"::equals)
                .findFirst()
                .orElse(null);

        float playerSpawnX = switch (areaId) {
            case "area_hollow_depths_camp" -> 140f;
            case "area_hearth_hall" -> 180f;
            default -> 200f;
        };
        float playerSpawnY = switch (plateauId) {
            case "HOLLOW_DEPTHS" -> 300f;
            case "EMBER_MONASTERY" -> 260f;
            default -> 280f;
        };

        return new RunGameContentProfile(
                areaId,
                plateauId,
                playerSpawnX,
                playerSpawnY,
                List.copyOf(worldTiles),
                List.copyOf(npcPlacements),
                List.copyOf(enemyPlacements),
                merchantNpcId);
    }

    private static List<TileRect> buildTilesForArea(String areaId, String plateauId) {
        List<TileRect> tiles = new ArrayList<>();
        float worldWidth = switch (plateauId) {
            case "HOLLOW_DEPTHS" -> 4200f;
            case "EMBER_MONASTERY" -> 3900f;
            default -> 3500f;
        };
        tiles.add(new TileRect(0f, FLOOR_Y, worldWidth, 30f, false));

        switch (areaId) {
            case "area_lantern_heights_balcony" -> {
                tiles.add(new TileRect(260f, FLOOR_Y - 120f, 220f, 15f, true));
                tiles.add(new TileRect(560f, FLOOR_Y - 70f, 180f, 15f, true));
            }
            case "area_hollow_depths_camp" -> {
                tiles.add(new TileRect(280f, FLOOR_Y - 90f, 150f, 15f, true));
                tiles.add(new TileRect(560f, FLOOR_Y - 150f, 180f, 15f, true));
                tiles.add(new TileRect(860f, FLOOR_Y - 220f, 120f, 15f, true));
            }
            case "area_hearth_hall" -> {
                tiles.add(new TileRect(240f, FLOOR_Y - 110f, 200f, 15f, true));
                tiles.add(new TileRect(620f, FLOOR_Y - 110f, 200f, 15f, true));
                tiles.add(new TileRect(980f, FLOOR_Y - 180f, 160f, 15f, true));
            }
            default -> tiles.add(new TileRect(300f, FLOOR_Y - 120f, 200f, 15f, true));
        }
        return tiles;
    }

    private static List<RunGameContentProfile.NpcPlacement> buildNpcPlacements(
            GameState gameState,
            String areaId,
            String plateauId) {
        List<String> orderedNpcIds = orderedNpcIdsForCurrentBeat(gameState);
        List<RunGameContentProfile.NpcPlacement> placements = new ArrayList<>();

        Map<String, Float> preferredX = preferredNpcX(areaId);
        float anchorY = 288f;
        float startX = switch (areaId) {
            case "area_hollow_depths_camp" -> 340f;
            case "area_hearth_hall" -> 300f;
            default -> 350f;
        };
        float spacing = 170f;

        int index = 0;
        for (String npcId : orderedNpcIds) {
            NPC npc = gameState.getStoryState().getNPC(npcId);
            if (npc == null || !npc.isActive()) {
                continue;
            }
            String role = npc.getAllowedRoles().stream().sorted().findFirst().orElse("npc");
            float x = preferredX.getOrDefault(npc.getId(), startX + spacing * index++);
            placements.add(new RunGameContentProfile.NpcPlacement(npc.getId(), role, x, anchorY, x - 20f, x + 20f));
        }

        if (placements.stream().noneMatch(placement -> "MERCHANT_RILU".equals(placement.npcId()))
                && "LANTERN_HEIGHTS".equals(plateauId)) {
            placements.add(new RunGameContentProfile.NpcPlacement("MERCHANT_RILU", "merchant", 350f, 288f, 330f, 370f));
        }
        if (placements.stream().noneMatch(placement -> "INSTRUCTOR_TAI".equals(placement.npcId()))
                && "LANTERN_HEIGHTS".equals(plateauId)) {
            placements.add(new RunGameContentProfile.NpcPlacement("INSTRUCTOR_TAI", "teacher", 520f, 288f, 500f, 540f));
        }

        return placements;
    }

    private static List<String> orderedNpcIdsForCurrentBeat(GameState gameState) {
        List<String> orderedIds = new ArrayList<>();
        RunGameMissionInteraction.orderedRelevantNpcIds(gameState).forEach(npcId -> {
            if (!orderedIds.contains(npcId)) {
                orderedIds.add(npcId);
            }
        });
        gameState.getDataContracts().nextCriticalBeat(gameState.getStoryState())
                .ifPresent(beat -> orderedIds.addAll(beat.npcIds()));

        gameState.getStoryState().getAllNPCs().values().stream()
                .filter(NPC::isActive)
                .sorted(Comparator.comparing(NPC::getId))
                .map(NPC::getId)
                .forEach(npcId -> {
                    if (!orderedIds.contains(npcId)) {
                        orderedIds.add(npcId);
                    }
                });
        return List.copyOf(orderedIds);
    }

    private static Map<String, Float> preferredNpcX(String areaId) {
        Map<String, Float> positions = new LinkedHashMap<>();
        switch (areaId) {
            case "area_lantern_heights_balcony", "area_lantern_heights_hub", "area_lantern_heights_hub_dimming" -> {
                positions.put("MERCHANT_RILU", 350f);
                positions.put("INSTRUCTOR_TAI", 520f);
                positions.put("SMITH_JENRO", 690f);
                positions.put("SAMSON", 860f);
                positions.put("SOPHIA", 1030f);
            }
            case "area_hollow_depths_camp" -> {
                positions.put("SHADE_HERMIT", 360f);
                positions.put("SMITH_MONK", 560f);
                positions.put("LISTENING_ELDER", 760f);
                positions.put("ADVOCATE", 940f);
            }
            case "area_hearth_hall", "area_ember_monastery_hub" -> {
                positions.put("BROTHER_KAI", 340f);
                positions.put("BROTHER_LEN", 520f);
                positions.put("BROTHER_ASH", 700f);
                positions.put("HEARTH_BROTHER", 880f);
                positions.put("SAMSON", 1060f);
            }
            case "area_roga_dojo" -> positions.put("MENTOR_ROGA", 420f);
            default -> {
            }
        }
        return positions;
    }

    private static List<RunGameContentProfile.EnemyPlacement> buildEnemyPlacements(String areaId, String plateauId) {
        List<RunGameContentProfile.EnemyPlacement> enemies = new ArrayList<>();
        if ("HOLLOW_DEPTHS".equals(plateauId)) {
            enemies.add(new RunGameContentProfile.EnemyPlacement("goblin_1", "goblin", 760f, 280f));
            enemies.add(new RunGameContentProfile.EnemyPlacement("bat_1", "bat", 1020f, 210f));
        } else if ("EMBER_MONASTERY".equals(plateauId)) {
            enemies.add(new RunGameContentProfile.EnemyPlacement("skeleton_1", "skeleton", 880f, 280f));
        } else {
            enemies.add(new RunGameContentProfile.EnemyPlacement("goblin_1", "goblin", 600f, 280f));
        }
        return enemies;
    }
}
