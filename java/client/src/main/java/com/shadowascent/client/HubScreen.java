package com.shadowascent.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.shadowascent.core.GameState;

/**
 * First LibGDX screen — placeholder hub render loop.
 * Drains GameSimulator events each frame; tile/sprite rendering wired in subsequent phases.
 */
final class HubScreen implements Screen {

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
        Gdx.gl.glClearColor(0.08f, 0.11f, 0.17f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
