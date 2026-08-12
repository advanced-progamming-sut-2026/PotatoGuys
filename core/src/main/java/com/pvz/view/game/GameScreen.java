package com.pvz.view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;

public class GameScreen extends ScreenAdapter {
    public static final int SCREEN_HEIGHT = 720;
    public static final int SCREEN_WIDTH = 1280;

    GameController controller;

    private final AssetManager assetManager;
    private final SpriteBatch batch;



    public GameScreen(String seasonName, int levelNumber) {
        controller = new GameController(seasonName, levelNumber);

        batch = PvZ2.batch;

        assetManager = new AssetManager();
        assetManager.finishLoading();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        controller.update(delta);
        controller.draw();

        controller.getStage().act(delta);
        controller.getStage().draw();
    }

    @Override
    public void resize(int width, int height) {
        controller.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        controller.getShapeRenderer().dispose();
        assetManager.dispose();
        controller.getStage().dispose();
    }
}
