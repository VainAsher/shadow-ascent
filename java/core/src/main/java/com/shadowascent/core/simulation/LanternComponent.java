package com.shadowascent.core.simulation;

import java.util.Map;

/**
 * Lantern (Emotional Clarity) mechanic (GDD §3.4).
 * Imported from indie-ninja-adventures (com.indieniinja.sim.LanternComponent).
 * ECS base class (Component/SerializableComponent) dropped — pure simulation value type.
 *
 * Per-player float [0.0–1.0] measuring the Hollowed Ninja's emotional clarity.
 * Low: some PLATFORM tiles treated as SOLID (trap), full vignette.
 * High (>0.7): jump height +20%, coyote time 4→8 ticks.
 */
public final class LanternComponent {

    public static final float LOW_THRESHOLD    = 0.3f;
    public static final float HIGH_THRESHOLD   = 0.7f;
    public static final float DARK_DECAY_RATE  = 0.01f;
    public static final float NPC_RESTORE      = 0.05f;
    public static final float FRAGMENT_RESTORE = 0.20f;
    public static final float DAMAGE_DECAY     = 0.05f;
    public static final float HIGH_JUMP_BONUS  = 0.20f;
    public static final int   HIGH_COYOTE_TICKS = 8;

    public float value = 0.8f;

    public LanternComponent(int entityId) {}

    public void decay(float dt, boolean inDark) {
        if (inDark) {
            value = Math.max(0f, value - DARK_DECAY_RATE * dt);
        }
    }

    public void restore(float amount) {
        value = Math.min(1.0f, value + amount);
    }

    public void onDamage() {
        value = Math.max(0f, value - DAMAGE_DECAY);
    }

    public boolean isLow() {
        return value < LOW_THRESHOLD;
    }

    public boolean isHigh() {
        return value > HIGH_THRESHOLD;
    }

    public float vignetteIntensity() {
        return 1.0f - Math.min(1.0f, Math.max(0f, (value - LOW_THRESHOLD) / (HIGH_THRESHOLD - LOW_THRESHOLD)));
    }

    public Map<String, Object> toMap() {
        return Map.of("lantern", value);
    }

    public static LanternComponent fromMap(int entityId, Map<String, Object> m) {
        LanternComponent c = new LanternComponent(entityId);
        if (m == null) return c;
        c.value = flt(m, "lantern", 0.8f);
        return c;
    }

    private static float flt(Map<String, Object> m, String k, float def) {
        Object v = m.get(k);
        return v instanceof Number n ? n.floatValue() : def;
    }

    @Override public String toString() {
        return "Lantern{value=" + value + ", vignette=" + vignetteIntensity() + "}";
    }
}
