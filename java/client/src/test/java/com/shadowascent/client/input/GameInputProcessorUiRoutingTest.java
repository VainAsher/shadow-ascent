package com.shadowascent.client.input;

import com.badlogic.gdx.Input.Keys;
import com.shadowascent.client.ui.ModalOverlayManager;
import com.shadowascent.client.ui.OverlayType;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.InputCommand;
import com.shadowascent.core.simulation.SimPlayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GameInputProcessorUiRoutingTest {

    @Test
    void processorRequiresSharedOverlayManagerAtConstruction() {
        assertThrows(
                NoSuchMethodException.class,
                () -> GameInputProcessor.class.getDeclaredConstructor(GameSimulator.class, String.class)
        );
    }

    @Test
    void keyEventsReportWhetherTheProcessorActuallyHandledTheKey() {
        GameInputProcessor input = new GameInputProcessor(new GameSimulator(), "player1", new ModalOverlayManager());

        assertTrue(input.keyDown(Keys.RIGHT));
        assertTrue(input.keyUp(Keys.RIGHT));
        assertTrue(input.keyDown(Keys.T));
        assertTrue(input.keyUp(Keys.T));
        assertTrue(input.keyDown(Keys.ESCAPE));
        assertTrue(input.keyUp(Keys.ESCAPE));
    }

    @Test
    void submitFrameSuppressesGameplayInputWhileModalOverlayIsActive() {
        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player1", 0, 0f, 0f);
        ModalOverlayManager overlays = new ModalOverlayManager();
        GameInputProcessor input = new GameInputProcessor(simulator, "player1", overlays);
        SimPlayer player = simulator.getPlayer("player1");

        input.keyDown(Keys.RIGHT);
        input.keyDown(Keys.SPACE);
        input.keyDown(Keys.F);
        input.keyDown(Keys.E);

        overlays.open(OverlayType.INVENTORY);
        input.submitFrame();

        InputCommand modalCommand = input.lastSubmittedCommand();
        assertFalse(modalCommand.right);
        assertFalse(modalCommand.jump);
        assertFalse(modalCommand.attack);
        assertFalse(modalCommand.interact);
        assertFalse(player.prevJump);
        assertFalse(player.prevAttack);

        overlays.close();
        input.submitFrame();

        InputCommand gameplayCommand = input.lastSubmittedCommand();
        assertTrue(gameplayCommand.right);
        assertTrue(gameplayCommand.jump);
        assertTrue(gameplayCommand.attack);
        assertTrue(gameplayCommand.interact);
        assertTrue(player.prevJump);
        assertTrue(player.prevAttack);
    }

    @Test
    void modalSubmissionAllowlistSuppressesUnexpectedGameplayFlags() throws ReflectiveOperationException {
        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player1", 0, 0f, 0f);
        ModalOverlayManager overlays = new ModalOverlayManager();
        GameInputProcessor input = new GameInputProcessor(simulator, "player1", overlays);

        input.keyDown(Keys.I);
        setCommandFlag(input, "selectWeapon1", true);

        overlays.open(OverlayType.INVENTORY);
        input.submitFrame();

        InputCommand modalCommand = input.lastSubmittedCommand();
        assertTrue(modalCommand.inventory);
        assertFalse(modalCommand.selectWeapon1);
    }

    @Test
    void submitFrameUsesTheSharedOverlayManagerPassedFromGame() {
        GameSimulator simulator = new GameSimulator();
        simulator.addPlayer("player1", 0, 0f, 0f);
        ModalOverlayManager overlays = new ModalOverlayManager();
        GameInputProcessor input = new GameInputProcessor(simulator, "player1", overlays);

        input.keyDown(Keys.RIGHT);
        overlays.open(OverlayType.INVENTORY);
        input.submitFrame();

        assertFalse(input.lastSubmittedCommand().right);
    }

    @Test
    void inventoryCraftingMinimapAndCancelAreExposedAsOneFrameUiSignals() {
        GameInputProcessor input = new GameInputProcessor(new GameSimulator(), "player1", new ModalOverlayManager());

        assertTrue(input.keyDown(Keys.I));
        assertTrue(input.consumeInventoryTogglePressed());
        assertFalse(input.consumeInventoryTogglePressed());

        assertTrue(input.keyDown(Keys.T));
        assertTrue(input.consumeCraftingTogglePressed());
        assertFalse(input.consumeCraftingTogglePressed());

        assertTrue(input.keyDown(Keys.M));
        assertTrue(input.consumeMinimapTogglePressed());
        assertFalse(input.consumeMinimapTogglePressed());

        assertTrue(input.keyDown(Keys.E));
        assertTrue(input.consumeInteractPressed());
        assertFalse(input.consumeInteractPressed());

        assertTrue(input.keyDown(Keys.ESCAPE));
        assertTrue(input.consumeCancelPressed());
        assertFalse(input.consumeCancelPressed());

        assertTrue(input.keyDown(Keys.ENTER));
        assertTrue(input.consumeMenuConfirmPressed());
        assertFalse(input.consumeMenuConfirmPressed());

        assertTrue(input.keyDown(Keys.LEFT));
        assertTrue(input.consumeMenuLeftPressed());
        assertFalse(input.consumeMenuLeftPressed());

        assertTrue(input.keyDown(Keys.RIGHT));
        assertTrue(input.consumeMenuRightPressed());
        assertFalse(input.consumeMenuRightPressed());

        assertTrue(input.keyDown(Keys.UP));
        assertTrue(input.consumeMenuUpPressed());
        assertFalse(input.consumeMenuUpPressed());

        assertTrue(input.keyDown(Keys.DOWN));
        assertTrue(input.consumeMenuDownPressed());
        assertFalse(input.consumeMenuDownPressed());
    }

    private static void setCommandFlag(GameInputProcessor input, String fieldName, boolean value)
            throws ReflectiveOperationException {
        Field cmdField = GameInputProcessor.class.getDeclaredField("cmd");
        cmdField.setAccessible(true);
        InputCommand cmd = (InputCommand) cmdField.get(input);
        Field flagField = InputCommand.class.getDeclaredField(fieldName);
        flagField.setAccessible(true);
        flagField.setBoolean(cmd, value);
    }
}
