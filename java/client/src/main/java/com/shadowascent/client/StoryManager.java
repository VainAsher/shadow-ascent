package com.shadowascent.client;

import com.shadowascent.core.HubManager;
import com.shadowascent.core.MissionManager;
import com.shadowascent.core.StoryState;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Coordinates story-state mutations that span HubManager and MissionManager.
 * Extracted from PlaytestClient to separate story-lifecycle logic from input/rendering concerns.
 */
final class StoryManager {

    private final StoryState storyState;
    private final HubManager hubManager;
    private final MissionManager missionManager;
    private final Consumer<String> logSink;

    StoryManager(
            StoryState storyState,
            HubManager hubManager,
            MissionManager missionManager,
            Consumer<String> logSink) {
        this.storyState     = storyState;
        this.hubManager     = hubManager;
        this.missionManager = missionManager;
        this.logSink        = logSink;
    }

    /**
     * Seeds the canonical starting story state for Act I playtests.
     * Sets baseline flags, grants starter abilities, and syncs hub + mission availability.
     */
    void seedState() {
        storyState.setFlag("started_act_i");
        storyState.grantAbility("jump");
        storyState.grantAbility("basic_movement");
        refreshFromStoryChange();

        logSink.accept("Playable client ready. Press ENTER to start a mission.");
        logSink.accept("Use A/D to run, SPACE to jump, SHIFT/C to dash (short burst), E near NPCs to interact.");
        logSink.accept("Hold ALT for precision walk speed in tight platforming.");
        logSink.accept("Press M to toggle minimap and preview traversal routes.");
        logSink.accept("Traversal gates unlock from mission-earned abilities (dash, combat).");
        logSink.accept("Ability execution triggers unlock route shortcuts and objective progress.");
        logSink.accept("Press F to strike nearby encounter targets.");
        logSink.accept("Movement sign-off summary is written on exit in log kind `MOVEMENT_SIGNOFF`.");
    }

    /**
     * Synchronises hub state and mission availability after any story flag change.
     */
    void refreshFromStoryChange() {
        hubManager.updateHubState();
        missionManager.updateAvailableMissions();
    }

    /**
     * Restores ability trigger activation state from persisted story flags.
     *
     * @param traversalSubsystem subsystem that owns trigger records
     * @param flagPrefix         flag prefix used when triggers were originally persisted
     */
    void restoreAbilityTriggers(TraversalSubsystem traversalSubsystem, String flagPrefix) {
        traversalSubsystem.restoreFromFlags(id -> storyState.hasFlag(flagPrefix + id));
    }

    /**
     * Restores cleared combat encounter state from persisted story flags.
     *
     * @param combatSubsystem        subsystem that owns encounter records
     * @param clearedEncounterIds    mutable set to populate with cleared encounter IDs
     * @param flagPrefix             flag prefix used when encounters were originally persisted
     */
    void restoreCombatEncounters(
            CombatSubsystem combatSubsystem,
            Set<String> clearedEncounterIds,
            String flagPrefix) {
        clearedEncounterIds.clear();
        for (CombatEncounter encounter : combatSubsystem.allEncounters()) {
            if (storyState.hasFlag(flagPrefix + encounter.id())) {
                clearedEncounterIds.add(encounter.id());
                encounter.setCleared();
            } else {
                encounter.resetState();
            }
        }
    }
}
