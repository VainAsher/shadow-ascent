package com.shadowascent.core;

/**
 * Tracks emotional balance mechanics throughout the campaign.
 * Yin/Yang systems influence story outcomes and hub evolution.
 * Based on shadow_ascent_integrated_package emotional mechanics.
 */
public class EmotionalState {

    public enum CompanionState {
        ALIGNED,
        CONFLICTED,
        FRACTURED
    }

    private int yinInfluence = 0;    // Introspection, loss, acceptance
    private int yangInfluence = 0;   // Action, growth, transcendence
    private CompanionState companionState = CompanionState.ALIGNED;
    private int empathyBonds = 0;    // Track connection depth with NPCs

    /**
     * Add yin influence (introspection, reactive, loss-driven choices).
     */
    public void addYin(int amount) {
        if (amount > 0) {
            yinInfluence += amount;
        }
    }

    /**
     * Add yang influence (action, proactive, growth-driven choices).
     */
    public void addYang(int amount) {
        if (amount > 0) {
            yangInfluence += amount;
        }
    }

    /**
     * Get the current emotional balance.
     */
    public String getBalance() {
        if (yinInfluence > yangInfluence) {
            return "Yin-dominant";
        } else if (yangInfluence > yinInfluence) {
            return "Yang-dominant";
        }
        return "Balanced";
    }

    /**
     * Get dominance value (-100 to +100).
     * Negative = Yin dominant, Positive = Yang dominant.
     */
    public int getDominance() {
        int total = yinInfluence + yangInfluence;
        if (total == 0) return 0;
        return ((yangInfluence - yinInfluence) * 100) / total;
    }

    /**
     * Update companion state based on emotional balance.
     * ALIGNED: within 20% of balance
     * CONFLICTED: 20-40% imbalance
     * FRACTURED: >40% imbalance
     */
    public void updateCompanionState() {
        int dominance = Math.abs(getDominance());
        if (dominance <= 20) {
            companionState = CompanionState.ALIGNED;
        } else if (dominance <= 40) {
            companionState = CompanionState.CONFLICTED;
        } else {
            companionState = CompanionState.FRACTURED;
        }
    }

    /**
     * Record an empathy bond with an NPC.
     */
    public void recordEmpathyBond(String npcId) {
        empathyBonds++;
    }

    /**
     * Get total empathy bonds.
     */
    public int getEmpathyBonds() {
        return empathyBonds;
    }

    public int getYinInfluence() {
        return yinInfluence;
    }

    public int getYangInfluence() {
        return yangInfluence;
    }

    public CompanionState getCompanionState() {
        return companionState;
    }

    public void setCompanionState(CompanionState state) {
        this.companionState = state;
    }

    public void setYinInfluence(int yin) {
        this.yinInfluence = yin;
    }

    public void setYangInfluence(int yang) {
        this.yangInfluence = yang;
    }

    public void setEmpathyBonds(int empathy) {
        this.empathyBonds = empathy;
    }


    /**
     * Serialize emotional state for save/load.
     */
    public String serialize() {
        return String.format("yin=%d;yang=%d;companion=%s;empathy=%d",
                yinInfluence, yangInfluence, companionState, empathyBonds);
    }

    /**
     * Deserialize emotional state from save.
     */
    public static EmotionalState deserialize(String data) {
        EmotionalState state = new EmotionalState();
        if (data == null || data.isEmpty()) {
            return state;
        }

        String[] parts = data.split(";");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) continue;

            try {
                switch (kv[0]) {
                    case "yin" -> state.yinInfluence = Integer.parseInt(kv[1]);
                    case "yang" -> state.yangInfluence = Integer.parseInt(kv[1]);
                    case "companion" -> state.companionState = CompanionState.valueOf(kv[1]);
                    case "empathy" -> state.empathyBonds = Integer.parseInt(kv[1]);
                    default -> {}
                }
            } catch (Exception ignored) {
            }
        }
        return state;
    }
}
