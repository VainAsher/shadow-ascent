package com.shadowascent.core.data;

import java.util.List;

/**
 * Canonical narrative beat contract loaded from data/narrative_beats.json.
 */
public record BeatDefinition(
        String id,
        String plateau,
        int routeOrder,
        String beatType,
        String title,
        String areaId,
        String objectivePrompt,
        List<String> requiredFlags,
        List<String> setFlags,
        List<String> npcIds,
        List<String> dialogueRefs) {

    public boolean isCriticalPathBeat() {
        return switch (normalize(beatType)) {
            case "authored_critical", "authored_milestone", "transition", "cinematic" -> true;
            default -> false;
        };
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
