package com.shadowascent.client.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.shadowascent.client.world.RoomSpec;
import com.shadowascent.client.world.RoomSpecCatalog;
import com.shadowascent.client.world.RoomTransitionSpec;
import com.shadowascent.core.data.BeatDefinition;
import com.shadowascent.core.data.GameDataContracts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class RunGameAuthoringDiagnostics {

    private RunGameAuthoringDiagnostics() {
    }

    public static Report validate(RoomSpecCatalog catalog, GameDataContracts contracts) {
        List<String> errors = new ArrayList<>();
        List<RoomSpec> rooms = catalog.allRooms().stream()
                .sorted(Comparator.comparing(RoomSpec::plateauId).thenComparingInt(RoomSpec::routeOrder).thenComparing(RoomSpec::id))
                .toList();
        Map<String, List<RoomSpec>> roomsByPlateau = new LinkedHashMap<>();
        for (RoomSpec room : rooms) {
            roomsByPlateau.computeIfAbsent(normalize(room.plateauId()), ignored -> new ArrayList<>()).add(room);
        }

        validateNpcAnchors(rooms, contracts, errors);
        validateVariantOverlap(rooms, errors);
        validateAmbiguousTransitions(rooms, errors);
        validateBeatAreaCoverage(contracts, roomsByPlateau, errors);
        validateDialogueBeatAreaCoverage(contracts, roomsByPlateau, errors);

        return new Report(List.copyOf(errors));
    }

    public static void main(String[] args) {
        RoomSpecCatalog catalog = RoomSpecCatalog.loadDefault();
        GameDataContracts contracts = GameDataContracts.loadDefault();
        Report report = validate(catalog, contracts);
        if (report.isClean()) {
            System.out.println("[RunGameAuthoringDiagnostics] PASS");
            return;
        }
        System.err.println("[RunGameAuthoringDiagnostics] FAIL");
        for (String error : report.errors()) {
            System.err.println("  - " + error);
        }
        System.exit(1);
    }

    private static void validateNpcAnchors(List<RoomSpec> rooms, GameDataContracts contracts, List<String> errors) {
        Set<String> knownNpcIds = contracts.npcDefinitions().keySet();
        for (RoomSpec room : rooms) {
            for (RoomSpec.NpcAnchor anchor : room.npcAnchors()) {
                if (!knownNpcIds.contains(anchor.npcId())) {
                    errors.add("room `" + room.id() + "` references unknown npc_anchor npc_id `" + anchor.npcId() + "`");
                }
            }
        }
    }

    private static void validateVariantOverlap(List<RoomSpec> rooms, List<String> errors) {
        for (int i = 0; i < rooms.size(); i++) {
            RoomSpec left = rooms.get(i);
            for (int j = i + 1; j < rooms.size(); j++) {
                RoomSpec right = rooms.get(j);
                if (!left.plateauId().equalsIgnoreCase(right.plateauId())) {
                    continue;
                }
                if (!left.areaId().equalsIgnoreCase(right.areaId())) {
                    continue;
                }
                if (!left.sceneRole().equalsIgnoreCase(right.sceneRole())) {
                    continue;
                }
                if (left.requiredFlags().equals(right.requiredFlags())) {
                    errors.add("room variants `" + left.id() + "` and `" + right.id()
                            + "` share identical required_flags for area `" + left.areaId() + "`");
                }
            }
        }
    }

    private static void validateAmbiguousTransitions(List<RoomSpec> rooms, List<String> errors) {
        for (RoomSpec room : rooms) {
            for (int i = 0; i < room.transitions().size(); i++) {
                RoomTransitionSpec left = room.transitions().get(i);
                for (int j = i + 1; j < room.transitions().size(); j++) {
                    RoomTransitionSpec right = room.transitions().get(j);
                    if (!createsAmbiguousOptionalGatePair(left, right)) {
                        continue;
                    }
                    if (bandsOverlap(left, right)) {
                        errors.add("room `" + room.id() + "` has ambiguous transition band between `"
                                + left.id() + "` (" + left.type() + ") and `"
                                + right.id() + "` (" + right.type() + ")");
                    }
                }
            }
        }
    }

    private static void validateBeatAreaCoverage(
            GameDataContracts contracts,
            Map<String, List<RoomSpec>> roomsByPlateau,
            List<String> errors) {
        for (Map.Entry<String, List<RoomSpec>> entry : roomsByPlateau.entrySet()) {
            String plateauId = entry.getKey();
            List<RoomSpec> plateauRooms = entry.getValue();
            Set<String> areaIds = plateauRooms.stream()
                    .map(RoomSpec::areaId)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            int maxRouteOrder = plateauRooms.stream().mapToInt(RoomSpec::routeOrder).max().orElse(Integer.MAX_VALUE);
            for (BeatDefinition beat : contracts.beatsForPlateau(plateauId)) {
                if (beat.routeOrder() > maxRouteOrder) {
                    continue;
                }
                if (skipAreaCoverageForBeatType(beat)) {
                    continue;
                }
                if (beat.areaId() == null || beat.areaId().isBlank()) {
                    continue;
                }
                if (!areaIds.contains(beat.areaId())) {
                    errors.add("beat `" + beat.id() + "` references area_id `" + beat.areaId()
                            + "` with no matching room-spec area for plateau `" + plateauId + "`");
                }
            }
        }
    }

    private static void validateDialogueBeatAreaCoverage(
            GameDataContracts contracts,
            Map<String, List<RoomSpec>> roomsByPlateau,
            List<String> errors) {
        JsonNode dialogue = contracts.contractDocument("dialogue").orElse(null);
        if (dialogue == null || dialogue.get("lines") == null || !dialogue.get("lines").isArray()) {
            return;
        }

        Map<String, Set<String>> areaIdsByPlateau = new LinkedHashMap<>();
        Map<String, Integer> maxRouteOrderByPlateau = new LinkedHashMap<>();
        for (Map.Entry<String, List<RoomSpec>> entry : roomsByPlateau.entrySet()) {
            areaIdsByPlateau.put(entry.getKey(), entry.getValue().stream()
                    .map(RoomSpec::areaId)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
            maxRouteOrderByPlateau.put(entry.getKey(),
                    entry.getValue().stream().mapToInt(RoomSpec::routeOrder).max().orElse(Integer.MAX_VALUE));
        }

        for (JsonNode line : dialogue.get("lines")) {
            String lineId = text(line, "id");
            for (String beatRef : beatRefs(line.get("beat_ref"))) {
                BeatDefinition beat = contracts.beat(beatRef).orElse(null);
                if (beat == null) {
                    continue;
                }
                String plateauId = normalize(beat.plateau());
                if (!roomsByPlateau.containsKey(plateauId)) {
                    continue;
                }
                if (beat.routeOrder() > maxRouteOrderByPlateau.getOrDefault(plateauId, Integer.MAX_VALUE)) {
                    continue;
                }
                if (skipAreaCoverageForBeatType(beat)) {
                    continue;
                }
                if (beat.areaId() == null || beat.areaId().isBlank()) {
                    continue;
                }
                if (!areaIdsByPlateau.getOrDefault(plateauId, Set.of()).contains(beat.areaId())) {
                    errors.add("dialogue `" + lineId + "` references beat `" + beat.id()
                            + "` whose area_id `" + beat.areaId() + "` has no matching room-spec area for plateau `"
                            + plateauId + "`");
                }
            }
        }
    }

    private static boolean createsAmbiguousOptionalGatePair(RoomTransitionSpec left, RoomTransitionSpec right) {
        return (isOptionalFreeExit(left) && isBlockingGate(right))
                || (isOptionalFreeExit(right) && isBlockingGate(left));
    }

    private static boolean isOptionalFreeExit(RoomTransitionSpec transition) {
        return "free_exit".equalsIgnoreCase(transition.type());
    }

    private static boolean isBlockingGate(RoomTransitionSpec transition) {
        String type = transition.type() == null ? "" : transition.type().toLowerCase(Locale.ROOT);
        return "mission_gate".equals(type) || "encounter_gate".equals(type);
    }

    private static boolean bandsOverlap(RoomTransitionSpec left, RoomTransitionSpec right) {
        boolean xOverlap = left.maxX() >= right.minX() && right.maxX() >= left.minX();
        if (!xOverlap) {
            return false;
        }
        float leftMinY = left.minY() == null ? Float.NEGATIVE_INFINITY : left.minY();
        float leftMaxY = left.maxY() == null ? Float.POSITIVE_INFINITY : left.maxY();
        float rightMinY = right.minY() == null ? Float.NEGATIVE_INFINITY : right.minY();
        float rightMaxY = right.maxY() == null ? Float.POSITIVE_INFINITY : right.maxY();
        return leftMaxY >= rightMinY && rightMaxY >= leftMinY;
    }

    private static List<String> beatRefs(JsonNode beatRefNode) {
        if (beatRefNode == null || beatRefNode.isNull()) {
            return List.of();
        }
        if (beatRefNode.isTextual()) {
            String value = beatRefNode.asText("").trim();
            return value.isBlank() ? List.of() : List.of(value);
        }
        if (!beatRefNode.isArray()) {
            return List.of();
        }
        List<String> refs = new ArrayList<>();
        for (JsonNode item : beatRefNode) {
            if (item != null && item.isTextual() && !item.asText("").isBlank()) {
                refs.add(item.asText("").trim());
            }
        }
        return List.copyOf(refs);
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.get(field) == null) {
            return "";
        }
        return node.get(field).asText("").trim();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean skipAreaCoverageForBeatType(BeatDefinition beat) {
        if (beat == null || beat.beatType() == null) {
            return false;
        }
        String beatType = beat.beatType().trim().toUpperCase(Locale.ROOT);
        return "TRANSITION".equals(beatType) || "CINEMATIC".equals(beatType);
    }

    public record Report(List<String> errors) {
        public Report {
            errors = List.copyOf(errors);
        }

        public boolean isClean() {
            return errors.isEmpty();
        }
    }
}
