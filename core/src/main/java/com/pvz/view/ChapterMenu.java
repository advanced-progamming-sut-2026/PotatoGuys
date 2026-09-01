package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;
import com.pvz.controller.ChapterController;
import com.pvz.enums.AudioPaths;
import com.pvz.enums.GameAsset;

import com.pvz.view.game.GameScreen;
import pvz.skin.PvzSkin;

public class ChapterMenu extends ScreenAdapter {
    ChapterController controller;
    String chapterName;
    Stage stage;
    PvZ2 game;
    Stack stack;
    Table rootTable;
    TextButton level1Btn;
    TextButton level2Btn;
    TextButton level3Btn;
    TextButton level4Btn;
    ImageButton backBtn;
    public ChapterMenu(PvZ2 game, String seasonName){
        this.game=game;
        this.chapterName=seasonName;
        controller=new ChapterController(seasonName);
    }

    @Override
    public void show() {
        super.show();
        Viewport viewport=new FitViewport(1920,1080);
        stage=new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        MenuUiKit.installClickSound(stage);

        //stack
        stack=new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        //background image
        MenuUiKit.installRotatingBackground(stack, game.getGlobalAssetManager());

        //root table
        rootTable=new Table();
        rootTable.defaults().space(100);
        stack.add(rootTable);

        //Chapter name label
        Label chapterLabel=new Label(chapterName,PvzSkin.get(),"big");
        rootTable.add(chapterLabel).row();

        //level 1
        level1Btn =new TextButton("1", PvzSkin.get(),"brown");
        rootTable.add(level1Btn).width(150).height(150);
        level1Btn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new GameScreen(chapterName, 1));
            }
        });


        //level 2
        level2Btn =new TextButton("2",PvzSkin.get(),"brown");
        if (!controller.isLevelUnlocked(2)) level2Btn.setColor(1,1,1,0.3f);
        rootTable.add(level2Btn).width(150).height(150);
        level2Btn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (controller.isLevelUnlocked(2)) {
                    game.setScreen(new GameScreen(chapterName, 2));
                }
            }
        });

        //level 3
        level3Btn =new TextButton("3",PvzSkin.get(),"brown");
        if (!controller.isLevelUnlocked(3)) level3Btn.setColor(1,1,1,0.3f);
        rootTable.add(level3Btn).width(150).height(150);
        level3Btn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (controller.isLevelUnlocked(3)) {
                    game.setScreen(new GameScreen(chapterName, 3));
                }
            }
        });

        //level 4
        level4Btn =new TextButton("4",PvzSkin.get(),"brown");
        if (!controller.isLevelUnlocked(4)) level4Btn.setColor(1,1,1,0.3f);
        rootTable.add(level4Btn).width(150).height(150);
        level4Btn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (controller.isLevelUnlocked(4)) {
                    game.setScreen(new GameScreen(chapterName, 4));
                }
            }
        });

        //back button & wrapper
        Table backBtnWrapper=new Table();
        backBtnWrapper.top().left();
        backBtnWrapper.defaults().pad(50);
        stack.add(backBtnWrapper);

        backBtn=new ImageButton(MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)));
        backBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new GameModesMenu(game));
            }
        });
        backBtnWrapper.add(backBtn).size(75, 70);

        ImageButton greenhouseBtn = new ImageButton(MenuUiKit.textureDrawable(MenuUiKit.loadTextureSafe("textures/greenhouse/greenhouse.png")));
        greenhouseBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new GreenHouseMenu(game));
            }
        });
        backBtnWrapper.add(greenhouseBtn).size(75, 70).padLeft(24);

        AudioManager.getInstance().playMusic(AudioPaths.MAIN_MENU,true,AudioManager.getInstance().getUserMusicVolume());
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
