package com.shadowascent.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.shadowascent.client.input.GameInputProcessor;
import com.shadowascent.client.rendering.SpriteWorldRenderer;
import com.shadowascent.client.ui.HudOverlayRenderer;
import com.shadowascent.client.ui.InventoryOverlayRenderer;
import com.shadowascent.client.ui.MinimapOverlayRenderer;
import com.shadowascent.client.ui.ModalOverlayManager;
import com.shadowascent.core.GameState;
import com.shadowascent.core.physics.CollisionWorld;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.simulation.GameSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ShadowAscentGame extends Game {

    private static final String PLAYER_ID    = "player1";
    private static final float  FLOOR_Y      = 360f;
    private static final float  WORLD_WIDTH  = 3500f;
    private static final String ATLAS_PATH   = "assets/sprites/packed/sprites.atlas";

    GameSimulator      simulator;
    SpriteWorldRenderer spriteRenderer;
    GameInputProcessor inputProcessor;
    List<TileRect>     worldTiles;
    AssetManager       assetManager;
    TextureAtlas       atlas;
    SpriteBatch        batch;
    BitmapFont         uiFont;
    ShapeRenderer      uiShapes;
    HudOverlayRenderer hudOverlayRenderer;
    MinimapOverlayRenderer minimapOverlayRenderer;
    InventoryOverlayRenderer inventoryOverlayRenderer;
    ModalOverlayManager overlayManager;

    GameState gameState;

    @Override
    public void create() {
        gameState = new GameState();

        // Stub world geometry — solid floor + one platform
        List<TileRect> tiles = new ArrayList<>();
        tiles.add(new TileRect(0f,    FLOOR_Y,        WORLD_WIDTH, 30f,  false));  // solid floor
        tiles.add(new TileRect(300f,  FLOOR_Y - 120f, 200f,        15f,  true));   // one-way platform
        worldTiles = Collections.unmodifiableList(tiles);

        CollisionWorld collisionWorld = new CollisionWorld();
        for (TileRect t : worldTiles) collisionWorld.addTile(t);

        simulator = new GameSimulator();
        simulator.setCollisionWorld(collisionWorld);
        simulator.addPlayer(PLAYER_ID, 0, 200f, 280f);
        simulator.addEnemy("goblin_1", "goblin", 600f, 280f);

        batch        = new SpriteBatch();
        uiFont       = new BitmapFont();
        uiShapes     = new ShapeRenderer();
        overlayManager = new ModalOverlayManager();
        inputProcessor = new GameInputProcessor(simulator, PLAYER_ID, overlayManager);
        Gdx.input.setInputProcessor(inputProcessor);
        assetManager = new AssetManager();
        assetManager.load(ATLAS_PATH, TextureAtlas.class);
        assetManager.finishLoading();
        atlas          = assetManager.get(ATLAS_PATH, TextureAtlas.class);
        spriteRenderer = new SpriteWorldRenderer(batch, atlas);
        hudOverlayRenderer = new HudOverlayRenderer(batch, uiFont, uiShapes);
        minimapOverlayRenderer = new MinimapOverlayRenderer(uiShapes);
        inventoryOverlayRenderer = new InventoryOverlayRenderer(simulator.getPlayer(PLAYER_ID).inventory);
        Gdx.app.log("ShadowAscentGame", "atlas loaded: " + atlas.getRegions().size + " regions");

        setScreen(new HubScreen(this, gameState));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (uiShapes        != null) uiShapes.dispose();
        if (uiFont          != null) uiFont.dispose();
        if (batch           != null) batch.dispose();
        if (assetManager    != null) assetManager.dispose();
    }
}
