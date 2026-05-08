package com.shadowascent.core.simulation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Server-side portal entity. Placed by level layout; proximity + ability gate checked by simulator.
 * toState() dropped (network DTO incompatible with core layer contract); use toMap() instead.
 */
public final class SimPortal {

    public final String  portalId;
    public final String  portalType;
    public final String  destinationId;
    public final float   x;
    public final float   y;
    public final int     width  = 64;
    public final int     height = 96;
    public boolean isActive    = true;
    public float   pulseTimer  = 0f;

    public final String requiredAbility;

    public SimPortal(String id, String type, String destId, float x, float y, String requiredAbility) {
        this.portalId        = id;
        this.portalType      = type;
        this.destinationId   = destId;
        this.x               = x;
        this.y               = y;
        this.requiredAbility = requiredAbility != null ? requiredAbility : "";
    }

    public void step(float dt) {
        if (isActive) pulseTimer += dt * 2f;
    }

    public boolean canInteract(float px, float py) {
        float cx = x + width  * 0.5f;
        float cy = y + height * 0.5f;
        float dx = px - cx;
        float dy = py - cy;
        return (dx * dx + dy * dy) <= 56f * 56f;
    }

    public boolean canPlayerEnter(SimPlayer player) {
        if (requiredAbility.isEmpty()) return true;
        return player.unlockedAbilities.contains(requiredAbility);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("portal_id",        portalId);
        m.put("portal_type",      portalType);
        m.put("destination_id",   destinationId);
        m.put("x",                x);
        m.put("y",                y);
        m.put("width",            width);
        m.put("height",           height);
        m.put("is_active",        isActive);
        m.put("is_locked",        false);
        m.put("required_ability", requiredAbility);
        return m;
    }
}
