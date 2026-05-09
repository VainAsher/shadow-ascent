package com.shadowascent.client.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.InputCommand;

/**
 * LibGDX input processor for the production client.
 * Maps keyboard events to InputCommand fields and routes them to GameSimulator.
 * Replaces the Swing WHEN_IN_FOCUSED_WINDOW key bindings used by PlaytestClient.
 */
public final class GameInputProcessor extends InputAdapter {

    private final GameSimulator simulator;
    private final String        playerId;
    private final InputCommand  cmd = new InputCommand();
    private int                 frame = 0;

    public GameInputProcessor(GameSimulator simulator, String playerId) {
        this.simulator = simulator;
        this.playerId  = playerId;
    }

    @Override
    public boolean keyDown(int keycode) {
        apply(keycode, true);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        apply(keycode, false);
        return true;
    }

    /** Called each frame tick so the current command snapshot is sent to the sim. */
    public void submitFrame() {
        cmd.frame = frame++;
        simulator.applyInput(playerId, cmd);
    }

    private void apply(int keycode, boolean pressed) {
        switch (keycode) {
            case Keys.LEFT,  Keys.A -> cmd.left   = pressed;
            case Keys.RIGHT, Keys.D -> cmd.right  = pressed;
            case Keys.UP,    Keys.W -> cmd.up     = pressed;
            case Keys.DOWN,  Keys.S -> cmd.down   = pressed;
            case Keys.SPACE         -> cmd.jump   = pressed;
            case Keys.SHIFT_LEFT,
                 Keys.C            -> cmd.dash    = pressed;
            case Keys.F            -> cmd.attack  = pressed;
            case Keys.E            -> cmd.interact = pressed;
            case Keys.I            -> cmd.inventory = pressed;
            case Keys.T            -> { /* crafting — reserved */ }
            default                -> { /* unbound */ }
        }
    }
}
