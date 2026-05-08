package com.shadowascent.core.world.streaming;

import java.util.Map;
import java.util.Objects;

/**
 * Active mutation applied to a named MutableZone within a region.
 * zoneRole addresses zones by role (not position) so save-state survives template revisions.
 */
public record ZoneOverride(String zoneRole, String overlayKind, Map<String, String> params) {
    public ZoneOverride {
        Objects.requireNonNull(zoneRole, "zoneRole");
        Objects.requireNonNull(overlayKind, "overlayKind");
        params = params != null ? Map.copyOf(params) : Map.of();
    }
}
