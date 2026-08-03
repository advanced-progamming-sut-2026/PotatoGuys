package com.pvz.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import pvz.skin.PvzSkin;

public class MainMenu extends ScreenAdapter {
    private final PvZ2 game;
    private Stage stage;
    private Skin skin;

    public MainMenu(PvZ2 game) {
        this.game = game;
    }


    @Override
    public void show() {
        // ایجاد Stage با Viewport مناسب
        Viewport viewport=new FitViewport(1920,1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        skin= PvzSkin.get();

        Texture bgTexture=game.getGlobalAssetManager().get("textures/backgrounds/MainMenu.png");
        Image bgImage=new Image(bgTexture);
        stage.addActor(bgImage);

        // چیدمان با Table (دکمه در مرکز)
        Table table = new Table();
        table.setFillParent(true);
        table.defaults().space(20);
        table.center();
        /*table.add(playButton).width(200).height(60).row();*/

        stage.addActor(table);

        //logo
        Texture logoTexture=game.getGlobalAssetManager().get("textures/pvz2_logo_horizontal.png");
        Image logoImage=new Image(logoTexture);
        table.add(logoImage).row();


        //btn
        TextButton greenButton = new TextButton("Start Game", skin, "green");
        table.add(greenButton).width(200).height(60);
    }

    @Override
    public void render(float delta) {
        // پاک کردن صفحه با رنگ سیاه
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // به‌روزرسانی و رسم Stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
