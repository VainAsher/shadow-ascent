package com.shadowascent.client;

import java.util.List;
import java.util.Locale;

final class AbilityTrigger {
    private final String id;
    private final String requiredAbility;
    private final String displayName;
    private final TriggerMode mode;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final List<String> objectiveKeywords;

    AbilityTrigger(
            String id,
            String requiredAbility,
            String displayName,
            TriggerMode mode,
            float x,
            float y,
            float width,
            float height,
            List<String> objectiveKeywords) {
        this.id = id;
        this.requiredAbility = requiredAbility;
        this.displayName = displayName;
        this.mode = mode;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.objectiveKeywords = objectiveKeywords == null ? List.of() : List.copyOf(objectiveKeywords);
    }

    boolean matchesObjective(String objectiveId) {
        if (objectiveId == null || objectiveId.isBlank() || objectiveKeywords.isEmpty()) {
            return false;
        }
        String normalized = objectiveId.toLowerCase(Locale.ROOT);
        for (String keyword : objectiveKeywords) {
            if (keyword != null && !keyword.isBlank() && normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    String id() { return id; }
    String requiredAbility() { return requiredAbility; }
    String displayName() { return displayName; }
    TriggerMode mode() { return mode; }
    float x() { return x; }
    float y() { return y; }
    float width() { return width; }
    float height() { return height; }
    float centerX() { return x + (width * 0.5f); }
    float centerY() { return y + (height * 0.5f); }
}
