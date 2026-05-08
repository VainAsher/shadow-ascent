package com.shadowascent.core.simulation;

import java.util.Map;

/**
 * Yin/Yang balance mechanic (GDD §3.3).
 * Imported from indie-ninja-adventures (com.indieniinja.sim.YinYangComponent).
 * ECS base class (Component/SerializableComponent) dropped — pure simulation value type.
 *
 * Yin (Emotion) — reveals hidden platforms, environmental awareness.
 * Yang (Discipline) — attack strength, movement precision.
 * Flow Mode — when |yin − yang| < BALANCE_THRESHOLD both values contribute simultaneously.
 */
public final class YinYangComponent {

    public static final float BALANCE_THRESHOLD  = 0.15f;
    public static final float HIGH_YIN_THRESHOLD = 0.7f;
    public static final float HIGH_YANG_THRESHOLD = 0.7f;
    public static final float DECAY_RATE         = 0.005f;
    public static final float NEUTRAL            = 0.5f;

    public float yin  = NEUTRAL;
    public float yang = NEUTRAL;

    public YinYangComponent(int entityId) {}

    public void absorbYin(float amount) {
        yin = Math.min(1.0f, yin + amount);
    }

    public void absorbYang(float amount) {
        yang = Math.min(1.0f, yang + amount);
    }

    public void decay(float dt) {
        float d = DECAY_RATE * dt;
        yin  = approach(yin,  NEUTRAL, d);
        yang = approach(yang, NEUTRAL, d);
    }

    private static float approach(float value, float target, float step) {
        if (value < target) return Math.min(target, value + step);
        if (value > target) return Math.max(target, value - step);
        return value;
    }

    public boolean isBalanced() {
        return Math.abs(yin - yang) < BALANCE_THRESHOLD;
    }

    public boolean hasYinSight() {
        return yin > HIGH_YIN_THRESHOLD;
    }

    public boolean hasYangSurge() {
        return yang > HIGH_YANG_THRESHOLD;
    }

    public Map<String, Object> toMap() {
        return Map.of("yin", yin, "yang", yang);
    }

    public static YinYangComponent fromMap(int entityId, Map<String, Object> m) {
        YinYangComponent c = new YinYangComponent(entityId);
        if (m == null) return c;
        c.yin  = flt(m, "yin",  NEUTRAL);
        c.yang = flt(m, "yang", NEUTRAL);
        return c;
    }

    private static float flt(Map<String, Object> m, String k, float def) {
        Object v = m.get(k);
        return v instanceof Number n ? n.floatValue() : def;
    }

    @Override public String toString() {
        return "YinYang{yin=" + yin + ", yang=" + yang + ", flow=" + isBalanced() + "}";
    }
}
