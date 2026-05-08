package com.shadowascent.client;

import com.badlogic.gdx.Game;
import com.shadowascent.core.GameState;

public final class ShadowAscentGame extends Game {

    private GameState gameState;

    @Override
    public void create() {
        gameState = new GameState();
        setScreen(new HubScreen(this, gameState));
    }
}
