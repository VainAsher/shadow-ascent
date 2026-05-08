package com.shadowascent.core.ecs.components;

import com.shadowascent.core.ecs.Component;
import java.awt.Color;

/**
 * Rendering component: defines how to visually represent an entity.
 */
public class RenderComponent implements Component {
    public enum RenderShape {
        RECT,
        CIRCLE,
        SPRITE
    }

    private final RenderShape shape;
    private final Color color;
    private final String spriteId;  // For sprite-based rendering
    private final boolean showHealthBar;
    private boolean visible;

    public RenderComponent(RenderShape shape, Color color, boolean showHealthBar) {
        this.shape = shape;
        this.color = color;
        this.spriteId = "";
        this.showHealthBar = showHealthBar;
        this.visible = true;
    }

    public RenderComponent(String spriteId, Color overlayColor, boolean showHealthBar) {
        this.shape = RenderShape.SPRITE;
        this.spriteId = spriteId;
        this.color = overlayColor;
        this.showHealthBar = showHealthBar;
        this.visible = true;
    }

    @Override
    public Class<? extends Component> componentType() {
        return RenderComponent.class;
    }

    // Getters
    public RenderShape shape() { return shape; }
    public Color color() { return color; }
    public String spriteId() { return spriteId; }
    public boolean shouldShowHealthBar() { return showHealthBar; }
    public boolean isVisible() { return visible; }

    // Setters
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
