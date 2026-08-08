package com.pvz.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
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
import com.pvz.models.user.User;
import com.pvz.utils.PasswordUtils;
import com.pvz.utils.SaveManager;
import pvz.skin.PvzSkin;
import com.pvz.view.ui.MenuUiKit;

public class ForgotPasswordMenu extends ScreenAdapter {
    private final PvZ2 game;
    private Stage stage;
    private Skin skin;
    private Table mainTable;
    private User currentUser;

    public ForgotPasswordMenu(PvZ2 game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);
        skin = PvzSkin.get();

        Texture bgTexture = game.getGlobalAssetManager().get("textures/backgrounds/MainMenu.png");
        Image bgImage = new Image(bgTexture);
        stage.addActor(bgImage);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.defaults().space(20);
        mainTable.center();
        stage.addActor(mainTable);

        showUsernameScreen();
    }

    private void showUsernameScreen() {
        mainTable.clear();

        Label titleLabel = new Label("Forgot Password", skin);
        mainTable.add(titleLabel).padBottom(40).row();

        TextField usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");
        mainTable.add(usernameField).width(300).height(50).row();

        Label statusLabel = new Label("", skin);
        mainTable.add(statusLabel).padTop(10).row();

        TextButton nextBtn = new TextButton("Next", skin, "purple");
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                handleUsernameSubmit(usernameField.getText().trim(), statusLabel);
            }
        });
        mainTable.add(nextBtn).width(200).height(60).row();

        ImageButton backBtn = new ImageButton(MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)));
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new LoginMenu(game));
            }
        });
        mainTable.add(backBtn).size(60, 56).left().row();
    }

    private void handleUsernameSubmit(String username, Label statusLabel) {
        if (username.isEmpty()) {
            statusLabel.setText("Please enter your username.");
            return;
        }

        SaveManager saveManager = SaveManager.getInstance();
        HashMap<String, String> usernames = saveManager.load("users/username.json", HashMap.class);
        if (usernames == null) {
            statusLabel.setText("No users found.");
            return;
        }

        String id = usernames.get(username);
        if (id == null) {
            statusLabel.setText("Username not found.");
            return;
        }

        User user = saveManager.load("users/" + id + ".json", User.class);
        if (user == null) {
            statusLabel.setText("User data not found.");
            return;
        }

        currentUser = user;
        statusLabel.setText("Username found. Please verify your email.");
        showEmailScreen(statusLabel);
    }

    private void showEmailScreen(Label statusLabel) {
        mainTable.clear();

        Label verifyLabel = new Label("Verify your email:", skin);
        mainTable.add(verifyLabel).padBottom(20).row();

        TextField emailField = new TextField("", skin);
        emailField.setMessageText("Email");
        mainTable.add(emailField).width(300).height(50).row();

        Label emailStatus = new Label("", skin);
        mainTable.add(emailStatus).padTop(10).row();

        TextButton verifyBtn = new TextButton("Verify", skin, "purple");
        verifyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                String email = emailField.getText().trim();
                if (email.isEmpty()) {
                    emailStatus.setText("Please enter your email.");
                    return;
                }
                if (currentUser.getEmail() != null && currentUser.getEmail().equals(email)) {
                    emailStatus.setText("Email verified!");
                    showResetPasswordScreen();
                } else {
                    emailStatus.setText("username and email doesn't match.");
                }
            }
        });
        mainTable.add(verifyBtn).width(200).height(60).row();

        ImageButton backBtn = new ImageButton(MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)));
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new LoginMenu(game));
            }
        });
        mainTable.add(backBtn).size(60, 56).left().row();
    }

    private void showResetPasswordScreen() {
        mainTable.clear();

        Label resetLabel = new Label("Reset Password", skin);
        mainTable.add(resetLabel).padBottom(20).row();

        TextField newPasswordField = new TextField("", skin);
        newPasswordField.setMessageText("New Password");
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');
        mainTable.add(newPasswordField).width(300).height(50).row();

        TextField confirmPasswordField = new TextField("", skin);
        confirmPasswordField.setMessageText("Confirm New Password");
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        mainTable.add(confirmPasswordField).width(300).height(50).row();

        Label resetStatus = new Label("", skin);
        mainTable.add(resetStatus).padTop(10).row();

        TextButton resetBtn = new TextButton("Reset Password", skin, "purple");
        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                String newPassword = newPasswordField.getText().trim();
                String confirmPassword = confirmPasswordField.getText().trim();

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    resetStatus.setText("Please fill in all fields.");
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    resetStatus.setText("Passwords don't match.");
                    return;
                }

                currentUser.setPasswordHash(PasswordUtils.hashPassword(newPassword));
                SaveManager.getInstance().save(currentUser, "users/" + currentUser.getId() + ".json");

                resetStatus.setText("Password reset successfully! Please login.");
                Gdx.app.postRunnable(() -> game.setScreen(new LoginMenu(game)));
            }
        });
        mainTable.add(resetBtn).width(200).height(60).row();

        ImageButton backBtn = new ImageButton(MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)));
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new LoginMenu(game));
            }
        });
        mainTable.add(backBtn).size(60, 56).left().row();
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
