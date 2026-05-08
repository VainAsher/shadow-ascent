package com.shadowascent.core;

/**
 * Hub states for the narrative progression system.
 * Based on the hub evolution from indie-ninja-adventures and shadow_ascent_integrated_package.
 */
public enum HubState {
    // Act I - Rising Grounds
    VIBRANT("Full hub with all NPCs present"),
    DIMMING("Hub starting to empty, some NPCs distant"),
    DARK("Hub significantly emptied"),
    EMPTY("Only Veil Maiden remains"),

    // Act II - The Veil Descends
    VEIL_DESCENDS("Hub is empty and dark before scripted defeat"),

    // Act III - The Hollow Depths
    RECOVERING("Hub gradually brightening as Lanterns appear"),
    HOPEFUL("Hub showing signs of recovery"),
    REBORN("Hub fully restored");

    private final String description;

    HubState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}