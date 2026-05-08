package com.shadowascent.core.simulation;

import java.util.Map;

/**
 * Specifies the requirements an echo must satisfy to pass a puzzle check.
 *
 * minActionPresses: action name -> minimum number of rising-edge presses required.
 *   Supported action names: "jump", "attack", "dash", "interact", "move",
 *   "throw", "ninjutsu", "block", "crouch".
 * maxTicks: maximum ticks the echo may run (0 = no limit).
 * requireCompletion: if true the echo must have reached completed=true (not failed or still active).
 */
public record EchoPuzzleSolution(
        String puzzleId,
        Map<String, Integer> minActionPresses,
        long maxTicks,
        boolean requireCompletion,
        int minEchoKills) {

    public EchoPuzzleSolution {
        puzzleId = (puzzleId == null || puzzleId.isBlank()) ? "unnamed" : puzzleId;
        minActionPresses = minActionPresses != null ? Map.copyOf(minActionPresses) : Map.of();
        maxTicks = Math.max(0L, maxTicks);
        minEchoKills = Math.max(0, minEchoKills);
    }

    /** Convenience: no timing limit, completion not required, no kill requirement. */
    public static EchoPuzzleSolution ofActions(String puzzleId, Map<String, Integer> minPresses) {
        return new EchoPuzzleSolution(puzzleId, minPresses, 0L, false, 0);
    }

    /** Convenience: gate on echo kill count only. */
    public static EchoPuzzleSolution ofKills(String puzzleId, int minKills) {
        return new EchoPuzzleSolution(puzzleId, Map.of(), 0L, false, minKills);
    }
}
