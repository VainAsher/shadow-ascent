package com.shadowascent.client.world;

import com.shadowascent.client.RunGameMissionInteraction;
import com.shadowascent.core.GameState;
import com.shadowascent.core.NPC;
import com.shadowascent.core.physics.TileRect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AuthoringWorldBootstrap {
    private static final float FLOOR_Y = 360f;

    private final AreaPlacementResolver areaPlacementResolver;
    private final RoomSpecCatalog roomSpecCatalog;

    public AuthoringWorldBootstrap() {
        this(new AreaPlacementResolver(), RoomSpecCatalog.loadDefault());
    }

    AuthoringWorldBootstrap(AreaPlacementResolver areaPlacementResolver, RoomSpecCatalog roomSpecCatalog) {
        this.areaPlacementResolver = areaPlacementResolver;
        this.roomSpecCatalog = roomSpecCatalog;
    }

    public RunGameContentProfile bootstrap(GameState gameState) {
        String areaId = areaPlacementResolver.resolveAreaId(gameState);
        String plateauId = gameState.getStoryState().getCurrentPlateau().name();
        RoomSpec roomSpec = resolveRoomSpec(gameState, areaId, plateauId);

        if (roomSpec != null) {
            return bootstrapRoomSpecProfile(gameState, roomSpec, plateauId);
        }

        List<TileRect> worldTiles = buildTilesForArea(areaId, plateauId);
        List<RunGameContentProfile.NpcPlacement> npcPlacements = buildNpcPlacements(gameState, areaId, plateauId);
        List<RunGameContentProfile.EnemyPlacement> enemyPlacements = buildEnemyPlacements(areaId, plateauId);
        List<RunGameContentProfile.AreaGate> areaGates = buildAreaGates(areaId, plateauId);
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
                com.shadowascent.client.ui.UiText.areaName(areaId),
                "legacy_bootstrap",
                areaId,
                plateauId,
                playerSpawnX,
                playerSpawnY,
                List.copyOf(worldTiles),
                List.copyOf(npcPlacements),
                List.copyOf(enemyPlacements),
                List.of(),
                List.of(),
                merchantNpcId,
                List.copyOf(areaGates));
    }

    private RunGameContentProfile bootstrapRoomSpecProfile(GameState gameState, RoomSpec roomSpec, String plateauId) {
        List<TileRect> worldTiles = buildTilesForRoom(roomSpec);
        List<RunGameContentProfile.NpcPlacement> npcPlacements = roomSpec.npcAnchors().stream()
                .filter(anchor -> shouldStageNpcAnchor(gameState, roomSpec, anchor))
                .map(anchor -> new RunGameContentProfile.NpcPlacement(
                        anchor.npcId(),
                        anchor.role(),
                        anchor.x(),
                        anchor.y(),
                        anchor.patrolMinX(),
                        anchor.patrolMaxX()))
                .toList();
        List<RunGameContentProfile.EnemyPlacement> enemyPlacements = roomSpec.enemyPlacements().stream()
                .map(enemy -> new RunGameContentProfile.EnemyPlacement(
                        enemy.enemyId(),
                        enemy.enemyType(),
                        enemy.x(),
                        enemy.y()))
                .toList();
        List<EncounterSpec> encounters = roomSpec.encounters().stream()
                .map(roomSpecCatalog::encounter)
                .flatMap(java.util.Optional::stream)
                .toList();
        List<RoomTransitionSpec> roomTransitions = roomSpec.transitions().stream()
                .map(transition -> new RoomTransitionSpec(
                        transition.id(),
                        transition.type(),
                        transition.targetRoomId(),
                        roomSpecCatalog.room(transition.targetRoomId()).map(RoomSpec::areaId).orElse(roomSpec.areaId()),
                        transition.targetSpawnId(),
                        transition.minX(),
                        transition.maxX(),
                        transition.minY(),
                        transition.maxY(),
                        transition.requiredFlags(),
                        transition.setFlags()))
                .toList();
        List<RunGameContentProfile.AreaGate> areaGates = roomSpec.transitions().stream()
                .map(transition -> new RunGameContentProfile.AreaGate(
                        transition.id(),
                        transition.type(),
                        transition.minX(),
                        transition.maxX(),
                        roomSpecCatalog.room(transition.targetRoomId()).map(RoomSpec::areaId).orElse(roomSpec.areaId()),
                        false,
                        transition.requiredFlags(),
                        List.of()))
                .toList();

        String pendingSpawnId = gameState == null ? null : gameState.getPendingRoomSpawnId();
        RoomSpec.SpawnPoint spawnPoint = roomSpec.spawnPoints().stream()
                .filter(candidate -> pendingSpawnId != null && pendingSpawnId.equals(candidate.id()))
                .findFirst()
                .orElse(roomSpec.spawnPoints().getFirst());
        String merchantNpcId = npcPlacements.stream()
                .map(RunGameContentProfile.NpcPlacement::npcId)
                .filter("MERCHANT_RILU"::equals)
                .findFirst()
                .orElse(null);
        if (gameState != null) {
            gameState.setCurrentRoomId(roomSpec.id());
            gameState.setPendingRoomSpawnId(null);
        }

        return new RunGameContentProfile(
                roomSpec.id(),
                roomSpec.displayName(),
                roomSpec.sceneRole(),
                roomSpec.areaId(),
                plateauId,
                spawnPoint.x(),
                spawnPoint.y(),
                List.copyOf(worldTiles),
                List.copyOf(npcPlacements),
                List.copyOf(enemyPlacements),
                List.copyOf(encounters),
                List.copyOf(roomTransitions),
                merchantNpcId,
                List.copyOf(areaGates));
    }

    private static boolean shouldStageNpcAnchor(GameState gameState, RoomSpec roomSpec, RoomSpec.NpcAnchor anchor) {
        if (anchor == null || anchor.npcId() == null || anchor.npcId().isBlank()) {
            return false;
        }
        if (gameState == null) {
            return true;
        }
        NPC npc = gameState.getStoryState().getNPC(anchor.npcId());
        if (npc != null && npc.isActive()) {
            return true;
        }
        if ("return_changed".equalsIgnoreCase(roomSpec.sceneRole())) {
            boolean warningsHeard = gameState.getStoryState().hasFlag("warnings_heard");
            Set<String> stagedCast = warningsHeard
                    ? Set.of("AEN", "VEIL_MAIDEN")
                    : Set.of("AEN", "VEIL_MAIDEN", "MERCHANT_RILU", "SAMSON", "SOPHIA", "MARCEL", "HAZEL");
            return stagedCast.contains(anchor.npcId());
        }
        if ("isolation_night".equalsIgnoreCase(roomSpec.sceneRole())) {
            return Set.of("AEN", "VEIL_MAIDEN").contains(anchor.npcId());
        }
        return false;
    }

    private RoomSpec resolveRoomSpec(GameState gameState, String areaId, String plateauId) {
        if (gameState == null || !"LANTERN_HEIGHTS".equals(plateauId)) {
            return null;
        }
        String currentRoomId = gameState.getCurrentRoomId();
        if (currentRoomId != null && !currentRoomId.isBlank()) {
            RoomSpec pinnedRoom = roomSpecCatalog.room(currentRoomId)
                    .filter(room -> plateauId.equals(room.plateauId()))
                    .filter(room -> room.requiredFlags().stream().allMatch(gameState.getStoryState()::hasFlag))
                    .orElse(null);
            if (pinnedRoom != null) {
                return pinnedRoom;
            }
            gameState.setCurrentRoomId(null);
            gameState.setPendingRoomSpawnId(null);
        }
        return roomSpecCatalog.roomsForPlateau(plateauId).stream()
                .filter(room -> areaId.equals(room.areaId()))
                .filter(room -> room.requiredFlags().stream().allMatch(gameState.getStoryState()::hasFlag))
                .filter(room -> room.setFlags().isEmpty()
                        || room.setFlags().stream().anyMatch(flag -> !gameState.getStoryState().hasFlag(flag)))
                .min(Comparator.comparingInt(RoomSpec::routeOrder).thenComparing(RoomSpec::id))
                .map(room -> {
                    gameState.setCurrentRoomId(room.id());
                    return room;
                })
                .orElse(null);
    }

    private static List<TileRect> buildTilesForRoom(RoomSpec roomSpec) {
        List<TileRect> tiles = new ArrayList<>();
        for (RoomSpec.GeometrySpec geometry : roomSpec.geometry()) {
            boolean platform = "platform".equalsIgnoreCase(geometry.type());
            tiles.add(new TileRect(geometry.x(), geometry.y(), geometry.w(), geometry.h(), platform));
        }
        return tiles;
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
        Set<String> areaNpcFilter = areaNpcFilter(areaId);
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
            if (npc == null) {
                continue;
            }
            if (!areaNpcFilter.isEmpty() && !areaNpcFilter.contains(npc.getId())) {
                continue;
            }
            if (!npc.isActive() && areaNpcFilter.isEmpty()) {
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
        runtimeOrderedBeatNpcs(gameState).forEach(npcId -> {
            if (!orderedIds.contains(npcId)) {
                orderedIds.add(npcId);
            }
        });

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

    private static List<String> runtimeOrderedBeatNpcs(GameState gameState) {
        if (gameState == null) {
            return List.of();
        }
        Set<String> ordered = new LinkedHashSet<>();
        gameState.getDataContracts().beatsForPlateau(gameState.getStoryState().getCurrentPlateau().name()).stream()
                .filter(beat -> {
                    String beatType = beat.beatType() == null ? "" : beat.beatType().trim().toLowerCase();
                    return beat.isCriticalPathBeat() || switch (beatType) {
                        case "adaptable_authored", "authored_support", "recovery", "unlock" -> true;
                        default -> false;
                    };
                })
                .filter(beat -> beat.requiredFlags().stream().allMatch(gameState.getStoryState()::hasFlag))
                .filter(beat -> beat.setFlags().isEmpty()
                        || beat.setFlags().stream().anyMatch(flag -> !gameState.getStoryState().hasFlag(flag)))
                .sorted(Comparator.comparingInt(com.shadowascent.core.data.BeatDefinition::routeOrder)
                        .thenComparing(com.shadowascent.core.data.BeatDefinition::id))
                .findFirst()
                .ifPresent(beat -> ordered.addAll(beat.npcIds()));
        return List.copyOf(ordered);
    }

    private static Set<String> areaNpcFilter(String areaId) {
        return switch (areaId) {
            case "area_hollow_depths_caves" -> Set.of("SHADE_HERMIT", "LISTENING_ELDER");
            case "area_weightbound_mines_arena" -> Set.of("SHADE_HERMIT");
            case "area_hollow_hub_first_sparks" -> Set.of("SMITH_MONK");
            case "area_shatter_moth_nest" -> Set.of("SHADE_HERMIT");
            case "area_stone_judge_maze" -> Set.of("ADVOCATE");
            case "area_abyssal_gate" -> Set.of("SHADE_HERMIT", "ADVOCATE");
            default -> Set.of();
        };
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

    private static List<RunGameContentProfile.AreaGate> buildAreaGates(String areaId, String plateauId) {
        if (!"HOLLOW_DEPTHS".equals(plateauId)) {
            return List.of();
        }

        return switch (areaId) {
            case "area_hollow_depths_camp" -> List.of(
                    gate("gate_hollow_camp_descent", "Descend to Hollow Caves", 1500f, 1680f,
                            false,
                            "area_hollow_depths_caves", List.of("act2_unlocked"), List.of("awoke_in_depths"))
            );
            case "area_hollow_depths_caves" -> List.of(
                    gate("gate_hollow_caves_weight", "Push to Weightbound Arena", 1480f, 1660f,
                            false,
                            "area_weightbound_mines_arena", List.of("awoke_in_depths"), List.of("hollow_weight_understood"))
            );
            case "area_weightbound_mines_arena" -> List.of(
                    gate("gate_weightbound_first_sparks", "Reach First Sparks", 1340f, 1520f,
                            true,
                            "area_hollow_hub_first_sparks", List.of("hollow_weight_understood"), List.of("weightbound_ogre_defeated"))
            );
            case "area_hollow_hub_first_sparks" -> List.of(
                    gate("gate_first_sparks_shatter", "Dash into Shatter Moth Nest", 1260f, 1440f,
                            false,
                            "area_shatter_moth_nest", List.of("weightbound_ogre_defeated"), List.of("dash_restored"))
            );
            case "area_shatter_moth_nest" -> List.of(
                    gate("gate_shatter_fractured", "Cross to Fractured Contact", 1280f, 1460f,
                            true,
                            "area_fractured_contact_high_winds", List.of("dash_restored"), List.of("shatter_moth_defeated"))
            );
            case "area_fractured_contact_high_winds" -> List.of(
                    gate("gate_fractured_stone_judge", "Advance to Stone Judge Maze", 1420f, 1600f,
                            false,
                            "area_stone_judge_maze", List.of("shatter_moth_defeated"), List.of("double_jump_restored"))
            );
            case "area_stone_judge_maze" -> List.of(
                    gate("gate_stone_judge_abyss", "Open the Abyssal Gate", 1320f, 1500f,
                            true,
                            "area_abyssal_gate", List.of("double_jump_restored"), List.of("stone_judge_defeated"))
            );
            default -> List.of();
        };
    }

    private static RunGameContentProfile.AreaGate gate(
            String gateId,
            String label,
            float minX,
            float maxX,
            boolean requiresAreaClear,
            String targetAreaId,
            List<String> requiredFlags,
            List<String> setFlags) {
        return new RunGameContentProfile.AreaGate(
                gateId, label, minX, maxX, targetAreaId, requiresAreaClear, requiredFlags, setFlags);
    }
}
