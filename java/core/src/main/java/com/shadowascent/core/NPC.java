package com.shadowascent.core;

import java.util.Set;

/**
 * Represents an NPC in the hub world.
 * Based on the NPC registry from shadow_ascent_integrated_package.
 */
public class NPC {
    private final String id;
    private final String displayName;
    private final Set<String> allowedRoles;
    private final Set<String> eligiblePlateaus;
    private final String notes;
    private boolean isActive;
    private String currentDialogue;

    public NPC(String id, String displayName, Set<String> allowedRoles,
               Set<String> eligiblePlateaus, String notes) {
        this.id = id;
        this.displayName = displayName;
        this.allowedRoles = allowedRoles;
        this.eligiblePlateaus = eligiblePlateaus;
        this.notes = notes;
        this.isActive = true;
        this.currentDialogue = "";
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Set<String> getAllowedRoles() { return allowedRoles; }
    public Set<String> getEligiblePlateaus() { return eligiblePlateaus; }
    public String getNotes() { return notes; }
    public boolean isActive() { return isActive; }
    public String getCurrentDialogue() { return currentDialogue; }

    // Setters
    public void setActive(boolean active) { this.isActive = active; }
    public void setCurrentDialogue(String dialogue) { this.currentDialogue = dialogue; }

    @Override
    public String toString() {
        return String.format("NPC{id='%s', name='%s', active=%s}",
                           id, displayName, isActive);
    }
}