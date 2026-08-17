package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;
import com.pvz.enums.AudioPaths;
import com.pvz.models.AppContext;
import com.pvz.models.user.User;
import com.pvz.network.NetworkClient;
import com.pvz.utils.SaveManager;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class LoginMenu extends ScreenAdapter {

    private static final float FIELD_WIDTH = 420f;
    private static final float FIELD_HEIGHT = 70f;
    private static final Color LABEL_COLOR = new Color(0xD8E6FFFF);
    private static final Color ERROR_COLOR = new Color(0xFF6666FF);

    private final PvZ2 game;
    private Stage stage;
    private Skin skin;
    private TextField usernameField;
    private TextField passwordField;
    private Label statusLabel;
    private ImageButton stayLoggedInBtn;

    public LoginMenu(PvZ2 game) {
        this.game = game;
    }

    @Override
    public void show() {
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        skin = PvzSkin.get();

        // ── Background ────────────────────────────────────────────────────
        Texture bgTex = new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("textures/backgrounds/img_2.png"));
        Image bgImage = new Image(bgTex);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        // ── Root layout ───────────────────────────────────────────────────
        Table root = new Table();
        root.setFillParent(true);
        root.bottom().padBottom(120f);
        stage.addActor(root);

        // ── Logo ───────────────────────────────────────────────────────────
        Texture logoTex = new Texture(Gdx.files.internal("textures/pvz2_logo_horizontal.png"));
        Image logo = new Image(logoTex);
        logo.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        root.add(logo).width(500f).padTop(20f).padBottom(60f).row();

        // ── Title ─────────────────────────────────────────────────────────
        //Label titleLabel = new Label("Welcome Back", skin, "big");
        //titleLabel.setColor(Color.WHITE);
        //root.add(titleLabel).padBottom(80f).row();

        // ── Form panel ────────────────────────────────────────────────────
        BorderedTable panel = new BorderedTable();
        panel.setColor(Color.valueOf("8B4513"));
        panel.pad(24f, 40f, 24f, 40f);

        Table form = new Table();
        form.defaults().height(FIELD_HEIGHT).spaceBottom(16f);

        // ── Fields ────────────────────────────────────────────────────────
        usernameField = createField("Username");
        form.add(label("Username")).right().padRight(12f).padTop(12);
        form.add(usernameField).width(FIELD_WIDTH).left().row();

        passwordField = createPasswordField("Password");
        form.add(label("Password")).right().padRight(12f).padTop(15);
        form.add(passwordField).width(FIELD_WIDTH).left().row();

        // ── Status ────────────────────────────────────────────────────────
        statusLabel = new Label("", skin, "big");
        statusLabel.setWrap(true);
        statusLabel.setAlignment(Align.center);
        form.add(statusLabel).colspan(2).width(FIELD_WIDTH + 12f).padTop(4f).row();

        // ── Stay Logged In ─────────────────────────────────────────────────
        TextureRegionDrawable onDrawable = new TextureRegionDrawable(new Texture(Gdx.files.internal("textures/ui/checkbox_on.png")));
        TextureRegionDrawable offDrawable = new TextureRegionDrawable(new Texture(Gdx.files.internal("textures/ui/checkbox_off.png")));
        stayLoggedInBtn = new ImageButton(offDrawable);
        stayLoggedInBtn.getImage().setScaling(com.badlogic.gdx.utils.Scaling.fit);
        stayLoggedInBtn.getImageCell().size(40f, 40f);
        stayLoggedInBtn.getStyle().imageChecked = onDrawable;
        stayLoggedInBtn.getStyle().imageCheckedOver = onDrawable;
        Label stayLabel = label("Stay Logged In");
        Table stayRow = new Table();
        stayRow.add(stayLabel).left().padRight(30f);
        stayRow.add(stayLoggedInBtn).size(70f);
        form.add().colspan(1);
        form.add(stayRow).colspan(1).left().padTop(2f).padBottom(2f).row();

        // ── Buttons ───────────────────────────────────────────────────────
        TextButton loginBtn = new TextButton("Login", skin, "purple");
        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                handleLogin();
            }
        });
        form.add(loginBtn).colspan(2).width(320f).height(65f).padTop(-8f).row();

        TextButton forgotBtn = new TextButton("Forgot Password?", skin, "green");
        forgotBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ForgotPasswordMenu(game));
            }
        });
        form.add(forgotBtn).colspan(2).width(320f).height(65f).padTop(14f).row();

        TextButton registerBtn = new TextButton("Create an Account", skin, "brown");
        registerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new RegisterMenu(game));
            }
        });
        form.add(registerBtn).colspan(2).width(320f).height(65f).padTop(14f).row();

        panel.add(form);
        root.add(panel).row();

        // Restore saved username if session exists
        try {
            String saved = SaveManager.getInstance().load("session.json", String.class);
            if (saved != null && !saved.isEmpty()) {
                usernameField.setText(saved);
            }
        } catch (Exception ignored) {}

        AudioManager.getInstance().playMusic(AudioPaths.MAIN_MENU, true,
                AudioManager.getInstance().getUserMusicVolume());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Label label(String text) {
        Label l = new Label(text, skin, "big");
        l.setColor(LABEL_COLOR);
        return l;
    }

    private TextField createField(String placeholder) {
        TextField f = new TextField("", skin);
        f.setMessageText(placeholder);
        useBigFont(f);
        return f;
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

    // ── Login logic ──────────────────────────────────────────────────────────

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            statusLabel.setColor(ERROR_COLOR);
            return;
        }

        statusLabel.setText("Connecting...");
        statusLabel.setColor(new Color(0xCCCCCCFF));
        NetworkClient.getInstance().login(username, password, response -> {
            if (!response.success) {
                statusLabel.setText(response.errorMessage);
                statusLabel.setColor(ERROR_COLOR);
                return;
            }
            User user = NetworkClient.getInstance().parsePayload(response, User.class);
            AppContext.getInstance().setCurrentUser(user);
        if (stayLoggedInBtn.isChecked()) {
                java.util.HashMap<String, String> creds = new java.util.HashMap<>();
                creds.put("username", username);
                creds.put("password", password);
                SaveManager.getInstance().save(creds, "session.json");
            } else {
                SaveManager.getInstance().delete("session.json");
            }

            statusLabel.setText("Welcome " + user.getNickName() + "!");
            statusLabel.setColor(new Color(0x66FF66FF));
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
    }
}
