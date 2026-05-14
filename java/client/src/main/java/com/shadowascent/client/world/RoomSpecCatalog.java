package com.shadowascent.client.world;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class RoomSpecCatalog {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, RoomSpec> roomsById;
    private final Map<String, EncounterSpec> encountersById;

    private RoomSpecCatalog(Map<String, RoomSpec> roomsById, Map<String, EncounterSpec> encountersById) {
        this.roomsById = Map.copyOf(roomsById);
        this.encountersById = Map.copyOf(encountersById);
    }

    public static RoomSpecCatalog loadDefault() {
        Path roomSpecDir = resolveDefaultRoomSpecDir();
        if (!Files.isDirectory(roomSpecDir)) {
            throw new IllegalArgumentException("Room spec directory missing: " + roomSpecDir.toAbsolutePath());
        }
        try {
            List<Path> files = Files.list(roomSpecDir)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
            return load(files);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to enumerate room specs: " + ex.getMessage(), ex);
        }
    }

    public static RoomSpecCatalog load(List<Path> files) {
        Map<String, RoomSpec> rooms = new LinkedHashMap<>();
        Map<String, EncounterSpec> encounters = new LinkedHashMap<>();
        for (Path file : files) {
            JsonNode root;
            try {
                root = MAPPER.readTree(file.toFile());
            } catch (IOException ex) {
                throw new IllegalArgumentException("Failed to parse room spec file `" + file + "`: " + ex.getMessage(), ex);
            }

            for (JsonNode encounterNode : arrayNode(root, "encounter_definitions")) {
                EncounterSpec encounter = new EncounterSpec(
                        text(encounterNode, "id"),
                        text(encounterNode, "clear_type"),
                        stringArray(encounterNode.get("enemy_ids")),
                        stringArray(encounterNode.get("set_flags")));
                if (encounter.id().isBlank()) {
                    continue;
                }
                if (encounters.putIfAbsent(encounter.id(), encounter) != null) {
                    throw new IllegalArgumentException("Duplicate encounter id `" + encounter.id() + "` in `" + file + "`");
                }
            }

            for (JsonNode roomNode : arrayNode(root, "rooms")) {
                RoomSpec room = parseRoom(roomNode);
                if (room.id().isBlank()) {
                    continue;
                }
                if (rooms.putIfAbsent(room.id(), room) != null) {
                    throw new IllegalArgumentException("Duplicate room id `" + room.id() + "`");
                }
            }
        }

        validate(rooms, encounters);
        return new RoomSpecCatalog(rooms, encounters);
    }

    public Optional<RoomSpec> room(String roomId) {
        return Optional.ofNullable(roomsById.get(roomId));
    }

    public Optional<EncounterSpec> encounter(String encounterId) {
        return Optional.ofNullable(encountersById.get(encounterId));
    }

    public List<RoomSpec> roomsForPlateau(String plateauId) {
        String normalized = normalize(plateauId);
        return roomsById.values().stream()
                .filter(room -> normalize(room.plateauId()).equals(normalized))
                .sorted(Comparator.comparingInt(RoomSpec::routeOrder).thenComparing(RoomSpec::id))
                .toList();
    }

    private static Path resolveDefaultRoomSpecDir() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        for (Path cursor = current; cursor != null; cursor = cursor.getParent()) {
            Path candidate = cursor.resolve("data").resolve("room_specs");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return current.resolve("data").resolve("room_specs");
    }

    private static RoomSpec parseRoom(JsonNode roomNode) {
        List<RoomSpec.SpawnPoint> spawnPoints = new ArrayList<>();
        for (JsonNode spawnNode : arrayNode(roomNode, "spawn_points")) {
            spawnPoints.add(new RoomSpec.SpawnPoint(
                    text(spawnNode, "id"),
                    floatValue(spawnNode, "x", 0f),
                    floatValue(spawnNode, "y", 0f)));
        }

        List<RoomSpec.GeometrySpec> geometry = new ArrayList<>();
        for (JsonNode geometryNode : arrayNode(roomNode, "geometry")) {
            geometry.add(new RoomSpec.GeometrySpec(
                    text(geometryNode, "type"),
                    floatValue(geometryNode, "x", 0f),
                    floatValue(geometryNode, "y", 0f),
                    floatValue(geometryNode, "w", 0f),
                    floatValue(geometryNode, "h", 0f)));
        }

        List<RoomSpec.NpcAnchor> npcAnchors = new ArrayList<>();
        for (JsonNode npcNode : arrayNode(roomNode, "npc_anchors")) {
            npcAnchors.add(new RoomSpec.NpcAnchor(
                    text(npcNode, "npc_id"),
                    text(npcNode, "role"),
                    floatValue(npcNode, "x", 0f),
                    floatValue(npcNode, "y", 0f),
                    floatValue(npcNode, "patrol_min_x", 0f),
                    floatValue(npcNode, "patrol_max_x", 0f)));
        }

        List<RoomSpec.EnemyPlacementSpec> enemyPlacements = new ArrayList<>();
        for (JsonNode enemyNode : arrayNode(roomNode, "enemy_placements")) {
            enemyPlacements.add(new RoomSpec.EnemyPlacementSpec(
                    text(enemyNode, "enemy_id"),
                    text(enemyNode, "enemy_type"),
                    floatValue(enemyNode, "x", 0f),
                    floatValue(enemyNode, "y", 0f)));
        }

        List<RoomTransitionSpec> transitions = new ArrayList<>();
        for (JsonNode transitionNode : arrayNode(roomNode, "transitions")) {
            transitions.add(new RoomTransitionSpec(
                    text(transitionNode, "id"),
                    text(transitionNode, "type"),
                    text(transitionNode, "target_room_id"),
                    "",
                    text(transitionNode, "target_spawn_id"),
                    floatValue(transitionNode, "min_x", 0f),
                    floatValue(transitionNode, "max_x", 0f),
                    optionalFloatValue(transitionNode, "min_y"),
                    optionalFloatValue(transitionNode, "max_y"),
                    stringArray(transitionNode.get("required_flags")),
                    stringArray(transitionNode.get("set_flags"))));
        }

        return new RoomSpec(
                text(roomNode, "id"),
                text(roomNode, "display_name"),
                text(roomNode, "plateau_id"),
                text(roomNode, "area_id"),
                text(roomNode, "scene_role"),
                intValue(roomNode, "route_order", Integer.MAX_VALUE),
                stringArray(roomNode.get("required_flags")),
                stringArray(roomNode.get("set_flags")),
                spawnPoints,
                geometry,
                npcAnchors,
                enemyPlacements,
                stringArray(roomNode.get("encounters")),
                transitions
        );
    }

    private static void validate(Map<String, RoomSpec> rooms, Map<String, EncounterSpec> encounters) {
        for (RoomSpec room : rooms.values()) {
            if (room.spawnPoints().isEmpty()) {
                throw new IllegalArgumentException("Room `" + room.id() + "` is missing spawn points");
            }
            for (RoomTransitionSpec transition : room.transitions()) {
                if (!rooms.containsKey(transition.targetRoomId())) {
                    throw new IllegalArgumentException("Room `" + room.id() + "` references unknown target room `" + transition.targetRoomId() + "`");
                }
                boolean spawnExists = rooms.get(transition.targetRoomId()).spawnPoints().stream()
                        .anyMatch(spawn -> spawn.id().equals(transition.targetSpawnId()));
                if (!spawnExists) {
                    throw new IllegalArgumentException("Transition `" + transition.id() + "` targets missing spawn `" + transition.targetSpawnId() + "`");
                }
            }
            for (String encounterId : room.encounters()) {
                if (!encounters.containsKey(encounterId)) {
                    throw new IllegalArgumentException("Room `" + room.id() + "` references unknown encounter `" + encounterId + "`");
                }
            }
        }
    }

    private static List<JsonNode> arrayNode(JsonNode root, String field) {
        JsonNode node = root == null ? null : root.get(field);
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<JsonNode> values = new ArrayList<>();
        node.forEach(values::add);
        return values;
    }

    private static List<String> stringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> {
            if (item != null && !item.asText("").isBlank()) {
                values.add(item.asText());
            }
        });
        return List.copyOf(values);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null ? "" : value.asText("");
    }

    private static int intValue(JsonNode node, String field, int fallback) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.isNumber() ? value.intValue() : fallback;
    }

    private static float floatValue(JsonNode node, String field, float fallback) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.isNumber() ? value.floatValue() : fallback;
    }

    private static Float optionalFloatValue(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value != null && value.isNumber() ? value.floatValue() : null;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
