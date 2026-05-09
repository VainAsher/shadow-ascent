package com.shadowascent.client;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Owns raw key-binding state and per-tick input queues for PlaytestClient.
 *
 * Call {@link #install(JComponent)} once during panel setup.
 * {@code tick()} reads held-key state via {@code pressedKeys} and polls queue flags directly.
 * Queue flags are package-private booleans — the caller is responsible for resetting each flag
 * to {@code false} after consuming it (matches the existing poll-and-reset pattern in tick()).
 */
final class InputHandler {

    // ── Held-key state (A, D, S, ALT) ──────────────────────────────────────────
    final Set<Integer> pressedKeys = new HashSet<>();

    // ── One-shot queue flags — reset to false by tick() after consumption ────────
    boolean queueInteract;
    boolean queueStartMission;
    boolean queueResolveObjective;
    boolean queueJump;
    boolean queueDash;
    boolean queueAttack;
    boolean queueSave;
    boolean queueLoad;
    boolean queueInventoryToggle;
    boolean queueCraftToggle;
    boolean queuePanelUp;
    boolean queuePanelDown;
    boolean queuePanelLeft;
    boolean queuePanelRight;
    boolean queuePanelAction;
    boolean queuePanelClose;

    /**
     * Registers all PlaytestClient key bindings against {@code panel}.
     * Extracted from {@code GamePanel.configureKeyBindings()}; call once in {@code GamePanel}.
     *
     * @param isPanelOpen  returns true when any UI overlay panel is currently open
     *                     (needed for the ENTER key dual-purpose binding)
     * @param toggleMinimap  toggles the minimap overlay (UISubsystem callback)
     */
    void install(JComponent panel, BooleanSupplier isPanelOpen, Runnable toggleMinimap) {
        bindHold(panel, KeyEvent.VK_A);
        bindHold(panel, KeyEvent.VK_D);
        bindHold(panel, KeyEvent.VK_S);
        bindHold(panel, KeyEvent.VK_ALT);

        bindPress(panel, "interact",             KeyEvent.VK_E,      () -> queueInteract          = true);
        bindPress(panel, "start_mission_enter",  KeyEvent.VK_ENTER,  () -> {
            if (isPanelOpen.getAsBoolean()) queuePanelAction = true;
            else                            queueStartMission = true;
        });
        bindPress(panel, "start_mission_tab",    KeyEvent.VK_TAB,    () -> queueStartMission      = true);
        bindPress(panel, "toggle_inventory",     KeyEvent.VK_I,      () -> queueInventoryToggle   = true);
        bindPress(panel, "toggle_crafting",      KeyEvent.VK_T,      () -> queueCraftToggle       = true);
        bindPress(panel, "panel_up",             KeyEvent.VK_UP,     () -> queuePanelUp           = true);
        bindPress(panel, "panel_down",           KeyEvent.VK_DOWN,   () -> queuePanelDown         = true);
        bindPress(panel, "panel_left",           KeyEvent.VK_LEFT,   () -> queuePanelLeft         = true);
        bindPress(panel, "panel_right",          KeyEvent.VK_RIGHT,  () -> queuePanelRight        = true);
        bindPress(panel, "panel_close",          KeyEvent.VK_ESCAPE, () -> queuePanelClose        = true);
        bindPress(panel, "jump",                 KeyEvent.VK_SPACE,  () -> queueJump              = true);
        bindPress(panel, "resolve_objective",    KeyEvent.VK_R,      () -> queueResolveObjective  = true);
        bindPress(panel, "dash",                 KeyEvent.VK_SHIFT,  () -> queueDash              = true);
        bindPress(panel, "dash_alt_key",         KeyEvent.VK_C,      () -> queueDash              = true);
        bindPress(panel, "attack",               KeyEvent.VK_F,      () -> queueAttack            = true);
        bindPress(panel, "toggle_minimap",       KeyEvent.VK_M,      toggleMinimap);
        bindPress(panel, "save",                 KeyEvent.VK_F5,     () -> queueSave              = true);
        bindPress(panel, "load",                 KeyEvent.VK_F9,     () -> queueLoad              = true);

        // SHIFT-masked variant so SHIFT+A/D hold-state is not swallowed
        bindPress(panel, "dash_shift_masked",    KeyEvent.VK_SHIFT,
                  InputEvent.SHIFT_DOWN_MASK,    () -> queueDash     = true);
    }

    // ── Key binding helpers ─────────────────────────────────────────────────────

    private void bindHold(JComponent panel, int keyCode) {
        String held     = "held_"     + keyCode;
        String released = "released_" + keyCode;

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(keyCode, 0, false), held);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(keyCode, 0, true),  released);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(keyCode, InputEvent.SHIFT_DOWN_MASK, false), held);
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(keyCode, InputEvent.SHIFT_DOWN_MASK, true),  released);

        panel.getActionMap().put(held,     action(e -> pressedKeys.add(keyCode)));
        panel.getActionMap().put(released, action(e -> pressedKeys.remove(keyCode)));
    }

    private static void bindPress(JComponent panel, String key, int keyCode, Runnable action) {
        String name = "press_" + key;
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(keyCode, 0, false), name);
        panel.getActionMap().put(name, action(e -> action.run()));
    }

    private static void bindPress(JComponent panel, String key, int keyCode,
                                  int modifiers, Runnable action) {
        String name = "press_" + key + "_" + modifiers;
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(keyCode, modifiers, false), name);
        panel.getActionMap().put(name, action(e -> action.run()));
    }

    private static AbstractAction action(java.util.function.Consumer<ActionEvent> handler) {
        return new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { handler.accept(e); }
        };
    }
}
