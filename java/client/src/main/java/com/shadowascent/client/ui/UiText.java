package com.shadowascent.client.ui;

import com.shadowascent.core.simulation.ItemDatabase;

import java.util.Locale;

public final class UiText {
    private static final String DEFAULT_MISSION_TITLE = "No active mission";
    private static final String DEFAULT_OBJECTIVE = "Explore east through traversal rooms.";
    private static final String DEFAULT_HINT = "Traverse east, test movement, and watch the feed.";

    private UiText() {
    }

    public static String humanizeToken(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        String normalized = value.replace('-', ' ').replace('_', ' ').trim().toLowerCase(Locale.ROOT);
        String[] parts = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    public static String missionTitle(String missionTitle) {
        return hasText(missionTitle) ? missionTitle.trim() : DEFAULT_MISSION_TITLE;
    }

    public static String objectiveLine(String objectiveLine) {
        return hasText(objectiveLine) ? objectiveLine.trim() : DEFAULT_OBJECTIVE;
    }

    public static String contextualHint(String hint) {
        return hasText(hint) ? hint.trim() : DEFAULT_HINT;
    }

    public static String overlayStatus(OverlayType overlayType) {
        return overlayType == null
                ? "Overlay: none"
                : "Overlay: " + humanizeToken(overlayType.name());
    }

    public static String healthLine(int health, int maxHealth) {
        return "Health: " + Math.max(0, health) + "/" + Math.max(1, maxHealth);
    }

    public static String itemName(String itemId) {
        if (!hasText(itemId)) {
            return "Unknown item";
        }
        ItemDatabase.ItemDef def = ItemDatabase.get(itemId);
        return def == null ? humanizeToken(itemId) : def.name();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
