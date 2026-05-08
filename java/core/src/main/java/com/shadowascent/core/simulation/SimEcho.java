package com.shadowascent.core.simulation;

import java.util.Map;

/**
 * Server-side echo playback entity.
 * Reads one tick of inputs at a time from ReplayPlayer for puzzle evaluation.
 */
public final class SimEcho {

    public final String echoId;
    public final int ownerSlot;
    public final ReplayPlayer replay;

    public float x;
    public float y;
    public int   facing      = 1;

    public String animState   = "idle";
    public String weaponState = "unarmed";

    public final boolean recallable;
    public final String  echoType;

    // ── Combat dimensions ─────────────────────────────────────────────────────
    public static final int W            = 20;
    public static final int H            = 28;
    public static final int ATTACK_REACH = 14;
    public static final int ECHO_DAMAGE  = 1;

    public boolean active    = true;
    public boolean completed = false;
    public boolean failed    = false;
    public boolean looping   = false;

    private boolean prevAttackInput  = false;
    public  boolean attackedThisTick = false;
    public  int     echoKillCount    = 0;

    private long         tickCursor   = 0L;
    private InputCommand currentInput = InputCommand.neutral(0);

    public SimEcho(String echoId, int ownerSlot, float startX, float startY,
                   ReplayPlayer replay, boolean recallable, String weaponState, String echoType) {
        this.echoId      = echoId;
        this.ownerSlot   = ownerSlot;
        this.x           = startX;
        this.y           = startY;
        this.replay      = replay;
        this.recallable  = recallable;
        this.weaponState = weaponState != null ? weaponState : "unarmed";
        this.echoType    = echoType != null ? echoType : "silent";
    }

    public void step() {
        attackedThisTick = false;
        if (!active || replay == null) return;

        if (replay.isDone(tickCursor)) {
            if (looping) {
                tickCursor = 0L;
                completed  = false;
            } else {
                completed = true;
                active    = false;
                return;
            }
        }

        InputCommand cmd = pickCommand(replay.inputsForTick(tickCursor));
        if (cmd != null) {
            currentInput     = InputCommand.fromMap(cmd.toMap());
            attackedThisTick = currentInput.attack && !prevAttackInput;
            prevAttackInput  = currentInput.attack;
            if (currentInput.left && !currentInput.right)  facing = -1;
            if (currentInput.right && !currentInput.left)  facing =  1;
            if (currentInput.jump)           animState = "jump";
            else if (currentInput.dash)      animState = "dash";
            else if (currentInput.attack)    animState = "attack";
            else if (currentInput.left || currentInput.right) animState = "run";
            else                             animState = "idle";
        }

        tickCursor++;
        if (!looping && replay.isDone(tickCursor)) {
            completed = true;
            active    = false;
        }
    }

    public void restart() {
        tickCursor       = 0L;
        completed        = false;
        failed           = false;
        active           = true;
        animState        = "idle";
        currentInput     = InputCommand.neutral(ownerSlot);
        prevAttackInput  = false;
        attackedThisTick = false;
        echoKillCount    = 0;
    }

    public boolean recall() {
        if (failed) return false;
        if (completed) {
            active = false;
            return true;
        }
        if (!recallable) {
            failed = true;
            active = false;
            return false;
        }
        active = false;
        return true;
    }

    public long ticksPlayed() { return tickCursor; }

    public InputCommand currentInput() {
        return InputCommand.fromMap(currentInput.toMap());
    }

    private InputCommand pickCommand(Map<Integer, InputCommand> inputs) {
        if (inputs == null || inputs.isEmpty()) return null;
        InputCommand owned = inputs.get(ownerSlot);
        if (owned != null) return owned;
        return inputs.values().iterator().next();
    }
}
