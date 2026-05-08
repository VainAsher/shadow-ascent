package com.shadowascent.core.world.streaming;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encodes and decodes Map<String, List<ZoneOverride>> to/from a Base64 string
 * suitable for embedding in the SAVE_V3 region_overlays_b64 field.
 *
 * Format (UTF-8 text, then Base64-URL-no-pad encoded):
 *   One line per override: <regionId>\t<zoneRole>\t<overlayKind>
 *   ZoneOverride.properties is always empty in current usage; deserialized as Map.of().
 */
public final class OverlayPayloadCodec {

    private OverlayPayloadCodec() {
    }

    public static String encodeToB64(Map<String, List<ZoneOverride>> overlays) {
        if (overlays == null || overlays.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<ZoneOverride>> entry : overlays.entrySet()) {
            String regionId = entry.getKey();
            for (ZoneOverride zo : entry.getValue()) {
                sb.append(regionId)
                        .append('\t')
                        .append(zo.zoneRole())
                        .append('\t')
                        .append(zo.overlayKind())
                        .append('\n');
            }
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static Map<String, List<ZoneOverride>> decodeFromB64(String b64) {
        Map<String, List<ZoneOverride>> result = new HashMap<>();
        if (b64 == null || b64.isBlank()) {
            return result;
        }
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(b64);
            String text = new String(bytes, StandardCharsets.UTF_8);
            for (String line : text.split("\n")) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\t", 3);
                if (parts.length < 3) {
                    continue;
                }
                String regionId = parts[0].trim();
                String zoneRole = parts[1].trim();
                String overlayKind = parts[2].trim();
                if (regionId.isEmpty() || zoneRole.isEmpty() || overlayKind.isEmpty()) {
                    continue;
                }
                result.computeIfAbsent(regionId, k -> new ArrayList<>())
                        .add(new ZoneOverride(zoneRole, overlayKind, Map.of()));
            }
        } catch (IllegalArgumentException ignored) {
            // malformed b64 → return empty
        }
        return result;
    }
}
