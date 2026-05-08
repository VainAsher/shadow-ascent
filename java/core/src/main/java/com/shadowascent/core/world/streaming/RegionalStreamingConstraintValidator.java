package com.shadowascent.core.world.streaming;

import com.shadowascent.core.world.progression.WorldProgressionGraph;
import com.shadowascent.core.world.progression.WorldProgressionGraph.ProgressionNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Validates a set of RegionManifest records against a WorldProgressionGraph for safety.
 *
 * Three checks:
 *  1. Connectivity    — no manifest regionId is orphaned from centralHub in the progression graph
 *  2. Accessibility   — every criticalPath node reachable with playerAbilities
 *  3. Softlock        — no non-optional manifest is route-blocked with no unblocked escape path
 * Plus: Socket mismatch — adjacent nodes in the graph with manifests but no socket binding
 */
public final class RegionalStreamingConstraintValidator {

    public record ValidationResult(boolean valid, List<ValidationIssue> issues) {
        public ValidationResult {
            issues = issues != null ? List.copyOf(issues) : List.of();
        }
    }

    public record ValidationIssue(
            String regionId,
            String kind,
            String message,
            String repairAction) {}

    public ValidationResult validate(
            WorldProgressionGraph graph,
            List<RegionManifest> manifests,
            List<String> playerAbilities) {

        List<ValidationIssue> issues = new ArrayList<>();
        if (graph == null || manifests == null || manifests.isEmpty()) {
            return new ValidationResult(true, issues);
        }

        Map<String, ProgressionNode> nodesById = buildAllNodesById(graph);
        Set<String> manifestIds = new HashSet<>();
        Map<String, RegionManifest> manifestById = new HashMap<>();
        for (RegionManifest m : manifests) {
            manifestIds.add(m.regionId());
            manifestById.put(m.regionId(), m);
        }

        Set<String> playerAbilitySet = playerAbilities != null ? new HashSet<>(playerAbilities) : Set.of();

        checkConnectivity(graph, manifestIds, nodesById, issues);
        checkAccessibility(graph, manifestById, nodesById, playerAbilitySet, issues);
        checkSoftlock(graph, manifests, manifestById, nodesById, issues);
        checkSocketMismatch(graph, nodesById, manifestById, issues);

        return new ValidationResult(issues.isEmpty(), issues);
    }

    // --- Connectivity ---

    private void checkConnectivity(
            WorldProgressionGraph graph,
            Set<String> manifestIds,
            Map<String, ProgressionNode> nodesById,
            List<ValidationIssue> issues) {

        Set<String> reachable = graphReachableFromHub(graph, nodesById);
        for (String id : manifestIds) {
            if (!reachable.contains(id)) {
                issues.add(new ValidationIssue(
                        id,
                        "DISCONNECTED",
                        "Region '" + id + "' is not reachable from centralHub in the progression graph.",
                        "Remove the manifest or add a children link from an ancestor node to '" + id + "'."));
            }
        }
    }

    private Set<String> graphReachableFromHub(
            WorldProgressionGraph graph,
            Map<String, ProgressionNode> nodesById) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        String hubId = graph.centralHub().id();
        queue.add(hubId);
        visited.add(hubId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            ProgressionNode node = nodesById.get(current);
            if (node == null) {
                continue;
            }
            for (String child : node.children()) {
                if (visited.add(child)) {
                    queue.add(child);
                }
            }
        }
        return visited;
    }

    // --- Accessibility ---

    private void checkAccessibility(
            WorldProgressionGraph graph,
            Map<String, RegionManifest> manifestById,
            Map<String, ProgressionNode> nodesById,
            Set<String> playerAbilitySet,
            List<ValidationIssue> issues) {

        for (ProgressionNode node : graph.criticalPath()) {
            if (!manifestById.containsKey(node.id())) {
                continue;
            }
            for (String req : node.requires()) {
                if (!playerAbilitySet.contains(req)) {
                    issues.add(new ValidationIssue(
                            node.id(),
                            "INACCESSIBLE",
                            "Critical-path node '" + node.id() + "' requires ability '" + req
                                    + "' which is not in the player ability set.",
                            "Grant ability '" + req + "' before loading this region, or mark the node optional."));
                    break;
                }
            }
        }
    }

    // --- Softlock ---

    private void checkSoftlock(
            WorldProgressionGraph graph,
            List<RegionManifest> manifests,
            Map<String, RegionManifest> manifestById,
            Map<String, ProgressionNode> nodesById,
            List<ValidationIssue> issues) {

        Set<String> routeBlockedIds = new HashSet<>();
        for (RegionManifest m : manifests) {
            if (isRouteBlocked(m)) {
                routeBlockedIds.add(m.regionId());
            }
        }

        String centralHubId = graph.centralHub().id();

        for (RegionManifest m : manifests) {
            ProgressionNode node = nodesById.get(m.regionId());
            if (node == null || node.optional()) {
                continue;
            }
            if (!routeBlockedIds.contains(m.regionId())) {
                continue;
            }
            // Non-optional manifest is route-blocked — check if it can escape to centralHub
            boolean canEscape = canReachHubUnblocked(
                    m.regionId(), centralHubId, manifestById, routeBlockedIds);
            if (!canEscape) {
                issues.add(new ValidationIssue(
                        m.regionId(),
                        "SOFTLOCK",
                        "Non-optional region '" + m.regionId()
                                + "' has route-blocking overlays with no unblocked escape path to centralHub.",
                        "Remove the CORRUPTION_SURGE/ROUTE_HAZARD overlay from the traversal zone of '"
                                + m.regionId() + "', or ensure an alternate unblocked socket path to centralHub."));
            }
        }
    }

    private boolean isRouteBlocked(RegionManifest manifest) {
        return manifest.zoneOverrides().stream().anyMatch(zo ->
                (zo.overlayKind().equals("CORRUPTION_SURGE") || zo.overlayKind().equals("ROUTE_HAZARD"))
                        && isTraversalRole(zo.zoneRole()));
    }

    private boolean isTraversalRole(String role) {
        if (role == null) {
            return false;
        }
        String lower = role.toLowerCase();
        return lower.contains("traversal") || lower.contains("route") || lower.contains("path");
    }

    private boolean canReachHubUnblocked(
            String startId,
            String targetId,
            Map<String, RegionManifest> manifestById,
            Set<String> routeBlockedIds) {
        // A route-blocked node has its traversal zone sealed — the player inside cannot
        // use any socket to exit. No escape path exists from within a blocked node.
        if (routeBlockedIds.contains(startId)) {
            return false;
        }
        if (startId.equals(targetId)) {
            return true;
        }
        Set<String> visited = new HashSet<>();
        visited.add(startId);
        Queue<String> queue = new ArrayDeque<>();
        queue.add(startId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(targetId)) {
                return true;
            }
            RegionManifest m = manifestById.get(current);
            if (m == null) {
                continue;
            }
            for (SocketBinding socket : m.sockets()) {
                String neighbor = socket.connectedRegionId();
                if (!routeBlockedIds.contains(neighbor) && visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        return false;
    }

    // --- Socket mismatch ---

    private void checkSocketMismatch(
            WorldProgressionGraph graph,
            Map<String, ProgressionNode> nodesById,
            Map<String, RegionManifest> manifestById,
            List<ValidationIssue> issues) {

        // For each parent→child edge in the graph, if both have manifests check bidirectional sockets
        for (ProgressionNode parent : getAllNodes(graph)) {
            for (String childId : parent.children()) {
                if (!manifestById.containsKey(parent.id()) || !manifestById.containsKey(childId)) {
                    continue;
                }
                RegionManifest parentManifest = manifestById.get(parent.id());
                RegionManifest childManifest = manifestById.get(childId);

                boolean parentHasSocketToChild = parentManifest.sockets().stream()
                        .anyMatch(s -> s.connectedRegionId().equals(childId));
                boolean childHasSocketToParent = childManifest.sockets().stream()
                        .anyMatch(s -> s.connectedRegionId().equals(parent.id()));

                if (!parentHasSocketToChild && !childHasSocketToParent) {
                    String pairKey = parent.id() + " <-> " + childId;
                    issues.add(new ValidationIssue(
                            parent.id(),
                            "SOCKET_MISMATCH",
                            "Adjacent nodes " + pairKey + " have loaded manifests but no socket binding between them.",
                            "Add a SocketBinding in '" + parent.id() + "' targeting '" + childId
                                    + "' or vice versa."));
                }
            }
        }
    }

    // --- Helpers ---

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
}
