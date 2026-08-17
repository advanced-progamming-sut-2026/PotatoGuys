package com.pvz.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.util.HashMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;
import com.pvz.enums.AudioPaths;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.network.NetworkClient;
import com.pvz.utils.PasswordUtils;
import com.pvz.utils.SaveManager;
import pvz.skin.PvzSkin;

public class LoginMenu extends ScreenAdapter {
    private final PvZ2 game;
    private Stage stage;
    private Skin skin;
    private TextField usernameField;
    private TextField passwordField;
    private Label statusLabel;

    public LoginMenu(PvZ2 game) {
        this.game = game;
    }

    @Override
    public void show() {
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        skin = PvzSkin.get();

        Texture bgTexture = game.getGlobalAssetManager().get("textures/backgrounds/MainMenu.png");
        Image bgImage = new Image(bgTexture);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().space(20);
        table.center();
        stage.addActor(table);

        Label titleLabel = new Label("Login", skin);
        titleLabel.setFontScale(2f);
        table.add(titleLabel).padBottom(200).row();

        usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");
        table.add(usernameField).width(300).height(50).row();

        passwordField = new TextField("", skin);
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        table.add(passwordField).width(300).height(50).row();

        statusLabel = new Label("", skin);
        table.add(statusLabel).padTop(10).row();

        TextButton loginBtn = new TextButton("Login", skin, "purple");
        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                handleLogin();
            }
        });
        table.add(loginBtn).width(200).height(60).row();

        TextButton forgotPasswordBtn = new TextButton("Forgot Password?", skin, "green");
        forgotPasswordBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ForgotPasswordMenu(game));
            }
        });
        table.add(forgotPasswordBtn).width(200).height(60).row();

        TextButton backBtn = new TextButton("Register", skin, "brown");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new RegisterMenu(game));
            }
        });
        table.add(backBtn).width(200).height(60).row();
        AudioManager.getInstance().playMusic(AudioPaths.MAIN_MENU,true,AudioManager.getInstance().getUserMusicVolume());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        statusLabel.setText("Connecting...");
        NetworkClient.getInstance().login(username, password, response -> {
            if (!response.success) {
                statusLabel.setText(response.errorMessage);
                return;
            }
            User user = NetworkClient.getInstance().parsePayload(response, User.class);
            AppContext.getInstance().setCurrentUser(user);
            SaveManager.getInstance().save(username, "session.json"); // فقط کش محلیِ «آخرین کاربر»

            statusLabel.setText("Welcome " + user.getNickName() + "!");
            game.setScreen(new MainMenu(game));
        });
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
