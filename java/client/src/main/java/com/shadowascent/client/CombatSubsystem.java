package com.shadowascent.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Owns combat encounter state and attack resolution.
 * Does not hold player state, story state, or rendering concerns.
 */
final class CombatSubsystem {

    record AttackOutcome(
            AttackType type,
            String encounterName,
            String requiredAbility,
            int hitsRemaining,
            String phaseLabel) {

        enum AttackType { HIT, MISS_TIMING, ABILITY_LOCKED, NO_TARGET }

        static AttackOutcome noTarget() {
            return new AttackOutcome(AttackType.NO_TARGET, "", "", 0, "");
        }

        static AttackOutcome abilityLocked(String name, String ability) {
            return new AttackOutcome(AttackType.ABILITY_LOCKED, name, ability, 0, "");
        }

        static AttackOutcome missTiming(String name, String phase) {
            return new AttackOutcome(AttackType.MISS_TIMING, name, "", 0, phase);
        }

        static AttackOutcome hit(String name, int remaining) {
            return new AttackOutcome(AttackType.HIT, name, "", remaining, "");
        }
    }

    private final List<CombatEncounter> encounters = new ArrayList<>();
    private final Consumer<CombatEncounter> onClearedCallback;

    CombatSubsystem(Consumer<CombatEncounter> onCleared) {
        this.onClearedCallback = onCleared;
    }

    void addEncounter(
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
        encounters.add(new CombatEncounter(
                id, requiredAbility, displayName,
                centerX, centerY, activationRadius,
                requiredHits, telegraphSeconds, vulnerableSeconds, recoverSeconds,
                pattern, objectiveKeywords));
    }

    void clearAll() {
        encounters.clear();
    }

    List<CombatEncounter> allEncounters() {
        return Collections.unmodifiableList(encounters);
    }

    void update(float playerX, float playerY, Predicate<String> abilityCheck,
                Consumer<String> phaseLogger, float dt) {
        for (CombatEncounter encounter : encounters) {
            CombatEncounterPhase before = encounter.phase();
            boolean abilityUnlocked = encounter.requiredAbility().isBlank()
                    || abilityCheck.test(encounter.requiredAbility());
            encounter.update(playerX, playerY, abilityUnlocked, dt);
            CombatEncounterPhase after = encounter.phase();
            if (before != after) {
                phaseLogger.accept("Encounter state: " + encounter.displayName()
                        + " -> " + encounter.phaseLabel());
            }
        }
    }

    AttackOutcome tryAttack(float playerX, float playerY, Predicate<String> abilityCheck, float maxRange) {
        CombatEncounter encounter = nearestNonCleared(playerX, playerY, maxRange);
        if (encounter == null) {
            return AttackOutcome.noTarget();
        }
        if (!encounter.requiredAbility().isBlank() && !abilityCheck.test(encounter.requiredAbility())) {
            return AttackOutcome.abilityLocked(encounter.displayName(), encounter.requiredAbility());
        }
        if (!encounter.isAttackWindowOpen()) {
            return AttackOutcome.missTiming(encounter.displayName(), encounter.phaseLabel());
        }
        int remaining = encounter.consumeVulnerableHit();
        if (remaining <= 0 || encounter.isCleared()) {
            onClearedCallback.accept(encounter);
        }
        return AttackOutcome.hit(encounter.displayName(), remaining);
    }

    CombatEncounter nearestWithinRange(float playerX, float playerY, float maxRange) {
        CombatEncounter nearest = null;
        float nearestDist = Float.MAX_VALUE;
        for (CombatEncounter encounter : encounters) {
            float dist = distance(playerX, playerY, encounter.centerX(), encounter.centerY());
            if (dist <= maxRange && dist < nearestDist) {
                nearestDist = dist;
                nearest = encounter;
            }
        }
        return nearest;
    }

    private CombatEncounter nearestNonCleared(float playerX, float playerY, float maxRange) {
        CombatEncounter nearest = null;
        float nearestDist = Float.MAX_VALUE;
        for (CombatEncounter encounter : encounters) {
            if (encounter.isCleared()) {
                continue;
            }
            float dist = distance(playerX, playerY, encounter.centerX(), encounter.centerY());
            if (dist <= maxRange && dist < nearestDist) {
                nearestDist = dist;
                nearest = encounter;
            }
        }
        return nearest;
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
