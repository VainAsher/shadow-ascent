package com.shadowascent.client;

import com.shadowascent.core.GameState;
import com.shadowascent.core.Mission;
import com.shadowascent.core.MissionManager;
import com.shadowascent.core.StoryState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class SummitShrineOptionalContentTest {

    @Test
    void summitShrineExposesNoOptionalSideQuestsByContract() {
        GameState gameState = new GameState();
        gameState.getStoryState().setPlateau(StoryState.Plateau.SUMMIT_SHRINE);
        gameState.getStoryState().setFlag("npc_withdrawal_started");
        gameState.getStoryState().setFlag("entered_summit");

        MissionManager manager = gameState.getMissionManager();
        manager.updateAvailableMissions();
        List<Mission> summitSideQuests = manager.getAvailableMissions().stream()
                .filter(mission -> manager.sideQuestStep(mission.getId())
                        .map(step -> "SUMMIT_SHRINE".equals(step.plateau()))
                        .orElse(false))
                .toList();

        assertTrue(summitSideQuests.isEmpty(),
                "SUMMIT_SHRINE is contract-authored as a critical-only scripted plateau with no optional side quests");
    }
}
