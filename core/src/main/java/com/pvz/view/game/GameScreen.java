package com.pvz.view.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.controller.game.State;
import com.pvz.models.engine.GameEngine;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.modes.capabilities.PlantPlacer;

import pvz.skin.PvzSkin;

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
        controller.getStage().getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        controller.getShapeRenderer().dispose();
        assetManager.dispose();
        controller.getStage().dispose();
    }
}
