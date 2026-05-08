package com.shadowascent.core;

import com.shadowascent.core.data.BeatDefinition;
import com.shadowascent.core.data.GameDataContracts;

import java.util.List;
import java.util.Optional;

/**
 * Manages narrative beat progression and story state transitions.
 * This is the core of the M2 Campaign Spine: beats drive mission availability and story advancement.
 */
public class BeatProgressionManager {
    private final StoryState storyState;
    private final GameDataContracts gameDataContracts;
    private final MissionManager missionManager;

    public BeatProgressionManager(StoryState storyState, GameDataContracts gameDataContracts,
                                  MissionManager missionManager) {
        this.storyState = storyState;
        this.gameDataContracts = gameDataContracts;
        this.missionManager = missionManager;
    }

    /**
     * Advances the story to the next critical beat if conditions are met.
     * This is called after missions complete or player reaches certain areas.
     */
    public void advanceToNextBeat() {
        Optional<BeatDefinition> nextBeat = gameDataContracts.nextCriticalBeat(storyState);

        if (nextBeat.isPresent()) {
            BeatDefinition beat = nextBeat.get();
            // Automatically apply the flags that this beat sets
            for (String flag : beat.setFlags()) {
                storyState.setFlag(flag);
            }

            // Log the beat progression
            onBeatReached(beat);
        }
    }

    /**
     * Called when a specific beat is reached.
     * Subclasses or listeners can override to add custom behavior.
     */
    protected void onBeatReached(BeatDefinition beat) {
        // Placeholder for beat-triggered events
        // In the full system, this would trigger:
        // - Cinematic sequences
        // - Dialogue forced progression
        // - Environment changes
        // - Mission unlocks
    }

    /**
     * Gets all missions that should be available given the current story state.
     */
    public List<Mission> getAvailableMissions() {
        // Filter missions based on current beat progression and story flags
        List<BeatDefinition> currentBeats = gameDataContracts.beatsForPlateau(
                storyState.getCurrentPlateau().name());

        // Missions are available if their unlock conditions are met
        return missionManager.getAvailableMissions().stream()
                .filter(mission -> isMissionUnlockedByBeats(mission, currentBeats))
                .toList();
    }

    /**
     * Checks if a mission should be unlocked based on critical beats.
     */
    private boolean isMissionUnlockedByBeats(Mission mission, List<BeatDefinition> currentBeats) {
        // This logic can be extended based on how missions map to beats
        // For now, check if any beat's required flags match the mission's implicit requirements
        for (BeatDefinition beat : currentBeats) {
            if (isMissionRelatedToBeat(mission, beat)) {
                return beat.requiredFlags().stream().allMatch(storyState::hasFlag);
            }
        }
        return true;  // Default to available if no beat-mission mapping
    }

    /**
     * Determines if a mission is tied to a specific narrative beat.
     * This mapping can be expanded with explicit mission-to-beat links in the contracts.
     */
    private boolean isMissionRelatedToBeat(Mission mission, BeatDefinition beat) {
        String missionId = mission.getId().toLowerCase();

        // Simple mapping: check if mission ID appears in beat dialogue or beat IDs
        if (beat.id().contains(missionId)) {
            return true;
        }

        // Check beat type for implicit relationships
        return switch (mission.getId()) {
            case "village_bonds" -> beat.beatType().contains("AUTHORED_CRITICAL") && 
                                   beat.id().contains("opening");
            case "dojo_practice" -> beat.id().contains("opening") || beat.id().contains("practice");
            case "mistwood_beast" -> beat.id().contains("mistwood") || beat.beatType().contains("CHALLENGE");
            default -> false;
        };
    }

    /**
     * Gets the next critical narrative beat.
     */
    public Optional<BeatDefinition> getNextCriticalBeat() {
        return gameDataContracts.nextCriticalBeat(storyState);
    }

    /**
     * Gets the current act based on story flags and narrative progression.
     */
    public String getCurrentAct() {
        if (storyState.hasFlag("act_iii_started")) {
            return "ACT_III";
        } else if (storyState.hasFlag("act_ii_started")) {
            return "ACT_II";
        } else {
            return "ACT_I";
        }
    }

    /**
     * Checks if a story transition condition is met.
     * This allows explicit control over when acts/chapters transition.
     */
    public boolean canTransitionToNextAct() {
        String currentAct = getCurrentAct();
        return switch (currentAct) {
            case "ACT_I" -> storyState.hasFlag("act_i_complete") &&
                           storyState.hasFlag("veil_request_resolved") &&
                           storyState.hasFlag("mistwood_beast_defeated");
            case "ACT_II" -> storyState.hasFlag("act_ii_complete") &&
                            storyState.hasFlag("monastery_preparation_complete");
            case "ACT_III" -> storyState.hasFlag("act_iii_complete");
            default -> false;
        };
    }
}
