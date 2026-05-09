package com.shadowascent.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.shadowascent.core.GameState;
import com.shadowascent.core.simulation.SimEvent;

import java.util.List;

/**
 * LibGDX game screen — ticks GameSimulator, drains events, renders entities via StubWorldRenderer.
 * Sprite rendering and camera follow wired in Phase P3.
 */
final class HubScreen implements Screen {

    private static final float MAX_DELTA = 0.05f;

    private final ShadowAscentGame game;
    @SuppressWarnings("unused")
    private final GameState gameState;

    HubScreen(ShadowAscentGame game, GameState gameState) {
        this.game      = game;
        this.gameState = gameState;
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, MAX_DELTA);

        game.inputProcessor.submitFrame();
        game.simulator.tick(dt);

        @SuppressWarnings("unused")
        List<SimEvent> events = game.simulator.drainEvents();

        Gdx.gl.glClearColor(0.08f, 0.11f, 0.17f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.stubRenderer.render(game.simulator);
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
