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

    public AuthoringWorldBootstrap(AreaPlacementResolver areaPlacementResolver, RoomSpecCatalog roomSpecCatalog) {
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
        if ("LANTERN_HEIGHTS".equals(plateauId)) {
            System.err.println("[AuthoringWorldBootstrap] WARNING: no room spec resolved for LANTERN_HEIGHTS area '"
                    + areaId + "' — add a room spec entry to data/room_specs/");
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
        // For room-spec-driven Act I scenes, the room spec is the source of truth for who should be staged.
        // This keeps NPC additions/repositioning in JSON instead of Java whitelists.
        return true;
    }

    private RoomSpec resolveRoomSpec(GameState gameState, String areaId, String plateauId) {
        if (gameState == null) {
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
        java.util.Optional<RoomSpec> areaSpecific = roomSpecCatalog.roomsForPlateau(plateauId).stream()
                .filter(room -> areaId.equals(room.areaId()))
                .filter(room -> room.requiredFlags().stream().allMatch(gameState.getStoryState()::hasFlag))
                .filter(room -> room.setFlags().isEmpty()
                        || room.setFlags().stream().anyMatch(flag -> !gameState.getStoryState().hasFlag(flag)))
                // Room-state precedence is intentionally "first matching route_order wins" for one area.
                .min(Comparator.comparingInt(RoomSpec::routeOrder).thenComparing(RoomSpec::id));
        java.util.Optional<RoomSpec> plateauFallback = roomSpecCatalog.roomsForPlateau(plateauId).stream()
                .filter(room -> room.requiredFlags().stream().allMatch(gameState.getStoryState()::hasFlag))
                .filter(room -> room.setFlags().isEmpty()
                        || room.setFlags().stream().anyMatch(flag -> !gameState.getStoryState().hasFlag(flag)))
                .min(Comparator.comparingInt(RoomSpec::routeOrder).thenComparing(RoomSpec::id));
        RoomSpec resolved = areaSpecific.or(() -> plateauFallback).orElse(null);
        if (resolved != null) {
            gameState.setCurrentRoomId(resolved.id());
            gameState.setPendingRoomSpawnId(null);
        }
        return resolved;
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
            default -> Set.of();
        };
    }

    private static Map<String, Float> preferredNpcX(String areaId) {
        Map<String, Float> positions = new LinkedHashMap<>();
        switch (areaId) {
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
        if ("EMBER_MONASTERY".equals(plateauId)) {
            enemies.add(new RunGameContentProfile.EnemyPlacement("skeleton_1", "skeleton", 880f, 280f));
        } else {
            enemies.add(new RunGameContentProfile.EnemyPlacement("goblin_1", "goblin", 600f, 280f));
        }
        return enemies;
    }

    private static List<RunGameContentProfile.AreaGate> buildAreaGates(String areaId, String plateauId) {
        return List.of();
    }
}
