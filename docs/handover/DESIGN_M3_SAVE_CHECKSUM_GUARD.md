---
handover_type: design
milestone: M3
topic: save_checksum_guard
status: in_progress
created: 2026-05-08
---
# Design — M3 Save-State Integrity Checksum Guard

Adds a SHA-256 checksum field to the SAVE_V3 envelope to detect payload integrity drift
(corruption, schema mismatches, manual edits that break the payload).

This is the last open M3 prep-lane item noted in `docs/CURRENT_STATE.md`.

---

## Scope

### In scope
- `checksum_sha256=<hex>` field written into every new SAVE_V3 envelope
- Verification on load: mismatch → `IOException` with descriptive message
- Backward compatibility: V3 saves without the field load silently (field absent = skip check)
- Regression test: `testSaveChecksumGuard` (4 sub-tests); 42→43 tests

### Out of scope
- Checksum field on V1/V2 envelopes (those migrate forward on first save)
- Key-based signing / encryption

---

## Layer contract
- `SaveMigrationMatrix` (package-private) — only change
- No client/server imports

---

## Files modified

| File | Change |
|---|---|
| `SaveMigrationMatrix.java` | `buildV3Envelope` appends `checksum_sha256=` field; `parseV3Envelope` extracts and verifies it |
| `RegressionTest.java` | New section `testSaveChecksumGuard`; dispatch entry; 42→43 tests |

---

## Checksum design

**Field name**: `checksum_sha256=`
**Input**: UTF-8 bytes of `<story_state_b64_value>|<region_overlays_b64_value>` (the two payload tokens joined with `|`)
**Algorithm**: SHA-256, lowercase hex output (64 chars)
**Position**: inserted between `region_overlays_b64=` and `encoding=`

Resulting envelope format:
```
SAVE_V3|story_state_b64=AAA|region_overlays_b64=BBB|checksum_sha256=<64hex>|encoding=utf8_base64
```

**Verification**: if field present, recompute from decoded tokens and compare. Mismatch throws:
```
IOException("Save checksum mismatch: expected <expected>, found <actual>. Save file may be corrupted.")
```

**Backward compat**: if `checksum_sha256` field absent in a V3 envelope, skip verification and load normally.

---

## `buildV3Envelope` change

```java
// compute checksum input: story_b64 + "|" + overlays_b64
String checksumInput = encodedStory + "|" + overlaysOrEmpty;
String checksum = sha256Hex(checksumInput.getBytes(StandardCharsets.UTF_8));

return ENVELOPE_PREFIX + CURRENT_VERSION + "|"
     + STORY_STATE_FIELD + encodedStory + "|"
     + REGION_OVERLAYS_FIELD + overlaysOrEmpty + "|"
     + CHECKSUM_FIELD + checksum + "|"
     + ENCODING_FIELD + CURRENT_ENCODING;
```

## `parseV3Envelope` change

```java
// In token-parsing loop, also extract:
} else if (token.startsWith(CHECKSUM_FIELD)) {
    parsedChecksum = token.substring(CHECKSUM_FIELD.length()).trim();
}

// After extracting encodedStory and encodedOverlays, before returning:
if (parsedChecksum != null && !parsedChecksum.isBlank()) {
    String expected = sha256Hex((encodedStory + "|" + encodedOverlays).getBytes(StandardCharsets.UTF_8));
    if (!expected.equals(parsedChecksum)) {
        throw new IOException("Save checksum mismatch: expected " + expected
                + ", found " + parsedChecksum + ". Save file may be corrupted.");
    }
}
```

---

## Regression tests (4 sub-tests)

### `testSaveChecksumGuard`

1. **checksum written** — `encodeCurrent(payload)` → envelope string contains `checksum_sha256=` with 64-char hex
2. **valid round-trip** — encode → decode via `decodeToCurrentStoryState` → payload matches original; no exception
3. **tampered payload rejected** — encode → replace story_state_b64 value with tampered base64 → `decodeToCurrentStoryState` throws `IOException` containing "checksum mismatch"
4. **backward compat: missing checksum loads** — manually construct a V3 envelope WITHOUT `checksum_sha256=` field → `decodeToCurrentStoryState` succeeds (returns story payload)

---

## Prior test count: 42 → Target: 43/43
