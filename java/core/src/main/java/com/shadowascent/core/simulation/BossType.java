package com.shadowascent.core.simulation;

/**
 * Boss type definitions.
 * Imported from indie-ninja-adventures (com.indieniinja.sim.BossType).
 *
 * Shadow Ascent narrative bosses (M5): SIREN, ECHO_WARDEN, TIME_LEECH_LORD, MEMORY_EATER.
 * Each has a distinct psychological pattern (see BossPatternLibrary).
 */
public enum BossType {
    // ── Shadow Ascent narrative bosses ────────────────────────────────────────
    SIREN          ("siren",           8,  0, 48f,  "boss_siren"),
    ECHO_WARDEN    ("echo_warden",     20, 3, 72f,  "boss_echo_warden"),
    TIME_LEECH_LORD("time_leech_lord", 25, 4, 84f,  "boss_time_leech_lord"),
    MEMORY_EATER   ("memory_eater",    30, 3, 60f,  "boss_memory_eater"),

    // Campaign-authored boss IDs (mission objective compatibility)
    SHADOW_LORD    ("shadow_lord",     25, 1, 80f,  "boss_shadow_lord"),
    FIRE_DEMON     ("fire_demon",      30, 1, 100f, "boss_fire_demon"),
    ICE_QUEEN      ("ice_queen",       28, 1, 70f,  "boss_ice_queen"),
    NECROMANCER    ("necromancer",     20, 1, 60f,  "boss_necromancer"),
    DRAGON         ("dragon",          35, 1, 90f,  "boss_dragon"),
    VEIL_MAIDEN    ("veil_maiden",     40, 1, 90f,  "boss_veil_maiden"),

    // ── Legacy generic bosses (arcade/seed rooms) ─────────────────────────────
    FOREST_GUARDIAN("forest_guardian", 15, 2, 72f,  "boss_forest_guardian"),
    CORRUPT_MAYOR  ("corrupt_mayor",   12, 1, 60f,  "boss_corrupt_mayor"),
    CRYSTAL_GOLEM  ("crystal_golem",   20, 3, 48f,  "boss_crystal_golem"),
    DARK_KNIGHT    ("dark_knight",     16, 2, 84f,  "boss_dark_knight"),
    PLAGUE_RAT     ("plague_rat",      14, 2, 96f,  "boss_plague_rat");

    public final String wire;
    public final int    maxHp;
    public final int    baseDamage;
    public final float  moveSpeed;
    public final String atlasKey;

    BossType(String wire, int maxHp, int baseDamage, float moveSpeed, String atlasKey) {
        this.wire       = wire;
        this.maxHp      = maxHp;
        this.baseDamage = baseDamage;
        this.moveSpeed  = moveSpeed;
        this.atlasKey   = atlasKey;
    }

    public int width()     { return 64; }
    public int height()    { return 96; }
    public int xpReward()  { return 80 + ordinal() * 20; }

    public static BossType fromSeed(long seed) {
        BossType[] vals = values();
        return vals[(int)(Math.abs(seed) % vals.length)];
    }

    public static BossType fromWire(String wire) {
        for (BossType t : values()) if (t.wire.equals(wire)) return t;
        return FOREST_GUARDIAN;
    }
}
