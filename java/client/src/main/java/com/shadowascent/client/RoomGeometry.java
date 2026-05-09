package com.shadowascent.client;

/**
 * Static room-boundary constants and region-lookup logic for the four-room Act I traversal layout.
 * Single source of truth for room X-ranges and Y-bounds; referenced by PlaytestClient and any
 * future client that needs the authored layout.
 */
final class RoomGeometry {

    // ── World Y-bounds ──────────────────────────────────────────────────────────
    static final float FLOOR_Y       = 670f;   // WINDOW_HEIGHT(720) - PLAYER_MARGIN_Y_BOTTOM(50)
    static final float CEILING_Y     = 84f;    // PLAYER_MARGIN_Y_TOP(70) + PLAYER_RADIUS(14)

    // ── World X-bounds ──────────────────────────────────────────────────────────
    static final float WORLD_LEFT_X  = 50f;    // PLAYER_MARGIN_X
    static final float WORLD_WIDTH   = 3450f;
    static final float WORLD_RIGHT_X = WORLD_LEFT_X + WORLD_WIDTH;

    // ── Room boundary X-coordinates (right edge of each room) ───────────────────
    static final float HUB_ROOM_END_X    = WORLD_LEFT_X + 860f;
    static final float FORGE_ROOM_END_X  = HUB_ROOM_END_X  + 860f;
    static final float SHAFT_ROOM_END_X  = FORGE_ROOM_END_X + 860f;
    static final float SUMMIT_ROOM_END_X = WORLD_RIGHT_X;

    // ── Notable spawn / NPC X-positions ────────────────────────────────────────
    static final float PLAYER_SPAWN_X = WORLD_LEFT_X + 180f;
    static final float SHOP_NPC_X     = WORLD_LEFT_X + 300f;

    private RoomGeometry() {}

    /**
     * Returns the authored section-template ID for the room containing world-X {@code x}.
     * Mirrors the authored chunk-grammar assignment for the four Act I rooms.
     */
    static String resolveRegionIdForX(float x) {
        if (x <= HUB_ROOM_END_X)   return "hub_lantern_heights";
        if (x <= FORGE_ROOM_END_X)  return "dungeon_forge_terrace_a";
        return "region_hollow_shaft";
    }
}
