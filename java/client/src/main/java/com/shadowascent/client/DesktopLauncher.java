package com.shadowascent.client;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Shadow Ascent");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new ShadowAscentGame(), config);
    }
}
