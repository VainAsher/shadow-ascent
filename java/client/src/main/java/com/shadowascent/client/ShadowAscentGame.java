package com.shadowascent.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.shadowascent.client.input.GameInputProcessor;
import com.shadowascent.client.rendering.StubWorldRenderer;
import com.shadowascent.core.GameState;
import com.shadowascent.core.simulation.GameSimulator;

public final class ShadowAscentGame extends Game {

    private static final String PLAYER_ID = "player1";

    GameSimulator      simulator;
    StubWorldRenderer  stubRenderer;
    GameInputProcessor inputProcessor;

    private GameState gameState;

    @Override
    public void create() {
        gameState = new GameState();

        simulator = new GameSimulator();
        simulator.addPlayer(PLAYER_ID, 0, 200f, 300f);
        simulator.addEnemy("goblin_1", "goblin", 600f, 300f);

        stubRenderer   = new StubWorldRenderer();
        inputProcessor = new GameInputProcessor(simulator, PLAYER_ID);
        Gdx.input.setInputProcessor(inputProcessor);

        setScreen(new HubScreen(this, gameState));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (stubRenderer != null) stubRenderer.dispose();
    }
}
