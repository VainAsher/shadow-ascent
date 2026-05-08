package com.shadowascent.core.world.streaming;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowascent.core.physics.SpatialHash;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.world.progression.WorldProgressionGraph;
import com.shadowascent.core.world.progression.WorldProgressionGraph.ProgressionNode;
import com.shadowascent.core.world.sections.SectionTemplate;
import com.shadowascent.core.world.sections.SectionTemplateLibrary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Resolves a ProgressionNode to a RegionManifest and then to a live RegionInstance.
 * Validates connectivity before returning from loadNeighborhood.
 */
public final class RegionLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SectionTemplateLibrary library;
    private final RegionalStreamingConstraintValidator validator;
    private final Map<String, RegionFragmentData> fragments;

    public RegionLoader(SectionTemplateLibrary library, RegionalStreamingConstraintValidator validator) {
        this(library, validator, loadDefaultFragments());
    }

    public RegionLoader(
            SectionTemplateLibrary library,
            RegionalStreamingConstraintValidator validator,
            Map<String, RegionFragmentData> fragments) {
        this.library = library != null ? library : SectionTemplateLibrary.empty();
        this.validator = validator != null ? validator : new RegionalStreamingConstraintValidator();
        this.fragments = fragments != null ? fragments : Map.of();
    }

    /**
     * Resolve a single ProgressionNode to a RegionInstance.
     * Template selection and tile generation run eagerly; constraint pre-check is minimal.
     * Throws RegionLoadException if a blocking INACCESSIBLE or SOFTLOCK is detected.
     */
    public RegionInstance load(ProgressionNode node, long seed, List<ZoneOverride> savedOverrides) {
        RegionFragmentData fragment = fragments.get(node.id());

        String biome = fragment != null ? fragment.biome() : node.biome();
        String kind = fragment != null ? fragment.kind() : inferKind(node);
        List<SocketBinding> sockets = fragment != null ? fragment.socketBindings() : List.of();
        List<ZoneOverride> authored = fragment != null ? fragment.authoredZoneOverrides() : List.of();

        List<ZoneOverride> allOverrides = new ArrayList<>(authored);
        if (savedOverrides != null) {
            allOverrides.addAll(savedOverrides);
        }

        Optional<SectionTemplate> templateOpt = library.select(biome, kind, seed);
        if (templateOpt.isEmpty()) {
            System.out.println("[WARN] RegionLoader: no template for biome='" + biome + "' kind='" + kind
                    + "' node='" + node.id() + "' — using stub geometry.");
        }

        String selectedTemplateId = templateOpt.map(SectionTemplate::id).orElse(null);
        RegionManifest manifest = new RegionManifest(
                node.id(), selectedTemplateId, biome, kind, seed, sockets, allOverrides);

        List<TileRect> staticTiles;
        if (fragment != null && !fragment.tiles().isEmpty()) {
            staticTiles = fragment.tiles().stream()
                    .map(t -> new TileRect(t.x(), t.y(), t.w(), t.h(), t.platform()))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            staticTiles = templateOpt
                    .map(t -> RegionInstance.generateStubTiles(t.footprint().gridW(), t.footprint().gridH()))
                    .orElse(RegionInstance.generateStubTiles(1, 1));
        }

        List<ResolvedAnchor> anchors = templateOpt
                .map(t -> resolveAnchors(t, seed))
                .orElse(List.of());

        SpatialHash hash = new SpatialHash();
        for (TileRect tile : staticTiles) {
            hash.insert(tile);
        }

        return new RegionInstance(manifest, hash, staticTiles, List.of(), anchors);
    }

    /**
     * Resolve a connected subgraph centered on centerNodeId out to the given radius.
     * Runs full constraint validation on the combined manifests.
     * Throws RegionLoadException if any critical-path node has a blocking constraint issue.
     */
    public List<RegionInstance> loadNeighborhood(
            WorldProgressionGraph graph,
            String centerNodeId,
            int radius,
            long seed,
            Map<String, List<ZoneOverride>> savedOverrides) {

        Map<String, ProgressionNode> nodesById = buildAllNodesById(graph);
        Set<String> neighborhood = collectNeighborhood(graph, nodesById, centerNodeId, radius);

        List<RegionInstance> instances = new ArrayList<>();
        for (String nodeId : neighborhood) {
            ProgressionNode node = nodesById.get(nodeId);
            if (node == null) {
                continue;
            }
            List<ZoneOverride> saved = savedOverrides != null
                    ? savedOverrides.getOrDefault(nodeId, List.of())
                    : List.of();
            instances.add(load(node, seed ^ nodeId.hashCode(), saved));
        }

        // Validate the full neighborhood
        List<RegionManifest> manifests = instances.stream().map(ri -> ri.manifest()).toList();
        List<String> playerAbilities = collectGrantedAbilities(graph, nodesById, centerNodeId);
        RegionalStreamingConstraintValidator.ValidationResult result =
                validator.validate(graph, manifests, playerAbilities);

        if (!result.valid()) {
            for (RegionalStreamingConstraintValidator.ValidationIssue issue : result.issues()) {
                if (issue.kind().equals("INACCESSIBLE") || issue.kind().equals("SOFTLOCK")) {
                    throw new RegionLoadException(
                            "Blocking constraint in neighborhood around '" + centerNodeId + "': "
                                    + issue.message(),
                            result);
                }
            }
        }

        return instances;
    }

    public Map<String, RegionFragmentData> fragments() {
        return Map.copyOf(fragments);
    }

    // --- Fragment loading ---

    public static Map<String, RegionFragmentData> loadDefaultFragments() {
        for (Path candidate : List.of(
                Paths.get("data", "worldgen", "regions"),
                Paths.get("..", "data", "worldgen", "regions"),
                Paths.get("..", "..", "data", "worldgen", "regions"))) {
            if (Files.isDirectory(candidate)) {
                return loadFragments(candidate);
            }
        }
        return Map.of();
    }

    public static Map<String, RegionFragmentData> loadFragments(Path root) {
        if (root == null || !Files.isDirectory(root)) {
            return Map.of();
        }
        Map<String, RegionFragmentData> out = new HashMap<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(p -> parseFragment(p).ifPresent(f -> out.put(f.regionId(), f)));
        } catch (IOException ignored) {
        }
        return Map.copyOf(out);
    }

    private static Optional<RegionFragmentData> parseFragment(Path path) {
        try {
            JsonNode root = MAPPER.readTree(path.toFile());
            String regionId = textField(root, "regionId");
            String biome = textField(root, "biome");
            String kind = textField(root, "kind");

            List<SocketBinding> sockets = new ArrayList<>();
            JsonNode socketsNode = root.get("socketBindings");
            if (socketsNode != null && socketsNode.isArray()) {
                for (JsonNode s : socketsNode) {
                    String socketId = textField(s, "socketId");
                    String connectedId = textField(s, "connectedRegionId");
                    if (socketId != null && connectedId != null) {
                        sockets.add(new SocketBinding(socketId, connectedId));
                    }
                }
            }

            List<ZoneOverride> overrides = new ArrayList<>();
            JsonNode overridesNode = root.get("authoredZoneOverrides");
            if (overridesNode != null && overridesNode.isArray()) {
                for (JsonNode o : overridesNode) {
                    String zoneRole = textField(o, "zoneRole");
                    String overlayKind = textField(o, "overlayKind");
                    if (zoneRole != null && overlayKind != null) {
                        overrides.add(new ZoneOverride(zoneRole, overlayKind, Map.of()));
                    }
                }
            }

            List<RegionFragmentData.AuthoredTile> tiles = new ArrayList<>();
            JsonNode tilesNode = root.get("tiles");
            if (tilesNode != null && tilesNode.isArray()) {
                for (JsonNode t : tilesNode) {
                    float tx = (float) t.path("x").asDouble(0);
                    float ty = (float) t.path("y").asDouble(0);
                    float tw = (float) t.path("w").asDouble(16);
                    float th = (float) t.path("h").asDouble(16);
                    boolean platform = t.path("platform").asBoolean(false);
                    tiles.add(new RegionFragmentData.AuthoredTile(tx, ty, tw, th, platform));
                }
            }

            return Optional.of(new RegionFragmentData(regionId, biome, kind, sockets, overrides, tiles));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static String textField(JsonNode node, String field) {
        JsonNode v = node != null ? node.get(field) : null;
        return v != null && v.isTextual() && !v.asText().isBlank() ? v.asText() : null;
    }

    // --- Helpers ---

    private Set<String> collectNeighborhood(
            WorldProgressionGraph graph,
            Map<String, ProgressionNode> nodesById,
            String centerNodeId,
            int radius) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        Map<String, Integer> distance = new HashMap<>();
        queue.add(centerNodeId);
        visited.add(centerNodeId);
        distance.put(centerNodeId, 0);

        // Build bidirectional adjacency from children links
        Map<String, Set<String>> adj = new HashMap<>();
        for (ProgressionNode node : getAllNodes(graph)) {
            for (String child : node.children()) {
                adj.computeIfAbsent(node.id(), k -> new HashSet<>()).add(child);
                adj.computeIfAbsent(child, k -> new HashSet<>()).add(node.id());
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int d = distance.getOrDefault(current, 0);
            if (d >= radius) {
                continue;
            }
            Set<String> neighbors = adj.getOrDefault(current, Set.of());
            for (String neighbor : neighbors) {
                if (visited.add(neighbor)) {
                    distance.put(neighbor, d + 1);
                    queue.add(neighbor);
                }
            }
        }
        return visited;
    }

    private Map<String, ProgressionNode> buildAllNodesById(WorldProgressionGraph graph) {
        Map<String, ProgressionNode> out = new HashMap<>(graph.nodesById());
        out.put(graph.centralHub().id(), graph.centralHub());
        return out;
    }

    private List<ProgressionNode> getAllNodes(WorldProgressionGraph graph) {
        List<ProgressionNode> all = new ArrayList<>(graph.allNodes());
        all.add(graph.centralHub());
        return all;
    }

    private List<String> collectGrantedAbilities(
            WorldProgressionGraph graph,
            Map<String, ProgressionNode> nodesById,
            String upToNodeId) {
        // Collect all grants along the critical path up to (and including) the center node
        List<String> abilities = new ArrayList<>();
        for (ProgressionNode node : graph.criticalPath()) {
            abilities.addAll(node.grants());
            if (node.id().equals(upToNodeId)) {
                break;
            }
        }
        return abilities;
    }

    private static List<ResolvedAnchor> resolveAnchors(SectionTemplate template, long seed) {
        List<ResolvedAnchor> out = new ArrayList<>();
        for (SectionTemplate.Anchor anchor : template.anchors()) {
            float worldX = anchor.localBounds().x() * 96f;
            float worldY = anchor.localBounds().y() * 96f;
            boolean active = anchor.requires().isEmpty();
            out.add(new ResolvedAnchor(
                    anchor.id(), anchor.kind(), worldX, worldY, anchor.requires(), active));
        }
        return out;
    }

    private static String inferKind(ProgressionNode node) {
        return switch (node.kind()) {
            case CENTRAL_HUB -> "hub";
            case REGION_HUB -> "region_hub";
            case DUNGEON -> "dungeon";
        };
    }
}
