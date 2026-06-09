package com.shadowascent.client.world;

import com.shadowascent.core.GameState;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.data.BeatDefinition;
import com.shadowascent.core.data.GameDataContracts;

import java.util.Comparator;
import java.util.Optional;

public final class AreaPlacementResolver {

    public String resolveAreaId(GameState gameState) {
        if (gameState == null) {
            return "area_lantern_heights_hub";
        }

        GameDataContracts contracts = gameState.getDataContracts();
        StoryState storyState = gameState.getStoryState();
        if (storyState.hasFlag("ending_complete")) {
            return "area_final_npc_overlook";
        }
        if (storyState.getCurrentPlateau() == StoryState.Plateau.SUMMIT_SHRINE) {
            return resolveSummitShrineArea(storyState);
        }
        if (storyState.getCurrentPlateau() == StoryState.Plateau.EMBER_MONASTERY) {
            return resolveEmberMonasteryArea(storyState);
        }
        if (storyState.getCurrentPlateau() == StoryState.Plateau.WINDING_SKYROAD) {
            return resolveWindingSkyroadArea(storyState);
        }
        if (storyState.getCurrentPlateau() == StoryState.Plateau.MIRROR_SUMMIT) {
            return resolveMirrorSummitArea(storyState);
        }
        if (storyState.getCurrentPlateau() == StoryState.Plateau.BEACON_CLIFF) {
            return resolveBeaconCliffArea(storyState);
        }

        return resolveFromContractBeats(contracts, storyState);
    }

    private static String resolveSummitShrineArea(StoryState storyState) {
        if (storyState.hasFlag("yin_yang_taken")) {
            return "area_summit_shrine_fall";
        }
        if (storyState.hasFlag("empty_hub_only_maiden")) {
            return "area_summit_shrine_arena";
        }
        if (storyState.hasFlag("entered_summit")) {
            return "area_empty_lantern_heights_hub";
        }
        return "area_summit_shrine_gate";
    }

    private static String resolveEmberMonasteryArea(StoryState storyState) {
        if (storyState.hasFlag("skyroad_opened")) {
            return "area_skyroad_gate";
        }
        if (storyState.hasFlag("hearth_joined") && !storyState.hasFlag("air_dodge_restored")) {
            return "area_hearth_of_brothers_trial";
        }
        if (storyState.hasFlag("grapple_restored") && !storyState.hasFlag("wall_cling_mastered")) {
            return "area_roga_dojo";
        }
        if (storyState.hasFlag("hearth_joined") && !storyState.hasFlag("samson_returned")) {
            return "area_ember_monastery_hub";
        }
        if (storyState.hasFlag("hearth_joined") && !storyState.hasFlag("marcel_returned")) {
            return "area_ember_forge";
        }
        if (storyState.hasFlag("wall_cling_mastered") && !storyState.hasFlag("skyroad_map_acquired")) {
            return "area_star_ink_garden";
        }
        if (storyState.hasFlag("hearth_joined") && !storyState.hasFlag("beacon_lantern_prepared")) {
            return "area_lantern_silk_workshop";
        }
        if (storyState.hasFlag("wall_cling_mastered") && storyState.hasFlag("skyroad_map_acquired")) {
            return "area_skyroad_gate";
        }
        if (storyState.hasFlag("entered_ember_monastery")) {
            return "area_hearth_hall";
        }
        return "area_old_road_to_ember";
    }

    private static String resolveWindingSkyroadArea(StoryState storyState) {
        if (storyState.hasFlag("mirror_gate_opened")) {
            return "area_mirror_summit_gate";
        }
        if (storyState.hasFlag("stars_accepted")) {
            return "area_star_wind_fields";
        }
        return "area_skyroad_base";
    }

    private static String resolveMirrorSummitArea(StoryState storyState) {
        if (storyState.hasFlag("release_accepted")) {
            return "area_release_walk";
        }
        if (storyState.hasFlag("reflection_fight_started")) {
            return "area_hollow_reflection_arena";
        }
        if (storyState.hasFlag("mirror_approached")) {
            return "area_mirror_summit_peak";
        }
        return "area_mirror_summit_gate";
    }

    private static String resolveBeaconCliffArea(StoryState storyState) {
        if (storyState.hasFlag("beacon_lit")) {
            return "area_twofold_stars";
        }
        if (storyState.hasFlag("final_words_heard")) {
            return "area_ancient_beacon";
        }
        return "area_beacon_cliff_approach";
    }

    private static String resolveFromContractBeats(GameDataContracts contracts, StoryState storyState) {
        Optional<BeatDefinition> plateauBeat = contracts.beatsForPlateau(storyState.getCurrentPlateau().name()).stream()
                .filter(AreaPlacementResolver::isRuntimeAreaBeat)
                .filter(beat -> beat.requiredFlags().stream().allMatch(storyState::hasFlag))
                .filter(beat -> isUnresolvedForAreaPlacement(beat, storyState))
                .filter(beat -> !beat.areaId().isBlank())
                .min(Comparator.comparingInt(BeatDefinition::routeOrder).thenComparing(BeatDefinition::id));
        if (plateauBeat.isPresent()) {
            return plateauBeat.get().areaId();
        }

        Optional<BeatDefinition> nextBeat = contracts.nextCriticalBeat(storyState)
                .filter(beat -> !"GLOBAL".equalsIgnoreCase(beat.plateau()))
                .filter(beat -> !beat.areaId().isBlank());
        if (nextBeat.isPresent()) {
            return nextBeat.get().areaId();
        }

        return contracts.beatsForPlateau(storyState.getCurrentPlateau().name()).stream()
                .filter(BeatDefinition::isCriticalPathBeat)
                .filter(beat -> !beat.areaId().isBlank())
                .filter(beat -> isUnresolvedForAreaPlacement(beat, storyState))
                .min(Comparator.comparingInt(BeatDefinition::routeOrder).thenComparing(BeatDefinition::id))
                .map(BeatDefinition::areaId)
                .orElse("area_lantern_heights_hub");
    }

    private static boolean isRuntimeAreaBeat(BeatDefinition beat) {
        if (beat == null) {
            return false;
        }
        String beatType = beat.beatType() == null ? "" : beat.beatType().trim().toLowerCase();
        return beat.isCriticalPathBeat() || switch (beatType) {
            case "adaptable_authored", "authored_support", "recovery", "unlock" -> true;
            default -> false;
        };
    }

    private static boolean isUnresolvedForAreaPlacement(BeatDefinition beat, StoryState storyState) {
        if (beat == null || storyState == null) {
            return false;
        }
        if (beat.setFlags().isEmpty()) {
            return true;
        }
        String routeAdvanceFlag = beat.setFlags().getFirst();
        return routeAdvanceFlag == null || routeAdvanceFlag.isBlank() || !storyState.hasFlag(routeAdvanceFlag);
    }
}
