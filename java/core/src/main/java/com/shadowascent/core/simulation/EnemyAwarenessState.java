package com.shadowascent.core.simulation;

/**
 * Enemy awareness tier — driven by player noise, parallel to EnemyAIState.
 *
 * UNAWARE    → normal patrol
 * SUSPICIOUS → heard something; investigates noise source
 * SEARCHING  → lost sight/sound after ALERTED; sweeps last known position
 * ALERTED    → spotted player; overrides aiState to CHASE
 */
public enum EnemyAwarenessState {
    UNAWARE,
    SUSPICIOUS,
    SEARCHING,
    ALERTED
}
