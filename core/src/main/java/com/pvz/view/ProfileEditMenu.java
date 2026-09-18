package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pvz.PvZ2;
import com.pvz.controller.user.PatternManager;
import com.pvz.enums.commands.RegisterMenuCommand;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.network.NetworkClient;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class ProfileEditMenu extends ScreenAdapter {
    private final PvZ2 game;
    private Stage stage;
    private Skin skin;
    private User currentUser;
    private TextField usernameField;
    private TextField nicknameField;
    private TextField emailField;
    private Label statusLabel;

    private static final float FIELD_WIDTH = 420f;
    private static final float FIELD_HEIGHT = 70f;

    public ProfileEditMenu(PvZ2 game) {
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
        mainPanel.setSize(600, 700);
        mainPanel.setPosition((1920 - 600) / 2, (1080 - 700) / 2);
        mainPanel.defaults().space(18);
        mainPanel.center();
        stage.addActor(mainPanel);

        Label titleLabel = new Label("Edit Profile", skin, "big");
        titleLabel.setFontScale(1.8f);
        titleLabel.setColor(Color.BLACK);
        mainPanel.add(titleLabel).padBottom(30).row();

        usernameField = createField("Username");
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
        }
        mainPanel.add(usernameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).row();

        nicknameField = createField("Nickname");
        if (currentUser != null) {
            nicknameField.setText(currentUser.getNickName());
        }
        mainPanel.add(nicknameField).width(FIELD_WIDTH).height(FIELD_HEIGHT).row();

        emailField = createField("Email");
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
        }
        mainPanel.add(emailField).width(FIELD_WIDTH).height(FIELD_HEIGHT).row();

        statusLabel = new Label("", skin);
        statusLabel.setFontScale(1.2f);
        statusLabel.setColor(Color.BLACK);
        mainPanel.add(statusLabel).padTop(10).row();

        TextButton saveBtn = new TextButton("Save", skin, "green");
        saveBtn.getLabel().setFontScale(1.3f);
        saveBtn.getLabel().setColor(Color.BLACK);
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                handleSave();
            }
        });
        mainPanel.add(saveBtn).width(250).height(60).row();

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

    private void handleSave() {
        String newUsername = usernameField.getText().trim();
        String newNickname = nicknameField.getText().trim();
        String newEmail = emailField.getText().trim();

        if (newUsername.isEmpty() || newNickname.isEmpty() || newEmail.isEmpty()) {
            setError("All fields are required.");
            return;
        }

        if (!newUsername.matches(RegisterMenuCommand.USERNAME.getPattern())) {
            setError("Invalid username format. Use letters, digits and dashes only.");
            return;
        }

        String nicknameValidation = PatternManager.validateNickname(newNickname);
        if (nicknameValidation != null) {
            setError(nicknameValidation);
            return;
        }

        String emailValidation = PatternManager.validateEmail(newEmail);
        if (emailValidation != null) {
            setError(emailValidation);
            return;
        }

        if (currentUser.getUsername().equals(newUsername)
                && currentUser.getNickName().equals(newNickname)
                && currentUser.getEmail().equals(newEmail)) {
            setError("Nothing to change.");
            return;
        }

        statusLabel.setText("Saving...");
        statusLabel.setColor(new Color(0xCCCCCCFF));

        Runnable doUpdate = () -> NetworkClient.getInstance().updateProfile(
                currentUser.getId(), currentUser.getUsername(), newUsername, newNickname, newEmail,
                response -> {
                    if (!response.success) {
                        setError(response.errorMessage);
                        return;
                    }
                    User updated = NetworkClient.getInstance().parsePayload(response, User.class);
                    if (updated != null) {
                        AppContext.getInstance().setCurrentUser(updated);
                        currentUser = updated;
                    }
                    statusLabel.setText("Profile updated successfully!");
                    statusLabel.setColor(Color.GREEN);
                });

        boolean usernameChanged = !newUsername.equals(currentUser.getUsername());
        if (usernameChanged) {
            NetworkClient.getInstance().checkUsername(newUsername, taken -> {
                if (taken) {
                    setError("Username is already taken.");
                    return;
                }
                doUpdate.run();
            });
        } else {
            doUpdate.run();
        }
    }

    private void setError(String message) {
        statusLabel.setText(message);
        statusLabel.setColor(Color.RED);
    }

    private TextField createField(String placeholder) {
        TextField f = new TextField("", skin);
        f.setMessageText(placeholder);
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
