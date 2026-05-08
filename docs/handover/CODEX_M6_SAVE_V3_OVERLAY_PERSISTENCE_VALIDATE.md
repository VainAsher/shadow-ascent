---
handover_type: codex_validate
milestone: M6
topic: save_v3_overlay_persistence
status: validated
created: 2026-05-08
---
# Codex Validate — M6 SAVE_V3 Overlay Persistence

Gate evidence for the SAVE_V3 envelope promotion and region overlay round-trip persistence.

---

## Gate Result

```
BUILD SUCCESSFUL in 45s
[PASS] All regression tests PASSED  (37/37)
```

Full command:
```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava
             runDataContractDiagnostics runWorldgenDiagnostics
             runWorldSimulationDiagnostics runRegressionTests
```

---

## Files Created

| File | Package | Lines | Purpose |
|---|---|---|---|
| `OverlayPayloadCodec.java` | `core.world.streaming` | ~70 | Tab-delimited B64 encode/decode for `Map<String, List<ZoneOverride>>` |

---

## Files Modified

| File | Change |
|---|---|
| `SaveMigrationMatrix.java` | `CURRENT_VERSION=3`; V2 migratable; `parseV3Envelope`; `encodeCurrentWithOverlays`; `decodeOverlaysB64`; `ParsedEnvelope` gains `overlaysB64` |
| `GameState.java` | `save(Path, String overlaysB64)` overload; `loadOverlaysB64(Path)` accessor |
| `PlaytestClient.java` | `saveState` extracts + encodes overlays via `MutationOverlay`/`OverlayPayloadCodec`; `loadState` decodes + restores `savedOverlays` + reloads neighborhood |
| `RegressionTest.java` | `testVersionedAndLegacySaveCompatibility` updated for V3 header + V2 migration path; `testForwardSaveVersionHandling` updated (V4 throws, V3 loads); `testSaveV3OverlayPersistence` added |

---

## Regression Sections

### `testVersionedAndLegacySaveCompatibility` — [PASS] (updated)
- `GameState.save()` now writes `SAVE_V3|` header with `region_overlays_b64=` field
- V2 saves (`SAVE_V2|story_state_b64=...|encoding=utf8_base64`) load cleanly via v2→v3 migrator
- V1 saves still load via migrator
- Legacy (v0) saves still load via migrator

### `testForwardSaveVersionHandling` — [PASS] (updated)
- `SAVE_V3` with valid fields loads successfully (was previously the failing "unsupported" case)
- `SAVE_V4` throws `IOException("Unsupported save version")`

### `testSaveV3OverlayPersistence` — [PASS] (new)
- Apply two `ZoneOverride`s (`CORRUPTION_SURGE`, `PROSPERITY_CRISIS`) to a `RegionInstance`
- `OverlayPayloadCodec.encodeToB64` produces non-empty B64
- `GameState.save(path, overlaysB64)` writes `SAVE_V3` envelope with `region_overlays_b64` field
- `GameState.load` restores story state flag correctly
- `GameState.loadOverlaysB64` returns non-empty B64
- `OverlayPayloadCodec.decodeFromB64` round-trips region ID, both overlay kinds
- Empty overlay map → empty B64 → empty map (round-trip stable)

---

## Diagnostics

```
save_migration_matrix=v0:legacy_raw_state -> migrate to v3 envelope,
                      v1:native_v1_envelope -> migrate to v3 envelope,
                      v2:native_v2_envelope -> migrate to v3 envelope,
                      v3:native_v3_envelope
```

---

## Prior Test Count

36 tests prior. Now 37/37.
