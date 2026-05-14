package com.shadowascent.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.shadowascent.client.audio.AudioManager;
import com.shadowascent.client.input.GameInputProcessor;
import com.shadowascent.client.rendering.SpriteWorldRenderer;
import com.shadowascent.client.screens.TitleScreen;
import com.shadowascent.client.ui.CraftingOverlayRenderer;
import com.shadowascent.client.ui.DialogueOverlayRenderer;
import com.shadowascent.client.ui.HudOverlayRenderer;
import com.shadowascent.client.ui.InventoryOverlayRenderer;
import com.shadowascent.client.ui.MinimapOverlayRenderer;
import com.shadowascent.client.ui.ModalOverlayManager;
import com.shadowascent.client.ui.PauseMenuOverlayRenderer;
import com.shadowascent.client.ui.ShopOverlayRenderer;
import com.shadowascent.client.world.AuthoringWorldBootstrap;
import com.shadowascent.client.world.RunGameContentProfile;
import com.shadowascent.core.GameState;
import com.shadowascent.core.physics.CollisionWorld;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.simulation.GameSimulator;
import com.shadowascent.core.simulation.SimInventory;
import com.shadowascent.core.simulation.SimShop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class ShadowAscentGame extends Game {

    private static final String PLAYER_ID = "player1";
    private static final String ATLAS_PATH = "assets/sprites/packed/sprites.atlas";
    private static final Path RUN_GAME_SAVE_PATH = Path.of("save", "runGame_slot1.sav");

    GameSimulator simulator;
    SpriteWorldRenderer spriteRenderer;
    GameInputProcessor inputProcessor;
    List<TileRect> worldTiles;
    AssetManager assetManager;
    TextureAtlas atlas;
    SpriteBatch batch;
    BitmapFont uiFont;
    ShapeRenderer uiShapes;
    HudOverlayRenderer hudOverlayRenderer;
    MinimapOverlayRenderer minimapOverlayRenderer;
    PauseMenuOverlayRenderer pauseMenuOverlayRenderer;
    InventoryOverlayRenderer inventoryOverlayRenderer;
    ShopOverlayRenderer shopOverlayRenderer;
    CraftingOverlayRenderer craftingOverlayRenderer;
    DialogueOverlayRenderer dialogueOverlayRenderer;
    ModalOverlayManager overlayManager;
    SaveLoad saveLoad;
    SimShop hubShop;
    TitleScreen titleScreen;
    AudioManager audioManager;
    RunGameContentProfile contentProfile;

    GameState gameState;

    @Override
    public void create() {
        batch = new SpriteBatch();
        uiFont = new BitmapFont();
        uiShapes = new ShapeRenderer();
        assetManager = new AssetManager();
        assetManager.load(ATLAS_PATH, TextureAtlas.class);
        assetManager.finishLoading();
        atlas = assetManager.get(ATLAS_PATH, TextureAtlas.class);
        spriteRenderer = new SpriteWorldRenderer(batch, atlas);
        hudOverlayRenderer = new HudOverlayRenderer(batch, uiFont, uiShapes);
        minimapOverlayRenderer = new MinimapOverlayRenderer(uiShapes);
        pauseMenuOverlayRenderer = new PauseMenuOverlayRenderer();
        audioManager = new AudioManager();
        Gdx.app.log("ShadowAscentGame", "atlas loaded: " + atlas.getRegions().size + " regions");

        showTitleScreen();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (audioManager != null) audioManager.dispose();
        if (uiShapes != null) uiShapes.dispose();
        if (uiFont != null) uiFont.dispose();
        if (batch != null) batch.dispose();
        if (assetManager != null) assetManager.dispose();
    }

    public void startNewGame() {
        createGameplaySession(new GameState());
        setScreen(new HubScreen(this, gameState));
    }

    public void continueFromSave() {
        if (!hasContinueSave()) {
            startNewGame();
            return;
        }

        createGameplaySession(new GameState());
        try {
            saveLoad.loadRunGame(simulator, PLAYER_ID);
        } catch (Exception ex) {
            Gdx.app.error("ShadowAscentGame", "Continue load failed: " + ex.getMessage(), ex);
        }
        setScreen(new HubScreen(this, gameState));
    }

    public void showTitleScreen() {
        if (titleScreen == null) {
            titleScreen = new TitleScreen(this);
        }
        setScreen(titleScreen);
    }

    public void refreshGameplaySession() {
        createGameplaySession(gameState);
        setScreen(new HubScreen(this, gameState));
    }

    public boolean hasContinueSave() {
        return Files.exists(RUN_GAME_SAVE_PATH) && Files.exists(runGameRuntimeSavePath());
    }

    String saveRunGameState() {
        try {
            saveLoad.saveRunGame(simulator, PLAYER_ID);
            return "System: saved to " + saveLoad.savePath().toAbsolutePath();
        } catch (Exception ex) {
            return "Save failed: " + ex.getMessage();
        }
    }

    String loadRunGameState() {
        if (!saveLoad.hasRunGameSave()) {
            return "Load failed: no save at " + saveLoad.savePath().toAbsolutePath();
        }
        try {
            if (!saveLoad.loadRunGame(simulator, PLAYER_ID)) {
                return "Load failed: runtime snapshot unavailable.";
            }
            return "System: loaded from " + saveLoad.savePath().toAbsolutePath();
        } catch (Exception ex) {
            return "Load failed: " + ex.getMessage();
        }
    }

    public void activateTitleMusic() {
        audioManager.activateTitleMusic();
    }

    private void createGameplaySession(GameState sessionState) {
        gameState = sessionState;
        contentProfile = new AuthoringWorldBootstrap().bootstrap(gameState);
        audioManager.activatePlateauMusic(contentProfile.plateauId());
        worldTiles = Collections.unmodifiableList(contentProfile.worldTiles());

        CollisionWorld collisionWorld = new CollisionWorld();
        for (TileRect tile : worldTiles) {
            collisionWorld.addTile(tile);
        }

        simulator = new GameSimulator();
        simulator.setCollisionWorld(collisionWorld);
        simulator.addPlayer(PLAYER_ID, 0, contentProfile.playerSpawnX(), contentProfile.playerSpawnY());
        for (RunGameContentProfile.EnemyPlacement enemyPlacement : contentProfile.enemyPlacements()) {
            simulator.addEnemy(
                    enemyPlacement.enemyId(),
                    enemyPlacement.enemyType(),
                    enemyPlacement.x(),
                    enemyPlacement.y());
        }
        for (RunGameContentProfile.NpcPlacement npcPlacement : contentProfile.npcPlacements()) {
            simulator.addNpc(
                    npcPlacement.npcId(),
                    npcPlacement.role(),
                    npcPlacement.x(),
                    npcPlacement.y(),
                    npcPlacement.patrolMinX(),
                    npcPlacement.patrolMaxX());
        }
        seedPlayerInventory(simulator.getPlayer(PLAYER_ID).inventory);
        hubShop = new SimShop("merchant_npc", 2, 12345L);

        overlayManager = new ModalOverlayManager();
        saveLoad = new SaveLoad(gameState, RUN_GAME_SAVE_PATH);
        inputProcessor = new GameInputProcessor(simulator, PLAYER_ID, overlayManager);
        Gdx.input.setInputProcessor(inputProcessor);
        inventoryOverlayRenderer = new InventoryOverlayRenderer(simulator.getPlayer(PLAYER_ID).inventory);
        shopOverlayRenderer = new ShopOverlayRenderer();
        craftingOverlayRenderer = new CraftingOverlayRenderer(simulator.getPlayer(PLAYER_ID).inventory);
        dialogueOverlayRenderer = new DialogueOverlayRenderer();
    }

    private static Path runGameRuntimeSavePath() {
        return RUN_GAME_SAVE_PATH.resolveSibling(RUN_GAME_SAVE_PATH.getFileName().toString() + ".runtime.properties");
    }

    private static void seedPlayerInventory(SimInventory inventory) {
        inventory.addCurrency(100);
        inventory.addItem("weapon_dagger", 1);
        inventory.addItem("health_potion", 3);
        inventory.addItem("material_iron", 4);
        inventory.addItem("material_cloth", 2);
        inventory.addItem("material_leather", 1);
    }
}
