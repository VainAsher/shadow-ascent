package com.shadowascent.core;

import com.shadowascent.core.data.QuestRewardEffectType;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class StoryState {
    public enum Act {
        ACT_I,
        ACT_II,
        ACT_III,
        ACT_IV,
        ACT_V,
        ACT_VI,
        ACT_VII
    }

    public enum Plateau {
        LANTERN_HEIGHTS,
        SUMMIT_SHRINE,
        HOLLOW_DEPTHS,
        EMBER_MONASTERY,
        WINDING_SKYROAD,
        MIRROR_SUMMIT,
        BEACON_CLIFF
    }

    private Act currentAct = Act.ACT_I;
    private Plateau currentPlateau = Plateau.LANTERN_HEIGHTS;
    private HubState currentHubState = HubState.VIBRANT;
    private int yinScore = 0;
    private int yangScore = 0;
    private int lanternCount = 0;
    private final Set<String> flags = new LinkedHashSet<>();
    private final Set<String> abilities = new LinkedHashSet<>();
    private final Map<QuestRewardEffectType, Set<String>> questRewardEffects = new LinkedHashMap<>();
    private int weaponUpgradeTier = 0;
    private final EmotionalState emotionalState = new EmotionalState();

    // Hub NPCs - keyed by NPC ID
    private final Map<String, NPC> npcs = new LinkedHashMap<>();

    // Available missions - keyed by mission ID
    private final Map<String, Mission> missions = new LinkedHashMap<>();

    // Active mission tracking
    private String activeMissionId = null;
    private float missionTimer = 0f;

    public Act getCurrentAct() {
        return currentAct;
    }

    public Plateau getCurrentPlateau() {
        return currentPlateau;
    }

    public HubState getCurrentHubState() {
        return currentHubState;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public boolean hasAbility(String ability) {
        return abilities.contains(ability);
    }

    public void setFlag(String flag) {
        flags.add(Objects.requireNonNull(flag));
    }

    public void removeFlag(String flag) {
        flags.remove(flag);
    }

    public void grantAbility(String ability) {
        abilities.add(Objects.requireNonNull(ability));
    }

    public void revokeAbility(String ability) {
        abilities.remove(ability);
    }

    public Set<String> getAbilities() {
        return abilities;
    }

    public boolean hasQuestRewardEffect(QuestRewardEffectType type, String rewardId) {
        if (type == null || rewardId == null || rewardId.isBlank()) {
            return false;
        }
        Set<String> bucket = questRewardEffects.get(type);
        if (bucket == null) {
            return false;
        }
        return bucket.contains(normalizeRewardId(rewardId));
    }

    public int questRewardEffectCount() {
        int total = 0;
        for (Set<String> bucket : questRewardEffects.values()) {
            total += bucket.size();
        }
        return total;
    }

    public int getWeaponUpgradeTier() {
        return weaponUpgradeTier;
    }

    public void recordQuestRewardEffect(QuestRewardEffectType type, String rewardId, int magnitude) {
        Objects.requireNonNull(type, "type");
        String normalizedRewardId = normalizeRewardId(rewardId);
        if (normalizedRewardId.isEmpty()) {
            return;
        }

        Set<String> bucket = questRewardEffects.computeIfAbsent(type, ignored -> new LinkedHashSet<>());
        boolean added = bucket.add(normalizedRewardId);
        if (!added) {
            return;
        }

        int delta = Math.max(1, magnitude);
        switch (type) {
            case MAP_UPGRADE -> grantAbility("map_upgrade:" + normalizedRewardId);
            case CHARM_UNLOCK -> grantAbility("charm_unlock:" + normalizedRewardId);
            case PASSIVE_BUFF -> grantAbility("passive_buff:" + normalizedRewardId);
            case WEAPON_UPGRADE -> {
                weaponUpgradeTier += delta;
                grantAbility("weapon_upgrade_tier_" + weaponUpgradeTier);
            }
            case COSMETIC_UNLOCK -> grantAbility("cosmetic_unlock:" + normalizedRewardId);
            case ITEM_UNLOCK -> grantAbility("item_unlock:" + normalizedRewardId);
            case UNKNOWN_REWARD -> setFlag("unknown_reward_effect_applied");
            default -> { }
        }
    }

    public int getYinScore() {
        return yinScore;
    }

    public int getYangScore() {
        return yangScore;
    }

    public int getLanternCount() {
        return lanternCount;
    }

    public void addYin(int amount) {
        if (amount > 0) {
            yinScore += amount;
            emotionalState.addYin(amount);
            emotionalState.updateCompanionState();
        }
    }

    public void addYang(int amount) {
        if (amount > 0) {
            yangScore += amount;
            emotionalState.addYang(amount);
            emotionalState.updateCompanionState();
        }
    }

    public void collectLantern() {
        lanternCount++;
    }

    public String getEmotionalBalance() {
        return emotionalState.getBalance();
    }

    public int getEmotionalDominance() {
        return emotionalState.getDominance();
    }

    public EmotionalState getEmotionalState() {
        return emotionalState;
    }

    public void advanceAct() {
        if (currentAct != Act.ACT_VII) {
            currentAct = Act.values()[currentAct.ordinal() + 1];
            currentPlateau = switch (currentAct) {
                case ACT_I -> Plateau.LANTERN_HEIGHTS;
                case ACT_II -> Plateau.HOLLOW_DEPTHS;
                case ACT_III -> Plateau.EMBER_MONASTERY;
                case ACT_IV -> Plateau.WINDING_SKYROAD;
                case ACT_V -> Plateau.MIRROR_SUMMIT;
                case ACT_VI -> Plateau.BEACON_CLIFF;
                case ACT_VII -> Plateau.BEACON_CLIFF;
            };
        }
    }

    public void setPlateau(Plateau plateau) {
        this.currentPlateau = Objects.requireNonNull(plateau);
    }

    public void setHubState(HubState state) {
        this.currentHubState = Objects.requireNonNull(state);
    }

    // NPC management
    public void addNPC(NPC npc) {
        npcs.put(npc.getId(), npc);
    }

    public NPC getNPC(String id) {
        return npcs.get(id);
    }

    public Map<String, NPC> getAllNPCs() {
        return npcs;
    }

    public void setNPCActive(String npcId, boolean active) {
        NPC npc = npcs.get(npcId);
        if (npc != null) {
            npc.setActive(active);
        }
    }

    // Mission management
    public void addMission(Mission mission) {
        missions.put(mission.getId(), mission);
    }

    public Mission getMission(String id) {
        return missions.get(id);
    }

    public Map<String, Mission> getAllMissions() {
        return missions;
    }

    public void setActiveMission(String missionId) {
        this.activeMissionId = missionId;
        if (missionId != null) {
            Mission mission = missions.get(missionId);
            if (mission != null) {
                mission.setState(Mission.MissionState.ACTIVE);
            }
        }
    }

    public String getActiveMissionId() {
        return activeMissionId;
    }

    public void completeMission(String missionId) {
        Mission mission = missions.get(missionId);
        if (mission != null) {
            mission.setState(Mission.MissionState.COMPLETED);
            mission.setBestTime(missionTimer);
            if (missionId.equals(activeMissionId)) {
                activeMissionId = null;
                missionTimer = 0f;
            }
        }
    }

    public void updateMissionTimer(float deltaTime) {
        if (activeMissionId != null) {
            missionTimer += deltaTime;
        }
    }

    public float getMissionTimer() {
        return missionTimer;
    }
    public void copyFrom(StoryState other) {
        Objects.requireNonNull(other, "other");
        this.currentAct = other.currentAct;
        this.currentPlateau = other.currentPlateau;
        this.currentHubState = other.currentHubState;
        this.flags.clear();
        this.flags.addAll(other.flags);
        this.abilities.clear();
        this.abilities.addAll(other.abilities);
        if (!other.npcs.isEmpty()) {
            this.npcs.clear();
            this.npcs.putAll(other.npcs);
        }
        if (!other.missions.isEmpty()) {
            this.missions.clear();
            this.missions.putAll(other.missions);
        }
        this.activeMissionId = other.activeMissionId;
        this.missionTimer = other.missionTimer;
        this.yinScore = other.yinScore;
        this.yangScore = other.yangScore;
        this.lanternCount = other.lanternCount;
        this.questRewardEffects.clear();
        for (Map.Entry<QuestRewardEffectType, Set<String>> entry : other.questRewardEffects.entrySet()) {
            this.questRewardEffects.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
        }
        this.weaponUpgradeTier = other.weaponUpgradeTier;
        this.emotionalState.setYinInfluence(other.emotionalState.getYinInfluence());
        this.emotionalState.setYangInfluence(other.emotionalState.getYangInfluence());
        this.emotionalState.setCompanionState(other.emotionalState.getCompanionState());
        this.emotionalState.setEmpathyBonds(other.emotionalState.getEmpathyBonds());
        this.emotionalState.updateCompanionState();
    }
    public String serialize() {
        return String.format("act=%s;plateau=%s;hub=%s;flags=%s;abilities=%s;activeMission=%s;missionTimer=%f;yin=%d;yang=%d;lantern=%d;questRewards=%s;weaponTier=%d;emotional=%s",
                currentAct, currentPlateau, currentHubState,
                String.join(",", flags), String.join(",", abilities),
                activeMissionId != null ? activeMissionId : "",
                missionTimer,
                yinScore, yangScore, lanternCount,
                serializeQuestRewards(questRewardEffects),
                weaponUpgradeTier,
                emotionalState.serialize().replace(";", "|"));
    }

    public static StoryState deserialize(String serialized) {
        StoryState state = new StoryState();
        if (serialized == null || serialized.isEmpty()) {
            return state;
        }

        String[] entries = serialized.split(";");
        for (String entry : entries) {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2) {
                continue;
            }
            switch (parts[0]) {
                case "act" -> state.currentAct = Act.valueOf(parts[1]);
                case "plateau" -> state.currentPlateau = Plateau.valueOf(parts[1]);
                case "hub" -> state.currentHubState = HubState.valueOf(parts[1]);
                case "flags" -> {
                    if (!parts[1].isEmpty()) {
                        for (String flag : parts[1].split(",")) {
                            state.setFlag(flag);
                        }
                    }
                }
                case "abilities" -> {
                    if (!parts[1].isEmpty()) {
                        for (String ability : parts[1].split(",")) {
                            state.grantAbility(ability);
                        }
                    }
                }
                case "activeMission" -> {
                    if (!parts[1].isEmpty()) {
                        state.activeMissionId = parts[1];
                    }
                }
                case "missionTimer" -> {
                    try {
                        state.missionTimer = Float.parseFloat(parts[1]);
                    } catch (NumberFormatException e) {
                        state.missionTimer = 0f;
                    }
                }
                case "yin" -> {
                    try {
                        state.yinScore = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        state.yinScore = 0;
                    }
                }
                case "yang" -> {
                    try {
                        state.yangScore = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        state.yangScore = 0;
                    }
                }
                case "lantern" -> {
                    try {
                        state.lanternCount = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        state.lanternCount = 0;
                    }
                }
                case "questRewards" -> deserializeQuestRewards(state, parts[1]);
                case "weaponTier" -> {
                    try {
                        state.weaponUpgradeTier = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        state.weaponUpgradeTier = 0;
                    }
                }
                case "emotional" -> {
                    EmotionalState deserialized = EmotionalState.deserialize(parts[1].replace("|", ";"));
                    state.emotionalState.setYinInfluence(deserialized.getYinInfluence());
                    state.emotionalState.setYangInfluence(deserialized.getYangInfluence());
                    state.emotionalState.setCompanionState(deserialized.getCompanionState());
                    state.emotionalState.setEmpathyBonds(deserialized.getEmpathyBonds());
                }
                default -> { }
            }
        }
        return state;
    }

    private static String normalizeRewardId(String rewardId) {
        if (rewardId == null) {
            return "";
        }
        return rewardId.trim().toLowerCase(Locale.ROOT);
    }

    private static String serializeQuestRewards(Map<QuestRewardEffectType, Set<String>> effectsByType) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<QuestRewardEffectType, Set<String>> entry : effectsByType.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("|");
            }
            builder.append(entry.getKey().name());
            builder.append(":");
            builder.append(String.join(",", entry.getValue()));
        }
        return builder.toString();
    }

    private static void deserializeQuestRewards(StoryState state, String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return;
        }
        String[] buckets = encoded.split("\\|");
        for (String bucket : buckets) {
            String[] typeAndValues = bucket.split(":", 2);
            if (typeAndValues.length != 2) {
                continue;
            }
            QuestRewardEffectType type;
            try {
                type = QuestRewardEffectType.valueOf(typeAndValues[0]);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            if (typeAndValues[1].isBlank()) {
                continue;
            }
            for (String rewardId : typeAndValues[1].split(",")) {
                state.recordQuestRewardEffect(type, rewardId, 1);
            }
        }
    }
}
