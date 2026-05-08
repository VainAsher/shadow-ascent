package com.shadowascent.core.simulation;

/**
 * Enemy AI behaviour states.
 * Imported from indie-ninja-adventures; wire values kept for snapshot serialisation parity.
 */
public enum EnemyAIState {
    IDLE     ("idle"),
    PATROL   ("patrol"),
    CHASE    ("chase"),
    ATTACK   ("attack"),
    FLEE     ("flee"),
    GUARD    ("guard"),
    STUNNED  ("stunned"),
    DEAD     ("dead");

    public final String wire;

    EnemyAIState(String wire) { this.wire = wire; }
}
