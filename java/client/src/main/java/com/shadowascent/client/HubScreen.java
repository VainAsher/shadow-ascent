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
import com.shadowascent.core.NPC;
import com.shadowascent.core.StoryState;
import com.shadowascent.core.data.BeatDefinition;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.simulation.SimEvent;
import com.shadowascent.core.simulation.SimNPC;
import com.shadowascent.core.simulation.SimPlayer;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
        OverlayType activeOverlay = game.overlayManager.activeOverlay();
        boolean pausePressed = game.inputProcessor.consumePausePressed();
        boolean savePressed = game.inputProcessor.consumeSavePressed();
        boolean loadPressed = game.inputProcessor.consumeLoadPressed();
        boolean inventoryToggled = game.inputProcessor.consumeInventoryTogglePressed();
        boolean craftingToggled = game.inputProcessor.consumeCraftingTogglePressed();
        boolean interactPressed = game.inputProcessor.consumeInteractPressed();
        boolean openedPauseThisFrame = false;
        if (pausePressed && activeOverlay == null) {
            game.pauseMenuOverlayRenderer.resetSelection();
            game.overlayManager.open(OverlayType.PAUSE);
            activeOverlay = OverlayType.PAUSE;
            openedPauseThisFrame = true;
        }
        if (inventoryToggled && (activeOverlay == null || activeOverlay == OverlayType.INVENTORY)) {
            game.overlayManager.toggle(OverlayType.INVENTORY);
            activeOverlay = game.overlayManager.activeOverlay();
        }
        if (craftingToggled && (activeOverlay == null || activeOverlay == OverlayType.CRAFTING)) {
            game.overlayManager.toggle(OverlayType.CRAFTING);
            activeOverlay = game.overlayManager.activeOverlay();
        }
        boolean openedShopThisFrame = false;
        boolean openedDialogueThisFrame = false;
        if (interactPressed && hudPlayer != null && !game.overlayManager.hasActiveOverlay()) {
            String npcId = nearbyDialogueNpcId(hudPlayer);
            if (npcId != null) {
                RunGameMissionInteraction.InteractionResult interactionResult =
                        RunGameMissionInteraction.applyNpcInteraction(gameState, npcId);
                for (String line : interactionResult.feedLines()) {
                    appendEventFeedLine(line);
                }
                NPC npc = gameState.getStoryState().getNPC(npcId);
                String speakerName = npc == null ? UiText.humanizeToken(npcId) : npc.getDisplayName();
                List<String> dialogueLines = RunGameMissionInteraction.authoredDialogueLines(
                        gameState, npcId, game.contentProfile == null ? null : game.contentProfile.areaId());
                if (dialogueLines.isEmpty()) {
                    String dialogue = gameState.getHubManager().getNPCDialogue(npcId);
                    dialogueLines = List.of(dialogue == null || dialogue.isBlank() ? "..." : dialogue);
                }
                game.dialogueOverlayRenderer.open(speakerName, dialogueLines);
                game.overlayManager.open(OverlayType.DIALOGUE);
                appendEventFeedLine("Talk: " + speakerName);
                openedDialogueThisFrame = true;
                activeOverlay = OverlayType.DIALOGUE;
            } else if (nearMerchant(hudPlayer)) {
                game.shopOverlayRenderer.open(game.hubShop, hudPlayer.inventory);
                game.overlayManager.open(OverlayType.SHOP);
                appendEventFeedLine("Shop: " + game.hubShop.npcId + " opened.");
                openedShopThisFrame = true;
                activeOverlay = OverlayType.SHOP;
            } else {
                RunGameAreaTransition.TraversalResult traversalResult =
                        RunGameAreaTransition.tryTraverse(gameState, game.contentProfile, hudPlayer, game.simulator);
                if (traversalResult.feedLine() != null) {
                    appendEventFeedLine(traversalResult.feedLine());
                }
                if (traversalResult.transitioned()) {
                    game.refreshGameplaySession();
                    return;
                }
            }
        }
        if (game.inputProcessor.consumeMinimapTogglePressed()) {
            showMinimap = !showMinimap;
        }
        if (savePressed) {
            appendEventFeedLine(game.saveRunGameState());
        }
        if (loadPressed) {
            appendEventFeedLine(game.loadRunGameState());
        }

        List<SimEvent> events = List.of();
        if (activeOverlay != OverlayType.PAUSE) {
            game.inputProcessor.submitFrame();
            game.simulator.tick(dt);
            events = game.simulator.drainEvents();
            game.audioManager.processEvents(events);
        }
        for (SimEvent event : events) {
            appendEventFeedLine(formatEventLine(event));
        }

        if (activeOverlay == OverlayType.PAUSE) {
            if (openedPauseThisFrame) {
                discardModalSignals();
            } else {
                handlePauseNavigation();
                if (game.inputProcessor.consumeCancelPressed()) {
                    game.overlayManager.close();
                } else if (game.inputProcessor.consumeMenuConfirmPressed() && handlePauseMenuSelection()) {
                    game.overlayManager.close();
                }
            }
        } else if (activeOverlay == OverlayType.INVENTORY) {
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
        } else if (activeOverlay == OverlayType.DIALOGUE) {
            if (openedDialogueThisFrame) {
                discardModalSignals();
            } else if (game.inputProcessor.consumeCancelPressed()) {
                game.dialogueOverlayRenderer.close();
                game.overlayManager.close();
            } else if (game.inputProcessor.consumeMenuConfirmPressed()) {
                boolean advanced = game.dialogueOverlayRenderer.advance();
                if (!advanced) {
                    game.dialogueOverlayRenderer.close();
                    game.overlayManager.close();
                }
            } else {
                game.inputProcessor.consumeMenuLeftPressed();
                game.inputProcessor.consumeMenuRightPressed();
                game.inputProcessor.consumeMenuUpPressed();
                game.inputProcessor.consumeMenuDownPressed();
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
                game.contentProfile,
                Gdx.graphics.getWidth() - 220f,
                16f
        );
        if (game.overlayManager.activeOverlay() == OverlayType.PAUSE) {
            game.pauseMenuOverlayRenderer.render(
                    game.batch,
                    game.uiFont,
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight()
            );
        } else if (game.overlayManager.activeOverlay() == OverlayType.INVENTORY) {
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
        } else if (game.overlayManager.activeOverlay() == OverlayType.DIALOGUE) {
            game.dialogueOverlayRenderer.render(
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
        BeatDefinition nextBeat = nextRuntimeBeat(storyState);
        Mission recommendedMission = activeMission == null
                ? recommendedMissionForPlateau(storyState)
                : null;
        String areaId = game.contentProfile == null ? null : game.contentProfile.areaId();

        String missionTitle;
        String objectiveLine;
        if (activeMission != null) {
            missionTitle = activeMission.getDisplayName() + " [" + activeMission.getId() + "]";
            objectiveLine = activeMission.getObjectives().stream()
                    .filter(objective -> !activeMission.isObjectiveComplete(objective))
                    .map(UiText::objectiveToken)
                    .findFirst()
                    .orElse(activeMission.getDescription());
        } else if (recommendedMission != null) {
            missionTitle = "Available: " + recommendedMission.getDisplayName() + " [" + recommendedMission.getId() + "]";
            objectiveLine = recommendedMission.getDescription();
        } else {
            missionTitle = nextBeat == null ? null : nextBeat.title();
            objectiveLine = nextBeat == null ? null : nextBeat.objectivePrompt();
        }

        String hint = resolveContextualHint(activeMission, recommendedMission, nextBeat, areaId, storyState);
        String interactionHint = player == null
                ? "Explore east through traversal rooms."
                : resolveInteractionHint(player);

        return new HudOverlayState(
                UiText.humanizeToken(storyState.getCurrentAct().name()),
                UiText.humanizeToken(storyState.getCurrentPlateau().name()),
                areaId,
                missionTitle,
                objectiveLine,
                player == null ? 0 : player.health,
                player == null ? 1 : player.maxHealth,
                hint,
                UiText.overlayStatus(game.overlayManager.activeOverlay()),
                List.copyOf(recentEventFeed),
                showMinimap,
                interactionHint
        );
    }

    private Mission recommendedMissionForPlateau(StoryState storyState) {
        String currentPlateau = storyState == null ? "" : storyState.getCurrentPlateau().name();
        return gameState.getMissionManager().getAvailableMissions().stream()
                .filter(mission -> currentPlateau.equals(plateauForMission(mission)))
                .findFirst()
                .orElseGet(() -> gameState.getMissionManager().getAvailableMissions().stream().findFirst().orElse(null));
    }

    private BeatDefinition nextRuntimeBeat(StoryState storyState) {
        if (storyState == null) {
            return null;
        }
        return gameState.getDataContracts().beatsForPlateau(storyState.getCurrentPlateau().name()).stream()
                .filter(HubScreen::isRuntimeHudBeat)
                .filter(beat -> beat.requiredFlags().stream().allMatch(storyState::hasFlag))
                .filter(beat -> beat.setFlags().isEmpty() || beat.setFlags().stream().anyMatch(flag -> !storyState.hasFlag(flag)))
                .filter(beat -> beat.areaId() != null && !beat.areaId().isBlank())
                .min(Comparator.comparingInt(BeatDefinition::routeOrder).thenComparing(BeatDefinition::id))
                .orElseGet(() -> gameState.getDataContracts().nextCriticalBeat(storyState).orElse(null));
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

        if ("PLAYER_MELEE_HIT".equals(event == null ? null : event.type())) {
            String comboSuffix = "";
            String comboStep = stringValue(data.get("comboStep"));
            if (!comboStep.isBlank() && !"0".equals(comboStep) && !"1".equals(comboStep)) {
                comboSuffix = " (combo x" + comboStep + ")";
            }
            return "Hit " + UiText.humanizeToken(stringValue(data.get("enemyId")))
                    + " for " + stringValue(data.get("dmg")) + comboSuffix;
        }
        if ("ENEMY_DAMAGED".equals(event == null ? null : event.type()) && "player_melee".equals(stringValue(data.get("source")))) {
            return "Enemy damaged: " + UiText.humanizeToken(entityId);
        }

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

    private void handlePauseNavigation() {
        game.inputProcessor.consumeMenuLeftPressed();
        game.inputProcessor.consumeMenuRightPressed();
        if (game.inputProcessor.consumeMenuUpPressed()) {
            game.pauseMenuOverlayRenderer.moveUp();
        }
        if (game.inputProcessor.consumeMenuDownPressed()) {
            game.pauseMenuOverlayRenderer.moveDown();
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

    private boolean handlePauseMenuSelection() {
        return switch (game.pauseMenuOverlayRenderer.selectedOption()) {
            case "Resume" -> true;
            case "Save" -> {
                appendEventFeedLine(game.saveRunGameState());
                yield true;
            }
            case "Load" -> {
                appendEventFeedLine(game.loadRunGameState());
                yield true;
            }
            case "Quit To Title" -> {
                game.showTitleScreen();
                yield true;
            }
            default -> false;
        };
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

    private boolean nearMerchant(SimPlayer player) {
        return merchantNpc(player) != null;
    }

    private SimNPC merchantNpc(SimPlayer player) {
        String merchantNpcId = game.contentProfile == null ? null : game.contentProfile.merchantNpcId();
        if (merchantNpcId == null || merchantNpcId.isBlank()) {
            return null;
        }

        float playerCenterX = player.physics.x + player.physics.width * 0.5f;
        float playerCenterY = player.physics.y + player.physics.height * 0.5f;
        for (SimNPC simNpc : game.simulator.getNpcs()) {
            if (!merchantNpcId.equals(simNpc.id)) {
                continue;
            }
            float npcCenterX = simNpc.physics.x + simNpc.physics.width * 0.5f;
            float npcCenterY = simNpc.physics.y + simNpc.physics.height * 0.5f;
            float dx = playerCenterX - npcCenterX;
            float dy = playerCenterY - npcCenterY;
            if ((dx * dx + dy * dy) <= INTERACT_RADIUS * INTERACT_RADIUS) {
                return simNpc;
            }
        }
        return null;
    }

    private String resolveInteractionHint(SimPlayer player) {
        String npcId = nearbyDialogueNpcId(player);
        if (npcId != null) {
            NPC npc = gameState.getStoryState().getNPC(npcId);
            String name = npc == null ? UiText.humanizeToken(npcId) : npc.getDisplayName();
            return RunGameMissionInteraction.interactionPrompt(gameState, npcId, name);
        }
        if (nearMerchant(player)) {
            return "[E] Open merchant stock";
        }
        if (game.contentProfile != null) {
            Optional<String> gatePrompt = RunGameAreaTransition.describeNearbyGate(gameState, game.contentProfile, player, game.simulator);
            if (gatePrompt.isPresent()) {
                return gatePrompt.get();
            }
        }
        return "Explore east through traversal rooms.";
    }

    private String resolveContextualHint(
            Mission activeMission,
            Mission recommendedMission,
            BeatDefinition nextBeat,
            String areaId,
            StoryState storyState) {
        String areaName = UiText.areaName(areaId);
        String routeHint = resolveRouteHint(activeMission, recommendedMission, nextBeat, areaId);
        return "Area " + areaName
                + "  |  " + routeHint
                + "  |  Lanterns: " + storyState.getLanternCount()
                + "  |  Abilities: " + storyState.getAbilities().size();
    }

    private String resolveRouteHint(Mission activeMission, Mission recommendedMission, BeatDefinition nextBeat, String areaId) {
        if (activeMission != null) {
            String missionPrompt = missionRoutePrompt(activeMission, areaId);
            if (missionPrompt != null) {
                return missionPrompt;
            }
        }
        if (recommendedMission != null) {
            List<String> missionNpcIds = RunGameMissionInteraction.orderedRelevantNpcIds(gameState);
            if (!missionNpcIds.isEmpty()) {
                return "Speak to " + displayNameForNpc(missionNpcIds.getFirst()) + " to begin " + recommendedMission.getDisplayName();
            }
            return "Start " + recommendedMission.getDisplayName();
        }
        if (nextBeat != null) {
            if (nextBeat.areaId() != null && !nextBeat.areaId().isBlank() && !nextBeat.areaId().equals(areaId)) {
                return "Route to " + UiText.areaName(nextBeat.areaId()) + " for " + nextBeat.title();
            }
            if (!nextBeat.npcIds().isEmpty()) {
                return "Speak to " + displayNameForNpc(nextBeat.npcIds().getFirst()) + " for " + nextBeat.title();
            }
            return UiText.objectiveLine(nextBeat.objectivePrompt());
        }
        return "Hub " + UiText.humanizeToken(gameState.getStoryState().getCurrentHubState().name());
    }

    private String missionRoutePrompt(Mission mission, String areaId) {
        if (mission == null) {
            return null;
        }
        return switch (mission.getId()) {
            case "village_bonds" -> "Speak to " + displayNameForFirstIncompleteVillageNpc(mission);
            case "dojo_practice" -> "Speak to Instructor Tai in " + UiText.areaName(areaId);
            case "veil_request" -> "Speak to Veil Maiden in " + UiText.areaName(areaId);
            case "mistwood_beast" -> "Push east toward the mission route";
            case "hollow_descent" -> "Cross toward Hollow Depths objectives";
            case "lantern_restoration" -> "Collect lantern pieces, then restore them";
            case "monastery_arrival" -> "Climb toward the Ember Monastery gate";
            case "yin_yang_balance" -> "Return to Sophia and follow the skyroad lead";
            default -> null;
        };
    }

    private String displayNameForFirstIncompleteVillageNpc(Mission mission) {
        if (!mission.isObjectiveComplete("talk_to_samson")) {
            return displayNameForNpc("SAMSON");
        }
        if (!mission.isObjectiveComplete("talk_to_sophia")) {
            return displayNameForNpc("SOPHIA");
        }
        if (!mission.isObjectiveComplete("talk_to_marcel")) {
            return displayNameForNpc("MARCEL");
        }
        if (!mission.isObjectiveComplete("talk_to_hazel")) {
            return displayNameForNpc("HAZEL");
        }
        return "the villagers";
    }

    private String displayNameForNpc(String npcId) {
        NPC npc = gameState.getStoryState().getNPC(npcId);
        return npc == null ? UiText.humanizeToken(npcId) : npc.getDisplayName();
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static boolean isRuntimeHudBeat(BeatDefinition beat) {
        if (beat == null) {
            return false;
        }
        String beatType = beat.beatType() == null ? "" : beat.beatType().trim().toLowerCase(Locale.ROOT);
        return beat.isCriticalPathBeat() || switch (beatType) {
            case "adaptable_authored", "authored_support", "recovery", "unlock" -> true;
            default -> false;
        };
    }

    private static String plateauForMission(Mission mission) {
        if (mission == null || mission.getRegion() == null) {
            return "";
        }
        return switch (mission.getRegion().trim().toLowerCase(Locale.ROOT)) {
            case "town", "forest", "summit" -> "LANTERN_HEIGHTS";
            case "caves" -> "HOLLOW_DEPTHS";
            case "mountain", "monastery" -> "EMBER_MONASTERY";
            case "skyroad" -> "WINDING_SKYROAD";
            case "mirror" -> "MIRROR_SUMMIT";
            case "beacon" -> "BEACON_CLIFF";
            default -> "";
        };
    }

    private String nearbyDialogueNpcId(SimPlayer player) {
        float playerCenterX = player.physics.x + player.physics.width * 0.5f;
        float playerCenterY = player.physics.y + player.physics.height * 0.5f;
        String nearestNpcId = null;
        float nearestDistanceSq = Float.MAX_VALUE;

        for (SimNPC simNpc : game.simulator.getNpcs()) {
            if ("MERCHANT_RILU".equals(simNpc.id)) {
                continue;
            }
            NPC storyNpc = gameState.getStoryState().getNPC(simNpc.id);
            if (storyNpc == null || !storyNpc.isActive()) {
                continue;
            }

            float npcCenterX = simNpc.physics.x + simNpc.physics.width * 0.5f;
            float npcCenterY = simNpc.physics.y + simNpc.physics.height * 0.5f;
            float dx = playerCenterX - npcCenterX;
            float dy = playerCenterY - npcCenterY;
            float distanceSq = dx * dx + dy * dy;
            if (distanceSq <= INTERACT_RADIUS * INTERACT_RADIUS && distanceSq < nearestDistanceSq) {
                nearestNpcId = storyNpc.getId();
                nearestDistanceSq = distanceSq;
            }
        }
        return nearestNpcId;
    }

    private SimPlayer firstPlayer() {
        Collection<SimPlayer> players = game.simulator.getPlayers();
        return players.isEmpty() ? null : players.iterator().next();
    }
}
