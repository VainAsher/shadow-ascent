package com.shadowascent.core;

import com.shadowascent.core.data.ContractMissionTemplateCatalog;
import com.shadowascent.core.data.GameDataContracts;
import com.shadowascent.core.data.MissionTemplateDefinition;
import com.shadowascent.core.data.QuestRewardEffectDefinition;
import com.shadowascent.core.data.SideQuestStepDefinition;
import com.shadowascent.core.data.WorldRegionStateDefinition;
import com.shadowascent.core.simulation.QuestEcologyEngine;
import com.shadowascent.core.simulation.QuestOpportunity;
import com.shadowascent.core.simulation.WorldSimulationEvent;
import com.shadowascent.core.simulation.WorldSimulationTick;
import com.shadowascent.core.simulation.WorldSimulationTickResult;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Deque;

/**
 * Manages mission lifecycle and progression.
 * Based on the mission system from indie-ninja-adventures.
 */
public class MissionManager {
    private static final long WORLD_SIMULATION_SEED = 20260507L;
    private static final int WORLD_SIGNAL_BOOTSTRAP_TICKS = 2;
    private static final int WORLD_SIGNAL_HISTORY_LIMIT = 48;

    private final StoryState storyState;
    private final GameDataContracts dataContracts;
    private final ContractMissionTemplateCatalog missionTemplateCatalog;
    private final Map<String, Mission> missionRegistry;
    private final WorldSimulationTick worldSimulationTick;
    private final Map<String, String> regionPlateausByRegionId;
    private final Map<String, Double> plateauSignalScores;
    private final Map<String, WorldSimulationEvent> latestSignalEventByPlateau;
    private final Deque<WorldSimulationEvent> recentSimulationEvents;
    private final Map<String, String> availabilityTraceByMissionId;
    private final QuestEcologyEngine ecologyEngine;
    private List<QuestOpportunity> currentEcologyOpportunities;

    public MissionManager(StoryState storyState, GameDataContracts dataContracts) {
        this.storyState = storyState;
        this.dataContracts = dataContracts;
        this.missionTemplateCatalog = ContractMissionTemplateCatalog.load(dataContracts);
        this.missionRegistry = new HashMap<>();
        this.worldSimulationTick = new WorldSimulationTick(WORLD_SIMULATION_SEED);
        this.regionPlateausByRegionId = indexRegionPlateaus(dataContracts);
        this.plateauSignalScores = new HashMap<>();
        this.latestSignalEventByPlateau = new HashMap<>();
        this.recentSimulationEvents = new ArrayDeque<>();
        this.availabilityTraceByMissionId = new LinkedHashMap<>();
        this.ecologyEngine = new QuestEcologyEngine();
        this.currentEcologyOpportunities = List.of();

        primeWorldSignals();
        initializeMissions();
        updateAvailableMissions();
    }

    private void initializeMissions() {
        List<String> canonicalMissionIds = List.of(
                "village_bonds",
                "dojo_practice",
                "mistwood_beast",
                "veil_request",
                "hollow_descent",
                "lantern_restoration",
                "monastery_arrival",
                "yin_yang_balance");

        for (String missionId : canonicalMissionIds) {
            MissionTemplateDefinition template = missionTemplateCatalog.template(missionId)
                    .orElseGet(() -> fallbackTemplate(missionId));
            addMission(new Mission(
                    template.id(),
                    template.displayName(),
                    template.description(),
                    template.region(),
                    template.objectives(),
                    template.requiredCounts()));
        }

        // Side-quest chain steps loaded from quests contract.
        for (Map.Entry<String, SideQuestStepDefinition> entry : missionTemplateCatalog.sideQuestStepsByMissionId().entrySet()) {
            String missionId = entry.getKey();
            MissionTemplateDefinition template = missionTemplateCatalog.template(missionId)
                    .orElseThrow(() -> new IllegalStateException("Missing side-quest template: " + missionId));
            addMission(new Mission(
                    template.id(),
                    template.displayName(),
                    template.description(),
                    template.region(),
                    template.objectives(),
                    template.requiredCounts()));
        }
    }

    private static MissionTemplateDefinition fallbackTemplate(String missionId) {
        return switch (missionId) {
            case "village_bonds" -> new MissionTemplateDefinition(
                    "village_bonds",
                    "Village Bonds",
                    "Connect with the villagers of Lantern Heights and learn about their concerns.",
                    "town",
                    List.of("talk_to_samson", "talk_to_sophia", "talk_to_marcel", "talk_to_hazel"),
                    Map.of("talk_to_samson", 1, "talk_to_sophia", 1, "talk_to_marcel", 1, "talk_to_hazel", 1));
            case "dojo_practice" -> new MissionTemplateDefinition(
                    "dojo_practice",
                    "Dojo Training",
                    "Train with Instructor Tai to learn basic combat techniques.",
                    "town",
                    List.of("practice_forms", "learn_dash"),
                    Map.of("practice_forms", 1, "learn_dash", 1));
            case "mistwood_beast" -> new MissionTemplateDefinition(
                    "mistwood_beast",
                    "Mistwood Beast",
                    "Defeat the corrupted beast threatening the village outskirts.",
                    "forest",
                    List.of("defeat_beast"),
                    Map.of("defeat_beast", 1));
            case "veil_request" -> new MissionTemplateDefinition(
                    "veil_request",
                    "The Veil's Call",
                    "Respond to the mysterious figure who offers protection from the growing darkness.",
                    "town",
                    List.of("hear_veil_offer", "accept_or_decline"),
                    Map.of("hear_veil_offer", 1, "accept_or_decline", 1));
            case "hollow_descent" -> new MissionTemplateDefinition(
                    "hollow_descent",
                    "Into the Hollow Depths",
                    "Descend into the underground caverns to find the source of the veil's power.",
                    "caves",
                    List.of("enter_hollow_depths", "find_veil_source"),
                    Map.of("enter_hollow_depths", 1, "find_veil_source", 1));
            case "lantern_restoration" -> new MissionTemplateDefinition(
                    "lantern_restoration",
                    "Restoring the Lanterns",
                    "Collect scattered lantern pieces to restore light to the depths.",
                    "caves",
                    List.of("collect_lantern_pieces", "restore_lanterns"),
                    Map.of("collect_lantern_pieces", 5, "restore_lanterns", 1));
            case "monastery_arrival" -> new MissionTemplateDefinition(
                    "monastery_arrival",
                    "The Ember Monastery",
                    "Reach the ancient monastery where the veil's influence began.",
                    "mountain",
                    List.of("climb_to_monastery", "enter_monastery"),
                    Map.of("climb_to_monastery", 1, "enter_monastery", 1));
            case "yin_yang_balance" -> new MissionTemplateDefinition(
                    "yin_yang_balance",
                    "Balance of Yin and Yang",
                    "Learn to balance the emotional forces that fuel the veil.",
                    "monastery",
                    List.of("understand_yin_yang", "achieve_balance"),
                    Map.of("understand_yin_yang", 1, "achieve_balance", 1));
            default -> new MissionTemplateDefinition(
                    missionId,
                    missionId,
                    "Fallback mission template.",
                    "unknown",
                    List.of("complete"),
                    Map.of("complete", 1));
        };
    }

    private void addMission(Mission mission) {
        missionRegistry.put(mission.getId(), mission);
        storyState.addMission(mission);
    }

    /**
     * Update which missions are available based on story state.
     */
    public void updateAvailableMissions() {
        advanceWorldSignals();

        // Act I mission availability logic

        // Village bonds always available at start
        Mission villageBonds = missionRegistry.get("village_bonds");
        if (villageBonds != null && villageBonds.getState() == Mission.MissionState.LOCKED) {
            villageBonds.setState(Mission.MissionState.AVAILABLE);
        }

        // Dojo practice available after talking to Samson
        Mission dojoPractice = missionRegistry.get("dojo_practice");
        if (dojoPractice != null && storyState.hasFlag("talked_to_samson") &&
            dojoPractice.getState() == Mission.MissionState.LOCKED) {
            dojoPractice.setState(Mission.MissionState.AVAILABLE);
        }

        // Mistwood beast available after village bonds
        Mission mistwoodBeast = missionRegistry.get("mistwood_beast");
        if (mistwoodBeast != null && storyState.hasFlag("village_bonds") &&
            mistwoodBeast.getState() == Mission.MissionState.LOCKED) {
            mistwoodBeast.setState(Mission.MissionState.AVAILABLE);
        }

        // Veil request available after mistwood beast
        Mission veilRequest = missionRegistry.get("veil_request");
        if (veilRequest != null && storyState.hasFlag("mistwood_beast_defeated") &&
            veilRequest.getState() == Mission.MissionState.LOCKED) {
            veilRequest.setState(Mission.MissionState.AVAILABLE);
        }

        // Act II missions available after veil request resolution
        if (storyState.hasFlag("veil_request_accepted") || storyState.hasFlag("veil_request_declined")) {
            Mission hollowDescent = missionRegistry.get("hollow_descent");
            if (hollowDescent != null && hollowDescent.getState() == Mission.MissionState.LOCKED) {
                hollowDescent.setState(Mission.MissionState.AVAILABLE);
            }

            Mission lanternRestoration = missionRegistry.get("lantern_restoration");
            if (lanternRestoration != null && lanternRestoration.getState() == Mission.MissionState.LOCKED) {
                lanternRestoration.setState(Mission.MissionState.AVAILABLE);
            }
        }

        // Act III missions available after Act II completion
        if (storyState.hasFlag("hollow_depths_explored")) {
            Mission monasteryArrival = missionRegistry.get("monastery_arrival");
            if (monasteryArrival != null && monasteryArrival.getState() == Mission.MissionState.LOCKED) {
                monasteryArrival.setState(Mission.MissionState.AVAILABLE);
            }
        }

        if (storyState.hasFlag("monastery_reached")) {
            Mission yinYangBalance = missionRegistry.get("yin_yang_balance");
            if (yinYangBalance != null && yinYangBalance.getState() == Mission.MissionState.LOCKED) {
                yinYangBalance.setState(Mission.MissionState.AVAILABLE);
            }
        }

        updateSideQuestAvailability();
    }

    private void updateSideQuestAvailability() {
        availabilityTraceByMissionId.clear();
        for (SideQuestStepDefinition step : missionTemplateCatalog.sideQuestStepsByMissionId().values()) {
            Mission mission = missionRegistry.get(step.missionId());
            if (mission == null) {
                continue;
            }

            SideQuestSignalEvaluation signal = evaluateSideQuestSignal(step);
            availabilityTraceByMissionId.put(step.missionId(), signal.trace());

            if (mission.getState() != Mission.MissionState.LOCKED) {
                continue;
            }
            if (isSideQuestStepAvailable(step, signal)) {
                mission.setState(Mission.MissionState.AVAILABLE);
            }
        }
    }

    private boolean isSideQuestStepAvailable(SideQuestStepDefinition step, SideQuestSignalEvaluation signal) {
        if (!isActWindowAllowed(step.actWindow())) {
            return false;
        }
        if (!step.plateau().isBlank() && !storyState.getCurrentPlateau().name().equals(step.plateau())) {
            return false;
        }
        for (String requiredFlag : step.requiredFlags()) {
            if (!storyState.hasFlag(requiredFlag)) {
                return false;
            }
        }
        return signal.passed();
    }

    private boolean isActWindowAllowed(String actWindow) {
        if (actWindow == null || actWindow.isBlank()) {
            return true;
        }
        StoryState.Act currentAct = storyState.getCurrentAct();
        String[] segments = actWindow.split("_TO_");
        for (String segment : segments) {
            StoryState.Act mapped = mapContractAct(segment.trim());
            if (mapped == null) {
                continue;
            }
            if (mapped == currentAct) {
                return true;
            }
        }
        return false;
    }

    private static StoryState.Act mapContractAct(String contractAct) {
        return switch (contractAct) {
            case "ACT_0", "ACT_1" -> StoryState.Act.ACT_I;
            case "ACT_2" -> StoryState.Act.ACT_II;
            case "ACT_3" -> StoryState.Act.ACT_III;
            case "ACT_4" -> StoryState.Act.ACT_IV;
            case "ACT_5" -> StoryState.Act.ACT_V;
            case "ACT_6" -> StoryState.Act.ACT_VI;
            case "ACT_7" -> StoryState.Act.ACT_VII;
            default -> null;
        };
    }

    /**
     * Start a mission if it's available.
     */
    public boolean startMission(String missionId) {
        updateAvailableMissions();
        Mission mission = storyState.getMission(missionId);
        if (mission != null && mission.getState() == Mission.MissionState.AVAILABLE) {
            mission.resetObjectiveProgress();
            storyState.setActiveMission(missionId);
            mission.incrementAttempts();
            return true;
        }
        return false;
    }

    /**
     * Update mission progress for an objective.
     */
    public void updateObjectiveProgress(String missionId, String objectiveId, int count) {
        Mission mission = storyState.getMission(missionId);
        if (mission != null && missionId.equals(storyState.getActiveMissionId())) {
            boolean progressed = mission.addObjectiveProgress(objectiveId, count);
            if (!progressed) {
                return;
            }
            applyObjectiveSideEffects(missionId, objectiveId);
            checkMissionCompletion(mission);
        }
    }

    /**
     * Check if the active mission should be completed.
     */
    private void checkMissionCompletion(Mission mission) {
        if (mission.areAllObjectivesComplete()) {
            String missionId = mission.getId();
            storyState.completeMission(missionId);
            onMissionCompleted(missionId);
        }
    }

    /**
     * Apply objective-level side effects that unlock downstream mission availability.
     */
    private void applyObjectiveSideEffects(String missionId, String objectiveId) {
        if ("village_bonds".equals(missionId)) {
            switch (objectiveId) {
                case "talk_to_samson" -> storyState.setFlag("talked_to_samson");
                case "talk_to_sophia" -> storyState.setFlag("talked_to_sophia");
                case "talk_to_marcel" -> storyState.setFlag("talked_to_marcel");
                case "talk_to_hazel" -> storyState.setFlag("talked_to_hazel");
                default -> { }
            }
        }

        if ("veil_request".equals(missionId) && "accept_or_decline".equals(objectiveId)) {
            // Default to acceptance unless gameplay explicitly sets a decline branch first.
            if (!storyState.hasFlag("veil_request_accepted") && !storyState.hasFlag("veil_request_declined")) {
                storyState.setFlag("veil_request_accepted");
            }
        }
    }

    /**
     * Handle mission completion effects.
     */
    private void onMissionCompleted(String missionId) {
        switch (missionId) {
            case "village_bonds" -> {
                storyState.setFlag("village_bonds");
                // Grant initial abilities
                storyState.grantAbility("basic_movement");
            }
            case "dojo_practice" -> {
                storyState.setFlag("dojo_practiced");
                storyState.grantAbility("dash");
            }
            case "mistwood_beast" -> {
                storyState.setFlag("mistwood_beast_defeated");
                storyState.grantAbility("combat_basic");
            }
            case "veil_request" -> {
                // This branches the story
                if (storyState.hasFlag("veil_request_accepted")) {
                    storyState.setFlag("npc_withdrawal_started");
                }
            }
            case "hollow_descent" -> {
                storyState.setFlag("hollow_depths_explored");
                storyState.advanceAct(); // Move to Act II
                storyState.setPlateau(StoryState.Plateau.HOLLOW_DEPTHS);
            }
            case "lantern_restoration" -> {
                storyState.setFlag("lanterns_restored");
                storyState.collectLantern(); // Add lantern to collection
            }
            case "monastery_arrival" -> {
                storyState.setFlag("monastery_reached");
                storyState.advanceAct(); // Move to Act III
                storyState.setPlateau(StoryState.Plateau.EMBER_MONASTERY);
            }
            case "yin_yang_balance" -> {
                storyState.setFlag("emotional_balance_achieved");
                // Grant emotional abilities
                storyState.grantAbility("emotional_insight");
            }
            default -> applySideQuestCompletionEffects(missionId);
        }

        updateAvailableMissions();
    }

    private void applySideQuestCompletionEffects(String missionId) {
        missionTemplateCatalog.sideQuestStep(missionId).ifPresent(step -> {
            for (String flag : step.setFlags()) {
                storyState.setFlag(flag);
            }
            for (QuestRewardEffectDefinition effect : step.rewardEffects()) {
                storyState.recordQuestRewardEffect(effect.type(), effect.effectId(), effect.magnitude());
            }
        });
    }

    /**
     * Get all available missions for the current story state.
     */
    public java.util.List<Mission> getAvailableMissions() {
        return storyState.getAllMissions().values().stream()
            .filter(Mission::isAvailable)
            .sorted(Comparator.comparingDouble(this::availabilityScoreForMission).reversed()
                    .thenComparing(Mission::getId))
            .toList();
    }

    public String availabilityTraceForMission(String missionId) {
        if (missionId == null || missionId.isBlank()) {
            return "";
        }
        return availabilityTraceByMissionId.getOrDefault(missionId, "");
    }

    public java.util.List<WorldSimulationEvent> recentSimulationEvents() {
        return List.copyOf(recentSimulationEvents);
    }

    public List<QuestOpportunity> currentEcologyOpportunities() {
        return currentEcologyOpportunities;
    }

    /**
     * Update mission timer (called each frame).
     */
    public void update(float deltaTime) {
        storyState.updateMissionTimer(deltaTime);
    }

    private void primeWorldSignals() {
        if (!hasWorldSignalContracts()) {
            return;
        }
        for (int i = 0; i < WORLD_SIGNAL_BOOTSTRAP_TICKS; i++) {
            WorldSimulationTickResult frame = worldSimulationTick.next(dataContracts);
            ingestSignalFrame(frame);
        }
    }

    private void advanceWorldSignals() {
        if (!hasWorldSignalContracts()) {
            return;
        }
        WorldSimulationTickResult frame = worldSimulationTick.next(dataContracts);
        ingestSignalFrame(frame);
    }

    private void ingestSignalFrame(WorldSimulationTickResult frame) {
        decaySignalScores();
        currentEcologyOpportunities = List.copyOf(ecologyEngine.generateOpportunities(frame));
        for (WorldSimulationEvent event : frame.events()) {
            recentSimulationEvents.addLast(event);
            while (recentSimulationEvents.size() > WORLD_SIGNAL_HISTORY_LIMIT) {
                recentSimulationEvents.removeFirst();
            }

            String plateau = regionPlateausByRegionId.get(normalize(event.sourceId()));
            if (plateau == null || plateau.isBlank()) {
                continue;
            }

            double existing = plateauSignalScores.getOrDefault(plateau, 0.0d);
            double contribution = signalContribution(event);
            plateauSignalScores.put(plateau, clamp01(existing + contribution));
            latestSignalEventByPlateau.put(plateau, event);
        }
    }

    private SideQuestSignalEvaluation evaluateSideQuestSignal(SideQuestStepDefinition step) {
        String plateau = normalize(step.plateau());
        if (plateau.isBlank() || !plateauSignalScores.containsKey(plateau)) {
            return new SideQuestSignalEvaluation(1.0d, 0.0d, true,
                    "plateau=" + plateau + " score=1.000 threshold=0.000 passed=true cause=no_sim_region");
        }

        double plateauSignal = plateauSignalScores.getOrDefault(plateau, 0.0d);
        double stepProgressionWeight = Math.min(0.20d, Math.max(0, step.stepOrder()) * 0.05d);
        double score = clamp01((plateauSignal * 0.85d) + stepProgressionWeight);
        double threshold = switch (Math.max(1, step.stepOrder())) {
            case 1 -> 0.20d;
            case 2 -> 0.30d;
            default -> 0.40d;
        };
        boolean passed = score >= threshold;
        String cause = describeSignalCause(plateau);
        String trace = String.format(
                Locale.ROOT,
                "plateau=%s score=%.3f threshold=%.3f passed=%s cause=%s",
                plateau,
                score,
                threshold,
                passed,
                cause);
        return new SideQuestSignalEvaluation(score, threshold, passed, trace);
    }

    private double availabilityScoreForMission(Mission mission) {
        if (mission == null) {
            return 0.0d;
        }
        SideQuestStepDefinition sideQuest = missionTemplateCatalog.sideQuestStep(mission.getId()).orElse(null);
        if (sideQuest != null) {
            return evaluateSideQuestSignal(sideQuest).score();
        }
        String plateau = plateauFromRegion(mission.getRegion());
        if (plateau.isBlank() || !plateauSignalScores.containsKey(plateau)) {
            return 0.5d;
        }
        double plateauSignal = plateauSignalScores.getOrDefault(plateau, 0.0d);
        return clamp01(0.35d + (plateauSignal * 0.65d));
    }

    private static String plateauFromRegion(String region) {
        return switch (region == null ? "" : region.trim().toLowerCase(Locale.ROOT)) {
            case "town", "forest", "summit" -> "LANTERN_HEIGHTS";
            case "caves" -> "HOLLOW_DEPTHS";
            case "monastery", "mountain" -> "EMBER_MONASTERY";
            case "skyroad" -> "WINDING_SKYROAD";
            case "mirror" -> "MIRROR_SUMMIT";
            case "beacon" -> "BEACON_CLIFF";
            default -> "";
        };
    }

    private String describeSignalCause(String plateau) {
        WorldSimulationEvent event = latestSignalEventByPlateau.get(plateau);
        if (event == null) {
            return "no_recent_event";
        }
        double delta = round3(Math.abs(event.currentValue() - event.previousValue()));
        return event.eventType() + ":" + event.metric() + ":delta=" + delta;
    }

    private boolean hasWorldSignalContracts() {
        return dataContracts != null
                && !dataContracts.worldRegionStatesById().isEmpty()
                && !regionPlateausByRegionId.isEmpty();
    }

    private static Map<String, String> indexRegionPlateaus(GameDataContracts dataContracts) {
        Map<String, String> byRegionId = new HashMap<>();
        if (dataContracts == null) {
            return byRegionId;
        }
        for (WorldRegionStateDefinition region : dataContracts.worldRegionStatesById().values()) {
            String regionId = normalize(region.id());
            String plateau = normalize(region.plateauId());
            if (!regionId.isBlank() && !plateau.isBlank()) {
                byRegionId.put(regionId, plateau);
            }
        }
        return byRegionId;
    }

    private void decaySignalScores() {
        for (Map.Entry<String, Double> entry : plateauSignalScores.entrySet()) {
            entry.setValue(clamp01(entry.getValue() * 0.72d));
        }
    }

    private static double signalContribution(WorldSimulationEvent event) {
        double deltaMagnitude = Math.abs(event.currentValue() - event.previousValue());
        double eventBase = "pressure_spike".equals(event.eventType()) ? 0.10d : 0.03d;
        double metricWeight = switch (event.metric()) {
            case "corruption_pressure", "route_risk" -> 0.03d;
            case "faction_tension" -> 0.02d;
            case "prosperity_pressure" -> 0.015d;
            default -> 0.01d;
        };
        double deltaWeight = Math.min(0.06d, deltaMagnitude * 3.0d);
        return clamp01(eventBase + metricWeight + deltaWeight);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static double clamp01(double value) {
        return Math.max(0.0d, Math.min(1.0d, value));
    }

    private static double round3(double value) {
        return Math.round(value * 1000.0d) / 1000.0d;
    }

    private record SideQuestSignalEvaluation(
            double score,
            double threshold,
            boolean passed,
            String trace) {
    }
}
