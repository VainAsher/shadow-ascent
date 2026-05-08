package com.shadowascent.client;

import java.awt.Color;
import java.util.List;
import java.util.Locale;

final class CombatEncounter {
    private final String id;
    private final String requiredAbility;
    private final String displayName;
    private final float centerX;
    private final float centerY;
    private final float activationRadius;
    private final int requiredHits;
    private final float telegraphSeconds;
    private final float vulnerableSeconds;
    private final float recoverSeconds;
    private final EncounterPattern pattern;
    private final List<String> objectiveKeywords;
    private int hitsRemaining;
    private CombatEncounterPhase phase;
    private float phaseTimeRemainingSeconds;

    CombatEncounter(
            String id,
            String requiredAbility,
            String displayName,
            float centerX,
            float centerY,
            float activationRadius,
            int requiredHits,
            float telegraphSeconds,
            float vulnerableSeconds,
            float recoverSeconds,
            EncounterPattern pattern,
            List<String> objectiveKeywords) {
        this.id = id;
        this.requiredAbility = requiredAbility == null ? "" : requiredAbility;
        this.displayName = displayName;
        this.centerX = centerX;
        this.centerY = centerY;
        this.activationRadius = Math.max(32f, activationRadius);
        this.requiredHits = Math.max(1, requiredHits);
        this.telegraphSeconds = Math.max(0.35f, telegraphSeconds);
        this.vulnerableSeconds = Math.max(0.25f, vulnerableSeconds);
        this.recoverSeconds = Math.max(0.30f, recoverSeconds);
        this.pattern = pattern == null ? EncounterPattern.STANDARD : pattern;
        this.objectiveKeywords = objectiveKeywords == null ? List.of() : List.copyOf(objectiveKeywords);
        this.hitsRemaining = this.requiredHits;
        this.phase = CombatEncounterPhase.DORMANT;
        this.phaseTimeRemainingSeconds = 0f;
    }

    void update(float playerX, float playerY, boolean abilityUnlocked, float dt) {
        if (isCleared()) {
            phase = CombatEncounterPhase.CLEARED;
            phaseTimeRemainingSeconds = 0f;
            return;
        }
        if (!abilityUnlocked) {
            phase = CombatEncounterPhase.DORMANT;
            phaseTimeRemainingSeconds = 0f;
            return;
        }

        if (phase == CombatEncounterPhase.DORMANT) {
            if (distance(playerX, playerY, centerX, centerY) <= activationRadius) {
                enterTelegraph();
            }
            return;
        }

        phaseTimeRemainingSeconds = Math.max(0f, phaseTimeRemainingSeconds - Math.max(0f, dt));
        if (phaseTimeRemainingSeconds > 0f) {
            return;
        }

        switch (phase) {
            case TELEGRAPH -> enterVulnerable();
            case VULNERABLE -> enterRecover();
            case RECOVER -> enterTelegraph();
            case CLEARED, DORMANT -> {
                // no-op
            }
        }
    }

    int consumeVulnerableHit() {
        if (!isAttackWindowOpen() || isCleared()) {
            return hitsRemaining;
        }
        hitsRemaining = Math.max(0, hitsRemaining - 1);
        if (hitsRemaining <= 0) {
            setCleared();
        } else {
            enterRecover();
        }
        return hitsRemaining;
    }

    boolean matchesObjective(String objectiveId) {
        if (objectiveId == null || objectiveId.isBlank() || objectiveKeywords.isEmpty()) {
            return false;
        }
        String normalized = objectiveId.toLowerCase(Locale.ROOT);
        for (String keyword : objectiveKeywords) {
            if (keyword != null && !keyword.isBlank() && normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    void setCleared() {
        this.phase = CombatEncounterPhase.CLEARED;
        this.hitsRemaining = 0;
        this.phaseTimeRemainingSeconds = 0f;
    }

    void resetState() {
        this.hitsRemaining = requiredHits;
        this.phase = CombatEncounterPhase.DORMANT;
        this.phaseTimeRemainingSeconds = 0f;
    }

    boolean isAttackWindowOpen() {
        return phase == CombatEncounterPhase.VULNERABLE;
    }

    CombatEncounterPhase phase() {
        return phase;
    }

    String phaseLabel() {
        return switch (phase) {
            case DORMANT -> "Dormant";
            case TELEGRAPH -> "Telegraph";
            case VULNERABLE -> "Vulnerable";
            case RECOVER -> "Recover";
            case CLEARED -> "Cleared";
        };
    }

    String phaseShortLabel() {
        return switch (phase) {
            case DORMANT -> "DORM";
            case TELEGRAPH -> "TEL";
            case VULNERABLE -> "OPEN";
            case RECOVER -> "REC";
            case CLEARED -> "CLR";
        };
    }

    float phaseTimeRemainingSeconds() {
        return phaseTimeRemainingSeconds;
    }

    EncounterPattern pattern() {
        return pattern;
    }

    Color renderColorWorld() {
        return switch (phase) {
            case DORMANT -> new Color(164, 186, 214, 120);
            case TELEGRAPH -> new Color(246, 201, 120, 185);
            case VULNERABLE -> new Color(127, 246, 174, 205);
            case RECOVER -> new Color(243, 133, 121, 170);
            case CLEARED -> new Color(118, 244, 176, 128);
        };
    }

    Color renderColorMap() {
        return switch (phase) {
            case DORMANT -> new Color(176, 199, 228);
            case TELEGRAPH -> new Color(244, 208, 131);
            case VULNERABLE -> new Color(120, 236, 164);
            case RECOVER -> new Color(245, 136, 124);
            case CLEARED -> new Color(114, 240, 170);
        };
    }

    void enterTelegraph() {
        phase = CombatEncounterPhase.TELEGRAPH;
        phaseTimeRemainingSeconds = adjustedTime(telegraphSeconds);
    }

    void enterVulnerable() {
        phase = CombatEncounterPhase.VULNERABLE;
        phaseTimeRemainingSeconds = adjustedTime(vulnerableSeconds);
    }

    void enterRecover() {
        phase = CombatEncounterPhase.RECOVER;
        phaseTimeRemainingSeconds = adjustedTime(recoverSeconds);
    }

    private float adjustedTime(float baseTime) {
        return switch (pattern) {
            case STANDARD -> baseTime;
            case FAST -> baseTime * 0.8f;
            case SLOW -> baseTime * 1.2f;
            case AGILE -> baseTime * 0.6f;
        };
    }

    String id() { return id; }
    String requiredAbility() { return requiredAbility; }
    String displayName() { return displayName; }
    float centerX() { return centerX; }
    float centerY() { return centerY; }
    float activationRadius() { return activationRadius; }
    int hitsRemaining() { return hitsRemaining; }
    boolean isCleared() { return phase == CombatEncounterPhase.CLEARED; }

    private static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
