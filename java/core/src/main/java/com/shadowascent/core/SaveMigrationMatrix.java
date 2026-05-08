package com.shadowascent.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Versioned save envelope encode/decode policy.
 *
 * Matrix:
 * - v0: legacy raw StoryState serialization (no header) -> migrated to current runtime payload.
 * - v1: legacy envelope: SAVE_V1|story_state=... -> migrated through v3 codec.
 * - v2: previous envelope: SAVE_V2|story_state_b64=...|encoding=utf8_base64 -> migrated to v3.
 * - v3: current envelope: SAVE_V3|story_state_b64=...|region_overlays_b64=...|encoding=utf8_base64
 */
final class SaveMigrationMatrix {
    static final int LEGACY_VERSION = 0;
    static final int SAVE_V1_VERSION = 1;
    static final int SAVE_V2_VERSION = 2;
    static final int CURRENT_VERSION = 3;

    private static final String ENVELOPE_PREFIX = "SAVE_V";
    private static final String STORY_STATE_FIELD_V1 = "story_state=";
    private static final String STORY_STATE_FIELD = "story_state_b64=";
    private static final String REGION_OVERLAYS_FIELD = "region_overlays_b64=";
    private static final String CHECKSUM_FIELD = "checksum_sha256=";
    private static final String ENCODING_FIELD = "encoding=";
    private static final String CURRENT_ENCODING = "utf8_base64";

    private static final Map<Integer, String> MIGRATION_MATRIX;
    static {
        Map<Integer, String> matrix = new LinkedHashMap<>();
        matrix.put(LEGACY_VERSION, "legacy_raw_state -> migrate to v3 envelope");
        matrix.put(SAVE_V1_VERSION, "native_v1_envelope -> migrate to v3 envelope");
        matrix.put(SAVE_V2_VERSION, "native_v2_envelope -> migrate to v3 envelope");
        matrix.put(CURRENT_VERSION, "native_v3_envelope");
        MIGRATION_MATRIX = Collections.unmodifiableMap(new LinkedHashMap<>(matrix));
    }

    private SaveMigrationMatrix() {
    }

    static String encodeCurrent(String storyStatePayload) {
        return encodeCurrentWithOverlays(storyStatePayload, "");
    }

    static String encodeCurrentWithOverlays(String storyStatePayload, String overlaysB64) {
        return buildV3Envelope(storyStatePayload, overlaysB64 != null ? overlaysB64 : "");
    }

    static String decodeToCurrentStoryState(String rawContent) throws IOException {
        ParsedEnvelope envelope = parseEnvelope(rawContent == null ? "" : rawContent);
        return migrateToCurrentStoryPayload(envelope);
    }

    static String decodeOverlaysB64(String rawContent) throws IOException {
        ParsedEnvelope envelope = parseEnvelope(rawContent == null ? "" : rawContent);
        return envelope.overlaysB64();
    }

    static String migrationMatrixSummary() {
        StringBuilder out = new StringBuilder();
        out.append("save_migration_matrix=");
        boolean first = true;
        for (Map.Entry<Integer, String> entry : MIGRATION_MATRIX.entrySet()) {
            if (!first) {
                out.append(", ");
            }
            out.append("v").append(entry.getKey()).append(":").append(entry.getValue());
            first = false;
        }
        return out.toString();
    }

    private static String migrateToCurrentStoryPayload(ParsedEnvelope envelope) throws IOException {
        return switch (envelope.version()) {
            case LEGACY_VERSION -> migrateLegacyStoryPayload(envelope.storyStatePayload());
            case SAVE_V1_VERSION -> migrateV1StoryPayload(envelope.storyStatePayload());
            case SAVE_V2_VERSION -> envelope.storyStatePayload();
            case CURRENT_VERSION -> envelope.storyStatePayload();
            default -> throw unsupportedVersion(envelope.version());
        };
    }

    private static ParsedEnvelope parseEnvelope(String raw) throws IOException {
        if (!raw.startsWith(ENVELOPE_PREFIX)) {
            return new ParsedEnvelope(LEGACY_VERSION, raw, "");
        }

        int firstSeparator = raw.indexOf('|');
        if (firstSeparator < 0) {
            throw new IOException("Invalid save envelope: missing field separator `|`");
        }

        String versionToken = raw.substring(ENVELOPE_PREFIX.length(), firstSeparator).trim();
        int version;
        try {
            version = Integer.parseInt(versionToken);
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid save envelope version token: `" + versionToken + "`", ex);
        }

        String fields = raw.substring(firstSeparator + 1);
        return switch (version) {
            case SAVE_V1_VERSION -> parseV1Envelope(fields);
            case SAVE_V2_VERSION -> parseV2Envelope(fields);
            case CURRENT_VERSION -> parseV3Envelope(fields);
            default -> throw unsupportedVersion(version);
        };
    }

    private static ParsedEnvelope parseV1Envelope(String fields) throws IOException {
        if (!fields.startsWith(STORY_STATE_FIELD_V1)) {
            throw new IOException("Invalid v1 save envelope: expected `" + STORY_STATE_FIELD_V1 + "` field");
        }
        String payload = fields.substring(STORY_STATE_FIELD_V1.length());
        return new ParsedEnvelope(SAVE_V1_VERSION, payload, "");
    }

    private static ParsedEnvelope parseV2Envelope(String fields) throws IOException {
        String encodedPayload = null;
        String encoding = CURRENT_ENCODING;
        for (String token : fields.split("\\|")) {
            if (token.startsWith(STORY_STATE_FIELD)) {
                encodedPayload = token.substring(STORY_STATE_FIELD.length());
            } else if (token.startsWith(ENCODING_FIELD)) {
                encoding = token.substring(ENCODING_FIELD.length()).trim();
            }
        }
        if (encodedPayload == null) {
            throw new IOException("Invalid v2 save envelope: expected `" + STORY_STATE_FIELD + "` field");
        }
        if (!CURRENT_ENCODING.equalsIgnoreCase(encoding)) {
            throw new IOException("Unsupported v2 save encoding `" + encoding + "`");
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);
            return new ParsedEnvelope(SAVE_V2_VERSION,
                    new String(decoded, StandardCharsets.UTF_8), "");
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid v2 save payload encoding", ex);
        }
    }

    private static ParsedEnvelope parseV3Envelope(String fields) throws IOException {
        String encodedStory = null;
        String encodedOverlays = "";
        String encoding = CURRENT_ENCODING;
        String parsedChecksum = null;
        for (String token : fields.split("\\|")) {
            if (token.startsWith(STORY_STATE_FIELD)) {
                encodedStory = token.substring(STORY_STATE_FIELD.length());
            } else if (token.startsWith(REGION_OVERLAYS_FIELD)) {
                encodedOverlays = token.substring(REGION_OVERLAYS_FIELD.length());
            } else if (token.startsWith(CHECKSUM_FIELD)) {
                parsedChecksum = token.substring(CHECKSUM_FIELD.length()).trim();
            } else if (token.startsWith(ENCODING_FIELD)) {
                encoding = token.substring(ENCODING_FIELD.length()).trim();
            }
        }
        if (encodedStory == null) {
            throw new IOException("Invalid v3 save envelope: expected `" + STORY_STATE_FIELD + "` field");
        }
        if (!CURRENT_ENCODING.equalsIgnoreCase(encoding)) {
            throw new IOException("Unsupported v3 save encoding `" + encoding + "`");
        }
        if (parsedChecksum != null && !parsedChecksum.isBlank()) {
            String expected = sha256Hex((encodedStory + "|" + encodedOverlays)
                    .getBytes(StandardCharsets.UTF_8));
            if (!expected.equals(parsedChecksum)) {
                throw new IOException("Save checksum mismatch: expected " + expected
                        + ", found " + parsedChecksum + ". Save file may be corrupted.");
            }
        }
        try {
            byte[] decodedStory = Base64.getUrlDecoder().decode(encodedStory);
            return new ParsedEnvelope(CURRENT_VERSION,
                    new String(decodedStory, StandardCharsets.UTF_8),
                    encodedOverlays);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid v3 save payload encoding", ex);
        }
    }

    private static String migrateLegacyStoryPayload(String payload) throws IOException {
        return decodeStoryFromV3Envelope(buildV3Envelope(payload, ""));
    }

    private static String migrateV1StoryPayload(String payload) throws IOException {
        return decodeStoryFromV3Envelope(buildV3Envelope(payload, ""));
    }

    private static String buildV3Envelope(String storyStatePayload, String overlaysB64) {
        String story = storyStatePayload != null ? storyStatePayload : "";
        String overlays = overlaysB64 != null ? overlaysB64 : "";
        String encodedStory = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(story.getBytes(StandardCharsets.UTF_8));
        String checksum = sha256Hex((encodedStory + "|" + overlays).getBytes(StandardCharsets.UTF_8));
        return ENVELOPE_PREFIX + CURRENT_VERSION + "|"
                + STORY_STATE_FIELD + encodedStory + "|"
                + REGION_OVERLAYS_FIELD + overlays + "|"
                + CHECKSUM_FIELD + checksum + "|"
                + ENCODING_FIELD + CURRENT_ENCODING;
    }

    private static String sha256Hex(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String decodeStoryFromV3Envelope(String envelope) throws IOException {
        ParsedEnvelope parsed = parseEnvelope(envelope);
        if (parsed.version() != CURRENT_VERSION) {
            throw new IOException("Migration internal error: expected current v" + CURRENT_VERSION + " envelope");
        }
        return parsed.storyStatePayload();
    }

    private static IOException unsupportedVersion(int version) {
        String message = "Unsupported save version: v" + version
                + ". Supported: v0 legacy, v1/v2 migratable, v3 current. "
                + migrationMatrixSummary();
        return new IOException(message);
    }

    private record ParsedEnvelope(int version, String storyStatePayload, String overlaysB64) {
    }
}
