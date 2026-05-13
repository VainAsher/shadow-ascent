package com.shadowascent.client.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.shadowascent.client.ui.ModalOverlayManager;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.InputCommand;

import java.util.Objects;

/**
 * LibGDX input processor for the production client.
 * Maps keyboard events to InputCommand fields and routes them to GameSimulator.
 * Replaces the Swing WHEN_IN_FOCUSED_WINDOW key bindings used by PlaytestClient.
 */
public final class GameInputProcessor extends InputAdapter {

    private final GameSimulator simulator;
    private final String playerId;
    private final ModalOverlayManager overlays;
    private final InputCommand cmd = new InputCommand();
    private InputCommand lastSubmittedCommand = InputCommand.neutral(0);
    private boolean inventoryTogglePressed;
    private boolean craftingTogglePressed;
    private boolean minimapTogglePressed;
    private boolean cancelPressed;
    private boolean menuConfirmPressed;
    private boolean menuLeftPressed;
    private boolean menuRightPressed;
    private boolean menuUpPressed;
    private boolean menuDownPressed;
    private int frame = 0;

    public GameInputProcessor(GameSimulator simulator, String playerId, ModalOverlayManager overlays) {
        this.simulator = Objects.requireNonNull(simulator, "simulator");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.overlays = Objects.requireNonNull(overlays, "overlays");
    }

    @Override
    public boolean keyDown(int keycode) {
        return apply(keycode, true);
    }

    @Override
    public boolean keyUp(int keycode) {
        return apply(keycode, false);
    }

    /** Called each frame tick so the current command snapshot is sent to the sim. */
    public void submitFrame() {
        InputCommand submitted = snapshotForSubmit();
        submitted.frame = frame++;
        lastSubmittedCommand = copyOf(submitted);
        simulator.applyInput(playerId, submitted);
    }

    public InputCommand lastSubmittedCommand() {
        return copyOf(lastSubmittedCommand);
    }

    public boolean consumeInventoryTogglePressed() {
        boolean pressed = inventoryTogglePressed;
        inventoryTogglePressed = false;
        return pressed;
    }

    public boolean consumeCraftingTogglePressed() {
        boolean pressed = craftingTogglePressed;
        craftingTogglePressed = false;
        return pressed;
    }

    public boolean consumeMinimapTogglePressed() {
        boolean pressed = minimapTogglePressed;
        minimapTogglePressed = false;
        return pressed;
    }

    public boolean consumeCancelPressed() {
        boolean pressed = cancelPressed;
        cancelPressed = false;
        return pressed;
    }

    public boolean consumeMenuConfirmPressed() {
        boolean pressed = menuConfirmPressed;
        menuConfirmPressed = false;
        return pressed;
    }

    public boolean consumeMenuLeftPressed() {
        boolean pressed = menuLeftPressed;
        menuLeftPressed = false;
        return pressed;
    }

    public boolean consumeMenuRightPressed() {
        boolean pressed = menuRightPressed;
        menuRightPressed = false;
        return pressed;
    }

    public boolean consumeMenuUpPressed() {
        boolean pressed = menuUpPressed;
        menuUpPressed = false;
        return pressed;
    }

    public boolean consumeMenuDownPressed() {
        boolean pressed = menuDownPressed;
        menuDownPressed = false;
        return pressed;
    }

    private boolean apply(int keycode, boolean pressed) {
        switch (keycode) {
            case Keys.LEFT, Keys.A -> {
                cmd.left = pressed;
                if (pressed) {
                    menuLeftPressed = true;
                }
                return true;
            }
            case Keys.RIGHT, Keys.D -> {
                cmd.right = pressed;
                if (pressed) {
                    menuRightPressed = true;
                }
                return true;
            }
            case Keys.UP, Keys.W -> {
                cmd.up = pressed;
                if (pressed) {
                    menuUpPressed = true;
                }
                return true;
            }
            case Keys.DOWN, Keys.S -> {
                cmd.down = pressed;
                if (pressed) {
                    menuDownPressed = true;
                }
                return true;
            }
            case Keys.SPACE -> {
                cmd.jump = pressed;
                return true;
            }
            case Keys.SHIFT_LEFT, Keys.C -> {
                cmd.dash = pressed;
                return true;
            }
            case Keys.F -> {
                cmd.attack = pressed;
                return true;
            }
            case Keys.E -> {
                cmd.interact = pressed;
                return true;
            }
            case Keys.I -> {
                if (pressed) {
                    inventoryTogglePressed = true;
                }
                cmd.inventory = pressed;
                return true;
            }
            case Keys.T -> {
                if (pressed) {
                    craftingTogglePressed = true;
                }
                return true;
            }
            case Keys.M -> {
                if (pressed) {
                    minimapTogglePressed = true;
                }
                cmd.minimap = pressed;
                return true;
            }
            case Keys.ESCAPE -> {
                if (pressed) {
                    cancelPressed = true;
                }
                cmd.menuBack = pressed;
                return true;
            }
            case Keys.ENTER -> {
                if (pressed) {
                    menuConfirmPressed = true;
                }
                cmd.menuConfirm = pressed;
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private InputCommand snapshotForSubmit() {
        if (!overlays.hasActiveOverlay()) {
            return copyOf(cmd);
        }
        InputCommand snapshot = InputCommand.neutral(cmd.frame);
        snapshot.inventory = cmd.inventory;
        snapshot.minimap = cmd.minimap;
        snapshot.fullmap = cmd.fullmap;
        snapshot.controlsOverlay = cmd.controlsOverlay;
        snapshot.debugOverlay = cmd.debugOverlay;
        snapshot.menuConfirm = cmd.menuConfirm;
        snapshot.menuBack = cmd.menuBack;
        return snapshot;
    }

    private static InputCommand copyOf(InputCommand source) {
        InputCommand copy = new InputCommand(source.frame);
        copy.up = source.up;
        copy.down = source.down;
        copy.left = source.left;
        copy.right = source.right;
        copy.jump = source.jump;
        copy.dash = source.dash;
        copy.crouch = source.crouch;
        copy.toggleProc = source.toggleProc;
        copy.cycleCamera = source.cycleCamera;
        copy.attack = source.attack;
        copy.throwShuriken = source.throwShuriken;
        copy.teleport = source.teleport;
        copy.ninjutsu = source.ninjutsu;
        copy.block = source.block;
        copy.interact = source.interact;
        copy.inventory = source.inventory;
        copy.consumable = source.consumable;
        copy.minimap = source.minimap;
        copy.fullmap = source.fullmap;
        copy.controlsOverlay = source.controlsOverlay;
        copy.debugOverlay = source.debugOverlay;
        copy.slowWalk = source.slowWalk;
        copy.menuConfirm = source.menuConfirm;
        copy.menuBack = source.menuBack;
        copy.stanceSwitch = source.stanceSwitch;
        copy.selectWeapon1 = source.selectWeapon1;
        copy.selectWeapon2 = source.selectWeapon2;
        return copy;
    }
}
