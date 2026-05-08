package com.shadowascent.core.world.streaming;

import java.util.List;
import java.util.Objects;

/** A resolved spawn/encounter anchor within a loaded RegionInstance. */
public record ResolvedAnchor(
        String anchorId,
        String kind,
        float worldX,
        float worldY,
        List<String> satisfiedRequires,
        boolean active) {

    public ResolvedAnchor {
        Objects.requireNonNull(anchorId, "anchorId");
        Objects.requireNonNull(kind, "kind");
        satisfiedRequires = satisfiedRequires != null ? List.copyOf(satisfiedRequires) : List.of();
    }
}
