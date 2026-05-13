package com.shadowascent.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.shadowascent.client.ui.HudOverlayState;
import com.shadowascent.client.ui.OverlayType;
import com.shadowascent.client.ui.ShopOverlayRenderer;
import com.shadowascent.client.ui.UiText;
import com.shadowascent.core.GameState;
import com.shadowascent.core.Mission;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.simulation.SimEvent;
import com.shadowascent.core.simulation.SimPlayer;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * LibGDX game screen — ticks GameSimulator, drains events, renders entities via StubWorldRenderer.
 * Sprite rendering and camera follow wired in Phase P3.
 */
final class HubScreen implements Screen {

    private static final float MAX_DELTA   = 0.05f;
    private static final float CAM_LERP    = 0.1f;
    private static final float VIEWPORT_W  = 1280f;
    private static final float VIEWPORT_H  = 720f;
    private static final float INTERACT_RADIUS = 70f;
    private static final float SHOP_NPC_X = 350f;
    private static final int MAX_FEED_LINES = 4;

    private final ShadowAscentGame game;
    @SuppressWarnings("unused")
    private final GameState gameState;
    private final ArrayDeque<String> recentEventFeed = new ArrayDeque<>();
    private boolean showMinimap = true;

    private OrthographicCamera camera;
    private float worldRight;
    private float worldBottom;

    HubScreen(ShadowAscentGame game, GameState gameState) {
        this.game      = game;
        this.gameState = gameState;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, VIEWPORT_W, VIEWPORT_H);

        // Derive world extents from tile geometry so camera never shows black.
        worldRight  = VIEWPORT_W;
        worldBottom = VIEWPORT_H;
        for (TileRect t : game.worldTiles) {
            worldRight  = Math.max(worldRight,  t.x() + t.w());
            worldBottom = Math.max(worldBottom, t.y() + t.h());
        }
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, MAX_DELTA);

        SimPlayer hudPlayer = firstPlayer();
        boolean inventoryToggled = game.inputProcessor.consumeInventoryTogglePressed();
        boolean craftingToggled = game.inputProcessor.consumeCraftingTogglePressed();
        boolean interactPressed = game.inputProcessor.consumeInteractPressed();
        if (inventoryToggled) {
            game.overlayManager.toggle(OverlayType.INVENTORY);
        }
        if (craftingToggled) {
            game.overlayManager.toggle(OverlayType.CRAFTING);
        }
        boolean openedShopThisFrame = false;
        if (interactPressed && hudPlayer != null && !game.overlayManager.hasActiveOverlay() && nearMerchant(hudPlayer)) {
            game.shopOverlayRenderer.open(game.hubShop, hudPlayer.inventory);
            game.overlayManager.open(OverlayType.SHOP);
            appendEventFeedLine("Shop: " + game.hubShop.npcId + " opened.");
            openedShopThisFrame = true;
        }
        if (game.inputProcessor.consumeMinimapTogglePressed()) {
            showMinimap = !showMinimap;
        }

        game.inputProcessor.submitFrame();
        game.simulator.tick(dt);

        List<SimEvent> events = game.simulator.drainEvents();
        for (SimEvent event : events) {
            appendEventFeedLine(formatEventLine(event));
        }

        OverlayType activeOverlay = game.overlayManager.activeOverlay();
        if (activeOverlay == OverlayType.INVENTORY) {
            if (inventoryToggled) {
                discardModalSignals();
            } else {
                handleInventoryNavigation();
                if (game.inputProcessor.consumeCancelPressed()) {
                    game.overlayManager.close();
                } else if (game.inputProcessor.consumeMenuConfirmPressed()) {
                    appendEventFeedLine(game.inventoryOverlayRenderer.useSelected());
                }
            }
        } else if (activeOverlay == OverlayType.SHOP) {
            if (openedShopThisFrame) {
                discardModalSignals();
            } else {
                handleShopNavigation();
                if (game.inputProcessor.consumeCancelPressed()) {
                    game.overlayManager.close();
                } else if (game.inputProcessor.consumeMenuConfirmPressed()) {
                    appendTradeResult(game.shopOverlayRenderer.performAction());
                }
            }
        } else if (activeOverlay == OverlayType.CRAFTING) {
            if (craftingToggled) {
                discardModalSignals();
            } else {
                handleCraftingNavigation();
                if (game.inputProcessor.consumeCancelPressed()) {
                    game.overlayManager.close();
                } else if (game.inputProcessor.consumeMenuConfirmPressed()) {
                    appendEventFeedLine(game.craftingOverlayRenderer.craftSelected());
                }
            }
        } else {
            discardModalSignals();
        }

        // Lerp camera to first alive player
        Collection<SimPlayer> players = game.simulator.getPlayers();
        if (!players.isEmpty()) {
            SimPlayer first = players.iterator().next();
            if (!first.isDead) {
                float cx = first.physics.x + first.physics.width  * 0.5f;
                float cy = first.physics.y + first.physics.height * 0.5f;
                camera.position.x += (cx - camera.position.x) * CAM_LERP;
                camera.position.y += (cy - camera.position.y) * CAM_LERP;
            }
        }

        // Clamp so the visible area never extends past tile geometry edges.
        float halfW = camera.viewportWidth  * 0.5f;
        float halfH = camera.viewportHeight * 0.5f;
        camera.position.x = Math.max(halfW, Math.min(camera.position.x, Math.max(halfW, worldRight  - halfW)));
        camera.position.y = Math.max(halfH, Math.min(camera.position.y, Math.max(halfH, worldBottom - halfH)));

        camera.update();

        Gdx.gl.glClearColor(0.08f, 0.11f, 0.17f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.spriteRenderer.render(game.simulator, game.worldTiles, camera.combined);
        HudOverlayState hudState = buildHudState(hudPlayer);
        game.hudOverlayRenderer.render(hudState, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.minimapOverlayRenderer.render(
                hudState,
                game.simulator,
                game.worldTiles,
                Gdx.graphics.getWidth() - 220f,
                16f
        );
        if (game.overlayManager.activeOverlay() == OverlayType.INVENTORY) {
            game.inventoryOverlayRenderer.render(
                    game.batch,
                    game.uiFont,
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight()
            );
        } else if (game.overlayManager.activeOverlay() == OverlayType.SHOP) {
            game.shopOverlayRenderer.render(
                    game.batch,
                    game.uiFont,
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight()
            );
        } else if (game.overlayManager.activeOverlay() == OverlayType.CRAFTING) {
            game.craftingOverlayRenderer.render(
                    game.batch,
                    game.uiFont,
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight()
            );
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(true, width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}

    private HudOverlayState buildHudState(SimPlayer player) {
        StoryState storyState = gameState.getStoryState();
        String activeMissionId = storyState.getActiveMissionId();
        Mission activeMission = activeMissionId == null ? null : storyState.getMission(activeMissionId);

        String missionTitle = activeMission == null
                ? null
                : activeMission.getDisplayName() + " [" + activeMission.getId() + "]";
        String objectiveLine = activeMission == null
                ? null
                : activeMission.getObjectives().stream()
                        .filter(objective -> !activeMission.isObjectiveComplete(objective))
                        .findFirst()
                        .orElse(activeMission.getDescription());

        String hint = activeMission == null
                ? "Hub " + UiText.humanizeToken(storyState.getCurrentHubState().name())
                : "Lanterns: " + storyState.getLanternCount()
                        + "  |  Abilities: " + storyState.getAbilities().size();

        return new HudOverlayState(
                UiText.humanizeToken(storyState.getCurrentAct().name()),
                UiText.humanizeToken(storyState.getCurrentPlateau().name()),
                missionTitle,
                objectiveLine,
                player == null ? 0 : player.health,
                player == null ? 1 : player.maxHealth,
                hint,
                UiText.overlayStatus(game.overlayManager.activeOverlay()),
                List.copyOf(recentEventFeed),
                showMinimap
        );
    }

    private void appendEventFeedLine(String line) {
        if (line == null || line.isBlank()) {
            return;
        }
        recentEventFeed.addFirst(line);
        while (recentEventFeed.size() > MAX_FEED_LINES) {
            recentEventFeed.removeLast();
        }
    }

    private static String formatEventLine(SimEvent event) {
        String type = event == null ? "" : UiText.humanizeToken(event.type());
        String entityId = event == null ? "" : event.entityId();
        Map<String, Object> data = event == null ? Map.of() : event.data();

        if (entityId == null || entityId.isBlank()) {
            return data.isEmpty() ? type : type + " " + data;
        }
        return type + ": " + entityId;
    }

    private void handleInventoryNavigation() {
        if (game.inputProcessor.consumeMenuLeftPressed()) {
            game.inventoryOverlayRenderer.moveLeft();
        }
        if (game.inputProcessor.consumeMenuRightPressed()) {
            game.inventoryOverlayRenderer.moveRight();
        }
        if (game.inputProcessor.consumeMenuUpPressed()) {
            game.inventoryOverlayRenderer.moveUp();
        }
        if (game.inputProcessor.consumeMenuDownPressed()) {
            game.inventoryOverlayRenderer.moveDown();
        }
    }

    private void handleShopNavigation() {
        if (game.inputProcessor.consumeMenuLeftPressed() || game.inputProcessor.consumeMenuRightPressed()) {
            game.shopOverlayRenderer.toggleFocus();
        }
        if (game.inputProcessor.consumeMenuUpPressed()) {
            game.shopOverlayRenderer.moveUp();
        }
        if (game.inputProcessor.consumeMenuDownPressed()) {
            game.shopOverlayRenderer.moveDown();
        }
    }

    private void handleCraftingNavigation() {
        game.inputProcessor.consumeMenuLeftPressed();
        game.inputProcessor.consumeMenuRightPressed();
        if (game.inputProcessor.consumeMenuUpPressed()) {
            game.craftingOverlayRenderer.moveUp();
        }
        if (game.inputProcessor.consumeMenuDownPressed()) {
            game.craftingOverlayRenderer.moveDown();
        }
    }

    private void discardModalSignals() {
        game.inputProcessor.consumeMenuLeftPressed();
        game.inputProcessor.consumeMenuRightPressed();
        game.inputProcessor.consumeMenuUpPressed();
        game.inputProcessor.consumeMenuDownPressed();
        game.inputProcessor.consumeCancelPressed();
        game.inputProcessor.consumeMenuConfirmPressed();
    }

    private void appendTradeResult(ShopOverlayRenderer.TradeRequest tradeRequest) {
        if (tradeRequest == null) {
            appendEventFeedLine("Shop: trade unavailable.");
            return;
        }
        appendEventFeedLine(
                "Shop: " + (tradeRequest.isBuy() ? "bought " : "sold ") + UiText.itemName(tradeRequest.itemId())
        );
    }

    private static boolean nearMerchant(SimPlayer player) {
        float centerX = player.physics.x + player.physics.width * 0.5f;
        return Math.abs(centerX - SHOP_NPC_X) <= INTERACT_RADIUS;
    }

    private SimPlayer firstPlayer() {
        Collection<SimPlayer> players = game.simulator.getPlayers();
        return players.isEmpty() ? null : players.iterator().next();
    }
}
