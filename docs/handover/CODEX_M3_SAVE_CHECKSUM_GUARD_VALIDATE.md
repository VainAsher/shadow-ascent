---
handover_type: codex_validate
milestone: M3
topic: save_checksum_guard
status: done
created: 2026-05-08
gate_result: PASS
test_count: 43
---
# Codex Validate — M3 Save-State Integrity Checksum Guard

## Gate Command

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Result

```
--- Testing M3 Save Checksum Guard ---
[PASS] PASSED

=== Test Results ===
[PASS] All regression tests PASSED
[READY] Release candidate is stable and ready

BUILD SUCCESSFUL in 45s
```

## Files Modified

| File | Change |
|---|---|
| `core/SaveMigrationMatrix.java` | `buildV3Envelope` appends `checksum_sha256=<64hex>` field; `parseV3Envelope` extracts and verifies; `sha256Hex` helper added |
| `core/RegressionTest.java` | `testSaveChecksumGuard` section — 4/4 sub-tests |

## Envelope Format (V3 with checksum)

```
SAVE_V3|story_state_b64=<b64>|region_overlays_b64=<b64>|checksum_sha256=<64hex>|encoding=utf8_base64
```

## Test Coverage

1. Checksum field written — `checksum_sha256=` present, value is 64-char lowercase hex
2. Valid round-trip — encode → decode → payload identity
3. Tampered story_state_b64 → `IOException("Save checksum mismatch...")`
4. V3 without checksum field (legacy) → loads silently (backward compat)

## Prior test count: 42 → Current: 43/43
