package com.shadowascent.core.data;

import java.util.List;
import java.util.Set;

/**
 * Contract-backed dialogue line used for runtime NPC dialogue selection.
 */
public record DialogueLineDefinition(
        String id,
        String speakerId,
        String text,
        String plateau,
        String lineRole,
        List<String> beatRefs,
        List<String> tags,
        Set<String> requiredFlags,
        Set<String> forbiddenFlags,
        Set<String> requiredAbilities,
        Set<String> forbiddenAbilities,
        Set<String> requiredHubStates) {
}

