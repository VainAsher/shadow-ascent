package com.shadowascent.core.data;

import java.util.List;
import java.util.Set;

/**
 * Canonical NPC contract loaded from data/npc_registry.json.
 */
public record NpcDefinition(
        String id,
        String displayName,
        List<String> allowedRoles,
        Set<String> eligiblePlateaus,
        String notes,
        String factionId) {
}
