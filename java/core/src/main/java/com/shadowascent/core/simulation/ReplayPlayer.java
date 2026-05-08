package com.shadowascent.core.simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Replays a recorded input log.
 * Reads NDJSON produced by InputRecorder and replays inputs tick-by-tick.
 */
public final class ReplayPlayer {

    private final long worldSeed;
    private final long totalEntries;
    private final long lastTick;
    /** tick -> (slot -> command) */
    private final Map<Long, Map<Integer, InputCommand>> log;

    private ReplayPlayer(long worldSeed, long totalEntries, long lastTick,
                         Map<Long, Map<Integer, InputCommand>> log) {
        this.worldSeed    = worldSeed;
        this.totalEntries = totalEntries;
        this.lastTick     = lastTick;
        this.log          = log;
    }

    public long seed()         { return worldSeed; }
    public long totalEntries() { return totalEntries; }

    public Map<Integer, InputCommand> inputsForTick(long tick) {
        return log.getOrDefault(tick, Collections.emptyMap());
    }

    public boolean isDone(long tick) {
        return tick > lastTick;
    }

    public static ReplayPlayer load(Path path) throws IOException {
        long seed = 0L;
        long entries = 0L;
        Map<Long, Map<Integer, InputCommand>> log = new LinkedHashMap<>();

        try (BufferedReader r = Files.newBufferedReader(path)) {
            String line;
            boolean first = true;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) {
                    first = false;
                    seed    = parseLong(line, "seed");
                    entries = parseLong(line, "entries");
                    continue;
                }
                long tick = parseLong(line, "tick");
                int  slot = (int) parseLong(line, "slot");
                InputCommand cmd = parseCommand(line);
                log.computeIfAbsent(tick, k -> new LinkedHashMap<>()).put(slot, cmd);
            }
        }
        return new ReplayPlayer(seed, entries, computeLastTick(log), log);
    }

    public static ReplayPlayer fromInputSequence(long worldSeed, int slot, List<InputCommand> commands) {
        Map<Long, Map<Integer, InputCommand>> log = new LinkedHashMap<>();
        long entries = 0L;
        if (commands != null) {
            for (int i = 0; i < commands.size(); i++) {
                InputCommand src = commands.get(i);
                if (src == null) continue;
                InputCommand copy = InputCommand.fromMap(src.toMap());
                copy.frame = i;
                log.computeIfAbsent((long) i, k -> new LinkedHashMap<>()).put(slot, copy);
                entries++;
            }
        }
        return new ReplayPlayer(worldSeed, entries, computeLastTick(log), log);
    }

    private static long parseLong(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0L;
        int start = idx + search.length();
        int end   = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try { return Long.parseLong(json.substring(start, end)); }
        catch (NumberFormatException e) { return 0L; }
    }

    private static boolean parseBool(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return false;
        return json.startsWith("true", idx + search.length());
    }

    private static InputCommand parseCommand(String json) {
        InputCommand c = new InputCommand();
        c.left         = parseBool(json, "left");
        c.right        = parseBool(json, "right");
        c.jump         = parseBool(json, "jump");
        c.dash         = parseBool(json, "dash");
        c.attack       = parseBool(json, "attack");
        c.throwShuriken = parseBool(json, "throw");
        c.crouch       = parseBool(json, "crouch");
        c.ninjutsu     = parseBool(json, "ninjutsu");
        c.block        = parseBool(json, "block");
        c.interact     = parseBool(json, "interact");
        c.slowWalk     = parseBool(json, "slowWalk");
        c.stanceSwitch = parseBool(json, "stanceSwitch");
        return c;
    }

    private static long computeLastTick(Map<Long, Map<Integer, InputCommand>> log) {
        return log.isEmpty()
                ? -1L
                : log.keySet().stream().mapToLong(Long::longValue).max().orElse(-1L);
    }
}
