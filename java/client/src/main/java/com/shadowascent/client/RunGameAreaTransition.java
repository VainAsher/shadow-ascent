package com.shadowascent.client;

import com.shadowascent.client.ui.UiText;
import com.shadowascent.client.world.EncounterSpec;
import com.shadowascent.client.world.RoomTransitionSpec;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.Mission;
import com.shadowascent.core.data.SideQuestStepDefinition;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.SimPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class RunGameAreaTransition {

    private RunGameAreaTransition() {
    }

    static Optional<String> describeNearbyGate(GameState gameState, RunGameContentProfile profile, SimPlayer player, GameSimulator simulator) {
        Optional<RoomTransitionSpec> nearbyRoomTransition = nearbyRoomTransition(profile, player);
        if (nearbyRoomTransition.isPresent()) {
            RoomTransitionSpec transition = nearbyRoomTransition.get();
            boolean unlocked = transition.requiredFlags().stream().allMatch(gameState.getStoryState()::hasFlag);
            if (!unlocked) {
                return Optional.of(blockedReasonForType(transition));
            }
            if (requiresEncounterClear(transition) && hasBlockingEncounter(gameState, profile, simulator)) {
                return Optional.of("[E] Clear the room to continue to " + UiText.areaName(transition.targetAreaId()));
            }
            return Optional.of("[E] Continue -> " + UiText.areaName(transition.targetAreaId()));
        }
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
        Optional<RoomTransitionSpec> nearbyRoomTransition = nearbyRoomTransition(profile, player);
        if (nearbyRoomTransition.isPresent()) {
            RoomTransitionSpec transition = nearbyRoomTransition.get();
            boolean unlocked = transition.requiredFlags().stream().allMatch(gameState.getStoryState()::hasFlag);
            if (!unlocked) {
                return TraversalResult.blocked(blockedReasonForType(transition));
            }
            if (requiresEncounterClear(transition) && hasBlockingEncounter(gameState, profile, simulator)) {
                return TraversalResult.blocked("Encounter gating: clear the room before leaving.");
            }

            applyResolvedEncounterFlags(gameState, profile, simulator);
            transition.setFlags().forEach(gameState.getStoryState()::setFlag);
            gameState.setCurrentRoomId(transition.targetRoomId());
            gameState.setPendingRoomSpawnId(transition.targetSpawnId());
            gameState.getHubManager().updateHubState();
            return TraversalResult.transitioned("Traverse: " + UiText.areaName(transition.targetAreaId()));
        }

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

    private static Optional<RoomTransitionSpec> nearbyRoomTransition(RunGameContentProfile profile, SimPlayer player) {
        if (profile == null || player == null) {
            return Optional.empty();
        }
        float playerCenterX = player.physics.x + player.physics.width * 0.5f;
        float playerCenterY = player.physics.y + player.physics.height * 0.5f;
        return profile.roomTransitions().stream()
                .filter(transition -> playerCenterX >= transition.minX() && playerCenterX <= transition.maxX())
                .filter(transition -> withinVerticalBand(transition, playerCenterY))
                .min(Comparator.comparingDouble(transition -> Math.abs(((transition.minX() + transition.maxX()) * 0.5f) - playerCenterX)));
    }

    private static boolean withinVerticalBand(RoomTransitionSpec transition, float playerCenterY) {
        if (transition == null) {
            return false;
        }
        if (transition.minY() == null && transition.maxY() == null) {
            return true;
        }
        float minY = transition.minY() == null ? Float.NEGATIVE_INFINITY : transition.minY();
        float maxY = transition.maxY() == null ? Float.POSITIVE_INFINITY : transition.maxY();
        return playerCenterY >= minY && playerCenterY <= maxY;
    }

    private static String blockedReasonForType(RoomTransitionSpec transition) {
        if (transition == null) {
            return "[E] Route blocked.";
        }
        return switch (transition.type() == null ? "" : transition.type().toLowerCase(java.util.Locale.ROOT)) {
            case "mission_gate" -> "[E] Mission required: accept a mission to continue toward " + UiText.areaName(transition.targetAreaId());
            case "npc_handoff_gate" -> "[E] Story bond needed: deepen your bonds with the village before continuing";
            case "encounter_gate" -> "[E] Encounter gating: clear the room to open the path forward";
            case "return_gate" -> "[E] Path sealed: defeat the enemy to unlock the return route";
            default -> "[E] Route blocked: " + UiText.areaName(transition.targetAreaId());
        };
    }

    private static boolean requiresEncounterClear(RoomTransitionSpec transition) {
        return transition != null && ("encounter_gate".equalsIgnoreCase(transition.type())
                || "return_gate".equalsIgnoreCase(transition.type()));
    }

    private static boolean hasBlockingEncounter(GameState gameState, RunGameContentProfile profile, GameSimulator simulator) {
        if (profile == null) {
            return hasLivingEnemies(simulator);
        }
        List<EncounterSpec> activeEncounters = unresolvedEncounters(gameState, profile);
        if (activeEncounters.isEmpty()) {
            return hasLivingEnemies(simulator);
        }
        return activeEncounters.stream().anyMatch(encounter -> !isEncounterCleared(encounter, simulator));
    }

    private static void applyResolvedEncounterFlags(GameState gameState, RunGameContentProfile profile, GameSimulator simulator) {
        if (gameState == null || profile == null) {
            return;
        }
        for (EncounterSpec encounter : unresolvedEncounters(gameState, profile)) {
            if (isEncounterCleared(encounter, simulator)) {
                encounter.setFlags().forEach(gameState.getStoryState()::setFlag);
                advanceCombatQuestForEncounter(gameState, encounter);
            }
        }
        gameState.getMissionManager().updateAvailableMissions();
    }

    private static void advanceCombatQuestForEncounter(GameState gameState, EncounterSpec encounter) {
        String missionId = gameState.getStoryState().getActiveMissionId();
        if (missionId == null) {
            return;
        }
        SideQuestStepDefinition step = gameState.getMissionManager().sideQuestStep(missionId).orElse(null);
        if (step == null) {
            return;
        }
        boolean encounterFlagsMatchQuest = encounter.setFlags().stream().anyMatch(step.setFlags()::contains);
        if (!encounterFlagsMatchQuest) {
            return;
        }
        Mission mission = gameState.getStoryState().getMission(missionId);
        if (mission == null) {
            return;
        }
        mission.getObjectives().stream()
                .filter(obj -> !mission.isObjectiveComplete(obj))
                .findFirst()
                .ifPresent(obj -> gameState.getMissionManager().updateObjectiveProgress(missionId, obj, 999));
    }

    private static List<EncounterSpec> unresolvedEncounters(GameState gameState, RunGameContentProfile profile) {
        if (profile == null) {
            return List.of();
        }
        return profile.encounters().stream()
                .filter(encounter -> encounter.setFlags().stream().anyMatch(flag -> !gameState.getStoryState().hasFlag(flag)))
                .toList();
    }

    private static boolean isEncounterCleared(EncounterSpec encounter, GameSimulator simulator) {
        if (encounter == null) {
            return true;
        }
        if (!"enemy_ids".equalsIgnoreCase(encounter.clearType())) {
            return !hasLivingEnemies(simulator);
        }
        if (simulator == null || encounter.enemyIds().isEmpty()) {
            return false;
        }
        return encounter.enemyIds().stream().allMatch(enemyId -> simulator.getEnemies().stream()
                .filter(enemy -> enemyId.equals(enemy.enemyId))
                .findFirst()
                .map(enemy -> !enemy.isAlive() || enemy.removed)
                .orElse(false));
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
