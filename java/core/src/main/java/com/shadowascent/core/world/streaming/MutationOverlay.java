package com.shadowascent.core.world.streaming;

import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.physics.TileType;
import com.shadowascent.core.simulation.WorldSimulationPressureSample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Applies WorldSimulationPressureSample signals to a RegionInstance,
 * producing and persisting ZoneOverride records.
 *
 * Overlay kind vocabulary matches QuestOpportunity.OpportunityType.
 */
public final class MutationOverlay {

    private static final double CORRUPTION_SURGE_THRESHOLD = 0.65;
    private static final double PROSPERITY_CRISIS_THRESHOLD = 0.35;
    private static final double ROUTE_HAZARD_THRESHOLD = 0.65;
    private static final double FACTION_CONFLICT_THRESHOLD = 0.65;
    private static final double STABILITY_WINDOW_MAX = 0.30;

    private static final int TILE_UNIT = 96;

    /**
     * Derive ZoneOverride records for a region from a pressure sample.
     * Does not modify the RegionInstance — call apply() to commit.
     */
    public List<ZoneOverride> compute(
            RegionManifest manifest,
            WorldSimulationPressureSample sample) {
        List<ZoneOverride> overrides = new ArrayList<>();
        if (manifest == null || sample == null) {
            return overrides;
        }

        if (sample.corruptionPressure() >= CORRUPTION_SURGE_THRESHOLD) {
            overrides.add(new ZoneOverride("traversal_main", "CORRUPTION_SURGE",
                    Map.of("pressure", String.valueOf(sample.corruptionPressure()))));
        }
        if (sample.prosperityPressure() <= PROSPERITY_CRISIS_THRESHOLD) {
            overrides.add(new ZoneOverride("service_zone", "PROSPERITY_CRISIS",
                    Map.of("pressure", String.valueOf(sample.prosperityPressure()))));
        }
        if (sample.routeRisk() >= ROUTE_HAZARD_THRESHOLD) {
            overrides.add(new ZoneOverride("traversal_secondary", "ROUTE_HAZARD",
                    Map.of("risk", String.valueOf(sample.routeRisk()))));
        }
        if (sample.factionTension() >= FACTION_CONFLICT_THRESHOLD) {
            overrides.add(new ZoneOverride("encounter_zone", "FACTION_CONFLICT",
                    Map.of("tension", String.valueOf(sample.factionTension()))));
        }
        if (sample.corruptionPressure() <= STABILITY_WINDOW_MAX
                && sample.routeRisk() <= STABILITY_WINDOW_MAX
                && sample.factionTension() <= STABILITY_WINDOW_MAX) {
            overrides.add(new ZoneOverride("shortcut_zone", "STABILITY_WINDOW", Map.of()));
        }

        return overrides;
    }

    /**
     * Apply derived overrides to a live RegionInstance.
     * Rebuilds overlayTiles and spatialHash; updates anchor active states.
     */
    public void apply(RegionInstance instance, List<ZoneOverride> overrides) {
        if (instance == null || overrides == null || overrides.isEmpty()) {
            return;
        }

        List<TileRect> newOverlayTiles = new ArrayList<>();
        List<ResolvedAnchor> updatedAnchors = new ArrayList<>(instance.anchors());

        for (ZoneOverride override : overrides) {
            switch (override.overlayKind()) {
                case "CORRUPTION_SURGE" -> newOverlayTiles.addAll(
                        buildBlockerTiles(instance, override.zoneRole()));
                case "ROUTE_HAZARD" -> newOverlayTiles.addAll(
                        buildHazardTiles(instance, override.zoneRole()));
                case "PROSPERITY_CRISIS" -> deactivateServiceAnchors(updatedAnchors);
                case "FACTION_CONFLICT" -> activateBarrierAnchors(updatedAnchors);
                case "STABILITY_WINDOW" -> clearBlockerTiles(newOverlayTiles);
                default -> { /* unknown overlay kind — ignore */ }
            }
        }

        instance.setOverlayTiles(newOverlayTiles, overrides);
        instance.setAnchors(updatedAnchors);
    }

    /**
     * Extract current overlay state from a list of instances for serialization into a save envelope.
     * Returns regionId → List<ZoneOverride> for all instances that have active overrides.
     */
    public Map<String, List<ZoneOverride>> extractSaveState(List<RegionInstance> instances) {
        Map<String, List<ZoneOverride>> out = new HashMap<>();
        if (instances == null) {
            return out;
        }
        for (RegionInstance instance : instances) {
            List<ZoneOverride> overrides = instance.appliedOverrides();
            if (!overrides.isEmpty()) {
                out.put(instance.regionId(), List.copyOf(overrides));
            }
        }
        return Map.copyOf(out);
    }

    // --- Tile generation helpers ---

    private List<TileRect> buildBlockerTiles(RegionInstance instance, String zoneRole) {
        // Generate DOOR_LOCKED tiles covering the zone's footprint area
        List<TileRect> tiles = new ArrayList<>();
        float zoneX = 0f;
        float zoneY = 0f;
        float zoneW = TILE_UNIT;
        float zoneH = TILE_UNIT;
        // Derive a rough zone position from static tile bounds as a placeholder
        if (!instance.staticTiles().isEmpty()) {
            TileRect floor = instance.staticTiles().get(0);
            zoneX = floor.x() + floor.w() * 0.25f;
            zoneY = floor.y() - floor.h();
            zoneW = floor.w() * 0.5f;
            zoneH = floor.h() * 2f;
        }
        tiles.add(new TileRect(zoneX, zoneY, zoneW, zoneH, false, TileType.DOOR_LOCKED.id));
        return tiles;
    }

    private List<TileRect> buildHazardTiles(RegionInstance instance, String zoneRole) {
        // Generate solid hazard tiles in the traversal secondary zone
        List<TileRect> tiles = new ArrayList<>();
        if (!instance.staticTiles().isEmpty()) {
            TileRect floor = instance.staticTiles().get(0);
            float hazardX = floor.x() + floor.w() * 0.6f;
            float hazardY = floor.y() - 8f;
            tiles.add(new TileRect(hazardX, hazardY, 16f, 8f, false));
        }
        return tiles;
    }

    private void deactivateServiceAnchors(List<ResolvedAnchor> anchors) {
        for (int i = 0; i < anchors.size(); i++) {
            ResolvedAnchor a = anchors.get(i);
            if (a.kind().equals("service") && a.active()) {
                anchors.set(i, new ResolvedAnchor(
                        a.anchorId(), a.kind(), a.worldX(), a.worldY(), a.satisfiedRequires(), false));
            }
        }
    }

    private void activateBarrierAnchors(List<ResolvedAnchor> anchors) {
        for (int i = 0; i < anchors.size(); i++) {
            ResolvedAnchor a = anchors.get(i);
            if (a.kind().equals("locked_door") && !a.active()) {
                anchors.set(i, new ResolvedAnchor(
                        a.anchorId(), a.kind(), a.worldX(), a.worldY(), a.satisfiedRequires(), true));
            }
        }
    }

    private void clearBlockerTiles(List<TileRect> overlayTiles) {
        overlayTiles.removeIf(t -> t.tileTypeEnum() == TileType.DOOR_LOCKED);
    }

}

