package com.shadowascent.client;

import com.shadowascent.core.Mission;
import com.shadowascent.core.MissionManager;
import com.shadowascent.core.StoryState;

import java.util.function.Consumer;

/**
 * Bridges objective progress events to mission state and UI feedback.
 * Extracted from PlaytestClient to isolate the objective-advance + feedback-flash chain.
 */
final class MissionUiCoordinator {

    private final StoryState storyState;
    private final MissionManager missionManager;
    private final Consumer<String> feedbackSink;
    private final Consumer<String> logSink;
    private final float feedbackFlashSeconds;

    MissionUiCoordinator(
            StoryState storyState,
            MissionManager missionManager,
            Consumer<String> feedbackSink,
            Consumer<String> logSink,
            float feedbackFlashSeconds) {
        this.storyState          = storyState;
        this.missionManager      = missionManager;
        this.feedbackSink        = feedbackSink;
        this.logSink             = logSink;
        this.feedbackFlashSeconds = feedbackFlashSeconds;
    }

    /**
     * Advances the first incomplete objective that matches the cleared encounter.
     */
    void applyCombatEncounterObjectiveProgress(CombatEncounter encounter) {
        String missionId = storyState.getActiveMissionId();
        if (missionId == null) {
            return;
        }
        Mission mission = storyState.getMission(missionId);
        if (mission == null) {
            return;
        }
        for (String objective : mission.getObjectives()) {
            if (mission.isObjectiveComplete(objective)) {
                continue;
            }
            if (encounter.matchesObjective(objective)) {
                advanceObjective(missionId, objective, 1);
                return;
            }
        }
    }

    /**
     * Advances the first incomplete objective that matches the activated trigger.
     */
    void applyAbilityTriggerObjectiveProgress(AbilityTrigger trigger) {
        String missionId = storyState.getActiveMissionId();
        if (missionId == null) {
            return;
        }
        Mission mission = storyState.getMission(missionId);
        if (mission == null) {
            return;
        }
        for (String objective : mission.getObjectives()) {
            if (mission.isObjectiveComplete(objective)) {
                continue;
            }
            if (trigger.matchesObjective(objective)) {
                advanceObjective(missionId, objective, 1);
                return;
            }
        }
    }

    /**
     * Records progress on an objective and flashes feedback if the value changed.
     */
    void advanceObjective(String missionId, String objectiveId, int amount) {
        Mission mission = storyState.getMission(missionId);
        if (mission == null) {
            return;
        }

        boolean objectiveWasComplete = mission.isObjectiveComplete(objectiveId);
        int before = mission.getObjectiveProgress(objectiveId);
        missionManager.updateObjectiveProgress(missionId, objectiveId, amount);
        Mission updated = storyState.getMission(missionId);
        if (updated == null) {
            return;
        }

        int after    = updated.getObjectiveProgress(objectiveId);
        int required = updated.getRequiredCount(objectiveId);
        if (after > before) {
            logSink.accept("Objective progress: " + objectiveId + " " + after + "/" + required);
            feedbackSink.accept(updated.getDisplayName() + ": " + objectiveId + " " + after + "/" + required);
        }
        boolean objectiveNowComplete = updated.isObjectiveComplete(objectiveId);
        if (!objectiveWasComplete && objectiveNowComplete) {
            logSink.accept("Objective complete: " + objectiveId);
            feedbackSink.accept("Objective complete: " + objectiveId);
        }

        if (storyState.getActiveMissionId() == null && updated.isCompleted()) {
            logSink.accept("Mission completed: " + updated.getDisplayName());
            feedbackSink.accept("Mission completed: " + updated.getDisplayName());
        }
    }
}
