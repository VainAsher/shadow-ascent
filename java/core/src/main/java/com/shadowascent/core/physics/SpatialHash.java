package com.shadowascent.core.physics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chunk-based spatial hash for fast tile candidate lookup.
 *
 * This is a bounded donor import slice used by collision and minimap readiness work.
 */
public final class SpatialHash {
    public static final int CHUNK_SIZE = 320;

    private final Map<Long, List<TileRect>> chunks = new HashMap<>();
    private final List<TileRect> dynamicTiles = new ArrayList<>();

    private static long key(int cx, int cy) {
        return ((long) cx << 32) | (cy & 0xFFFFFFFFL);
    }

    public void clear() {
        chunks.clear();
        dynamicTiles.clear();
    }

    public void insert(TileRect rect) {
        int x0 = (int) Math.floor(rect.x() / CHUNK_SIZE);
        int x1 = (int) Math.floor((rect.x() + rect.w() - 1) / CHUNK_SIZE);
        int y0 = (int) Math.floor(rect.y() / CHUNK_SIZE);
        int y1 = (int) Math.floor((rect.y() + rect.h() - 1) / CHUNK_SIZE);
        for (int cx = x0; cx <= x1; cx++) {
            for (int cy = y0; cy <= y1; cy++) {
                chunks.computeIfAbsent(key(cx, cy), ignored -> new ArrayList<>()).add(rect);
            }
        }
    }

    public List<TileRect> candidates(float x, float y, float w, float h) {
        int x0 = (int) Math.floor(x / CHUNK_SIZE);
        int x1 = (int) Math.floor((x + w) / CHUNK_SIZE);
        int y0 = (int) Math.floor(y / CHUNK_SIZE);
        int y1 = (int) Math.floor((y + h) / CHUNK_SIZE);

        if (x0 == x1 && y0 == y1 && dynamicTiles.isEmpty()) {
            List<TileRect> chunk = chunks.get(key(x0, y0));
            return chunk != null ? chunk : Collections.emptyList();
        }

        List<TileRect> result = new ArrayList<>();
        for (int cx = x0; cx <= x1; cx++) {
            for (int cy = y0; cy <= y1; cy++) {
                List<TileRect> chunk = chunks.get(key(cx, cy));
                if (chunk != null) {
                    result.addAll(chunk);
                }
            }
        }
        result.addAll(dynamicTiles);
        return result;
    }

    public void remove(TileRect rect) {
        int x0 = (int) Math.floor(rect.x() / CHUNK_SIZE);
        int x1 = (int) Math.floor((rect.x() + rect.w() - 1) / CHUNK_SIZE);
        int y0 = (int) Math.floor(rect.y() / CHUNK_SIZE);
        int y1 = (int) Math.floor((rect.y() + rect.h() - 1) / CHUNK_SIZE);
        for (int cx = x0; cx <= x1; cx++) {
            for (int cy = y0; cy <= y1; cy++) {
                List<TileRect> chunk = chunks.get(key(cx, cy));
                if (chunk != null) {
                    chunk.remove(rect);
                }
            }
        }
    }

    public void setDynamicTiles(List<TileRect> tiles) {
        dynamicTiles.clear();
        if (tiles != null && !tiles.isEmpty()) {
            dynamicTiles.addAll(tiles);
        }
    }

    public TileRect raycast(float x0, float y0, float x1, float y1) {
        float minX = Math.min(x0, x1);
        float maxX = Math.max(x0, x1);
        float minY = Math.min(y0, y1);
        float maxY = Math.max(y0, y1);

        int cx0 = (int) Math.floor(minX / CHUNK_SIZE);
        int cx1 = (int) Math.floor(maxX / CHUNK_SIZE);
        int cy0 = (int) Math.floor(minY / CHUNK_SIZE);
        int cy1 = (int) Math.floor(maxY / CHUNK_SIZE);

        TileRect best = null;
        float bestT = Float.MAX_VALUE;

        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cy = cy0; cy <= cy1; cy++) {
                List<TileRect> chunk = chunks.get(key(cx, cy));
                if (chunk == null) {
                    continue;
                }
                for (TileRect tile : chunk) {
                    if (isPassable(tile)) {
                        continue;
                    }
                    float t = rayVsRect(x0, y0, x1, y1, tile);
                    if (t >= 0f && t < bestT) {
                        bestT = t;
                        best = tile;
                    }
                }
            }
        }

        for (TileRect tile : dynamicTiles) {
            if (isPassable(tile)) {
                continue;
            }
            float t = rayVsRect(x0, y0, x1, y1, tile);
            if (t >= 0f && t < bestT) {
                bestT = t;
                best = tile;
            }
        }
        return best;
    }

    public int size() {
        return chunks.values().stream().mapToInt(List::size).sum();
    }

    private static boolean isPassable(TileRect tile) {
        TileType type = tile.tileTypeEnum();
        return type == TileType.AIR
                || type == TileType.WATER
                || type == TileType.GAS
                || tile.isPlatform();
    }

    private static float rayVsRect(float x0, float y0, float x1, float y1, TileRect rect) {
        float dx = x1 - x0;
        float dy = y1 - y0;

        float tMinX;
        float tMaxX;
        if (dx == 0f) {
            if (x0 < rect.x() || x0 >= rect.x() + rect.w()) {
                return -1f;
            }
            tMinX = Float.NEGATIVE_INFINITY;
            tMaxX = Float.POSITIVE_INFINITY;
        } else {
            tMinX = (rect.x() - x0) / dx;
            tMaxX = (rect.x() + rect.w() - x0) / dx;
            if (tMinX > tMaxX) {
                float tmp = tMinX;
                tMinX = tMaxX;
                tMaxX = tmp;
            }
        }

        float tMinY;
        float tMaxY;
        if (dy == 0f) {
            if (y0 < rect.y() || y0 >= rect.y() + rect.h()) {
                return -1f;
            }
            tMinY = Float.NEGATIVE_INFINITY;
            tMaxY = Float.POSITIVE_INFINITY;
        } else {
            tMinY = (rect.y() - y0) / dy;
            tMaxY = (rect.y() + rect.h() - y0) / dy;
            if (tMinY > tMaxY) {
                float tmp = tMinY;
                tMinY = tMaxY;
                tMaxY = tmp;
            }
        }

        float tEntry = Math.max(tMinX, tMinY);
        float tExit = Math.min(tMaxX, tMaxY);
        if (tEntry > tExit || tExit < 0f || tEntry > 1f) {
            return -1f;
        }
        return Math.max(tEntry, 0f);
    }
}
