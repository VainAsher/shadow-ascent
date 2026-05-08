package com.shadowascent.core.simulation;

/**
 * Boss AI states.
 * Imported from indie-ninja-adventures (com.indieniinja.sim.BossAIState).
 */
public enum BossAIState {
    INTRO            ("intro"),
    IDLE             ("idle"),
    MOVE             ("move"),
    ATTACK_MELEE     ("attack_melee"),
    ATTACK_RANGED    ("attack_ranged"),
    ATTACK_SPECIAL   ("attack_special"),
    VULNERABLE       ("vulnerable"),
    STUNNED          ("stunned"),
    PHASE_TRANSITION ("phase_transition"),
    DEAD             ("dead");

    public final String wire;
    BossAIState(String wire) { this.wire = wire; }
}
