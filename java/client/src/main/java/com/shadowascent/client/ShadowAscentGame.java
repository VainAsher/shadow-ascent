package com.shadowascent.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.shadowascent.client.input.GameInputProcessor;
import com.shadowascent.client.rendering.SpriteWorldRenderer;
import com.shadowascent.client.rendering.StubWorldRenderer;
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

    GameSimulator      simulator;
    StubWorldRenderer  stubRenderer;
    SpriteWorldRenderer spriteRenderer;
    GameInputProcessor inputProcessor;
    List<TileRect>     worldTiles;
    AssetManager       assetManager;
    TextureAtlas       atlas;
    SpriteBatch        batch;

    private GameState gameState;

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

        stubRenderer   = new StubWorldRenderer();
        inputProcessor = new GameInputProcessor(simulator, PLAYER_ID);
        Gdx.input.setInputProcessor(inputProcessor);

        batch        = new SpriteBatch();
        assetManager = new AssetManager();
        assetManager.load("assets/sprites/packed/sprites.atlas", TextureAtlas.class);
        assetManager.finishLoading();
        atlas          = assetManager.get("assets/sprites/packed/sprites.atlas", TextureAtlas.class);
        spriteRenderer = new SpriteWorldRenderer(batch, atlas);
        Gdx.app.log("ShadowAscentGame", "atlas loaded: " + atlas.getRegions().size + " regions");

        setScreen(new HubScreen(this, gameState));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (stubRenderer    != null) stubRenderer.dispose();
        if (batch           != null) batch.dispose();
        if (assetManager    != null) assetManager.dispose();
    }
}
