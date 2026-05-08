package com.shadowascent.core.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates a completed SimEcho against an EchoPuzzleSolution.
 *
 * Counts rising-edge action presses by walking the replay tick sequence,
 * then checks each solution requirement and returns a Result with pass/fail
 * trace lines per rule.
 */
public final class EchoPuzzleEvaluator {

    public record Result(
            boolean passed,
            List<String> trace,
            Map<String, Integer> actionPressCounts) {

        public Result {
            trace = trace != null ? List.copyOf(trace) : List.of();
            actionPressCounts = actionPressCounts != null ? Map.copyOf(actionPressCounts) : Map.of();
        }
    }

    public Result evaluate(SimEcho echo, EchoPuzzleSolution solution) {
        List<String> trace = new ArrayList<>();
        boolean allPassed = true;

        if (echo == null) {
            trace.add("[FAIL] echo is null");
            return new Result(false, trace, Map.of());
        }
        if (solution == null) {
            trace.add("[FAIL] solution is null");
            return new Result(false, trace, Map.of());
        }

        trace.add("puzzle=" + solution.puzzleId() + " echoId=" + echo.echoId
                + " ticks=" + echo.ticksPlayed());

        // Completion check
        if (solution.requireCompletion() && !echo.completed) {
            trace.add("[FAIL] requireCompletion: echo not completed"
                    + " (active=" + echo.active + " failed=" + echo.failed + ")");
            allPassed = false;
        } else if (solution.requireCompletion()) {
            trace.add("[PASS] requireCompletion: echo completed");
        }

        // Tick limit check
        if (solution.maxTicks() > 0 && echo.ticksPlayed() > solution.maxTicks()) {
            trace.add("[FAIL] maxTicks: played=" + echo.ticksPlayed()
                    + " limit=" + solution.maxTicks());
            allPassed = false;
        } else if (solution.maxTicks() > 0) {
            trace.add("[PASS] maxTicks: played=" + echo.ticksPlayed()
                    + " <= limit=" + solution.maxTicks());
        }

        // Count rising-edge action presses from the replay sequence
        Map<String, Integer> counts = countActionPresses(echo);

        // Check each minimum-press requirement
        for (Map.Entry<String, Integer> req : solution.minActionPresses().entrySet()) {
            String action = req.getKey();
            int required = req.getValue();
            int actual = counts.getOrDefault(action, 0);
            if (actual >= required) {
                trace.add("[PASS] " + action + ": presses=" + actual + " >= required=" + required);
            } else {
                trace.add("[FAIL] " + action + ": presses=" + actual + " < required=" + required);
                allPassed = false;
            }
        }

        // Echo kill count check
        if (solution.minEchoKills() > 0) {
            if (echo.echoKillCount >= solution.minEchoKills()) {
                trace.add("[PASS] echoKills: " + echo.echoKillCount
                        + " >= required=" + solution.minEchoKills());
            } else {
                trace.add("[FAIL] echoKills: " + echo.echoKillCount
                        + " < required=" + solution.minEchoKills());
                allPassed = false;
            }
        }

        if (solution.minActionPresses().isEmpty() && solution.maxTicks() == 0
                && !solution.requireCompletion() && solution.minEchoKills() == 0) {
            trace.add("[PASS] no requirements — trivially satisfied");
        }

        return new Result(allPassed, trace, counts);
    }

    private Map<String, Integer> countActionPresses(SimEcho echo) {
        Map<String, Integer> counts = new HashMap<>();
        if (echo.replay == null) return counts;

        Map<String, Boolean> prev = new HashMap<>();

        long tick = 0L;
        while (!echo.replay.isDone(tick)) {
            Map<Integer, InputCommand> frame = echo.replay.inputsForTick(tick);
            InputCommand cmd = frame.isEmpty() ? null : frame.values().iterator().next();

            recordPress(counts, prev, "jump",     cmd != null && cmd.jump);
            recordPress(counts, prev, "attack",   cmd != null && cmd.attack);
            recordPress(counts, prev, "dash",     cmd != null && cmd.dash);
            recordPress(counts, prev, "interact", cmd != null && cmd.interact);
            recordPress(counts, prev, "move",     cmd != null && (cmd.left || cmd.right));
            recordPress(counts, prev, "throw",    cmd != null && cmd.throwShuriken);
            recordPress(counts, prev, "ninjutsu", cmd != null && cmd.ninjutsu);
            recordPress(counts, prev, "block",    cmd != null && cmd.block);
            recordPress(counts, prev, "crouch",   cmd != null && cmd.crouch);

            tick++;
        }
        return counts;
    }

    private void recordPress(Map<String, Integer> counts, Map<String, Boolean> prev,
                             String action, boolean current) {
        boolean wasPrev = prev.getOrDefault(action, false);
        if (current && !wasPrev) {
            counts.merge(action, 1, Integer::sum);
        }
        prev.put(action, current);
    }
}
