package com.shadowascent.core.data;

import java.util.List;

/**
 * Side-quest step runtime rule loaded from quests contract.
 */
public record SideQuestStepDefinition(
        String missionId,
        String chainId,
        String chainTitle,
        String chainNpc,
        String stepId,
        int stepOrder,
        String actWindow,
        String plateau,
        List<String> objectiveIds,
        List<String> requiredFlags,
        List<String> setFlags,
        String rewardField,
        List<QuestRewardEffectDefinition> rewardEffects) {
}
