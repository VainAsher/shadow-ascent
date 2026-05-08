package com.shadowascent.client;

import com.shadowascent.core.physics.TileRect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Owns traversal encounter state: moving platforms, ability gates, triggered platforms,
 * ability triggers, and combat barriers.
 * Does not own player state, collision hash, rendering, or mission logic.
 */
final class TraversalSubsystem {

    record PlatformCarry(float dx, float dy) {}

    record InteractOutcome(OutcomeType type, String triggerName, String requiredAbility) {

        enum OutcomeType { ACTIVATED, ALREADY_ACTIVE, ABILITY_LOCKED, NO_TRIGGER }

        static InteractOutcome noTrigger() {
            return new InteractOutcome(OutcomeType.NO_TRIGGER, "", "");
        }

        static InteractOutcome alreadyActive(String name) {
            return new InteractOutcome(OutcomeType.ALREADY_ACTIVE, name, "");
        }

        static InteractOutcome abilityLocked(String name, String ability) {
            return new InteractOutcome(OutcomeType.ABILITY_LOCKED, name, ability);
        }

        static InteractOutcome activated(String name) {
            return new InteractOutcome(OutcomeType.ACTIVATED, name, "");
        }
    }

    private final List<MovingPlatform> movingPlatforms = new ArrayList<>();
    private final Map<String, MovingPlatform> movingPlatformsById = new HashMap<>();
    private final List<AbilityGate> abilityGates = new ArrayList<>();
    private final List<AbilityTrigger> abilityTriggers = new ArrayList<>();
    private final Set<String> activatedAbilityTriggers = new HashSet<>();
    private final List<TriggeredPlatform> triggeredPlatforms = new ArrayList<>();
    private final List<CombatBarrier> combatBarriers = new ArrayList<>();
    private final Consumer<AbilityTrigger> onTriggerActivated;

    private float playerX;
    private float playerY;
    private float playerRadius;

    TraversalSubsystem(Consumer<AbilityTrigger> onTriggerActivated, float playerRadius) {
        this.onTriggerActivated = onTriggerActivated;
        this.playerRadius = playerRadius;
    }

    void addMovingPlatform(String id, float x, float y, float w, float h,
                           float travelX, float travelY, float period) {
        MovingPlatform platform = new MovingPlatform(id, x, y, w, h, travelX, travelY, period);
        movingPlatforms.add(platform);
        movingPlatformsById.put(id, platform);
    }

    void addAbilityGate(String id, String ability, String name,
                        float x, float y, float w, float h) {
        abilityGates.add(new AbilityGate(id, ability, name, x, y, w, h));
    }

    void addAbilityTrigger(String id, String ability, String name, TriggerMode mode,
                           float x, float y, float w, float h, List<String> keywords) {
        abilityTriggers.add(new AbilityTrigger(id, ability, name, mode, x, y, w, h, keywords));
    }

    void addTriggeredPlatform(String id, String triggerId, float x, float y, float w, float h) {
        triggeredPlatforms.add(new TriggeredPlatform(id, triggerId, x, y, w, h));
    }

    void addCombatBarrier(String id, String encounterId, String name,
                          float x, float y, float w, float h) {
        combatBarriers.add(new CombatBarrier(id, encounterId, name, x, y, w, h));
    }

    void clearAll() {
        movingPlatforms.clear();
        movingPlatformsById.clear();
        abilityGates.clear();
        abilityTriggers.clear();
        activatedAbilityTriggers.clear();
        triggeredPlatforms.clear();
        combatBarriers.clear();
    }

    void update(float px, float py, boolean isDashing, Predicate<String> abilityCheck, float dt) {
        this.playerX = px;
        this.playerY = py;
        for (MovingPlatform platform : movingPlatforms) {
            platform.update(dt);
        }
        if (isDashing) {
            for (AbilityTrigger trigger : abilityTriggers) {
                if (activatedAbilityTriggers.contains(trigger.id())) continue;
                if (trigger.mode() != TriggerMode.DASH_PASS) continue;
                if (!abilityCheck.test(trigger.requiredAbility())) continue;
                if (overlapsTrigger(trigger)) {
                    fireActivation(trigger);
                }
            }
        }
    }

    InteractOutcome tryInteract(float px, float py, float radius, Predicate<String> abilityCheck) {
        AbilityTrigger nearby = nearestTriggerWithinRange(px, py, radius);
        if (nearby == null || nearby.mode() != TriggerMode.INTERACT) {
            return InteractOutcome.noTrigger();
        }
        if (activatedAbilityTriggers.contains(nearby.id())) {
            return InteractOutcome.alreadyActive(nearby.displayName());
        }
        if (!abilityCheck.test(nearby.requiredAbility())) {
            return InteractOutcome.abilityLocked(nearby.displayName(), nearby.requiredAbility());
        }
        fireActivation(nearby);
        return InteractOutcome.activated(nearby.displayName());
    }

    PlatformCarry carryFor(String groundedPlatformId) {
        if (groundedPlatformId == null || groundedPlatformId.isBlank()) {
            return null;
        }
        MovingPlatform platform = movingPlatformsById.get(groundedPlatformId);
        if (platform == null) {
            return null;
        }
        return new PlatformCarry(platform.deltaX(), platform.deltaY());
    }

    List<TileRect> buildDynamicTiles(Set<String> clearedEncounterIds, Predicate<String> abilityCheck) {
        List<TileRect> tiles = new ArrayList<>();
        for (MovingPlatform platform : movingPlatforms) {
            tiles.add(platform.toTileRect());
        }
        for (TriggeredPlatform platform : triggeredPlatforms) {
            if (platform.isUnlocked(activatedAbilityTriggers)) {
                tiles.add(platform.toTileRect());
            }
        }
        for (CombatBarrier barrier : combatBarriers) {
            if (!barrier.isUnlocked(clearedEncounterIds)) {
                tiles.add(barrier.toTileRect());
            }
        }
        for (AbilityGate gate : abilityGates) {
            if (!abilityCheck.test(gate.requiredAbility())) {
                tiles.add(gate.toTileRect());
            }
        }
        return tiles;
    }

    void restoreFromFlags(Predicate<String> hasFlag) {
        activatedAbilityTriggers.clear();
        for (AbilityTrigger trigger : abilityTriggers) {
            if (hasFlag.test(trigger.id())) {
                activatedAbilityTriggers.add(trigger.id());
            }
        }
    }

    AbilityGate gateForTile(TileRect tile) {
        for (AbilityGate gate : abilityGates) {
            if (almostEquals(gate.x(), tile.x())
                    && almostEquals(gate.y(), tile.y())
                    && almostEquals(gate.width(), tile.w())
                    && almostEquals(gate.height(), tile.h())) {
                return gate;
            }
        }
        return null;
    }

    CombatBarrier barrierForTile(TileRect tile) {
        for (CombatBarrier barrier : combatBarriers) {
            if (almostEquals(barrier.x(), tile.x())
                    && almostEquals(barrier.y(), tile.y())
                    && almostEquals(barrier.width(), tile.w())
                    && almostEquals(barrier.height(), tile.h())) {
                return barrier;
            }
        }
        return null;
    }

    String platformIdForTile(TileRect tile) {
        for (MovingPlatform platform : movingPlatforms) {
            if (almostEquals(platform.x(), tile.x())
                    && almostEquals(platform.y(), tile.y())) {
                return platform.id();
            }
        }
        return null;
    }

    AbilityTrigger nearestTriggerWithinRange(float px, float py, float radius) {
        AbilityTrigger nearest = null;
        float nearestDist = Float.MAX_VALUE;
        for (AbilityTrigger trigger : abilityTriggers) {
            float dist = distance(trigger.centerX(), trigger.centerY(), px, py);
            if (dist <= radius && dist < nearestDist) {
                nearestDist = dist;
                nearest = trigger;
            }
        }
        return nearest;
    }

    boolean isTriggerActivated(String id) {
        return activatedAbilityTriggers.contains(id);
    }

    List<MovingPlatform> allMovingPlatforms() {
        return Collections.unmodifiableList(movingPlatforms);
    }

    List<AbilityGate> allGates() {
        return Collections.unmodifiableList(abilityGates);
    }

    List<AbilityTrigger> allTriggers() {
        return Collections.unmodifiableList(abilityTriggers);
    }

    List<TriggeredPlatform> allTriggeredPlatforms() {
        return Collections.unmodifiableList(triggeredPlatforms);
    }

    List<CombatBarrier> allCombatBarriers() {
        return Collections.unmodifiableList(combatBarriers);
    }

    private void fireActivation(AbilityTrigger trigger) {
        activatedAbilityTriggers.add(trigger.id());
        onTriggerActivated.accept(trigger);
    }

    private boolean overlapsTrigger(AbilityTrigger trigger) {
        float closestX = clamp(playerX, trigger.x(), trigger.x() + trigger.width());
        float closestY = clamp(playerY, trigger.y(), trigger.y() + trigger.height());
        float dx = playerX - closestX;
        float dy = playerY - closestY;
        return (dx * dx) + (dy * dy) <= (playerRadius * playerRadius);
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean almostEquals(float a, float b) {
        return Math.abs(a - b) <= 0.01f;
    }
}
