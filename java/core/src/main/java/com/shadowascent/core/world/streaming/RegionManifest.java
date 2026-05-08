package com.shadowascent.core.world.streaming;

import java.util.List;
import java.util.Objects;

/**
 * Serializable description of a resolved region.
 * Stable round-trip contract for save/load — zoneOverrides reference zones by role,
 * not position, so template revisions don't break saved overlay state.
 */
public record RegionManifest(
        String regionId,
        String selectedTemplateId,
        String biome,
        String kind,
        long seed,
        List<SocketBinding> sockets,
        List<ZoneOverride> zoneOverrides) {

    public RegionManifest {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(biome, "biome");
        Objects.requireNonNull(kind, "kind");
        sockets = sockets != null ? List.copyOf(sockets) : List.of();
        zoneOverrides = zoneOverrides != null ? List.copyOf(zoneOverrides) : List.of();
    }
}
