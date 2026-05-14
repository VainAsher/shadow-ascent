package com.shadowascent.client;

import com.shadowascent.client.ui.UiText;
import com.shadowascent.core.GameState;
import com.shadowascent.core.Mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Applies authored mission start/progress rules for the LibGDX production client.
 */
public final class RunGameMissionInteraction {

    private static final Map<String, Set<String>> STARTER_NPCS_BY_MISSION = Map.of(
            "village_bonds", Set.of("SAMSON", "SOPHIA", "MARCEL", "HAZEL"),
            "dojo_practice", Set.of("INSTRUCTOR_TAI", "TAI"),
            "veil_request", Set.of("VEIL_MAIDEN", "LINZI"),
            "lantern_restoration", Set.of("HAZEL"),
            "yin_yang_balance", Set.of("SOPHIA"));

    private RunGameMissionInteraction() {
    }

    public static List<String> orderedRelevantNpcIds(GameState gameState) {
        if (gameState == null) {
            return List.of();
        }

        List<String> npcIds = new ArrayList<>();
        Mission activeMission = activeMission(gameState);
        if (activeMission != null) {
            npcIds.addAll(starterNpcIdsForMission(gameState, activeMission.getId()));
        } else {
            for (Mission mission : gameState.getMissionManager().getAvailableMissions()) {
                for (String npcId : starterNpcIdsForMission(gameState, mission.getId())) {
                    if (!npcIds.contains(npcId)) {
                        npcIds.add(npcId);
                    }
                }
            }
        }
        return List.copyOf(npcIds);
    }

    static InteractionResult applyNpcInteraction(GameState gameState, String npcId) {
        if (gameState == null || npcId == null || npcId.isBlank()) {
            return new InteractionResult(List.of(), false, false, null);
        }

        List<String> feedLines = new ArrayList<>();
        Mission startedMission = maybeStartMission(gameState, npcId);
        if (startedMission != null) {
            feedLines.add("Mission started: " + startedMission.getDisplayName());
        }

        ObjectiveAdvanceResult objectiveResult = maybeAdvanceNpcObjective(gameState, npcId);
        if (objectiveResult.advanced()) {
            feedLines.add("Objective advanced: " + UiText.objectiveToken(objectiveResult.objectiveId()));
        }
        if (objectiveResult.completedMissionTitle() != null) {
            feedLines.add("Mission complete: " + objectiveResult.completedMissionTitle());
        }

        return new InteractionResult(
                List.copyOf(feedLines),
                startedMission != null,
                objectiveResult.advanced(),
                startedMission == null ? null : startedMission.getId());
    }

    static String interactionPrompt(GameState gameState, String npcId, String npcName) {
        String label = UiText.humanizeToken(npcName);
        Mission activeMission = activeMission(gameState);
        if (activeMission != null) {
            String objectiveId = objectiveIdForNpc(gameState, activeMission, npcId);
            if (objectiveId != null && !activeMission.isObjectiveComplete(objectiveId)) {
                return "[E] Talk to " + label + " and advance " + activeMission.getDisplayName();
            }
            return "[E] Talk to " + label;
        }

        Mission availableMission = firstAvailableMissionForNpc(gameState, npcId);
        if (availableMission != null) {
            return "[E] Talk to " + label + " and start " + availableMission.getDisplayName();
        }
        return "[E] Talk to " + label;
    }

    private static Mission maybeStartMission(GameState gameState, String npcId) {
        if (gameState.getStoryState().getActiveMissionId() != null) {
            return null;
        }
        Mission mission = firstAvailableMissionForNpc(gameState, npcId);
        if (mission == null || !gameState.getMissionManager().startMission(mission.getId())) {
            return null;
        }
        refreshStoryState(gameState);
        return mission;
    }

    private static ObjectiveAdvanceResult maybeAdvanceNpcObjective(GameState gameState, String npcId) {
        Mission activeMission = activeMission(gameState);
        if (activeMission == null) {
            return ObjectiveAdvanceResult.none();
        }

        String objectiveId = objectiveIdForNpc(gameState, activeMission, npcId);
        if (objectiveId == null || activeMission.isObjectiveComplete(objectiveId)) {
            return ObjectiveAdvanceResult.none();
        }

        String missionId = activeMission.getId();
        gameState.getMissionManager().updateObjectiveProgress(missionId, objectiveId, 1);
        refreshStoryState(gameState);

        Mission updatedMission = gameState.getStoryState().getMission(missionId);
        String completedMissionTitle = updatedMission != null && updatedMission.isCompleted()
                ? updatedMission.getDisplayName()
                : null;
        return new ObjectiveAdvanceResult(true, objectiveId, completedMissionTitle);
    }

    private static Mission activeMission(GameState gameState) {
        if (gameState == null) {
            return null;
        }
        String activeMissionId = gameState.getStoryState().getActiveMissionId();
        return activeMissionId == null ? null : gameState.getStoryState().getMission(activeMissionId);
    }

    private static Mission firstAvailableMissionForNpc(GameState gameState, String npcId) {
        if (gameState == null) {
            return null;
        }
        String normalizedNpcId = npcId.toUpperCase(Locale.ROOT);
        return gameState.getMissionManager().getAvailableMissions().stream()
                .filter(mission -> missionStarterMatchesNpc(gameState, mission.getId(), normalizedNpcId))
                .findFirst()
                .orElse(null);
    }

    private static boolean missionStarterMatchesNpc(GameState gameState, String missionId, String npcId) {
        return starterNpcIdsForMission(gameState, missionId).stream().anyMatch(npcId::equalsIgnoreCase);
    }

    private static String objectiveIdForNpc(GameState gameState, Mission mission, String npcId) {
        String normalizedNpcId = npcId.toUpperCase(Locale.ROOT);
        return switch (mission.getId()) {
            case "village_bonds" -> Map.of(
                    "SAMSON", "talk_to_samson",
                    "SOPHIA", "talk_to_sophia",
                    "MARCEL", "talk_to_marcel",
                    "HAZEL", "talk_to_hazel").get(normalizedNpcId);
            case "dojo_practice" -> {
                if ("INSTRUCTOR_TAI".equals(normalizedNpcId) || "TAI".equals(normalizedNpcId)) {
                    yield mission.isObjectiveComplete("practice_forms") ? "learn_dash" : "practice_forms";
                }
                yield null;
            }
            case "veil_request" -> {
                if ("VEIL_MAIDEN".equals(normalizedNpcId) || "LINZI".equals(normalizedNpcId)) {
                    yield mission.isObjectiveComplete("hear_veil_offer") ? "accept_or_decline" : "hear_veil_offer";
                }
                yield null;
            }
            default -> {
                boolean sameChainNpc = gameState.getMissionManager()
                        .sideQuestStep(mission.getId())
                        .map(step -> normalizedNpcId.equalsIgnoreCase(step.chainNpc()))
                        .orElse(false);
                if (!sameChainNpc) {
                    yield null;
                }
                yield mission.getObjectives().stream()
                        .filter(objective -> !mission.isObjectiveComplete(objective))
                        .filter(RunGameMissionInteraction::isNpcResolvableObjective)
                        .findFirst()
                        .orElse(null);
            }
        };
    }

    private static boolean isNpcResolvableObjective(String objectiveId) {
        String normalized = objectiveId == null ? "" : objectiveId.toLowerCase(Locale.ROOT);
        return normalized.contains("resolve")
                || normalized.contains("read")
                || normalized.contains("follow")
                || normalized.contains("talk");
    }

    private static void refreshStoryState(GameState gameState) {
        gameState.getHubManager().updateHubState();
        gameState.getMissionManager().updateAvailableMissions();
    }

    private static List<String> starterNpcIdsForMission(GameState gameState, String missionId) {
        Set<String> starters = STARTER_NPCS_BY_MISSION.get(missionId);
        if (starters != null) {
            return starters.stream().sorted().toList();
        }
        return gameState.getMissionManager()
                .sideQuestStep(missionId)
                .map(step -> Stream.of(step.chainNpc()).filter(id -> id != null && !id.isBlank()).toList())
                .orElse(List.of());
    }

    record InteractionResult(
            List<String> feedLines,
            boolean missionStarted,
            boolean objectiveAdvanced,
            String startedMissionId) {
    }

    private record ObjectiveAdvanceResult(
            boolean advanced,
            String objectiveId,
            String completedMissionTitle) {
        private static ObjectiveAdvanceResult none() {
            return new ObjectiveAdvanceResult(false, null, null);
        }
    }
}
