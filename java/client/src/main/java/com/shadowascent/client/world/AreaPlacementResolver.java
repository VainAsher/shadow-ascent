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
        Optional<BeatDefinition> plateauBeat = contracts.beatsForPlateau(storyState.getCurrentPlateau().name()).stream()
                .filter(AreaPlacementResolver::isRuntimeAreaBeat)
                .filter(beat -> beat.requiredFlags().stream().allMatch(storyState::hasFlag))
                .filter(beat -> beat.setFlags().isEmpty() || beat.setFlags().stream().anyMatch(flag -> !storyState.hasFlag(flag)))
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
}
