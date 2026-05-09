package com.shadowascent.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.shadowascent.core.GameState;
import com.shadowascent.core.physics.TileRect;
import com.shadowascent.core.simulation.SimEvent;
import com.shadowascent.core.simulation.SimPlayer;

import java.util.Collection;
import java.util.List;

/**
 * LibGDX game screen — ticks GameSimulator, drains events, renders entities via StubWorldRenderer.
 * Sprite rendering and camera follow wired in Phase P3.
 */
final class HubScreen implements Screen {

    private static final float MAX_DELTA   = 0.05f;
    private static final float CAM_LERP    = 0.1f;
    private static final float VIEWPORT_W  = 1280f;
    private static final float VIEWPORT_H  = 720f;

    private final ShadowAscentGame game;
    @SuppressWarnings("unused")
    private final GameState gameState;

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

        game.inputProcessor.submitFrame();
        game.simulator.tick(dt);

        @SuppressWarnings("unused")
        List<SimEvent> events = game.simulator.drainEvents();

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

        game.stubRenderer.render(game.simulator, game.worldTiles, camera.combined);
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
}
