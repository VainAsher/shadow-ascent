package com.shadowascent.core.simulation;

/**
 * Horizontal oscillating platform bounded between leftBound and rightBound.
 */
public final class SimMovingPlatform {

    public final String id;
    public float x;
    public float y;
    public final float width;
    public final float height;
    public final float leftBound;
    public final float rightBound;
    public float vx;

    public SimMovingPlatform(String id, float x, float y,
                              float width, float height,
                              float leftBound, float rightBound,
                              float speed) {
        this.id         = id;
        this.x          = x;
        this.y          = y;
        this.width      = width;
        this.height     = height;
        this.leftBound  = leftBound;
        this.rightBound = rightBound;
        this.vx         = speed;
    }

    public void step() {
        x += vx;
        if (x <= leftBound) {
            x  = leftBound;
            vx = Math.abs(vx);
        } else if (x + width >= rightBound) {
            x  = rightBound - width;
            vx = -Math.abs(vx);
        }
    }

    public boolean isStandingOn(float px, float py, float pw, float ph) {
        float playerBottom = py + ph;
        float platTop      = y;
        if (playerBottom < platTop - 4f || playerBottom > platTop + 6f) return false;
        return px + pw > x + 2f && px < x + width - 2f;
    }
}
