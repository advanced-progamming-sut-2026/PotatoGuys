package com.pvz.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.MainController;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class MainMenu extends ScreenAdapter {
    MainController controller;
    private final PvZ2 game;
    private Stack stack;
    private Stage stage;
    private Skin skin;
    private NewsModal newsModal;

    public MainMenu(PvZ2 game) {
        this.game = game;
        newsModal=new NewsModal();
        controller=new MainController();
    }


    @Override
    public void show() {
        // ایجاد Stage با Viewport مناسب
        Viewport viewport=new FitViewport(1920,1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        skin= PvzSkin.get();

        //background
        Texture bgTexture=game.getGlobalAssetManager().get("textures/backgrounds/MainMenu.png");
        Image bgImage=new Image(bgTexture);
        stage.addActor(bgImage);

        //stack
        stack=new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        // چیدمان با Table (دکمه در مرکز)
        Table table = new Table();
        table.setFillParent(true);
        table.defaults().space(20);
        table.center();
        /*table.add(playButton).width(200).height(60).row();*/

        stack.addActor(table);

        //logo
        Texture logoTexture=game.getGlobalAssetManager().get("textures/pvz2_logo_horizontal.png");
        Image logoImage=new Image(logoTexture);
        table.add(logoImage).row();


        //play button
        TextButton playBtn = new TextButton("PLAY", skin, "purple");
        table.add(playBtn).width(200).height(60).row();
        playBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new GameModesMenu(game));
            }
        });

        //logout button
        TextButton registerBtn = new TextButton("Logout", skin, "brown");
        table.add(registerBtn).width(200).height(60).row();
        registerBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                controller.logout();
                game.setScreen(new RegisterMenu(game));
            }
        });


        //profile & settings buttons wrapper (bottom-right corner)
        Table bottomRightWrapper = new Table();
        bottomRightWrapper.right().bottom();
        bottomRightWrapper.pad(20);
        stack.add(bottomRightWrapper);

        //profile button: bordered table (from skin) with just the face icon
        BorderedTable profilePanel = new BorderedTable();
        profilePanel.pad(8);
        profilePanel.setSize(96, 96);

        Image profileFace = new Image(skin.getDrawable("image_ui_mainmenu_mm_playericon"));
        profilePanel.add(profileFace).size(44, 50);

        profilePanel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ProfileMenu(game));
            }
        });
        bottomRightWrapper.add(profilePanel).size(96).padRight(15);

        ImageButton settingsBtn = new ImageButton(skin, "settings");
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new SettingsMenu(game));
            }
        });
        bottomRightWrapper.add(settingsBtn).size(100);

        //news button & news wrapper
        Table newsBtnWrapper = new Table();
        newsBtnWrapper.defaults().pad(50);
        newsBtnWrapper.left().bottom();
        stack.add(newsBtnWrapper);

        stack.add(newsModal);

        // Stack the button and the badge so the (!) overlays the button's corner
        // instead of sitting in its own cell next to it.
        Stack newsBtnStack = new Stack();

        TextButton newsBtn = new TextButton("News", skin, "brown");
        newsBtnStack.add(newsBtn);

        Label unreadBadge = new Label("(!)", skin);
        unreadBadge.setColor(Color.RED);
        unreadBadge.setFontScale(2f);
        unreadBadge.setVisible(false);

        Container<Label> badgeContainer = new Container<>(unreadBadge);
        badgeContainer.top().right();
        badgeContainer.padTop(-12).padRight(-12);
        newsBtnStack.add(badgeContainer);

        newsBtnWrapper.add(newsBtnStack).width(140).height(70);

        newsModal.setUnreadBadge(unreadBadge);

        newsBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                newsModal.showNews();
            }
        });
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
