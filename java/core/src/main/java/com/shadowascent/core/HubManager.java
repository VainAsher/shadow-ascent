package com.shadowascent.core;

import com.shadowascent.core.data.BeatDefinition;
import com.shadowascent.core.data.GameDataContracts;
import com.shadowascent.core.data.NpcDefinition;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages hub state transitions and NPC presence based on story progression.
 */
public class HubManager {
    private final StoryState storyState;
    private final GameDataContracts dataContracts;

    // Fallback roster only when contract-derived schedule has no active candidates.
    private final Map<HubState, Set<String>> fallbackHubStateNPCs;

    public HubManager(StoryState storyState, GameDataContracts dataContracts) {
        this.storyState = storyState;
        this.dataContracts = dataContracts;
        this.fallbackHubStateNPCs = new HashMap<>();

        initializeNPCRegistry();
        initializeFallbackHubStateConfigurations();
        updateHubState();
    }

    private void initializeNPCRegistry() {
        if (dataContracts != null && !dataContracts.npcDefinitions().isEmpty()) {
            for (NpcDefinition definition : dataContracts.npcDefinitions().values()) {
                addNPC(new NPC(
                        definition.id(),
                        definition.displayName(),
                        Set.copyOf(definition.allowedRoles()),
                        Set.copyOf(definition.eligiblePlateaus()),
                        definition.notes()));
            }
            return;
        }

        // Fallback for invalid/missing contract startup.
        initializeLegacyFallbackNpcRegistry();
    }

    private void initializeLegacyFallbackNpcRegistry() {
        addNPC(new NPC("AEN", "Aen", Set.of("player", "speaker"),
                Set.of("GLOBAL"), "Playable protagonist"));
        addNPC(new NPC("SAMSON", "Samson", Set.of("mentor", "speaker"),
                Set.of("LANTERN_HEIGHTS"), "Wise mentor figure"));
        addNPC(new NPC("SOPHIA", "Sophia", Set.of("companion", "speaker"),
                Set.of("LANTERN_HEIGHTS"), "Supportive companion"));
        addNPC(new NPC("MARCEL", "Marcel", Set.of("merchant", "speaker"),
                Set.of("LANTERN_HEIGHTS"), "Local merchant"));
        addNPC(new NPC("HAZEL", "Hazel", Set.of("villager", "speaker"),
                Set.of("LANTERN_HEIGHTS"), "Concerned villager"));
        addNPC(new NPC("VEIL_MAIDEN", "Veil Maiden", Set.of("veil_maiden", "speaker"),
                Set.of("LANTERN_HEIGHTS"), "The Veil Maiden"));
        addNPC(new NPC("INSTRUCTOR_TAI", "Instructor Tai", Set.of("teacher", "speaker"),
                Set.of("LANTERN_HEIGHTS"), "Martial arts instructor"));
        addNPC(new NPC("MERCHANT_RILU", "Merchant Rilu", Set.of("merchant", "speaker"),
                Set.of("LANTERN_HEIGHTS"), "Village merchant"));
        addNPC(new NPC("SMITH_JENRO", "Smith Jenro", Set.of("smith", "speaker"),
                Set.of("LANTERN_HEIGHTS"), "Village smith"));
    }

    private void initializeFallbackHubStateConfigurations() {
        fallbackHubStateNPCs.put(HubState.VIBRANT, Set.of(
                "SAMSON", "SOPHIA", "MARCEL", "HAZEL", "INSTRUCTOR_TAI", "MERCHANT_RILU", "SMITH_JENRO"));

        fallbackHubStateNPCs.put(HubState.DIMMING, Set.of(
                "SAMSON", "SOPHIA", "MARCEL", "INSTRUCTOR_TAI", "MERCHANT_RILU"));

        fallbackHubStateNPCs.put(HubState.DARK, Set.of(
                "SAMSON", "INSTRUCTOR_TAI", "VEIL_MAIDEN"));

        fallbackHubStateNPCs.put(HubState.EMPTY, Set.of(
                "AEN", "VEIL_MAIDEN"));

        fallbackHubStateNPCs.put(HubState.VEIL_DESCENDS, Set.of("AEN", "VEIL_MAIDEN"));
        fallbackHubStateNPCs.put(HubState.RECOVERING, Set.of("SAMSON", "SOPHIA"));
        fallbackHubStateNPCs.put(HubState.HOPEFUL, Set.of("SAMSON", "SOPHIA", "INSTRUCTOR_TAI"));
        fallbackHubStateNPCs.put(HubState.REBORN, Set.of(
                "SAMSON", "SOPHIA", "MARCEL", "HAZEL", "INSTRUCTOR_TAI", "MERCHANT_RILU", "SMITH_JENRO"));
    }

    private void addNPC(NPC npc) {
        storyState.addNPC(npc);
    }

    /**
     * Update hub state based on story progression and trigger NPC visibility changes.
     */
    public void updateHubState() {
        HubState currentState = resolveHubStateFromProgression();
        storyState.setHubState(currentState);

        for (NPC npc : storyState.getAllNPCs().values()) {
            npc.setActive(false);
        }

        for (String npcId : resolveScheduledNpcIds(currentState)) {
            storyState.setNPCActive(npcId, true);
        }
    }

    private Set<String> resolveScheduledNpcIds(HubState currentState) {
        LinkedHashSet<String> scheduled = new LinkedHashSet<>();

        if (dataContracts != null) {
            scheduled.addAll(dataContracts.scheduledNpcIds(storyState));
            dataContracts.nextCriticalBeat(storyState).ifPresent(beat -> scheduled.addAll(beat.npcIds()));
            scheduled.addAll(dataContracts.factionInfluencedNpcIds(storyState));
        }

        if (scheduled.isEmpty()) {
            scheduled.addAll(fallbackHubStateNPCs.getOrDefault(currentState, Set.of()));
        }

        if (dataContracts != null) {
            String plateauId = storyState.getCurrentPlateau().name();
            scheduled.removeIf(npcId -> !dataContracts.isNpcAllowedForPlateau(npcId, plateauId));
        }

        scheduled.removeIf(npcId -> storyState.getNPC(npcId) == null);
        return Set.copyOf(scheduled);
    }

    private HubState resolveHubStateFromProgression() {
        Optional<BeatDefinition> nextBeat = dataContracts == null
                ? Optional.empty()
                : dataContracts.nextCriticalBeat(storyState);
        String nextBeatId = nextBeat.map(BeatDefinition::id).orElse("");

        if (storyState.hasFlag("marcel_returned") && storyState.hasFlag("beacon_lantern_prepared")) {
            return HubState.REBORN;
        }
        if (storyState.hasFlag("hearth_joined") || storyState.hasFlag("samson_returned")) {
            return HubState.HOPEFUL;
        }
        if (storyState.hasFlag("entered_ember_monastery") || "beat_ember_transition".equals(nextBeatId)) {
            return HubState.RECOVERING;
        }
        if (storyState.hasFlag("act2_unlocked")
                || storyState.getCurrentAct().ordinal() >= StoryState.Act.ACT_II.ordinal()
                || "beat_hollowing_intro".equals(nextBeatId)) {
            return HubState.VEIL_DESCENDS;
        }
        if (storyState.hasFlag("hub_emptied")
                || storyState.hasFlag("empty_hub_only_maiden")
                || "beat_final_departures".equals(nextBeatId)
                || "beat_summit_shrine_call".equals(nextBeatId)
                || "beat_empty_hub_only_maiden".equals(nextBeatId)) {
            return HubState.EMPTY;
        }
        if (storyState.hasFlag("hub_dimming_seen")
                || storyState.hasFlag("warnings_heard")
                || "beat_subtle_isolation_cutscene".equals(nextBeatId)
                || "beat_act0_isolation_dialogue".equals(nextBeatId)) {
            return HubState.DARK;
        }
        if (storyState.hasFlag("npc_withdrawal_started")
                || storyState.hasFlag("mistwood_beast_defeated")
                || "beat_npc_withdrawal".equals(nextBeatId)) {
            return HubState.DIMMING;
        }
        return HubState.VIBRANT;
    }

    /**
     * Get dialogue for an NPC based on current story state.
     */
    public String getNPCDialogue(String npcId) {
        NPC npc = storyState.getNPC(npcId);
        if (npc == null || !npc.isActive()) {
            return "";
        }

        if (dataContracts != null) {
            Optional<String> contractDialogue = dataContracts
                    .selectNpcDialogueLine(storyState, canonicalDialogueSpeakerId(npcId))
                    .map(line -> line.text() == null ? "" : line.text().trim())
                    .filter(line -> !line.isBlank());
            if (contractDialogue.isPresent()) {
                return contractDialogue.get();
            }
        }

        return switch (npcId) {
            case "SAMSON" -> getSamsonDialogue();
            case "SOPHIA" -> getSophiaDialogue();
            case "VEIL_MAIDEN", "LINZI" -> getVeilMaidenDialogue();
            case "INSTRUCTOR_TAI", "TAI" -> getTaiDialogue();
            default -> "Hello, traveler.";
        };
    }

    private static String canonicalDialogueSpeakerId(String npcId) {
        return switch (npcId) {
            case "LINZI" -> "VEIL_MAIDEN";
            case "TAI" -> "INSTRUCTOR_TAI";
            default -> npcId;
        };
    }

    private String getSamsonDialogue() {
        if (storyState.hasFlag("village_bonds")) {
            return "The village needs your help. Something dark is stirring in the woods.";
        } else if (storyState.hasFlag("mistwood_beast_defeated")) {
            return "You've proven yourself capable. But the real threat is just beginning.";
        } else if (storyState.hasFlag("hub_emptied")) {
            return "It's too late. The veil has fallen. You must face what you've unleashed.";
        }
        return "Welcome to Lantern Heights. I am Samson, keeper of the ancient ways.";
    }

    private String getSophiaDialogue() {
        if (storyState.hasFlag("village_bonds")) {
            return "I'm worried about everyone. Please, help us.";
        } else if (storyState.hasFlag("npc_withdrawal_started")) {
            return "...";
        }
        return "Hello! It's good to see a friendly face.";
    }

    private String getVeilMaidenDialogue() {
        if (storyState.hasFlag("veil_request_accepted")) {
            return "You've made the right choice. The veil will protect us all.";
        } else if (storyState.getCurrentHubState() == HubState.EMPTY) {
            return "The old ways are failing. Embrace the veil's embrace.";
        }
        return "The veil calls to those who listen...";
    }

    private String getTaiDialogue() {
        if (storyState.hasAbility("dash")) {
            return "Your training shows promise. Remember: speed is life.";
        } else if (storyState.hasFlag("dojo_practiced")) {
            return "You've begun your training. Practice the forms I taught you.";
        }
        return "Strength comes from discipline. Are you ready to train?";
    }

    /**
     * Advance hub state based on story progression.
     */
    public void advanceHubState() {
        updateHubState();
    }
}
