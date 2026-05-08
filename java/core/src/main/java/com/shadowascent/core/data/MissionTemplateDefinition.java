package com.shadowascent.core.data;

import java.util.List;
import java.util.Map;

/**
 * Runtime mission template loaded from data contracts.
 */
public record MissionTemplateDefinition(
        String id,
        String displayName,
        String description,
        String region,
        List<String> objectives,
        Map<String, Integer> requiredCounts) {
}
