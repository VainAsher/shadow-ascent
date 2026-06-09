package com.shadowascent.core.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowascent.core.StoryState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Loads and validates narrative/world data contracts from the repository data directory.
 *
 * This turns high-level design JSON into executable runtime rules:
 * - beat ordering and progression hints
 * - story flag validation
 * - plateau route validation
 * - NPC eligibility per plateau
 */
public final class GameDataContracts {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, String> CONTRACT_FILES;
    static {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("story_flags", "story_flags.json");
        files.put("narrative_beats", "narrative_beats.json");
        files.put("plateaus", "plateaus.json");
        files.put("area_catalog", "area_catalog.json");
        files.put("npc_registry", "npc_registry.json");
        files.put("dialogue", "dialogue.json");
        files.put("quests", "quests.json");
        files.put("adaptation_rules", "adaptation_rules.json");
        files.put("chunk_grammar", "chunk_grammar.json");
        files.put("world_state", "world_state.json");
        files.put("faction_state", "faction_state.json");
        files.put("settlement_state", "settlement_state.json");
        CONTRACT_FILES = Map.copyOf(files);
    }

    private final Path rootDirectory;
    private final Map<String, JsonNode> documents;
    private final List<String> validationIssues;

    private final Set<String> criticalFlags;
    private final Set<String> areaIds;
    private final Set<String> dialogueIds;
    private final Map<String, DialogueLineDefinition> dialogueLinesById;
    private final Map<String, List<DialogueLineDefinition>> dialogueLinesBySpeaker;
    private final Map<String, NpcDefinition> npcDefinitions;
    private final Map<String, Set<String>> npcEligiblePlateaus;
    private final Map<String, List<String>> criticalRoutesByPlateau;
    private final Map<String, BeatDefinition> beatsById;
    private final Map<String, List<BeatDefinition>> beatsByPlateau;
    private final Map<String, String> routeHintsByMissionId;
    private final Map<String, String> plateauByMissionId;
    private final Set<String> mainlineMissionIds;
    private final Map<String, WorldRegionStateDefinition> worldRegionStatesById;
    private final Map<String, FactionStateDefinition> factionStatesById;
    private final Map<String, SettlementStateDefinition> settlementStatesById;

    private static final Set<String> SUPPORTED_REWARD_EFFECT_TYPES = Set.of(
            "MAP_UPGRADE",
            "CHARM_UNLOCK",
            "PASSIVE_BUFF",
            "WEAPON_UPGRADE",
            "COSMETIC_UNLOCK",
            "ITEM_UNLOCK",
            "UNKNOWN_REWARD");

    private static final Map<String, Set<String>> REQUIRED_REWARD_PAYLOAD_KEYS_BY_TYPE;
    static {
        Map<String, Set<String>> required = new LinkedHashMap<>();
        required.put("MAP_UPGRADE", Set.of("id", "map_layer"));
        required.put("CHARM_UNLOCK", Set.of("id", "charm_key"));
        required.put("PASSIVE_BUFF", Set.of("id", "buff_family"));
        required.put("WEAPON_UPGRADE", Set.of("id", "upgrade_path"));
        required.put("COSMETIC_UNLOCK", Set.of("id", "cosmetic_slot"));
        required.put("ITEM_UNLOCK", Set.of("id", "item_key"));
        required.put("UNKNOWN_REWARD", Set.of("id"));
        REQUIRED_REWARD_PAYLOAD_KEYS_BY_TYPE = Map.copyOf(required);
    }

    private GameDataContracts(
            Path rootDirectory,
            Map<String, JsonNode> documents,
            List<String> initialIssues) {
        this.rootDirectory = rootDirectory;
        this.documents = Map.copyOf(documents);
        this.validationIssues = new ArrayList<>(initialIssues);

        this.criticalFlags = new LinkedHashSet<>();
        this.areaIds = new LinkedHashSet<>();
        this.dialogueIds = new LinkedHashSet<>();
        this.dialogueLinesById = new LinkedHashMap<>();
        this.dialogueLinesBySpeaker = new LinkedHashMap<>();
        this.npcDefinitions = new LinkedHashMap<>();
        this.npcEligiblePlateaus = new LinkedHashMap<>();
        this.criticalRoutesByPlateau = new LinkedHashMap<>();
        this.beatsById = new LinkedHashMap<>();
        this.beatsByPlateau = new LinkedHashMap<>();
        this.routeHintsByMissionId = new LinkedHashMap<>();
        this.plateauByMissionId = new LinkedHashMap<>();
        this.mainlineMissionIds = new LinkedHashSet<>();
        this.worldRegionStatesById = new LinkedHashMap<>();
        this.factionStatesById = new LinkedHashMap<>();
        this.settlementStatesById = new LinkedHashMap<>();

        buildIndexes();
        validateCrossReferences();
    }

    public static GameDataContracts loadDefault() {
        for (Path candidate : List.of(
                Paths.get("data"),
                Paths.get("..", "data"),
                Paths.get("..", "..", "data"))) {
            if (Files.isDirectory(candidate)) {
                return load(candidate);
            }
        }
        return new GameDataContracts(
                Paths.get("data"),
                Map.of(),
                List.of("No data contract directory found. Expected ./data or parent variants."));
    }

    public static GameDataContracts load(Path rootDirectory) {
        Map<String, JsonNode> docs = new LinkedHashMap<>();
        List<String> issues = new ArrayList<>();

        if (rootDirectory == null || !Files.isDirectory(rootDirectory)) {
            issues.add("Data contract directory missing: " + rootDirectory);
            return new GameDataContracts(rootDirectory, docs, issues);
        }

        for (Map.Entry<String, String> entry : CONTRACT_FILES.entrySet()) {
            String key = entry.getKey();
            Path file = rootDirectory.resolve(entry.getValue());
            if (!Files.exists(file)) {
                issues.add("Missing contract file: " + file);
                continue;
            }
            try {
                docs.put(key, MAPPER.readTree(file.toFile()));
            } catch (IOException ex) {
                issues.add("Failed to parse contract file: " + file + " (" + ex.getMessage() + ")");
            }
        }

        return new GameDataContracts(rootDirectory, docs, issues);
    }

    public Path rootDirectory() {
        return rootDirectory;
    }

    public boolean isLoaded() {
        return !documents.isEmpty();
    }

    public boolean isValid() {
        return validationIssues.isEmpty();
    }

    public List<String> validationIssues() {
        return List.copyOf(validationIssues);
    }

    public Set<String> criticalFlags() {
        return Set.copyOf(criticalFlags);
    }

    public Set<String> areaIds() {
        return Set.copyOf(areaIds);
    }

    public Map<String, NpcDefinition> npcDefinitions() {
        return Map.copyOf(npcDefinitions);
    }

    public Map<String, BeatDefinition> beatsById() {
        return Map.copyOf(beatsById);
    }

    public Map<String, WorldRegionStateDefinition> worldRegionStatesById() {
        return Map.copyOf(worldRegionStatesById);
    }

    public Map<String, FactionStateDefinition> factionStatesById() {
        return Map.copyOf(factionStatesById);
    }

    public Map<String, SettlementStateDefinition> settlementStatesById() {
        return Map.copyOf(settlementStatesById);
    }

    public Optional<JsonNode> contractDocument(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(documents.get(key.trim().toLowerCase(Locale.ROOT)));
    }

    public Optional<BeatDefinition> beat(String beatId) {
        return Optional.ofNullable(beatsById.get(beatId));
    }

    public Optional<DialogueLineDefinition> dialogueLine(String dialogueId) {
        if (dialogueId == null || dialogueId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(dialogueLinesById.get(dialogueId));
    }

    public Optional<String> routeHintForMission(String missionId) {
        if (missionId == null || missionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(routeHintsByMissionId.get(missionId));
    }

    public boolean isMainlineMission(String missionId) {
        if (missionId == null || missionId.isBlank()) {
            return false;
        }
        return mainlineMissionIds.contains(missionId);
    }

    public Optional<String> plateauForMission(String missionId) {
        if (missionId == null || missionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(plateauByMissionId.get(missionId));
    }

    public List<BeatDefinition> beatsForPlateau(String plateauId) {
        return beatsByPlateau.getOrDefault(normalize(plateauId), List.of());
    }

    public List<String> criticalRouteForPlateau(String plateauId) {
        return criticalRoutesByPlateau.getOrDefault(normalize(plateauId), List.of());
    }

    public boolean isNpcAllowedForPlateau(String npcId, String plateauId) {
        Set<String> eligible = npcEligiblePlateaus.get(npcId);
        if (eligible == null || eligible.isEmpty()) {
            return false;
        }
        String normalizedPlateau = normalize(plateauId);
        return eligible.contains("GLOBAL") || eligible.contains(normalizedPlateau);
    }

    /**
     * Derives the currently relevant NPC schedule from unlocked narrative beats.
     * Prefers unresolved upcoming beats and falls back to the most recent unlocked beat context.
     */
    public Set<String> scheduledNpcIds(StoryState state) {
        if (state == null) {
            return Set.of();
        }

        String activePlateau = normalize(state.getCurrentPlateau().name());
        List<BeatDefinition> candidates = new ArrayList<>();
        candidates.addAll(beatsForPlateau("GLOBAL"));
        candidates.addAll(beatsForPlateau(activePlateau));
        candidates.sort(Comparator.comparingInt(BeatDefinition::routeOrder).thenComparing(BeatDefinition::id));

        List<BeatDefinition> unlocked = candidates.stream()
                .filter(beat -> beat.requiredFlags().stream().allMatch(state::hasFlag))
                .toList();

        List<BeatDefinition> unresolved = unlocked.stream()
                .filter(beat -> beat.setFlags().isEmpty() || beat.setFlags().stream().anyMatch(flag -> !state.hasFlag(flag)))
                .toList();

        LinkedHashSet<String> schedule = new LinkedHashSet<>();
        unresolved.stream().limit(2).forEach(beat -> schedule.addAll(beat.npcIds()));

        if (schedule.isEmpty()) {
            nextCriticalBeat(state).ifPresent(beat -> schedule.addAll(beat.npcIds()));
        }

        if (schedule.isEmpty()) {
            unlocked.stream()
                    .max(Comparator.comparingInt(BeatDefinition::routeOrder).thenComparing(BeatDefinition::id))
                    .ifPresent(beat -> schedule.addAll(beat.npcIds()));
        }

        schedule.removeIf(npcId -> !isNpcAllowedForPlateau(npcId, activePlateau));
        return Set.copyOf(schedule);
    }

    /**
     * Returns NPCs whose faction has tension above 0.5 and holds territory on the active plateau.
     * These NPCs appear in the hub schedule regardless of narrative beat state.
     */
    public Set<String> factionInfluencedNpcIds(StoryState state) {
        if (state == null) return Set.of();
        String activePlateau = normalize(state.getCurrentPlateau().name());
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (FactionStateDefinition faction : factionStatesById.values()) {
            if (faction.tension() <= 0.5) continue;
            if (!faction.territoryPlateaus().contains(activePlateau)) continue;
            for (NpcDefinition npc : npcDefinitions.values()) {
                if (!faction.id().equals(npc.factionId())) continue;
                if (!isNpcAllowedForPlateau(npc.id(), activePlateau)) continue;
                result.add(npc.id());
            }
        }
        return Set.copyOf(result);
    }

    /**
     * Returns the next critical beat that is currently unlocked by story flags.
     */
    public Optional<BeatDefinition> nextCriticalBeat(StoryState state) {
        if (state == null) {
            return Optional.empty();
        }

        String activePlateau = normalize(state.getCurrentPlateau().name());
        Stream<BeatDefinition> candidateStream = Stream.concat(
                beatsForPlateau("GLOBAL").stream(),
                beatsForPlateau(activePlateau).stream());

        return candidateStream
                .filter(BeatDefinition::isCriticalPathBeat)
                .filter(beat -> beat.requiredFlags().stream().allMatch(state::hasFlag))
                .filter(beat -> beat.setFlags().isEmpty() || beat.setFlags().stream().anyMatch(flag -> !state.hasFlag(flag)))
                .min(Comparator.comparingInt(BeatDefinition::routeOrder).thenComparing(BeatDefinition::id));
    }

    /**
     * Select a contract-backed dialogue line for an NPC in current story context.
     */
    public Optional<DialogueLineDefinition> selectNpcDialogueLine(StoryState state, String npcId) {
        if (state == null || npcId == null || npcId.isBlank()) {
            return Optional.empty();
        }
        List<DialogueLineDefinition> speakerLines = dialogueLinesBySpeaker.get(normalize(npcId));
        if (speakerLines == null || speakerLines.isEmpty()) {
            return Optional.empty();
        }

        String activePlateau = normalize(state.getCurrentPlateau().name());
        String nextBeatId = nextCriticalBeat(state).map(BeatDefinition::id).orElse("");
        DialogueLineDefinition bestLine = null;
        int bestScore = Integer.MIN_VALUE;

        for (DialogueLineDefinition line : speakerLines) {
            if (!lineApplicableForState(line, state, activePlateau)) {
                continue;
            }
            int score = scoreDialogueLine(line, nextBeatId, activePlateau, state);
            if (score > bestScore) {
                bestScore = score;
                bestLine = line;
            } else if (score == bestScore && bestLine != null
                    && line.id().compareTo(bestLine.id()) < 0) {
                bestLine = line;
            }
        }
        return Optional.ofNullable(bestLine);
    }

    public String summaryLine() {
        return "contracts_loaded=" + isLoaded()
                + " valid=" + isValid()
                + " beats=" + beatsById.size()
                + " critical_flags=" + criticalFlags.size()
                + " plateaus=" + criticalRoutesByPlateau.size()
                + " world_regions=" + worldRegionStatesById.size()
                + " factions=" + factionStatesById.size()
                + " settlements=" + settlementStatesById.size();
    }

    private void buildIndexes() {
        for (JsonNode flagNode : arrayNode(doc("story_flags"), "flags")) {
            String id = text(flagNode, "id");
            if (!id.isEmpty()) {
                criticalFlags.add(id);
            }
        }

        for (JsonNode areaNode : arrayNode(doc("area_catalog"), "area_instances")) {
            String id = text(areaNode, "id");
            if (!id.isEmpty()) {
                areaIds.add(id);
            }
        }

        for (JsonNode dialogueNode : arrayNode(doc("dialogue"), "lines")) {
            String id = text(dialogueNode, "id");
            if (!id.isEmpty()) {
                dialogueIds.add(id);
            }
            String speakerId = text(dialogueNode, "speaker");
            String dialogueText = text(dialogueNode, "text");
            if (id.isEmpty() || speakerId.isEmpty() || dialogueText.isEmpty()) {
                continue;
            }

            ParsedDialogueConditions parsedConditions = parseDialogueConditions(dialogueNode.get("conditions"));
            DialogueLineDefinition line = new DialogueLineDefinition(
                    id,
                    normalize(speakerId),
                    dialogueText,
                    normalize(text(dialogueNode, "plateau")),
                    normalize(text(dialogueNode, "line_role")),
                    parseBeatRefs(dialogueNode.get("beat_ref")),
                    stringArray(dialogueNode.get("tags")),
                    Set.copyOf(parsedConditions.requiredFlags),
                    Set.copyOf(parsedConditions.forbiddenFlags),
                    Set.copyOf(parsedConditions.requiredAbilities),
                    Set.copyOf(parsedConditions.forbiddenAbilities),
                    Set.copyOf(parsedConditions.requiredHubStates));
            dialogueLinesById.put(id, line);
            dialogueLinesBySpeaker.computeIfAbsent(line.speakerId(), ignored -> new ArrayList<>()).add(line);
        }

        for (JsonNode npcNode : arrayNode(doc("npc_registry"), "npcs")) {
            String npcId = text(npcNode, "id");
            if (npcId.isEmpty()) {
                continue;
            }
            String displayName = text(npcNode, "display_name");
            if (displayName.isBlank()) {
                displayName = npcId;
            }
            List<String> allowedRoles = stringArray(npcNode.get("allowed_roles"));
            Set<String> eligiblePlateaus = new LinkedHashSet<>();
            for (String plateau : stringArray(npcNode.get("eligible_plateaus"))) {
                eligiblePlateaus.add(plateau);
            }
            NpcDefinition definition = new NpcDefinition(
                    npcId,
                    displayName,
                    List.copyOf(allowedRoles),
                    Set.copyOf(eligiblePlateaus),
                    text(npcNode, "notes"),
                    text(npcNode, "faction_id"));
            npcDefinitions.put(npcId, definition);
            npcEligiblePlateaus.put(npcId, eligiblePlateaus);
        }

        for (JsonNode plateauNode : arrayNode(doc("plateaus"), "plateaus")) {
            String plateauId = text(plateauNode, "id");
            if (plateauId.isEmpty()) {
                continue;
            }
            List<String> route = stringArray(plateauNode.get("critical_route"));
            criticalRoutesByPlateau.put(plateauId, List.copyOf(route));
        }

        for (JsonNode beatNode : arrayNode(doc("narrative_beats"), "beats")) {
            String id = text(beatNode, "id");
            if (id.isEmpty()) {
                continue;
            }

            BeatDefinition beat = new BeatDefinition(
                    id,
                    text(beatNode, "plateau"),
                    intValue(beatNode, "route_order", Integer.MAX_VALUE),
                    text(beatNode, "beat_type"),
                    text(beatNode, "title"),
                    text(beatNode, "area_id"),
                    text(beatNode, "objective_prompt"),
                    stringArray(beatNode.get("required_flags")),
                    stringArray(beatNode.get("sets_flags")),
                    stringArray(beatNode.get("npcs")),
                    stringArray(beatNode.get("dialogue_refs")));

            BeatDefinition previous = beatsById.put(id, beat);
            if (previous != null) {
                validationIssues.add("Duplicate beat id: " + id);
            }
            beatsByPlateau.computeIfAbsent(normalize(beat.plateau()), ignored -> new ArrayList<>()).add(beat);
        }

        for (JsonNode mainlineNode : arrayNode(doc("quests"), "mainline_missions")) {
            String missionId = text(mainlineNode, "id");
            if (missionId.isEmpty()) {
                continue;
            }
            String routeHint = text(mainlineNode, "route_hint");
            if (!routeHint.isBlank()) {
                routeHintsByMissionId.put(missionId, routeHint);
            }
            String plateau = normalize(text(mainlineNode, "plateau"));
            if (!plateau.isBlank()) {
                plateauByMissionId.put(missionId, plateau);
            }
            JsonNode mainline = mainlineNode.get("mainline");
            if (mainline != null && mainline.asBoolean(false)) {
                mainlineMissionIds.add(missionId);
            }
        }

        JsonNode quests = doc("quests");
        JsonNode chains = quests == null ? null : quests.get("chains");
        if (chains != null && chains.isArray()) {
            for (JsonNode chainNode : chains) {
                JsonNode steps = chainNode.get("steps");
                if (steps == null || !steps.isArray()) {
                    continue;
                }
                for (JsonNode stepNode : steps) {
                    String stepId = text(stepNode, "id");
                    if (stepId.isBlank()) {
                        continue;
                    }
                    String missionId = "sq_" + stepId;
                    String routeHint = text(stepNode, "route_hint");
                    if (!routeHint.isBlank()) {
                        routeHintsByMissionId.put(missionId, routeHint);
                    }
                    String plateau = normalize(text(stepNode, "plateau"));
                    if (!plateau.isBlank()) {
                        plateauByMissionId.put(missionId, plateau);
                    }
                }
            }
        }

        for (JsonNode regionNode : arrayNode(doc("world_state"), "regions")) {
            String id = text(regionNode, "id");
            if (id.isEmpty()) {
                continue;
            }
            WorldRegionStateDefinition definition = new WorldRegionStateDefinition(
                    id,
                    text(regionNode, "plateau"),
                    text(regionNode, "weather_profile"),
                    doubleValue(regionNode, "corruption_pressure", 0.0),
                    doubleValue(regionNode, "prosperity_pressure", 0.0),
                    doubleValue(regionNode, "route_risk", 0.0),
                    doubleValue(regionNode, "faction_tension", 0.0));
            WorldRegionStateDefinition previous = worldRegionStatesById.put(id, definition);
            if (previous != null) {
                validationIssues.add("Duplicate world region state id: " + id);
            }
        }

        for (JsonNode factionNode : arrayNode(doc("faction_state"), "factions")) {
            String id = text(factionNode, "id");
            if (id.isEmpty()) {
                continue;
            }
            FactionStateDefinition definition = new FactionStateDefinition(
                    id,
                    text(factionNode, "display_name"),
                    stringArray(factionNode.get("territory_plateaus")),
                    doubleValue(factionNode, "influence", 0.0),
                    doubleValue(factionNode, "tension", 0.0),
                    doubleValue(factionNode, "prosperity_effect", 0.0),
                    stringArray(factionNode.get("event_hooks")));
            FactionStateDefinition previous = factionStatesById.put(id, definition);
            if (previous != null) {
                validationIssues.add("Duplicate faction state id: " + id);
            }
        }

        for (JsonNode settlementNode : arrayNode(doc("settlement_state"), "settlements")) {
            String id = text(settlementNode, "id");
            if (id.isEmpty()) {
                continue;
            }
            SettlementStateDefinition definition = new SettlementStateDefinition(
                    id,
                    text(settlementNode, "plateau"),
                    text(settlementNode, "population_band"),
                    doubleValue(settlementNode, "prosperity", 0.0),
                    doubleValue(settlementNode, "safety", 0.0),
                    doubleValue(settlementNode, "supply", 0.0),
                    doubleValue(settlementNode, "morale", 0.0),
                    stringArray(settlementNode.get("memory_flags")));
            SettlementStateDefinition previous = settlementStatesById.put(id, definition);
            if (previous != null) {
                validationIssues.add("Duplicate settlement state id: " + id);
            }
        }

        for (List<BeatDefinition> beats : beatsByPlateau.values()) {
            beats.sort(Comparator.comparingInt(BeatDefinition::routeOrder).thenComparing(BeatDefinition::id));
        }
        for (List<DialogueLineDefinition> lines : dialogueLinesBySpeaker.values()) {
            lines.sort(Comparator.comparing(DialogueLineDefinition::id));
        }
    }

    private void validateCrossReferences() {
        for (BeatDefinition beat : beatsById.values()) {
            String plateauId = normalize(beat.plateau());
            if (!plateauId.equals("GLOBAL") && !criticalRoutesByPlateau.containsKey(plateauId)) {
                validationIssues.add("Beat " + beat.id() + " references unknown plateau: " + beat.plateau());
            }

            if (!beat.areaId().isEmpty() && !areaIds.contains(beat.areaId())) {
                validationIssues.add("Beat " + beat.id() + " references unknown area_id: " + beat.areaId());
            }

            for (String requiredFlag : beat.requiredFlags()) {
                if (!criticalFlags.contains(requiredFlag)) {
                    validationIssues.add("Beat " + beat.id() + " requires unknown critical flag: " + requiredFlag);
                }
            }

            for (String npcId : beat.npcIds()) {
                if (!npcEligiblePlateaus.containsKey(npcId)) {
                    validationIssues.add("Beat " + beat.id() + " references unknown NPC: " + npcId);
                    continue;
                }
                if (!isNpcAllowedForPlateau(npcId, beat.plateau())) {
                    validationIssues.add("Beat " + beat.id() + " places NPC " + npcId
                            + " outside eligible plateaus for " + beat.plateau());
                }
            }

            for (String dialogueRef : beat.dialogueRefs()) {
                if (!dialogueIds.contains(dialogueRef)) {
                    validationIssues.add("Beat " + beat.id() + " references unknown dialogue id: " + dialogueRef);
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : criticalRoutesByPlateau.entrySet()) {
            String plateau = entry.getKey();
            for (String beatId : entry.getValue()) {
                BeatDefinition beat = beatsById.get(beatId);
                if (beat == null) {
                    validationIssues.add("Plateau " + plateau + " critical route references unknown beat: " + beatId);
                    continue;
                }
                if (!normalize(beat.plateau()).equals(plateau)) {
                    validationIssues.add("Plateau " + plateau + " route beat " + beatId
                            + " points to plateau " + beat.plateau());
                }
            }
        }

        validateWorldSimulationContracts();
        validateQuestRewardEffects();
    }

    private void validateWorldSimulationContracts() {
        for (WorldRegionStateDefinition region : worldRegionStatesById.values()) {
            String plateauId = normalize(region.plateauId());
            if (!criticalRoutesByPlateau.containsKey(plateauId)) {
                validationIssues.add("World region " + region.id()
                        + " references unknown plateau: " + region.plateauId());
            }
            validateUnitInterval("World region " + region.id() + ".corruption_pressure", region.corruptionPressure());
            validateUnitInterval("World region " + region.id() + ".prosperity_pressure", region.prosperityPressure());
            validateUnitInterval("World region " + region.id() + ".route_risk", region.routeRisk());
            validateUnitInterval("World region " + region.id() + ".faction_tension", region.factionTension());
        }

        for (FactionStateDefinition faction : factionStatesById.values()) {
            if (faction.displayName().isBlank()) {
                validationIssues.add("Faction state " + faction.id() + " is missing display_name");
            }
            if (faction.territoryPlateaus().isEmpty()) {
                validationIssues.add("Faction state " + faction.id() + " requires at least one territory_plateau");
            }
            for (String plateau : faction.territoryPlateaus()) {
                if (!criticalRoutesByPlateau.containsKey(normalize(plateau))) {
                    validationIssues.add("Faction state " + faction.id()
                            + " references unknown territory plateau: " + plateau);
                }
            }
            validateUnitInterval("Faction state " + faction.id() + ".influence", faction.influence());
            validateUnitInterval("Faction state " + faction.id() + ".tension", faction.tension());
            validateUnitInterval("Faction state " + faction.id() + ".prosperity_effect", faction.prosperityEffect());
        }

        for (SettlementStateDefinition settlement : settlementStatesById.values()) {
            String plateauId = normalize(settlement.plateauId());
            if (!criticalRoutesByPlateau.containsKey(plateauId)) {
                validationIssues.add("Settlement state " + settlement.id()
                        + " references unknown plateau: " + settlement.plateauId());
            }
            if (settlement.populationBand().isBlank()) {
                validationIssues.add("Settlement state " + settlement.id() + " is missing population_band");
            }
            validateUnitInterval("Settlement state " + settlement.id() + ".prosperity", settlement.prosperity());
            validateUnitInterval("Settlement state " + settlement.id() + ".safety", settlement.safety());
            validateUnitInterval("Settlement state " + settlement.id() + ".supply", settlement.supply());
            validateUnitInterval("Settlement state " + settlement.id() + ".morale", settlement.morale());
            for (String flag : settlement.memoryFlags()) {
                if (!criticalFlags.contains(flag)) {
                    validationIssues.add("Settlement state " + settlement.id()
                            + " references unknown memory flag: " + flag);
                }
            }
        }
    }

    private void validateUnitInterval(String field, double value) {
        if (Double.isNaN(value) || value < 0.0 || value > 1.0) {
            validationIssues.add(field + " must be within [0.0, 1.0]");
        }
    }

    private JsonNode doc(String key) {
        return documents.get(key);
    }

    private static List<JsonNode> arrayNode(JsonNode root, String field) {
        if (root == null || root.get(field) == null || !root.get(field).isArray()) {
            return List.of();
        }
        List<JsonNode> out = new ArrayList<>();
        root.get(field).forEach(out::add);
        return out;
    }

    private static List<String> stringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        node.forEach(item -> {
            if (item.isTextual()) {
                String value = item.asText().trim();
                if (!value.isEmpty()) {
                    out.add(value);
                }
            }
        });
        return List.copyOf(out);
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return "";
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        return value.asText("").trim();
    }

    private static int intValue(JsonNode node, String field, int fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        if (value == null || !value.canConvertToInt()) {
            return fallback;
        }
        return value.asInt(fallback);
    }

    private static double doubleValue(JsonNode node, String field, double fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        if (value == null || !value.isNumber()) {
            return fallback;
        }
        return value.asDouble(fallback);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean lineApplicableForState(
            DialogueLineDefinition line,
            StoryState state,
            String activePlateau) {
        if (line == null || state == null) {
            return false;
        }
        if (!line.plateau().isBlank()
                && !"GLOBAL".equals(line.plateau())
                && !line.plateau().equals(activePlateau)) {
            return false;
        }
        for (String requiredFlag : line.requiredFlags()) {
            if (!state.hasFlag(requiredFlag)) {
                return false;
            }
        }
        for (String forbiddenFlag : line.forbiddenFlags()) {
            if (state.hasFlag(forbiddenFlag)) {
                return false;
            }
        }
        for (String requiredAbility : line.requiredAbilities()) {
            if (!state.hasAbility(requiredAbility)) {
                return false;
            }
        }
        for (String forbiddenAbility : line.forbiddenAbilities()) {
            if (state.hasAbility(forbiddenAbility)) {
                return false;
            }
        }
        if (!line.requiredHubStates().isEmpty()
                && !line.requiredHubStates().contains(normalize(state.getCurrentHubState().name()))) {
            return false;
        }
        return true;
    }

    private static int scoreDialogueLine(
            DialogueLineDefinition line,
            String nextBeatId,
            String activePlateau,
            StoryState state) {
        int score = 0;
        if (line.plateau().equals(activePlateau)) {
            score += 4;
        } else if ("GLOBAL".equals(line.plateau())) {
            score += 2;
        }
        if ("AUTHORED".equals(line.lineRole())) {
            score += 2;
        }
        if ("ELASTIC_BARK".equals(line.lineRole())) {
            score -= 1;
        }
        if (!nextBeatId.isBlank()) {
            String next = normalize(nextBeatId);
            for (String beatRef : line.beatRefs()) {
                if (next.equals(normalize(beatRef))) {
                    score += 8;
                    break;
                }
            }
        }
        if (line.requiredFlags().size() > 0) {
            score += line.requiredFlags().size();
        }
        if (!line.beatRefs().isEmpty()) {
            score += 1;
        }
        if (!line.tags().isEmpty()) {
            score += 1;
        }
        if (state.getCurrentAct().ordinal() >= StoryState.Act.ACT_II.ordinal()) {
            score += Math.min(2, line.requiredFlags().size());
        }
        return score;
    }

    private static List<String> parseBeatRefs(JsonNode beatRefNode) {
        if (beatRefNode == null || beatRefNode.isNull()) {
            return List.of();
        }
        if (beatRefNode.isTextual()) {
            String value = beatRefNode.asText("").trim();
            return value.isEmpty() ? List.of() : List.of(value);
        }
        if (beatRefNode.isArray()) {
            List<String> refs = new ArrayList<>();
            for (JsonNode node : beatRefNode) {
                if (node != null && node.isTextual()) {
                    String value = node.asText("").trim();
                    if (!value.isEmpty()) {
                        refs.add(value);
                    }
                }
            }
            return List.copyOf(refs);
        }
        return List.of();
    }

    private static ParsedDialogueConditions parseDialogueConditions(JsonNode conditionsNode) {
        ParsedDialogueConditions parsed = new ParsedDialogueConditions();
        if (conditionsNode == null || !conditionsNode.isArray()) {
            return parsed;
        }

        for (JsonNode condition : conditionsNode) {
            if (condition == null || condition.isNull()) {
                continue;
            }
            if (condition.isTextual()) {
                parseConditionToken(condition.asText(""), parsed);
                continue;
            }
            if (!condition.isObject()) {
                continue;
            }

            parsed.requiredFlags.addAll(stringArray(condition.get("required_flags")));
            parsed.forbiddenFlags.addAll(stringArray(condition.get("forbidden_flags")));
            parsed.requiredAbilities.addAll(stringArray(condition.get("required_abilities")));
            parsed.forbiddenAbilities.addAll(stringArray(condition.get("forbidden_abilities")));
            for (String hubState : stringArray(condition.get("required_hub_states"))) {
                parsed.requiredHubStates.add(normalize(hubState));
            }
        }
        return parsed;
    }

    private static void parseConditionToken(String raw, ParsedDialogueConditions parsed) {
        String token = raw == null ? "" : raw.trim();
        if (token.isEmpty()) {
            return;
        }
        boolean negated = token.startsWith("!");
        String normalized = negated ? token.substring(1).trim() : token;
        String[] parts = normalized.split(":", 2);
        if (parts.length != 2) {
            return;
        }
        String kind = parts[0].trim().toLowerCase(Locale.ROOT);
        String value = parts[1].trim();
        if (value.isEmpty()) {
            return;
        }
        switch (kind) {
            case "flag" -> {
                if (negated) {
                    parsed.forbiddenFlags.add(value);
                } else {
                    parsed.requiredFlags.add(value);
                }
            }
            case "ability" -> {
                if (negated) {
                    parsed.forbiddenAbilities.add(value);
                } else {
                    parsed.requiredAbilities.add(value);
                }
            }
            case "hub" -> {
                if (!negated) {
                    parsed.requiredHubStates.add(normalize(value));
                }
            }
            default -> { }
        }
    }

    private static final class ParsedDialogueConditions {
        private final Set<String> requiredFlags = new LinkedHashSet<>();
        private final Set<String> forbiddenFlags = new LinkedHashSet<>();
        private final Set<String> requiredAbilities = new LinkedHashSet<>();
        private final Set<String> forbiddenAbilities = new LinkedHashSet<>();
        private final Set<String> requiredHubStates = new LinkedHashSet<>();
    }

    private void validateQuestRewardEffects() {
        JsonNode quests = doc("quests");
        if (quests == null) {
            return;
        }
        JsonNode chains = quests.get("chains");
        if (chains == null || !chains.isArray()) {
            validationIssues.add("quests.chains must be an array for reward_effects validation");
            return;
        }

        for (JsonNode chainNode : chains) {
            String chainId = text(chainNode, "id");
            JsonNode steps = chainNode.get("steps");
            if (steps == null || !steps.isArray()) {
                validationIssues.add("Quest chain " + chainId + " has invalid steps array");
                continue;
            }

            for (JsonNode stepNode : steps) {
                String stepId = text(stepNode, "id");
                String prefix = "Quest step " + chainId + "/" + stepId + " reward_effects";

                JsonNode rewardEffects = stepNode.get("reward_effects");
                if (rewardEffects == null || !rewardEffects.isArray() || rewardEffects.isEmpty()) {
                    validationIssues.add(prefix + " missing or empty (explicit typed reward effects are required)");
                    continue;
                }

                int index = 0;
                for (JsonNode effect : rewardEffects) {
                    String entry = prefix + "[" + index + "]";
                    index++;

                    if (effect == null || !effect.isObject()) {
                        validationIssues.add(entry + " must be an object");
                        continue;
                    }

                    String type = normalize(text(effect, "type"));
                    if (type.isEmpty()) {
                        validationIssues.add(entry + ".type is required");
                        continue;
                    }
                    if (!SUPPORTED_REWARD_EFFECT_TYPES.contains(type)) {
                        validationIssues.add(entry + ".type unsupported: " + type);
                    }

                    JsonNode magnitudeNode = effect.get("magnitude");
                    if (magnitudeNode == null || !magnitudeNode.canConvertToInt() || magnitudeNode.asInt() < 1) {
                        validationIssues.add(entry + ".magnitude must be an integer >= 1");
                    }

                    JsonNode payload = effect.get("payload");
                    if (payload == null || !payload.isObject()) {
                        validationIssues.add(entry + ".payload must be an object");
                        continue;
                    }

                    Set<String> requiredKeys = REQUIRED_REWARD_PAYLOAD_KEYS_BY_TYPE.getOrDefault(type, Set.of("id"));
                    for (String requiredKey : requiredKeys) {
                        String value = text(payload, requiredKey);
                        if (value.isEmpty()) {
                            validationIssues.add(entry + ".payload." + requiredKey + " is required for type " + type);
                        }
                    }
                }
            }
        }
    }
}
