package com.shadowascent.core.world.streaming;

import com.shadowascent.core.physics.SpatialHash;
import com.shadowascent.core.physics.TileRect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Runtime representation of a loaded region.
 * Owns the resolved spatial hash and active anchor list for this region.
 * overlayTiles and spatialHash are rebuilt by MutationOverlay.apply().
 */
public final class RegionInstance {

    private static final int TILE_UNIT = 96;
    private static final int FLOOR_H = 16;

    private final String regionId;
    private final RegionManifest manifest;
    private final List<TileRect> staticTiles;
    private final List<ResolvedAnchor> anchors;
    private List<TileRect> overlayTiles;
    private List<ZoneOverride> appliedOverrides;
    private SpatialHash spatialHash;
    private boolean loaded;

    public RegionInstance(
            RegionManifest manifest,
            SpatialHash spatialHash,
            List<TileRect> staticTiles,
            List<TileRect> overlayTiles,
            List<ResolvedAnchor> anchors) {
        Objects.requireNonNull(manifest, "manifest");
        this.regionId = manifest.regionId();
        this.manifest = manifest;
        this.spatialHash = spatialHash != null ? spatialHash : new SpatialHash();
        this.staticTiles = staticTiles != null ? new ArrayList<>(staticTiles) : new ArrayList<>();
        this.overlayTiles = overlayTiles != null ? new ArrayList<>(overlayTiles) : new ArrayList<>();
        this.appliedOverrides = new ArrayList<>();
        this.anchors = anchors != null ? new ArrayList<>(anchors) : new ArrayList<>();
        this.loaded = true;
    }

    public String regionId() {
        return regionId;
    }

    public RegionManifest manifest() {
        return manifest;
    }

    public SpatialHash spatialHash() {
        return spatialHash;
    }

    public List<TileRect> staticTiles() {
        return List.copyOf(staticTiles);
    }

    public List<TileRect> overlayTiles() {
        return List.copyOf(overlayTiles);
    }

    public List<ResolvedAnchor> anchors() {
        return List.copyOf(anchors);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public List<ZoneOverride> appliedOverrides() {
        return List.copyOf(appliedOverrides);
    }

    /** Replaces overlay tiles, records the applied overrides, and rebuilds the spatial hash. */
    void setOverlayTiles(List<TileRect> newOverlayTiles, List<ZoneOverride> overrides) {
        this.overlayTiles = new ArrayList<>(newOverlayTiles);
        this.appliedOverrides = overrides != null ? new ArrayList<>(overrides) : new ArrayList<>();
        rebuildSpatialHash();
    }

    /** Replaces the anchor list with updated active states. Called by MutationOverlay.apply(). */
    void setAnchors(List<ResolvedAnchor> newAnchors) {
        this.anchors.clear();
        this.anchors.addAll(newAnchors);
    }

    private void rebuildSpatialHash() {
        SpatialHash rebuilt = new SpatialHash();
        for (TileRect tile : staticTiles) {
            rebuilt.insert(tile);
        }
        for (TileRect tile : overlayTiles) {
            rebuilt.insert(tile);
        }
        this.spatialHash = rebuilt;
    }

    /** Generate a minimal floor tile from a grid footprint for placeholder geometry. */
    public static List<TileRect> generateStubTiles(int gridW, int gridH) {
        List<TileRect> tiles = new ArrayList<>();
        float w = gridW * (float) TILE_UNIT;
        float h = gridH * (float) TILE_UNIT;
        tiles.add(new TileRect(0f, h - FLOOR_H, w, FLOOR_H, false));
        tiles.add(new TileRect(0f, 0f, FLOOR_H, h, false));
        tiles.add(new TileRect(w - FLOOR_H, 0f, FLOOR_H, h, false));
        return tiles;
    }
}
