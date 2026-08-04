package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.enums.GameAsset;
import pvz.skin.PvzSkin;

public class GameModesMenu extends ScreenAdapter {
    Stage stage;
    PvZ2 game;
    Stack stack;
    Table rootTable;
    TextButton adventureBtn;
    TextButton pennyPursuitBtn;
    TextButton arenaBtn;
    TextButton backBtn;
    public GameModesMenu(PvZ2 game){
        this.game=game;
    }

    @Override
    public void show() {
        super.show();
        Viewport viewport=new FitViewport(1920,1080);
        stage=new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        //stack
        stack=new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        //background image
        Texture bgTexture = GameAsset.MAIN_MENU_BG.get(game.getGlobalAssetManager());
        Image bgImage = new Image(bgTexture);
        stack.add(bgImage);

        //root table
        rootTable=new Table();
        rootTable.defaults().space(100);
        stack.add(rootTable);

        //adventure button
        adventureBtn=new TextButton("Adventure",PvzSkin.get(),"brown");
        rootTable.add(adventureBtn).width(150).height(60);

        //penny's pursuit button
        pennyPursuitBtn=new TextButton("Penny's Pursuit",PvzSkin.get(),"brown");
        pennyPursuitBtn.setColor(1,1,1,0.3f);
        rootTable.add(pennyPursuitBtn).width(150).height(60);

        //arena button
        arenaBtn=new TextButton("Arena",PvzSkin.get(),"brown");
        arenaBtn.setColor(1,1,1,0.3f);
        rootTable.add(arenaBtn).width(150).height(60);

        //back button & wrapper
        Table backBtnWrapper=new Table();
        backBtnWrapper.bottom().left();
        backBtnWrapper.defaults().pad(50);
        stack.add(backBtnWrapper);

        backBtn=new TextButton("Back",PvzSkin.get(),"default");
        backBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new MainMenu(game));
            }
        });
        backBtnWrapper.add(backBtn).width(80).height(30);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        // پاک کردن صفحه با رنگ سیاه
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // به‌روزرسانی و رسم Stage
        stage.act(delta);
        stage.draw();
    }
}
