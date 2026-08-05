package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AdventureController;
import com.pvz.enums.GameAsset;
import pvz.skin.PvzSkin;

public class AdventureMenu extends ScreenAdapter {
    AdventureController controller;
    Stage stage;
    PvZ2 game;
    Stack stack;
    Table rootTable;
    TextButton ancientEgyptBtn;
    TextButton frostbiteCavesBtn;
    TextButton darkAgesBtn;
    TextButton bigWaveBeachBtn;
    TextButton backBtn;
    public AdventureMenu(PvZ2 game){
        this.game=game;
        controller=new AdventureController();
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

        //Ancient Egypt button
        ancientEgyptBtn=new TextButton("Ancient Egypt", PvzSkin.get(),"brown");
        rootTable.add(ancientEgyptBtn).width(150).height(60);
        ancientEgyptBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ChapterMenu(game,"Ancient Egypt"));
            }
        });


        //Frostbite Caves button
        frostbiteCavesBtn=new TextButton("Frostbite Caves",PvzSkin.get(),"brown");
        if (controller.isSeasonLocked("frostbite caves")) frostbiteCavesBtn.setColor(1,1,1,0.3f);
        rootTable.add(frostbiteCavesBtn).width(150).height(60);
        frostbiteCavesBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ChapterMenu(game,"Frostbite Caves"));
            }
        });

        //Dark Ages button
        darkAgesBtn=new TextButton("Dark Ages",PvzSkin.get(),"brown");
        if (controller.isSeasonLocked("dark ages")) darkAgesBtn.setColor(1,1,1,0.3f);
        rootTable.add(darkAgesBtn).width(150).height(60);
        darkAgesBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ChapterMenu(game,"Dark Ages"));
            }
        });

        //Dark Ages button
        bigWaveBeachBtn=new TextButton("Big Wave Beach",PvzSkin.get(),"brown");
        if (controller.isSeasonLocked("big wave beach")) bigWaveBeachBtn.setColor(1,1,1,0.3f);
        rootTable.add(bigWaveBeachBtn).width(150).height(60);
        darkAgesBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ChapterMenu(game,"Big Wave Beach"));
            }
        });

        //back button & wrapper
        Table backBtnWrapper=new Table();
        backBtnWrapper.bottom().left();
        backBtnWrapper.defaults().pad(50);
        stack.add(backBtnWrapper);

        backBtn=new TextButton("Back",PvzSkin.get(),"green_small");
        backBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new GameModesMenu(game));
            }
        });
        backBtnWrapper.add(backBtn).width(100).height(50);
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
