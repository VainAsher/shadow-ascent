package com.shadowascent.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.shadowascent.client.ShadowAscentGame;

public final class TitleScreen extends ScreenAdapter {
    private final ShadowAscentGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Matrix4 projection = new Matrix4();

    public TitleScreen(ShadowAscentGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.N)
                || Gdx.input.isKeyJustPressed(Keys.ENTER)
                || Gdx.input.justTouched()) {
            game.startNewGame();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.C) && game.hasContinueSave()) {
            game.continueFromSave();
            return;
        }

        Gdx.gl.glClearColor(0.04f, 0.05f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        projection.setToOrtho2D(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(projection);
        batch.begin();
        font.draw(batch, "Shadow Ascent", 120f, Gdx.graphics.getHeight() - 160f);
        font.draw(batch, "N / Enter / Click: New Game", 120f, Gdx.graphics.getHeight() - 208f);
        font.draw(batch, "C: Continue", 120f, Gdx.graphics.getHeight() - 236f);
        font.draw(
                batch,
                game.hasContinueSave() ? "Continue save found." : "No continue save found yet.",
                120f,
                Gdx.graphics.getHeight() - 280f
        );
        batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
    }
}
