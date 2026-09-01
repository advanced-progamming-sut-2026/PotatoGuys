package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pvz.PvZ2;
import com.pvz.controller.user.PatternManager;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.utils.PasswordUtils;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class PasswordChangeMenu extends ScreenAdapter {
    private static final float FIELD_WIDTH = 420f;
    private static final float FIELD_HEIGHT = 70f;

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
        MenuUiKit.installClickSound(stage);
        skin = PvzSkin.get();

        MenuUiKit.installRotatingBackground(stage.getRoot(), game.getGlobalAssetManager());

        currentUser = AppContext.getInstance().getCurrentUser();

        BorderedTable mainPanel = new BorderedTable();
        mainPanel.setSize(600, 800);
        mainPanel.setPosition((1920 - 600) / 2, (1080 - 800) / 2);
        mainPanel.defaults().space(18);
        mainPanel.center();
        stage.addActor(mainPanel);

        Label titleLabel = new Label("Change Password", skin, "big");
        titleLabel.setFontScale(1.8f);
        titleLabel.setColor(Color.BLACK);
        mainPanel.add(titleLabel).padBottom(30).row();

        oldPasswordField = createPasswordField("Current Password");
        Table oldWrapper = new Table();
        oldWrapper.add(oldPasswordField).width(FIELD_WIDTH).height(FIELD_HEIGHT).row();
        oldWrapper.add(PasswordToggleHelper.createToggle(oldPasswordField, skin)).left().padLeft(4f).padTop(12f);
        mainPanel.add(oldWrapper).row();

        newPasswordField = createPasswordField("New Password");
        Table newWrapper = new Table();
        newWrapper.add(newPasswordField).width(FIELD_WIDTH).height(FIELD_HEIGHT).row();
        newWrapper.add(PasswordToggleHelper.createToggle(newPasswordField, skin)).left().padLeft(4f).padTop(12f);
        mainPanel.add(newWrapper).row();

        confirmPasswordField = createPasswordField("Confirm New Password");
        Table confirmWrapper = new Table();
        confirmWrapper.add(confirmPasswordField).width(FIELD_WIDTH).height(FIELD_HEIGHT).row();
        confirmWrapper.add(PasswordToggleHelper.createToggle(confirmPasswordField, skin))
            .left().padLeft(4f).padTop(12f);
        mainPanel.add(confirmWrapper).row();

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

        ImageButton backBtn = new ImageButton(
            MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)));
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ProfileMenu(game));
            }
        });
        mainPanel.add(backBtn).size(75, 70).left().padTop(10).row();
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
        currentUser.saveUser();

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

    private TextField createPasswordField(String placeholder) {
        TextField f = new TextField("", skin);
        f.setMessageText(placeholder);
        f.setPasswordMode(true);
        f.setPasswordCharacter('*');
        useBigFont(f);
        return f;
    }

    private void useBigFont(TextField field) {
        Label.LabelStyle bigStyle = skin.get("big", Label.LabelStyle.class);
        TextField.TextFieldStyle old = field.getStyle();
        TextField.TextFieldStyle ts = new TextField.TextFieldStyle(
                bigStyle.font, old.fontColor, old.cursor, old.selection,
                old.background);
        ts.messageFont = bigStyle.font;
        ts.messageFontColor = old.messageFontColor;
        ts.focusedBackground = old.focusedBackground;
        field.setStyle(ts);
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
    }
}
