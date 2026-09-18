package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;
import com.pvz.controller.MainController;
import com.pvz.enums.AudioPaths;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.utils.AvatarImages;
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
        newsModal = new NewsModal();
        controller = new MainController();
    }

    @Override
    public void show() {
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        MenuUiKit.installClickSound(stage);
        skin = PvzSkin.get();

        //background
        MenuUiKit.installRotatingBackground(stage.getRoot(), game.getGlobalAssetManager());

        //stack
        stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        // چیدمان با Table (دکمه در مرکز)
        Table table = new Table();
        table.setFillParent(true);
        table.defaults().space(20);
        table.center();
        stack.addActor(table);

        //logo
        Texture logoTexture = game.getGlobalAssetManager().get("textures/pvz2_logo_horizontal.png");
        Image logoImage = new Image(logoTexture);
        table.add(logoImage).row();

        //play button
        TextButton playBtn = new TextButton("PLAY", skin, "purple");
        table.add(playBtn).width(200).height(60).row();
        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new GameModesMenu(game));
            }
        });

        //travel log button
        TextButton travelLogBtn = new TextButton("Travel Log", skin, "green_small");
        table.add(travelLogBtn).width(200).height(60).row();
        travelLogBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new TravelLogMenu(game));
            }
        });

        //logout button
        TextButton registerBtn = new TextButton("Logout", skin, "brown");
        table.add(registerBtn).width(200).height(60).row();
        registerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                controller.logout();
                game.setScreen(new RegisterMenu(game));
            }
        });

        //username label (top-left corner)
        Table userInfoWrapper = new Table();
        userInfoWrapper.top().left();
        userInfoWrapper.pad(20);
        stack.add(userInfoWrapper);

        User currentUser = AppContext.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUsername() != null) {
            Label userNameLabel = new Label(currentUser.getUsername(),
                skin.get("big_outline", Label.LabelStyle.class));
            userNameLabel.setColor(Color.valueOf("FFD700"));
            userNameLabel.setFontScale(1.8f);
            userInfoWrapper.add(userNameLabel).left();
        }

        //quit button (top-right corner)
        Table topRightWrapper = new Table();
        topRightWrapper.top().right();
        topRightWrapper.pad(20);
        stack.add(topRightWrapper);

        ImageButton quitBtn = new ImageButton(skin, "generic_close_circle");
        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Gdx.app.exit();
            }
        });
        topRightWrapper.add(quitBtn).size(70);

        //profile & settings buttons wrapper (bottom-right corner)
        Table bottomRightWrapper = new Table();
        bottomRightWrapper.right().bottom();
        bottomRightWrapper.pad(20);
        stack.add(bottomRightWrapper);

        //profile button: shows the user's chosen avatar (fallback to zombie head)
        ImageButton profileFace;
        if (currentUser != null && currentUser.getProfilePicture() != null) {
            Texture avatar = AvatarImages.featheredCircle(AvatarImages.getTexture(currentUser.getProfilePicture()));
            profileFace = new ImageButton(new TextureRegionDrawable(avatar));
        } else {
            profileFace = new ImageButton(skin.getDrawable("image_ui_hud_eventbutton_event_icon_luckothezombie_up"));
        }

        profileFace.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ProfileMenu(game));
            }
        });
        bottomRightWrapper.add(profileFace).size(80).padRight(15);

        ImageButton settingsBtn = new ImageButton(skin, "settings");
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new SettingsMenu(game));
            }
        });
        bottomRightWrapper.add(settingsBtn).size(125);

        //news button wrapper (bottom-left corner)
        Table newsBtnWrapper = new Table();
        newsBtnWrapper.defaults().pad(50);
        newsBtnWrapper.left().bottom();
        stack.add(newsBtnWrapper);

        stack.add(newsModal);

        // Stack the button and the badge so the (!) overlays the button's corner
        Stack newsBtnStack = new Stack();

        Texture newsTexture = game.getGlobalAssetManager().get("news_preview/news_selected2.png");
        ImageButton newsBtn = new ImageButton(new Image(newsTexture).getDrawable());
        newsBtnStack.add(newsBtn);

        Label unreadBadge = new Label("(!)", skin);
        unreadBadge.setColor(Color.RED);
        unreadBadge.setFontScale(2f);
        unreadBadge.setVisible(false);

        Container<Label> badgeContainer = new Container<>(unreadBadge);
        badgeContainer.top().right();
        badgeContainer.padTop(-12).padRight(-12);
        newsBtnStack.add(badgeContainer);

        newsBtnWrapper.add(newsBtnStack).size(120);

        newsModal.setUnreadBadge(unreadBadge);

        newsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                newsModal.showNews();
            }
        });
        AudioManager.getInstance().playMusic(AudioPaths.MAIN_MENU,true,AudioManager.getInstance().getUserMusicVolume());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
