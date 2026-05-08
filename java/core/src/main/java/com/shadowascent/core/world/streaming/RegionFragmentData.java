package com.shadowascent.core.world.streaming;

import java.util.List;

/**
 * Authored data from a region fragment JSON file.
 * Seeds a RegionManifest before template resolution by RegionLoader.
 */
public record RegionFragmentData(
        String regionId,
        String biome,
        String kind,
        List<SocketBinding> socketBindings,
        List<ZoneOverride> authoredZoneOverrides,
        List<AuthoredTile> tiles) {

    public RegionFragmentData {
        socketBindings = socketBindings != null ? List.copyOf(socketBindings) : List.of();
        authoredZoneOverrides = authoredZoneOverrides != null
                ? List.copyOf(authoredZoneOverrides) : List.of();
        tiles = tiles != null ? List.copyOf(tiles) : List.of();
    }

    /** Compact backward-compat constructor for tests that don't supply tiles. */
    public RegionFragmentData(
            String regionId,
            String biome,
            String kind,
            List<SocketBinding> socketBindings,
            List<ZoneOverride> authoredZoneOverrides) {
        this(regionId, biome, kind, socketBindings, authoredZoneOverrides, List.of());
    }

    /** A single authored static tile in world-space coordinates. */
    public record AuthoredTile(float x, float y, float w, float h, boolean platform) {}
}
