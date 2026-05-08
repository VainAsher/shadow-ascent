package com.shadowascent.core.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds canonical and side-quest mission templates from narrative beat and quest contracts.
 */
public final class ContractMissionTemplateCatalog {
    private static final Pattern FIRST_NUMBER_PATTERN = Pattern.compile("(\\d+)");

    private final Map<String, MissionTemplateDefinition> templatesById;
    private final Map<String, SideQuestStepDefinition> sideQuestStepsByMissionId;

    private ContractMissionTemplateCatalog(
            Map<String, MissionTemplateDefinition> templatesById,
            Map<String, SideQuestStepDefinition> sideQuestStepsByMissionId) {
        this.templatesById = Map.copyOf(templatesById);
        this.sideQuestStepsByMissionId = Map.copyOf(sideQuestStepsByMissionId);
    }

    public static ContractMissionTemplateCatalog load(GameDataContracts contracts) {
        if (contracts == null) {
            return new ContractMissionTemplateCatalog(Map.of(), Map.of());
        }

        Map<String, JsonNode> beatsById = indexBeats(contracts.contractDocument("narrative_beats"));

        Map<String, MissionTemplateDefinition> templates = new LinkedHashMap<>();
        Map<String, SideQuestStepDefinition> sideQuestSteps = new LinkedHashMap<>();

        // Canonical campaign missions.
        templates.put("village_bonds", fromBeat(
                beatsById.get("beat_meet_villagers"),
                "village_bonds",
                "Village Bonds",
                "Connect with the villagers of Lantern Heights and learn about their concerns.",
                "town",
                List.of("talk_to_samson", "talk_to_sophia", "talk_to_marcel", "talk_to_hazel"),
                Map.of("talk_to_samson", 1, "talk_to_sophia", 1, "talk_to_marcel", 1, "talk_to_hazel", 1)));

        templates.put("dojo_practice", fromQuestStep(
                questStep(contracts.contractDocument("quests"), "samson_q1_unfinished_sparring_match"),
                "dojo_practice",
                "Dojo Training",
                "Train with Instructor Tai to learn basic combat techniques.",
                "town",
                List.of("practice_forms", "learn_dash"),
                Map.of("practice_forms", 1, "learn_dash", 1)));

        templates.put("mistwood_beast", fromBeat(
                beatsById.get("beat_mistwood_first_mission"),
                "mistwood_beast",
                "Mistwood Beast",
                "Defeat the corrupted beast threatening the village outskirts.",
                "forest",
                List.of("defeat_beast"),
                Map.of("defeat_beast", 1)));

        templates.put("veil_request", fromBeat(
                beatsById.get("beat_veil_maiden_request"),
                "veil_request",
                "The Veil's Call",
                "Respond to the mysterious figure who offers protection from the growing darkness.",
                "town",
                List.of("hear_veil_offer", "accept_or_decline"),
                Map.of("hear_veil_offer", 1, "accept_or_decline", 1)));

        templates.put("hollow_descent", fromBeat(
                beatsById.get("beat_hollowing_intro"),
                "hollow_descent",
                "Into the Hollow Depths",
                "Descend into the underground caverns to find the source of the veil's power.",
                "caves",
                List.of("enter_hollow_depths", "find_veil_source"),
                Map.of("enter_hollow_depths", 1, "find_veil_source", 1)));

        templates.put("lantern_restoration", fromQuestStep(
                questStep(contracts.contractDocument("quests"), "hazel_q1_gentle_glow"),
                "lantern_restoration",
                "Restoring the Lanterns",
                "Collect scattered lantern pieces to restore light to the depths.",
                "caves",
                List.of("collect_lantern_pieces", "restore_lanterns"),
                Map.of("collect_lantern_pieces", 5, "restore_lanterns", 1)));

        templates.put("monastery_arrival", fromBeat(
                beatsById.get("beat_ember_transition"),
                "monastery_arrival",
                "The Ember Monastery",
                "Reach the ancient monastery where the veil's influence began.",
                "mountain",
                List.of("climb_to_monastery", "enter_monastery"),
                Map.of("climb_to_monastery", 1, "enter_monastery", 1)));

        templates.put("yin_yang_balance", fromQuestStep(
                questStep(contracts.contractDocument("quests"), "sophia_q3_map_to_skyroad"),
                "yin_yang_balance",
                "Balance of Yin and Yang",
                "Learn to balance the emotional forces that fuel the veil.",
                "monastery",
                List.of("understand_yin_yang", "achieve_balance"),
                Map.of("understand_yin_yang", 1, "achieve_balance", 1)));

        // Full side-quest chain expansion from quests contract.
        List<QuestStepSource> sideQuestSources = parseQuestStepSources(contracts.contractDocument("quests"));
        for (QuestStepSource source : sideQuestSources) {
            String missionId = "sq_" + source.stepId();
            String title = source.hook().isBlank() ? source.chainTitle() : source.hook();
            String description = source.objective().isBlank() ? source.hook() : source.objective();
            String region = plateauToRegion(source.plateau(), "sidequest");

            ObjectivePlan objectivePlan = buildSideQuestObjectivePlan(source);

            templates.put(missionId, new MissionTemplateDefinition(
                    missionId,
                    title,
                    description,
                    region,
                    objectivePlan.objectives(),
                    objectivePlan.requiredCounts()));

            sideQuestSteps.put(missionId, new SideQuestStepDefinition(
                    missionId,
                    source.chainId(),
                    source.chainTitle(),
                    source.chainNpc(),
                    source.stepId(),
                    source.stepOrder(),
                    source.act(),
                    source.plateau(),
                    objectivePlan.objectives(),
                    source.requiredFlags(),
                    source.setFlags(),
                    source.reward(),
                    source.rewardEffects()));
        }

        return new ContractMissionTemplateCatalog(templates, sideQuestSteps);
    }

    public Optional<MissionTemplateDefinition> template(String missionId) {
        return Optional.ofNullable(templatesById.get(missionId));
    }

    public Map<String, MissionTemplateDefinition> allTemplates() {
        return templatesById;
    }

    public Optional<SideQuestStepDefinition> sideQuestStep(String missionId) {
        return Optional.ofNullable(sideQuestStepsByMissionId.get(missionId));
    }

    public Map<String, SideQuestStepDefinition> sideQuestStepsByMissionId() {
        return sideQuestStepsByMissionId;
    }

    private static ObjectivePlan buildSideQuestObjectivePlan(QuestStepSource source) {
        List<String> objectives = new ArrayList<>();
        Map<String, Integer> requiredCounts = new LinkedHashMap<>();

        if (!source.areaPool().isEmpty()) {
            addObjective(objectives, requiredCounts, "reach_area_pool", 1);
        }

        String actionObjectiveId = classifyActionObjective(source.objective());
        int actionCount = Math.max(1, firstNumberOrDefault(source.objective(), 1));
        addObjective(objectives, requiredCounts, actionObjectiveId, actionCount);

        if (!source.enemy().isBlank()) {
            addObjective(objectives, requiredCounts, "defeat_named_enemy", 1);
        }

        if (objectives.isEmpty()) {
            addObjective(objectives, requiredCounts, "resolve_step", 1);
        }

        return new ObjectivePlan(List.copyOf(objectives), Map.copyOf(requiredCounts));
    }

    private static void addObjective(
            List<String> objectives,
            Map<String, Integer> requiredCounts,
            String objectiveId,
            int requiredCount) {
        if (objectiveId == null || objectiveId.isBlank()) {
            return;
        }
        int count = Math.max(1, requiredCount);
        if (!requiredCounts.containsKey(objectiveId)) {
            objectives.add(objectiveId);
            requiredCounts.put(objectiveId, count);
        } else {
            requiredCounts.put(objectiveId, Math.max(requiredCounts.get(objectiveId), count));
        }
    }

    private static String classifyActionObjective(String objectiveText) {
        String text = objectiveText == null ? "" : objectiveText.toLowerCase(Locale.ROOT);

        if (containsAny(text, "collect", "gather", "retrieve", "bring", "find")) {
            return "collect_targets";
        }
        if (containsAny(text, "read", "rumour", "rumor", "overheard")) {
            return "read_clues";
        }
        if (containsAny(text, "defeat", "duel", "fight")) {
            return "defeat_targets";
        }
        if (containsAny(text, "follow")) {
            return "follow_route";
        }
        if (containsAny(text, "defend")) {
            return "defend_zone";
        }
        if (containsAny(text, "trial", "challenge", "complete")) {
            return "complete_trial";
        }
        return "resolve_step";
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static int firstNumberOrDefault(String text, int fallback) {
        if (text == null || text.isBlank()) {
            return fallback;
        }

        Matcher matcher = FIRST_NUMBER_PATTERN.matcher(text);
        if (!matcher.find()) {
            return fallback;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static List<QuestRewardEffectDefinition> parseRewardEffects(
            JsonNode rewardEffectsNode,
            String fallbackRewardField) {
        List<QuestRewardEffectDefinition> effects = new ArrayList<>();

        if (rewardEffectsNode != null && rewardEffectsNode.isArray()) {
            for (JsonNode rewardEffectNode : rewardEffectsNode) {
                QuestRewardEffectType type = parseRewardEffectType(text(rewardEffectNode, "type", "UNKNOWN_REWARD"));
                int magnitude = Math.max(1, intValue(rewardEffectNode, "magnitude", 1));
                Map<String, String> payload = stringMap(rewardEffectNode.get("payload"));
                effects.add(new QuestRewardEffectDefinition(type, magnitude, payload));
            }
        }

        if (!effects.isEmpty()) {
            return List.copyOf(effects);
        }

        String fallbackRewardId = normalizeRewardToken(fallbackRewardField);
        if (fallbackRewardId.isEmpty()) {
            return List.of();
        }

        return List.of(new QuestRewardEffectDefinition(
                QuestRewardEffectType.UNKNOWN_REWARD,
                1,
                Map.of("id", fallbackRewardId, "legacy_reward_token", fallbackRewardId)));
    }

    private static QuestRewardEffectType parseRewardEffectType(String value) {
        if (value == null || value.isBlank()) {
            return QuestRewardEffectType.UNKNOWN_REWARD;
        }
        try {
            return QuestRewardEffectType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return QuestRewardEffectType.UNKNOWN_REWARD;
        }
    }

    private static String normalizeRewardToken(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static MissionTemplateDefinition fromBeat(
            JsonNode beat,
            String missionId,
            String fallbackTitle,
            String fallbackDescription,
            String fallbackRegion,
            List<String> objectives,
            Map<String, Integer> requiredCounts) {
        if (beat == null || beat.isMissingNode()) {
            return fallbackTemplate(missionId, fallbackTitle, fallbackDescription, fallbackRegion, objectives, requiredCounts);
        }

        String title = text(beat, "title", fallbackTitle);
        String description = text(beat, "objective_prompt", fallbackDescription);
        if (description.isBlank()) {
            description = text(beat, "cinematic_intent", fallbackDescription);
        }
        String plateau = normalize(text(beat, "plateau", fallbackRegion));
        String region = plateauToRegion(plateau, fallbackRegion);

        return new MissionTemplateDefinition(
                missionId,
                title,
                description,
                region,
                List.copyOf(objectives),
                Map.copyOf(requiredCounts));
    }

    private static MissionTemplateDefinition fromQuestStep(
            QuestStepSource step,
            String missionId,
            String fallbackTitle,
            String fallbackDescription,
            String fallbackRegion,
            List<String> objectives,
            Map<String, Integer> requiredCounts) {
        if (step == null) {
            return fallbackTemplate(missionId, fallbackTitle, fallbackDescription, fallbackRegion, objectives, requiredCounts);
        }

        String title = step.hook().isBlank() ? fallbackTitle : step.hook();
        String description = step.objective().isBlank() ? fallbackDescription : step.objective();
        String region = plateauToRegion(step.plateau(), fallbackRegion);

        return new MissionTemplateDefinition(
                missionId,
                title,
                description,
                region,
                List.copyOf(objectives),
                Map.copyOf(requiredCounts));
    }

    private static MissionTemplateDefinition fallbackTemplate(
            String missionId,
            String title,
            String description,
            String region,
            List<String> objectives,
            Map<String, Integer> requiredCounts) {
        return new MissionTemplateDefinition(
                missionId,
                title,
                description,
                region,
                List.copyOf(objectives),
                Map.copyOf(requiredCounts));
    }

    private static Map<String, JsonNode> indexBeats(Optional<JsonNode> beatsDoc) {
        Map<String, JsonNode> beatsById = new LinkedHashMap<>();
        if (beatsDoc.isEmpty() || beatsDoc.get().get("beats") == null || !beatsDoc.get().get("beats").isArray()) {
            return beatsById;
        }
        for (JsonNode beat : beatsDoc.get().get("beats")) {
            String id = text(beat, "id", "");
            if (!id.isBlank()) {
                beatsById.put(id, beat);
            }
        }
        return beatsById;
    }

    private static QuestStepSource questStep(Optional<JsonNode> questsDoc, String stepId) {
        for (QuestStepSource source : parseQuestStepSources(questsDoc)) {
            if (source.stepId().equals(stepId)) {
                return source;
            }
        }
        return null;
    }

    private static List<QuestStepSource> parseQuestStepSources(Optional<JsonNode> questsDoc) {
        List<QuestStepSource> steps = new ArrayList<>();
        if (questsDoc.isEmpty() || questsDoc.get().get("chains") == null || !questsDoc.get().get("chains").isArray()) {
            return steps;
        }

        for (JsonNode chain : questsDoc.get().get("chains")) {
            String chainId = text(chain, "id", "");
            String chainTitle = text(chain, "title", "");
            String chainNpc = text(chain, "npc", "");
            JsonNode chainSteps = chain.get("steps");
            if (chainSteps == null || !chainSteps.isArray()) {
                continue;
            }

            List<String> priorCompletionFlags = List.of();
            int stepOrder = 0;
            for (JsonNode step : chainSteps) {
                stepOrder++;
                String stepId = text(step, "id", "");
                if (stepId.isBlank()) {
                    continue;
                }

                List<String> requiredFlags = new ArrayList<>(stringArray(step.get("required_flags")));
                requiredFlags.addAll(priorCompletionFlags);
                requiredFlags = dedupe(requiredFlags);

                List<String> declaredSetFlags = stringArray(step.get("sets_flags"));
                List<String> completionFlags = completionFlagsForStep(stepId, declaredSetFlags);
                List<String> setFlags = new ArrayList<>(declaredSetFlags);
                setFlags.addAll(completionFlags);
                setFlags = dedupe(setFlags);

                String hook = text(step, "hook", "");
                String objective = text(step, "objective", "");
                String plateau = normalize(text(step, "plateau", ""));
                String act = normalize(text(step, "act", ""));
                String enemy = text(step, "enemy", "");
                String reward = text(step, "reward", "");
                List<String> areaPool = stringArray(step.get("area_pool"));
                List<QuestRewardEffectDefinition> rewardEffects = parseRewardEffects(step.get("reward_effects"), reward);

                steps.add(new QuestStepSource(
                        chainId,
                        chainTitle,
                        chainNpc,
                        stepId,
                        stepOrder,
                        act,
                        plateau,
                        hook,
                        objective,
                        areaPool,
                        enemy,
                        reward,
                        rewardEffects,
                        List.copyOf(requiredFlags),
                        List.copyOf(setFlags)));

                priorCompletionFlags = completionFlags;
            }
        }

        return steps;
    }

    private static List<String> completionFlagsForStep(String stepId, List<String> setFlags) {
        List<String> completionFlags = new ArrayList<>();
        for (String flag : setFlags) {
            if (flag.endsWith("_complete")) {
                completionFlags.add(flag);
            }
        }
        if (completionFlags.isEmpty()) {
            completionFlags.add(stepId + "_complete");
        }
        return List.copyOf(completionFlags);
    }

    private static List<String> stringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            if (item.isTextual()) {
                String value = item.asText().trim();
                if (!value.isEmpty()) {
                    out.add(value);
                }
            }
        }
        return List.copyOf(out);
    }

    private static List<String> dedupe(List<String> values) {
        Set<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                unique.add(value.trim());
            }
        }
        return List.copyOf(unique);
    }

    private static Map<String, String> stringMap(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        Map<String, String> out = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry -> {
            String key = entry.getKey() == null ? "" : entry.getKey().trim();
            String value = entry.getValue() == null ? "" : entry.getValue().asText("").trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                out.put(key, value);
            }
        });
        return Map.copyOf(out);
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

    private static String plateauToRegion(String plateau, String fallbackRegion) {
        return switch (normalize(plateau)) {
            case "LANTERN_HEIGHTS" -> "town";
            case "SUMMIT_SHRINE" -> "summit";
            case "HOLLOW_DEPTHS" -> "caves";
            case "EMBER_MONASTERY" -> "monastery";
            case "WINDING_SKYROAD" -> "skyroad";
            case "MIRROR_SUMMIT" -> "mirror";
            case "BEACON_CLIFF" -> "beacon";
            default -> fallbackRegion;
        };
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String text(JsonNode node, String field, String fallback) {
        if (node == null) {
            return fallback;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return fallback;
        }
        String text = value.asText("").trim();
        return text.isEmpty() ? fallback : text;
    }

    private record QuestStepSource(
            String chainId,
            String chainTitle,
            String chainNpc,
            String stepId,
            int stepOrder,
            String act,
            String plateau,
            String hook,
            String objective,
            List<String> areaPool,
            String enemy,
            String reward,
            List<QuestRewardEffectDefinition> rewardEffects,
            List<String> requiredFlags,
            List<String> setFlags) {
    }

    private record ObjectivePlan(
            List<String> objectives,
            Map<String, Integer> requiredCounts) {
    }
}
