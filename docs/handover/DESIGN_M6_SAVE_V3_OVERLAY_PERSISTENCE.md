---
handover_type: design
milestone: M6
topic: save_v3_overlay_persistence
status: in_progress
created: 2026-05-08
---
# Design — M6 SAVE_V3 Overlay Persistence

Promotes the save envelope from V2 to V3, adding a `region_overlays_b64` field that
persists mutation overlay state across sessions. Closes the final M6 code gap.

---

## Problem Statement

`savedOverlays` (`Map<String, List<ZoneOverride>>`) exists on `PlaytestClient` and is passed
to `RegionLoader.loadNeighborhood`, but it is never populated on save and never restored on
load. Mutation overlays applied during a session are silently lost on every reload. The
SAVE_V3 scaffolding in `SaveMigrationMatrix` has always rejected V3 saves with a
"scaffolding prepared, migrator pending" error.

---

## New File

| File | Package | Purpose |
|---|---|---|
| `OverlayPayloadCodec.java` | `core.world.streaming` | Encode/decode `Map<String, List<ZoneOverride>>` to/from Base64 |

---

## Files Modified

| File | Change |
|---|---|
| `SaveMigrationMatrix.java` | V3 promoted to current; `ParsedEnvelope` gains `overlaysB64`; `parseV3Envelope` implemented; `encodeCurrentWithOverlays` added; v2→v3 migrator added |
| `GameState.java` | `save(Path, String overlaysB64)` overload; `loadOverlaysB64(Path)` accessor |
| `PlaytestClient.java` | `saveState` encodes overlays; `loadState` restores `savedOverlays` and reloads neighborhood |
| `RegressionTest.java` | `testForwardSaveVersionHandling` updated to test V4 throws; `testSaveV3OverlayPersistence` added |

---

## V3 Envelope Format

```
SAVE_V3|story_state_b64=<base64>|region_overlays_b64=<base64>|encoding=utf8_base64
```

Fields:
- `story_state_b64` — Base64-encoded story state payload (same as V2)
- `region_overlays_b64` — Base64-encoded overlay manifest (see codec below)
- `encoding` — always `utf8_base64`

---

## OverlayPayloadCodec Format

Plain-text tab-delimited, one line per override:
```
<regionId>\t<zoneRole>\t<overlayKind>
```

Multiple overrides per region are each on their own line. Empty overlay map → empty string →
`region_overlays_b64` field is present but encodes to the empty string. `ZoneOverride.properties`
is always empty in current usage and is not serialized; deserialized as `Map.of()`.

---

## Migration Policy

| Version | On load |
|---|---|
| V0 (legacy) | Migrate story state to V2 codec; overlays = empty |
| V1 | Migrate story state to V2 codec; overlays = empty |
| V2 | Pass story state through; overlays = empty (`migrateV2ToV3`) |
| V3 | Native; both story state and overlays restored |
| V4+ | Throw `IOException("Unsupported save version")` |

`CURRENT_VERSION` advances from `2` to `3`. V2 becomes a first-class migratable version
(same treatment V1 currently receives).

---

## SaveMigrationMatrix Changes

- `CURRENT_VERSION = 3` (was 2)
- `SAVE_V2_VERSION = 2` (new named constant)
- `ParsedEnvelope` record: `(int version, String storyStatePayload, String overlaysB64)`
- `parseV3Envelope` → tokenizes fields, extracts `story_state_b64` and `region_overlays_b64`
- `encodeCurrentWithOverlays(String storyPayload, String overlaysB64)` → builds V3 envelope
- `encodeCurrent(String storyPayload)` → unchanged signature, now calls `encodeCurrentWithOverlays(story, "")`
- `decodeOverlaysB64(String rawContent)` → new, returns overlaysB64 from parsed envelope (empty for V0/V1/V2)
- `migrateToCurrentPayload` handles V2 case: story pass-through, overlays empty
- Migration matrix entry: `v2:native_v2_envelope -> migrate to v3 envelope`

---

## GameState Changes

```java
// New overload — passes overlaysB64 into V3 envelope
public void save(Path savePath, String overlaysB64) throws IOException

// New accessor — returns overlaysB64 from the save file (empty string for V2/V1/V0 saves)
public String loadOverlaysB64(Path savePath) throws IOException
```

Existing `save(Path)` unchanged; now delegates to `save(path, "")`.

---

## PlaytestClient Changes

`saveState()` additions:
1. `MutationOverlay overlay = new MutationOverlay()`
2. `Map<String, List<ZoneOverride>> overlayMap = overlay.extractSaveState(activeRegions)`
3. `String overlaysB64 = OverlayPayloadCodec.encodeToB64(overlayMap)`
4. `gameState.save(savePath, overlaysB64)` (new overload)

`loadState()` additions (after existing `gameState.load`):
1. `String overlaysB64 = gameState.loadOverlaysB64(savePath)`
2. `savedOverlays = OverlayPayloadCodec.decodeFromB64(overlaysB64)`
3. `activeRegions.clear()` → `loadNeighborhood(progressionGraph, currentRegionId, 1, WORLD_SEED, savedOverlays)`
4. `refreshCollisionHashFromRegions()`

New imports: `MutationOverlay`, `OverlayPayloadCodec`.

---

## Regression Test

### `testSaveV3OverlayPersistence` — new

1. Build a `ProgressionNode`, load a `RegionInstance` via `RegionLoader`.
2. Apply two `ZoneOverride`s via `MutationOverlay`.
3. Encode overlays with `OverlayPayloadCodec.encodeToB64`.
4. Save via `GameState.save(path, overlaysB64)` — verify file starts with `SAVE_V3`.
5. Load with `GameState.load(path)` — story state correct.
6. Load overlays with `GameState.loadOverlaysB64(path)` — returns non-empty B64.
7. Decode with `OverlayPayloadCodec.decodeFromB64` — verify round-trip matches original.
8. Verify empty overlays round-trip (B64 of empty map → decode → empty map).

### `testForwardSaveVersionHandling` — updated

Change write to `SAVE_V4|...` (was V3). Verify `IOException` containing "Unsupported save version".
Also verify `SAVE_V3` with valid fields loads successfully (not throws).

---

## Layer Contract

`OverlayPayloadCodec` imports only `java.nio.charset.*`, `java.util.*`, `java.util.Base64` — no external deps.
`SaveMigrationMatrix` unchanged layer (package-private in `com.shadowascent.core`).
`GameState` additions add no new imports.
`PlaytestClient` adds `MutationOverlay` and `OverlayPayloadCodec` imports (both `core.world.streaming`).
