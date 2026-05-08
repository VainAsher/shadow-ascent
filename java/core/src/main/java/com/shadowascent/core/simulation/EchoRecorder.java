package com.shadowascent.core.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-player 10-second input ring buffer used by the Shadow Ascent echo system.
 * Imported from indie-ninja-adventures (com.indieniinja.sim.EchoRecorder).
 *
 * Stores one InputCommand snapshot per tick (60 Hz), fixed capacity 600.
 * snapshot() returns commands ordered oldest -> newest.
 */
public final class EchoRecorder {

    public static final int BUFFER = 600; // 10s at 60 Hz

    private final InputCommand[] ring = new InputCommand[BUFFER];
    private int head = 0;
    private int size = 0;

    public void record(InputCommand cmd) {
        if (cmd == null) return;
        ring[head] = copyOf(cmd);
        head = (head + 1) % BUFFER;
        if (size < BUFFER) size++;
    }

    public List<InputCommand> snapshot() {
        if (size == 0) return List.of();
        ArrayList<InputCommand> out = new ArrayList<>(size);
        int start = (head - size + BUFFER) % BUFFER;
        for (int i = 0; i < size; i++) {
            int idx = (start + i) % BUFFER;
            InputCommand cmd = ring[idx];
            if (cmd != null) out.add(copyOf(cmd));
        }
        return out;
    }

    public int size() { return size; }

    public void clear() {
        head = 0;
        size = 0;
        java.util.Arrays.fill(ring, null);
    }

    private static InputCommand copyOf(InputCommand src) {
        return InputCommand.fromMap(src.toMap());
    }
}
