package com.shadowascent.core.data;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Concrete typed reward effect parsed from explicit quest reward effect fields.
 */
public record QuestRewardEffectDefinition(
        QuestRewardEffectType type,
        int magnitude,
        Map<String, String> payload) {

    public QuestRewardEffectDefinition {
        type = Objects.requireNonNull(type, "type");
        magnitude = Math.max(1, magnitude);
        payload = normalizePayload(payload);
    }

    public String effectId() {
        String payloadId = payload.get("id");
        if (payloadId != null && !payloadId.isBlank()) {
            return payloadId;
        }
        return type.name().toLowerCase(Locale.ROOT);
    }

    private static Map<String, String> normalizePayload(Map<String, String> payload) {
        if (payload == null || payload.isEmpty()) {
            return Map.of();
        }
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String key = entry.getKey().trim();
            if (key.isEmpty()) {
                continue;
            }
            String value = entry.getValue() == null ? "" : entry.getValue().trim();
            if (!value.isEmpty()) {
                normalized.put(key, value);
            }
        }
        return Map.copyOf(normalized);
    }
}
