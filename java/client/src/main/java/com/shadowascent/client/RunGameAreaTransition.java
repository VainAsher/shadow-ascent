package com.shadowascent.client;

import com.shadowascent.client.ui.UiText;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.SimPlayer;

import java.util.Comparator;
import java.util.Optional;

final class RunGameAreaTransition {

    private RunGameAreaTransition() {
    }

    static Optional<String> describeNearbyGate(GameState gameState, RunGameContentProfile profile, SimPlayer player, GameSimulator simulator) {
        return nearbyGate(profile, player)
                .map(gate -> {
                    boolean unlocked = gate.requiredFlags().stream().allMatch(gameState.getStoryState()::hasFlag);
                    if (!unlocked) {
                        return "[E] Gate sealed: " + gate.label();
                    }
                    if (gate.requiresAreaClear() && hasLivingEnemies(simulator)) {
                        return "[E] Clear the area to open " + gate.label();
                    }
                    return "[E] " + gate.label() + " -> " + UiText.areaName(gate.targetAreaId());
                });
    }

    static TraversalResult tryTraverse(GameState gameState, RunGameContentProfile profile, SimPlayer player, GameSimulator simulator) {
        Optional<RunGameContentProfile.AreaGate> nearbyGate = nearbyGate(profile, player);
        if (nearbyGate.isEmpty()) {
            return TraversalResult.none();
        }

        RunGameContentProfile.AreaGate gate = nearbyGate.get();
        boolean unlocked = gate.requiredFlags().stream().allMatch(gameState.getStoryState()::hasFlag);
        if (!unlocked) {
            return TraversalResult.blocked("Gate sealed: " + gate.label());
        }
        if (gate.requiresAreaClear() && hasLivingEnemies(simulator)) {
            return TraversalResult.blocked("Gate blocked: clear the area first.");
        }

        gate.setFlags().forEach(gameState.getStoryState()::setFlag);
        gameState.getHubManager().updateHubState();
        return TraversalResult.transitioned("Traverse: " + gate.label());
    }

    private static Optional<RunGameContentProfile.AreaGate> nearbyGate(RunGameContentProfile profile, SimPlayer player) {
        if (profile == null || player == null) {
            return Optional.empty();
        }
        float playerCenterX = player.physics.x + player.physics.width * 0.5f;
        return profile.areaGates().stream()
                .filter(gate -> playerCenterX >= gate.minX() && playerCenterX <= gate.maxX())
                .min(Comparator.comparingDouble(gate -> Math.abs(((gate.minX() + gate.maxX()) * 0.5f) - playerCenterX)));
    }

    private static boolean hasLivingEnemies(GameSimulator simulator) {
        return simulator != null && simulator.getEnemies().stream().anyMatch(enemy -> enemy.isAlive() && !enemy.removed);
    }

    record TraversalResult(boolean transitioned, boolean blocked, String feedLine) {
        static TraversalResult none() {
            return new TraversalResult(false, false, null);
        }

        static TraversalResult blocked(String feedLine) {
            return new TraversalResult(false, true, feedLine);
        }

        static TraversalResult transitioned(String feedLine) {
            return new TraversalResult(true, false, feedLine);
        }
    }
}
