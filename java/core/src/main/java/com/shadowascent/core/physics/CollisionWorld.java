package com.shadowascent.core.physics;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns static tile geometry for one loaded world region and resolves
 * entity physics against it.
 *
 * Coordinate convention: entity x,y is the TOP-LEFT corner of its AABB,
 * matching how PhysicsState.x/y is used across all GameSimulator entities.
 *
 * Injected into GameSimulator via setCollisionWorld(). When null, GameSimulator
 * falls back to the spawnY stub floor so regression tests remain unaffected.
 */
public final class CollisionWorld {

    private final List<TileRect> tiles = new ArrayList<>();

    public void addTile(TileRect tile) {
        tiles.add(tile);
    }

    public int tileCount() {
        return tiles.size();
    }

    /**
     * Resolves horizontal collision after X integration has been applied.
     * Mutates p.x, p.vx, p.onWall, and p.wallDir.
     * Platforms are skipped — they only block vertical movement.
     *
     * @param p     entity physics — x,y is top-left; vx already applied
     * @param prevX entity top-left X BEFORE this tick's X integration
     */
    public void resolveX(PhysicsState p, float prevX) {
        p.onWall  = false;
        p.wallDir = 0;
        float entityRight = p.x + p.width;
        float prevRight   = prevX + p.width;
        float grace       = Math.max(1f, Math.abs(p.vx) + 1f);

        for (TileRect tile : tiles) {
            if (tile.isPlatform()) continue;
            if (!tile.overlaps(p.x, p.y, p.width, p.height)) continue;

            float overlapRight = entityRight - tile.x();
            float overlapLeft  = tile.right() - p.x;

            if (p.vx > 0f && prevRight <= tile.x() + grace && overlapRight > 0f) {
                p.x       = tile.x() - p.width;
                p.vx      = 0f;
                p.onWall  = true;
                p.wallDir = 1;
            } else if (p.vx < 0f && prevX >= tile.right() - grace && overlapLeft > 0f) {
                p.x       = tile.right();
                p.vx      = 0f;
                p.onWall  = true;
                p.wallDir = -1;
            }
        }
    }

    /**
     * Resolves vertical collision after gravity and Y integration have been applied.
     * Mutates p.y, p.vy, and p.onGround.
     *
     * @param p     entity physics — x,y is top-left; width/height is AABB size; vy already applied
     * @param prevY entity top-left Y BEFORE this tick's Y integration (one-way platform check)
     */
    public void resolveY(PhysicsState p, float prevY) {
        p.onGround = false;
        float entityBottom = p.y + p.height;
        float prevBottom   = prevY + p.height;

        for (TileRect tile : tiles) {
            if (!tile.overlaps(p.x, p.y, p.width, p.height)) continue;

            float overlapTop    = entityBottom - tile.y();
            float overlapBottom = tile.bottom() - p.y;
            float grace         = Math.max(1f, Math.abs(p.vy) + 1f);

            if (tile.isPlatform()) {
                // One-way: only resolve when falling into the surface from above
                if (p.vy >= 0f && prevBottom <= tile.y() + grace
                        && overlapTop > 0f && overlapTop < tile.h()) {
                    p.y        = tile.y() - p.height;
                    p.vy       = 0f;
                    p.onGround = true;
                }
                continue;
            }

            // Solid tile
            if (p.vy >= 0f && prevBottom <= tile.y() + grace
                    && overlapTop >= 0f && overlapTop < overlapBottom) {
                // Land on top surface
                p.y        = tile.y() - p.height;
                p.vy       = 0f;
                p.onGround = true;
            } else if (p.vy < 0f && prevY >= tile.bottom() - grace
                    && overlapBottom < overlapTop) {
                // Hit ceiling
                p.y  = tile.bottom();
                p.vy = 0f;
            }
        }
    }
}
