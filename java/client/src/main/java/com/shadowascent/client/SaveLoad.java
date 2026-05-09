package com.shadowascent.client;

import com.shadowascent.core.GameState;
import com.shadowascent.core.world.streaming.MutationOverlay;
import com.shadowascent.core.world.streaming.OverlayPayloadCodec;
import com.shadowascent.core.world.streaming.RegionInstance;
import com.shadowascent.core.world.streaming.ZoneOverride;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates GameState persistence and overlay codec operations for PlaytestClient.
 * Handles the pure I/O layer (save-file reads/writes + overlay encode/decode).
 * PlaytestClient retains post-load refresh callbacks and mutable region state.
 */
final class SaveLoad {

    record LoadResult(Map<String, List<ZoneOverride>> overlays) {}

    private final GameState gameState;
    private final Path      savePath;

    SaveLoad(GameState gameState, Path savePath) {
        this.gameState = gameState;
        this.savePath  = savePath;
    }

    /** Encodes the current active-region overlay state to Base64 for the save envelope. */
    String buildOverlaysB64(List<RegionInstance> activeRegions) throws Exception {
        MutationOverlay overlay = new MutationOverlay();
        return OverlayPayloadCodec.encodeToB64(overlay.extractSaveState(activeRegions));
    }

    /** Writes the save envelope (story state + overlaysB64) to disk. */
    void save(String overlaysB64) throws Exception {
        gameState.save(savePath, overlaysB64);
    }

    /**
     * Reads the save envelope from disk and decodes the overlay payload.
     * Caller is responsible for applying the returned overlays and triggering post-load refresh.
     */
    LoadResult load() throws Exception {
        gameState.load(savePath);
        String overlaysB64 = gameState.loadOverlaysB64(savePath);
        Map<String, List<ZoneOverride>> overlays = OverlayPayloadCodec.decodeFromB64(overlaysB64);
        return new LoadResult(overlays);
    }

    Path savePath() { return savePath; }
}
