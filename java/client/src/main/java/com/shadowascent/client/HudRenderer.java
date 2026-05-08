package com.shadowascent.client;

import com.shadowascent.core.GameConfig;
import com.shadowascent.core.Mission;
import com.shadowascent.core.MissionManager;
import com.shadowascent.core.StoryState;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Renders the HUD overlay and manages mutable HUD state.
 * Extracted from UISubsystem to decouple draw logic from world/NPC layout concerns.
 */
final class HudRenderer {

    static final float FEEDBACK_FLASH_SECONDS    = 4.5f;
    private static final float COMBAT_HINT_DETECT_RANGE  = 106f;
    private static final float TRIGGER_HINT_DETECT_RANGE = 96f;

    private final StoryState storyState;
    private final MissionManager missionManager;
    private final CombatSubsystem combatSubsystem;
    private final TraversalSubsystem traversalSubsystem;
    private final Consumer<String> eventLogSink;

    private String overlayStatusLine  = "Region Overlays: none";
    private String missionFeedbackLine;
    private float  missionFeedbackSeconds;
    private boolean showMinimap       = true;
    private String interactionHint    = "Explore east through traversal rooms and use ability triggers.";
    private final Set<String> surfacedAbilitySnapshot = new HashSet<>();

    HudRenderer(
            StoryState storyState,
            MissionManager missionManager,
            CombatSubsystem combatSubsystem,
            TraversalSubsystem traversalSubsystem,
            Consumer<String> eventLogSink) {
        this.storyState         = storyState;
        this.missionManager     = missionManager;
        this.combatSubsystem    = combatSubsystem;
        this.traversalSubsystem = traversalSubsystem;
        this.eventLogSink       = eventLogSink;
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    void tick(float playerX, float playerY, float dt) {
        missionFeedbackSeconds = Math.max(0f, missionFeedbackSeconds - dt);
        updateInteractionHint(playerX, playerY);
        detectNewAbilityUnlocks();
        updateMissionFeedbackLine();
    }

    // -------------------------------------------------------------------------
    // Draw
    // -------------------------------------------------------------------------

    void draw(Graphics2D g, UISubsystem.RenderState state) {
        g.setColor(new Color(8, 12, 20, 210));
        g.fillRoundRect(14, 14, 440, 334, 14, 14);
        g.setColor(new Color(173, 201, 255));
        g.drawRoundRect(14, 14, 440, 334, 14, 14);

        g.setColor(java.awt.Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 14));
        g.drawString(GameConfig.TITLE + " Playtest QA Runtime", 26, 36);

        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        g.drawString("Act: " + storyState.getCurrentAct() + "    Plateau: " + storyState.getCurrentPlateau(), 26, 56);
        g.drawString("Hub: " + storyState.getCurrentHubState(), 26, 74);
        g.drawString("Lanterns: " + storyState.getLanternCount() + "    Balance: " + storyState.getEmotionalBalance(), 26, 92);

        String activeMissionId = storyState.getActiveMissionId();
        Mission activeMission = activeMissionId == null ? null : storyState.getMission(activeMissionId);

        g.setFont(new Font("Dialog", Font.BOLD, 13));
        g.drawString("Active Mission:", 26, 114);
        g.setFont(new Font("Dialog", Font.PLAIN, 12));

        if (activeMission == null) {
            g.drawString("None (Press ENTER/TAB to start one)", 26, 132);
            g.drawString("Progress: 0%", 26, 150);
        } else {
            g.drawString(activeMission.getDisplayName() + " [" + activeMission.getId() + "]", 26, 132);
            int y = 150;
            int completed = 0;
            for (String objective : activeMission.getObjectives()) {
                int current = activeMission.getObjectiveProgress(objective);
                int required = activeMission.getRequiredCount(objective);
                String mark = current >= required ? "[x]" : "[ ]";
                if (current >= required) {
                    completed++;
                }
                g.drawString(mark + " " + objective + " " + current + "/" + required, 26, y);
                y += 16;
                if (y > 230) {
                    break;
                }
            }
            float completion = missionCompletionPercent(activeMission);
            int barX = 26;
            int barY = 236;
            int barW = 396;
            int barH = 10;
            g.setColor(new Color(41, 54, 84));
            g.fillRect(barX, barY, barW, barH);
            g.setColor(new Color(113, 211, 169));
            g.fillRect(barX, barY, Math.round(barW * completion), barH);
            g.setColor(new Color(163, 196, 255));
            g.drawRect(barX, barY, barW, barH);
            g.setColor(java.awt.Color.WHITE);
            g.drawString("Progress: " + Math.round(completion * 100f) + "%  ("
                    + completed + "/" + activeMission.getObjectives().size() + " objectives)", 26, 262);
            String pendingObjective = firstPendingObjective(activeMission);
            if (!pendingObjective.isBlank()) {
                g.drawString("Next Objective: " + pendingObjective, 26, 278);
            }
        }

        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.drawString("Traversal Abilities:", 26, 300);
        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        int abilityY = 316;
        for (String ability : List.of("basic_movement", "jump", "dash", "combat_basic", "emotional_insight")) {
            boolean unlocked = storyState.hasAbility(ability);
            g.setColor(unlocked ? new Color(126, 238, 167) : new Color(225, 131, 131));
            g.drawString((unlocked ? "[x] " : "[ ] ") + ability, 26, abilityY);
            abilityY += 14;
        }
        g.setColor(java.awt.Color.WHITE);

        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.drawString("Player Health:", 26, abilityY + 4);
        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        g.setColor(state.playerHealth() > 1
                ? new Color(126, 238, 167)
                : (state.playerHealth() == 1 ? java.awt.Color.YELLOW : java.awt.Color.RED));
        g.drawString(state.playerHealth() + "/3" + (state.playerDead() ? " (DEAD)" : ""), 26, abilityY + 20);
        g.setColor(java.awt.Color.WHITE);

        g.setColor(new Color(8, 12, 20, 210));
        g.fillRoundRect(470, 14, 790, 192, 14, 14);
        g.setColor(new Color(173, 201, 255));
        g.drawRoundRect(470, 14, 790, 192, 14, 14);
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new Font("Dialog", Font.PLAIN, 12));
        g.drawString("Controls: A/D run (ALT precision walk)  |  SPACE jump  |  SHIFT/C dash  |  F attack  |  E interact  |  ENTER/TAB start mission", 482, 38);
        g.drawString("R resolve objective  |  M minimap  |  F5 save  |  F9 load  |  Route: 4 rooms eastbound", 482, 56);

        String dashStatus = state.isDashing()
                ? String.format(Locale.ROOT, "Dashing (%.2fs)", Math.max(0f, state.dashTimerSeconds()))
                : String.format(Locale.ROOT, "Dash CD %.2fs", Math.max(0f, state.dashCooldownSeconds()));
        String attackStatus = state.attackCooldownSeconds() > 0f
                ? String.format(Locale.ROOT, "Attack CD %.2fs", state.attackCooldownSeconds())
                : "Attack Ready";
        String airState = "Airborne";
        String jumpState = state.jumpCount() == 0 ? "Jumps 0/2"
                : (state.jumpCount() == 1 ? "Jumps 1/2" : "Jumps 2/2");
        String wallState = state.wallExhaustedAwaitGround()
                ? "Wall Exhausted"
                : String.format(Locale.ROOT, "Wall Stamina %.2f", state.wallStaminaSeconds());
        String minimapState = showMinimap ? "Map On" : "Map Off";
        g.drawString("Movement: " + dashStatus + "  |  " + attackStatus + "  |  " + jumpState + "  |  " + wallState, 482, 76);
        g.drawString("State: " + airState + "  |  " + minimapState + "  |  Mission Time "
                + String.format(Locale.ROOT, "%.1fs", storyState.getMissionTimer()), 482, 94);
        g.drawString("Missions Available: " + missionManager.getAvailableMissions().size()
                + "  |  Encounters Cleared: " + state.clearedCombatEncounterIds().size()
                + "/" + combatSubsystem.allEncounters().size(), 482, 112);

        String gateSummary = traversalSubsystem.allGates().stream()
                .map(gate -> gate.displayName() + ":" + (gate.isUnlocked(storyState)
                        ? "OPEN" : "LOCKED(" + gate.requiredAbility() + ")"))
                .reduce((left, right) -> left + "  |  " + right)
                .orElse("No traversal gates");
        g.drawString("Gates: " + gateSummary, 482, 130);
        g.drawString("Hint: " + interactionHint, 482, 148);

        String encounterSummary = combatSubsystem.allEncounters().stream()
                .filter(encounter -> !encounter.isCleared())
                .findFirst()
                .map(encounter -> encounter.displayName() + " [" + encounter.pattern() + " "
                        + encounter.phaseLabel() + " "
                        + String.format(Locale.ROOT, "%.1fs", encounter.phaseTimeRemainingSeconds())
                        + ", " + encounter.hitsRemaining() + " hits]")
                .orElse("All encounter targets cleared");
        g.drawString("Encounter Focus: " + encounterSummary, 482, 166);
        g.setColor(overlayStatusLine.contains("none") ? new Color(173, 201, 255) : new Color(255, 200, 100));
        g.drawString(overlayStatusLine, 482, 184);
        g.setColor(java.awt.Color.WHITE);

        if (missionFeedbackSeconds > 0f && missionFeedbackLine != null && !missionFeedbackLine.isBlank()) {
            g.setColor(new Color(22, 34, 57, 220));
            g.fillRoundRect(470, 190, 790, 30, 12, 12);
            g.setColor(new Color(228, 244, 255));
            g.drawRoundRect(470, 190, 790, 30, 12, 12);
            g.drawString("Mission Feed: " + missionFeedbackLine, 482, 210);
        }
    }

    // -------------------------------------------------------------------------
    // Public setters (delegated from UISubsystem)
    // -------------------------------------------------------------------------

    void setMissionFeedback(String line, float seconds) {
        missionFeedbackLine    = line;
        missionFeedbackSeconds = seconds;
    }

    void setOverlayStatusLine(String statusLine) {
        this.overlayStatusLine = (statusLine == null || statusLine.isBlank())
                ? "Region Overlays: none" : statusLine;
    }

    void seedAbilitySnapshot(Set<String> abilities) {
        surfacedAbilitySnapshot.clear();
        surfacedAbilitySnapshot.addAll(abilities);
    }

    void toggleMinimap() {
        showMinimap = !showMinimap;
    }

    boolean isShowMinimap() {
        return showMinimap;
    }

    // -------------------------------------------------------------------------
    // Private update logic (moved from UISubsystem)
    // -------------------------------------------------------------------------

    private void updateInteractionHint(float playerX, float playerY) {
        CombatEncounter nearbyEncounter = combatSubsystem.nearestWithinRange(
                playerX, playerY, COMBAT_HINT_DETECT_RANGE);
        if (nearbyEncounter != null) {
            if (nearbyEncounter.isCleared()) {
                interactionHint = nearbyEncounter.displayName() + " cleared.";
                return;
            }
            if (nearbyEncounter.requiredAbility() != null
                    && !nearbyEncounter.requiredAbility().isBlank()
                    && !storyState.hasAbility(nearbyEncounter.requiredAbility())) {
                interactionHint = nearbyEncounter.displayName() + " locked: needs " + nearbyEncounter.requiredAbility() + ".";
                return;
            }
            if (nearbyEncounter.phase() == CombatEncounterPhase.DORMANT) {
                interactionHint = "Approach " + nearbyEncounter.displayName() + " to start encounter.";
                return;
            }
            if (nearbyEncounter.isAttackWindowOpen()) {
                interactionHint = "[F] Strike " + nearbyEncounter.displayName() + " (" + nearbyEncounter.hitsRemaining() + " hits left)";
            } else {
                interactionHint = nearbyEncounter.displayName() + " " + nearbyEncounter.phaseLabel()
                        + " (" + String.format(Locale.ROOT, "%.1fs", nearbyEncounter.phaseTimeRemainingSeconds()) + ")";
            }
            return;
        }

        AbilityTrigger nearby = traversalSubsystem.nearestTriggerWithinRange(
                playerX, playerY, TRIGGER_HINT_DETECT_RANGE);
        if (nearby == null) {
            interactionHint = "Explore east through traversal rooms and use ability triggers.";
            return;
        }
        if (traversalSubsystem.isTriggerActivated(nearby.id())) {
            interactionHint = nearby.displayName() + " already stabilized.";
            return;
        }
        if (!storyState.hasAbility(nearby.requiredAbility())) {
            interactionHint = nearby.displayName() + " locked: needs " + nearby.requiredAbility() + ".";
            return;
        }
        if (nearby.mode() == TriggerMode.INTERACT) {
            interactionHint = "[E] Activate " + nearby.displayName();
        } else {
            interactionHint = "[SHIFT] Dash through " + nearby.displayName();
        }
    }

    private void detectNewAbilityUnlocks() {
        for (String ability : storyState.getAbilities()) {
            if (surfacedAbilitySnapshot.add(ability)) {
                eventLogSink.accept("Ability unlocked: " + ability);
                missionFeedbackLine    = "Ability unlocked: " + ability;
                missionFeedbackSeconds = FEEDBACK_FLASH_SECONDS;
            }
        }
    }

    private void updateMissionFeedbackLine() {
        String missionId = storyState.getActiveMissionId();
        if (missionId == null) {
            if (missionFeedbackSeconds <= 0f) {
                missionFeedbackLine = "No active mission. Press ENTER/TAB to start.";
            }
            return;
        }
        Mission mission = storyState.getMission(missionId);
        if (mission == null) {
            return;
        }
        if (missionFeedbackSeconds <= 0f) {
            String nextObjective = firstPendingObjective(mission);
            missionFeedbackLine = mission.getDisplayName() + ": next -> " + nextObjective;
        }
    }

    // -------------------------------------------------------------------------
    // Static helpers
    // -------------------------------------------------------------------------

    private static float missionCompletionPercent(Mission mission) {
        if (mission == null || mission.getObjectives().isEmpty()) {
            return 0f;
        }
        int completed = 0;
        for (String objective : mission.getObjectives()) {
            if (mission.isObjectiveComplete(objective)) {
                completed++;
            }
        }
        return completed / (float) mission.getObjectives().size();
    }

    private static String firstPendingObjective(Mission mission) {
        if (mission == null) {
            return "";
        }
        for (String objective : mission.getObjectives()) {
            if (!mission.isObjectiveComplete(objective)) {
                return objective;
            }
        }
        return "All objectives complete";
    }
}
