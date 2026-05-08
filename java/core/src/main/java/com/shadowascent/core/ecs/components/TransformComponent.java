package com.shadowascent.core.ecs.components;

import com.shadowascent.core.ecs.Component;

/**
 * Transform component: stores position, velocity, and basic movement state.
 */
public class TransformComponent implements Component {
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private float width;
    private float height;
    private boolean grounded;

    public TransformComponent(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.velocityX = 0f;
        this.velocityY = 0f;
        this.grounded = false;
    }

    @Override
    public Class<? extends Component> componentType() {
        return TransformComponent.class;
    }

    // Getters
    public float x() { return x; }
    public float y() { return y; }
    public float velocityX() { return velocityX; }
    public float velocityY() { return velocityY; }
    public float width() { return width; }
    public float height() { return height; }
    public boolean isGrounded() { return grounded; }

    // Setters
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocity(float vx, float vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public void addVelocity(float dvx, float dvy) {
        this.velocityX += dvx;
        this.velocityY += dvy;
    }
}
