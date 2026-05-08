package com.shadowascent.core;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Represents a mission in the game world.
 * Based on the mission system from indie-ninja-adventures.
 */
public class Mission {
    private final String id;
    private final String displayName;
    private final String description;
    private final String region;
    private final List<String> objectives;
    private final Map<String, Integer> requiredCounts;
    private final Map<String, Integer> objectiveProgress;
    private MissionState state;
    private int attempts;
    private float bestTime;

    public enum MissionState {
        LOCKED,
        AVAILABLE,
        ACTIVE,
        COMPLETED,
        FAILED
    }

    public Mission(String id, String displayName, String description, String region,
                   List<String> objectives, Map<String, Integer> requiredCounts) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.region = region;
        this.objectives = List.copyOf(Objects.requireNonNull(objectives, "objectives"));
        this.requiredCounts = Map.copyOf(Objects.requireNonNull(requiredCounts, "requiredCounts"));
        this.objectiveProgress = new LinkedHashMap<>();
        for (String objective : this.objectives) {
            this.objectiveProgress.put(objective, 0);
        }
        this.state = MissionState.LOCKED;
        this.attempts = 0;
        this.bestTime = Float.MAX_VALUE;
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getRegion() { return region; }
    public List<String> getObjectives() { return objectives; }
    public Map<String, Integer> getRequiredCounts() { return requiredCounts; }
    public Map<String, Integer> getObjectiveProgress() { return Map.copyOf(objectiveProgress); }
    public MissionState getState() { return state; }
    public int getAttempts() { return attempts; }
    public float getBestTime() { return bestTime; }

    // Setters
    public void setState(MissionState state) { this.state = state; }
    public void incrementAttempts() { this.attempts++; }
    public void setBestTime(float time) {
        if (time < bestTime) {
            bestTime = time;
        }
    }

    public boolean isCompleted() {
        return state == MissionState.COMPLETED;
    }

    public boolean isAvailable() {
        return state == MissionState.AVAILABLE || state == MissionState.ACTIVE;
    }

    public int getObjectiveProgress(String objectiveId) {
        return objectiveProgress.getOrDefault(objectiveId, 0);
    }

    public int getRequiredCount(String objectiveId) {
        return requiredCounts.getOrDefault(objectiveId, 1);
    }

    public boolean addObjectiveProgress(String objectiveId, int count) {
        if (count <= 0 || !requiredCounts.containsKey(objectiveId)) {
            return false;
        }
        int current = objectiveProgress.getOrDefault(objectiveId, 0);
        int required = getRequiredCount(objectiveId);
        int updated = Math.min(required, current + count);
        if (updated == current) {
            return false;
        }
        objectiveProgress.put(objectiveId, updated);
        return true;
    }

    public boolean isObjectiveComplete(String objectiveId) {
        return getObjectiveProgress(objectiveId) >= getRequiredCount(objectiveId);
    }

    public boolean areAllObjectivesComplete() {
        for (String objective : objectives) {
            if (!isObjectiveComplete(objective)) {
                return false;
            }
        }
        return true;
    }

    public void resetObjectiveProgress() {
        for (String objective : objectives) {
            objectiveProgress.put(objective, 0);
        }
    }

    @Override
    public String toString() {
        return String.format("Mission{id='%s', name='%s', state=%s}",
                           id, displayName, state);
    }
}
