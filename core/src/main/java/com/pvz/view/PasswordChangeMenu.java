package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.user.PatternManager;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.utils.PasswordUtils;
import com.pvz.utils.SaveManager;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class PasswordChangeMenu extends ScreenAdapter {
    private final PvZ2 game;
    private Stage stage;
    private Skin skin;
    private User currentUser;
    private TextField oldPasswordField;
    private TextField newPasswordField;
    private TextField confirmPasswordField;
    private Label statusLabel;

    public PasswordChangeMenu(PvZ2 game) {
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

        currentUser = AppContext.getInstance().getCurrentUser();

        BorderedTable mainPanel = new BorderedTable();
        mainPanel.setSize(600, 650);
        mainPanel.setPosition((1920 - 600) / 2, (1080 - 650) / 2);
        mainPanel.defaults().space(15);
        mainPanel.center();
        stage.addActor(mainPanel);

        Label titleLabel = new Label("Change Password", skin, "big");
        titleLabel.setFontScale(1.8f);
        titleLabel.setColor(Color.BLACK);
        mainPanel.add(titleLabel).padBottom(30).row();

        oldPasswordField = new TextField("", skin);
        oldPasswordField.setMessageText("Current Password");
        oldPasswordField.setPasswordMode(true);
        oldPasswordField.setPasswordCharacter('*');
        mainPanel.add(oldPasswordField).width(350).height(50).row();

        newPasswordField = new TextField("", skin);
        newPasswordField.setMessageText("New Password");
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');
        mainPanel.add(newPasswordField).width(350).height(50).row();

        confirmPasswordField = new TextField("", skin);
        confirmPasswordField.setMessageText("Confirm New Password");
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        mainPanel.add(confirmPasswordField).width(350).height(50).row();

        statusLabel = new Label("", skin);
        statusLabel.setFontScale(1.2f);
        statusLabel.setColor(Color.BLACK);
        mainPanel.add(statusLabel).padTop(10).row();

        TextButton changeBtn = new TextButton("Change Password", skin, "green");
        changeBtn.getLabel().setFontScale(1.3f);
        changeBtn.getLabel().setColor(Color.BLACK);
        changeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                handleChange();
            }
        });
        mainPanel.add(changeBtn).width(250).height(60).row();

        TextButton backBtn = new TextButton("Back", skin, "purple");
        backBtn.getLabel().setFontScale(1.3f);
        backBtn.getLabel().setColor(Color.BLACK);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ProfileMenu(game));
            }
        });
        mainPanel.add(backBtn).width(250).height(60).padTop(10).row();
    }

    private void handleChange() {
        String oldPassword = oldPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            setError("All fields are required.");
            return;
        }

        if (!PasswordUtils.verifyPassword(oldPassword, currentUser.getPasswordHash())) {
            setError("Current password is incorrect.");
            return;
        }

        if (PasswordUtils.verifyPassword(newPassword, currentUser.getPasswordHash())) {
            setError("New password cannot be the same as the current password.");
            return;
        }

        String passwordValidation = PatternManager.validatePassword(newPassword, confirmPassword);
        if (passwordValidation != null) {
            setError(passwordValidation);
            return;
        }

        currentUser.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        SaveManager.getInstance().save(currentUser, "users/" + currentUser.getId() + ".json");

        statusLabel.setText("Password changed successfully!");
        statusLabel.setColor(Color.GREEN);
        oldPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void setError(String message) {
        statusLabel.setText(message);
        statusLabel.setColor(Color.RED);
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
