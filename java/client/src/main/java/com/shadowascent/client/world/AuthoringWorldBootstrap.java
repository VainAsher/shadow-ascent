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
            case "area_hollow_depths_caves" -> 180f;
            case "area_weightbound_mines_arena" -> 200f;
            case "area_hollow_hub_first_sparks" -> 180f;
            case "area_shatter_moth_nest" -> 220f;
            case "area_fractured_contact_high_winds" -> 160f;
            case "area_stone_judge_maze" -> 200f;
            case "area_abyssal_gate" -> 220f;
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
            case "area_hollow_depths_caves" -> {
                tiles.add(new TileRect(260f, FLOOR_Y - 70f, 160f, 15f, true));
                tiles.add(new TileRect(560f, FLOOR_Y - 130f, 180f, 15f, true));
                tiles.add(new TileRect(860f, FLOOR_Y - 80f, 140f, 15f, true));
                tiles.add(new TileRect(1180f, FLOOR_Y - 180f, 220f, 15f, true));
            }
            case "area_echo_galleries" -> {
                tiles.add(new TileRect(250f, FLOOR_Y - 110f, 150f, 15f, true));
                tiles.add(new TileRect(520f, FLOOR_Y - 190f, 120f, 15f, true));
                tiles.add(new TileRect(760f, FLOOR_Y - 110f, 180f, 15f, true));
                tiles.add(new TileRect(1080f, FLOOR_Y - 210f, 150f, 15f, true));
            }
            case "area_weightbound_mines_arena" -> {
                tiles.add(new TileRect(460f, FLOOR_Y - 60f, 260f, 18f, true));
                tiles.add(new TileRect(980f, FLOOR_Y - 60f, 260f, 18f, true));
            }
            case "area_hollow_hub_first_sparks" -> {
                tiles.add(new TileRect(260f, FLOOR_Y - 70f, 220f, 15f, true));
                tiles.add(new TileRect(620f, FLOOR_Y - 140f, 180f, 15f, true));
                tiles.add(new TileRect(980f, FLOOR_Y - 210f, 140f, 15f, true));
            }
            case "area_shatter_moth_nest" -> {
                tiles.add(new TileRect(280f, FLOOR_Y - 80f, 130f, 15f, true));
                tiles.add(new TileRect(520f, FLOOR_Y - 150f, 120f, 15f, true));
                tiles.add(new TileRect(740f, FLOOR_Y - 110f, 160f, 15f, true));
                tiles.add(new TileRect(1040f, FLOOR_Y - 200f, 100f, 15f, true));
            }
            case "area_fractured_contact_high_winds" -> {
                tiles.add(new TileRect(220f, FLOOR_Y - 90f, 120f, 15f, true));
                tiles.add(new TileRect(460f, FLOOR_Y - 170f, 100f, 15f, true));
                tiles.add(new TileRect(700f, FLOOR_Y - 250f, 100f, 15f, true));
                tiles.add(new TileRect(980f, FLOOR_Y - 140f, 120f, 15f, true));
                tiles.add(new TileRect(1260f, FLOOR_Y - 240f, 120f, 15f, true));
            }
            case "area_stone_judge_maze" -> {
                tiles.add(new TileRect(260f, FLOOR_Y - 90f, 160f, 15f, true));
                tiles.add(new TileRect(540f, FLOOR_Y - 170f, 120f, 15f, true));
                tiles.add(new TileRect(820f, FLOOR_Y - 90f, 160f, 15f, true));
                tiles.add(new TileRect(1120f, FLOOR_Y - 170f, 140f, 15f, true));
            }
            case "area_abyssal_gate" -> {
                tiles.add(new TileRect(260f, FLOOR_Y - 80f, 180f, 15f, true));
                tiles.add(new TileRect(600f, FLOOR_Y - 150f, 180f, 15f, true));
                tiles.add(new TileRect(960f, FLOOR_Y - 220f, 220f, 15f, true));
                tiles.add(new TileRect(1340f, FLOOR_Y - 290f, 180f, 15f, true));
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
            case "area_hollow_depths_caves" -> 420f;
            case "area_weightbound_mines_arena" -> 280f;
            case "area_hollow_hub_first_sparks" -> 420f;
            case "area_shatter_moth_nest" -> 320f;
            case "area_abyssal_gate" -> 420f;
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
            case "area_hollow_depths_caves" -> {
                positions.put("SHADE_HERMIT", 420f);
                positions.put("LISTENING_ELDER", 760f);
            }
            case "area_weightbound_mines_arena" -> positions.put("SHADE_HERMIT", 280f);
            case "area_hollow_hub_first_sparks" -> positions.put("SMITH_MONK", 460f);
            case "area_shatter_moth_nest" -> positions.put("SHADE_HERMIT", 320f);
            case "area_abyssal_gate" -> {
                positions.put("SHADE_HERMIT", 440f);
                positions.put("ADVOCATE", 760f);
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
            switch (areaId) {
                case "area_hollow_depths_camp" -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("goblin_1", "goblin", 760f, 280f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("bat_1", "bat", 1020f, 210f));
                }
                case "area_hollow_depths_caves" -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("slime_1", "slime", 680f, 300f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("bat_1", "bat", 930f, 205f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("goblin_1", "goblin", 1240f, 280f));
                }
                case "area_echo_galleries" -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("bat_1", "bat", 580f, 180f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("bat_2", "bat", 1040f, 170f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("slime_1", "slime", 860f, 300f));
                }
                case "area_weightbound_mines_arena" -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("weightbound_ogre", "ogre", 860f, 270f));
                }
                case "area_hollow_hub_first_sparks" -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("wolf_1", "wolf", 980f, 280f));
                }
                case "area_shatter_moth_nest" -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("bat_1", "bat", 620f, 170f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("bat_2", "bat", 980f, 150f));
                }
                case "area_fractured_contact_high_winds" -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("bat_1", "bat", 760f, 170f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("wolf_1", "wolf", 1160f, 280f));
                }
                case "area_stone_judge_maze" -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("skeleton_1", "skeleton", 760f, 275f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("skeleton_2", "skeleton", 1180f, 275f));
                }
                case "area_abyssal_gate" -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("wolf_1", "wolf", 920f, 280f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("bat_1", "bat", 1180f, 170f));
                }
                default -> {
                    enemies.add(new RunGameContentProfile.EnemyPlacement("goblin_1", "goblin", 760f, 280f));
                    enemies.add(new RunGameContentProfile.EnemyPlacement("bat_1", "bat", 1020f, 210f));
                }
            }
        } else if ("EMBER_MONASTERY".equals(plateauId)) {
            enemies.add(new RunGameContentProfile.EnemyPlacement("skeleton_1", "skeleton", 880f, 280f));
        } else {
            enemies.add(new RunGameContentProfile.EnemyPlacement("goblin_1", "goblin", 600f, 280f));
        }
        return enemies;
    }
}
