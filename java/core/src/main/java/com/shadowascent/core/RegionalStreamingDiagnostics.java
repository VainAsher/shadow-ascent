package com.shadowascent.core;

import com.shadowascent.core.world.progression.WorldProgressionGraph;
import com.shadowascent.core.world.progression.WorldProgressionGraph.ProgressionNode;
import com.shadowascent.core.world.sections.SectionTemplateLibrary;
import com.shadowascent.core.world.streaming.RegionInstance;
import com.shadowascent.core.world.streaming.RegionFragmentData;
import com.shadowascent.core.world.streaming.RegionLoader;
import com.shadowascent.core.world.streaming.RegionManifest;
import com.shadowascent.core.world.streaming.RegionalStreamingConstraintValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CLI diagnostics for M6 regional streaming — validates authored region fragments
 * and runs constraint checks against a minimal authored progression graph.
 */
public final class RegionalStreamingDiagnostics {
    private RegionalStreamingDiagnostics() {
    }

    public static void main(String[] args) {
        System.out.println("=== Shadow Ascent Regional Streaming Diagnostics ===");

        Map<String, RegionFragmentData> fragments = RegionLoader.loadDefaultFragments();
        System.out.println("Region fragments loaded: " + fragments.size());

        if (fragments.isEmpty()) {
            System.out.println("WARNING: No region fragments found in data/worldgen/regions/");
        } else {
            fragments.forEach((id, f) -> System.out.println(
                    "  fragment: " + id + " biome=" + f.biome() + " kind=" + f.kind()
                            + " sockets=" + f.socketBindings().size()));
        }

        SectionTemplateLibrary library = SectionTemplateLibrary.loadDefault();
        System.out.println("Section templates loaded: " + library.templates().size());

        // Build a minimal progression graph from the authored fragment regionIds
        WorldProgressionGraph graph = buildDiagnosticsGraph(fragments);
        System.out.println("Progression graph: centralHub=" + graph.centralHub().id()
                + " worldNodes=" + graph.worldNodes().size()
                + " criticalPath=" + graph.criticalPath().size());

        RegionalStreamingConstraintValidator validator = new RegionalStreamingConstraintValidator();
        RegionLoader loader = new RegionLoader(library, validator, fragments);

        List<RegionManifest> manifests = new ArrayList<>();
        int loadedCount = 0;
        for (ProgressionNode node : getAllNodes(graph)) {
            try {
                RegionInstance instance = loader.load(node, graph.worldSeed(), List.of());
                manifests.add(instance.manifest());
                loadedCount++;
            } catch (Exception e) {
                System.out.println("  [WARN] load failed for node=" + node.id() + ": " + e.getMessage());
            }
        }
        System.out.println("Regions loaded: " + loadedCount);

        // Pass abilities that reflect progression through the authored route
        List<String> defaultAbilities = List.of("basic_movement", "jump", "dash", "combat_basic");
        RegionalStreamingConstraintValidator.ValidationResult result =
                validator.validate(graph, manifests, defaultAbilities);
        System.out.println("Constraint validation: " + (result.valid() ? "PASS" : "FAIL"));
        if (result.issues().isEmpty()) {
            System.out.println("Validation issues: none");
        } else {
            System.out.println("Validation issues (" + result.issues().size() + "):");
            result.issues().forEach(issue -> System.out.println(
                    "  [" + issue.kind() + "] " + issue.regionId() + ": " + issue.message()));
        }
    }

    /** Build a minimal 3-node progression graph wired to match the authored region fragments. */
    private static WorldProgressionGraph buildDiagnosticsGraph(Map<String, RegionFragmentData> fragments) {
        boolean hasForge = fragments.containsKey("dungeon_forge_terrace_a");
        boolean hasShaft = fragments.containsKey("region_hollow_shaft");

        ProgressionNode hub = new ProgressionNode(
                "hub_lantern_heights",
                WorldProgressionGraph.NodeKind.CENTRAL_HUB,
                "lantern",
                List.of(), List.of(),
                hasForge ? List.of("dungeon_forge_terrace_a") : List.of(),
                List.of(), "low", false);

        ProgressionNode forge = new ProgressionNode(
                "dungeon_forge_terrace_a",
                WorldProgressionGraph.NodeKind.REGION_HUB,
                "lantern",
                List.of(), List.of("dash"),
                hasShaft ? List.of("region_hollow_shaft") : List.of(),
                List.of(), "medium", false);

        ProgressionNode shaft = new ProgressionNode(
                "region_hollow_shaft",
                WorldProgressionGraph.NodeKind.DUNGEON,
                "hollow",
                List.of("dash"), List.of("combat_basic"),
                List.of(),
                List.of(), "high", false);

        List<ProgressionNode> worldNodes = new ArrayList<>();
        if (hasForge) worldNodes.add(forge);
        List<ProgressionNode> dungeonNodes = new ArrayList<>();
        if (hasShaft) dungeonNodes.add(shaft);

        List<ProgressionNode> criticalPath = new ArrayList<>();
        criticalPath.add(hub);
        if (hasForge) criticalPath.add(forge);
        if (hasShaft) criticalPath.add(shaft);

        return new WorldProgressionGraph(
                1337L, hub, worldNodes, List.of(), dungeonNodes, criticalPath, "diagnostics");
    }

    private static List<ProgressionNode> getAllNodes(WorldProgressionGraph graph) {
        List<ProgressionNode> all = new ArrayList<>();
        all.add(graph.centralHub());
        all.addAll(graph.worldNodes());
        all.addAll(graph.dungeonNodes());
        return all;
    }
}
