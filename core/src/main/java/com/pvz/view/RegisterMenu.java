package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
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
import com.pvz.controller.user.PatternManager;
import com.pvz.enums.AudioPaths;
import com.pvz.enums.SecurityQuestions;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.seasons.Season;
import com.pvz.models.user.Gender;
import com.pvz.models.user.User;
import com.pvz.network.NetworkClient;
import com.pvz.utils.PasswordUtils;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class RegisterMenu extends ScreenAdapter {

    private static final float FIELD_WIDTH = 420f;
    private static final float FIELD_HEIGHT = 70f;
    private static final float PAIR_GAP = 20f;
    private static final Color LABEL_COLOR = new Color(0xD8E6FFFF);
    private static final Color ERROR_COLOR = new Color(0xFF6666FF);
    private static final Color SUCCESS_COLOR = new Color(0x66FF66FF);

    private final PvZ2 game;
    private Stage stage;
    private Skin skin;

    private TextField usernameField;
    private TextField passwordField;
    private TextField confirmPasswordField;
    private TextField nicknameField;
    private TextField emailField;
    private SelectBox<String> genderSelect;
    private SelectBox<String> questionSelect;
    private TextField answerField;
    private TextField confirmAnswerField;
    private Label statusLabel;

    public RegisterMenu(PvZ2 game) {
        this.game = game;
    }

    @Override
    public void show() {
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        skin = PvzSkin.get();

        // ── Background ────────────────────────────────────────────────────
        Drawable bgDrawable = PlantData.regionDrawableOr(
                "IMAGE_TITLEBACKGROUNDS_BACKDROP_I",
                new TextureRegionDrawable(MenuUiKit.solidTexture(Color.valueOf("1A1A2E"))));
        Image bgImage = new Image(bgDrawable);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        // ── Root layout ───────────────────────────────────────────────────
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        // ── Title ─────────────────────────────────────────────────────────
        Label titleLabel = new Label("Create Account", skin, "big");
        titleLabel.setColor(Color.WHITE);
        root.add(titleLabel).padBottom(40f).row();

        // ── Form panel ─────────────────────────────────────────────────────
        BorderedTable panel = new BorderedTable();
        panel.setColor(Color.valueOf("8B4513"));
        panel.pad(20f, 36f, 20f, 36f);

        Table form = new Table();
        form.defaults().height(FIELD_HEIGHT).spaceBottom(10f);

        // ── Account section ───────────────────────────────────────────────
        Label sectionAccount = new Label("Account", skin, "big");
        sectionAccount.setColor(new Color(0xFFD75AFF));
        form.add(sectionAccount).colspan(4).left().padBottom(6f).row();

        usernameField = createField("Username");
        form.add(label("Username")).right().padRight(8f);
        form.add(usernameField).width(FIELD_WIDTH).left().padRight(PAIR_GAP);

        nicknameField = createField("Nickname");
        form.add(label("Nickname")).right().padRight(8f);
        form.add(nicknameField).width(FIELD_WIDTH).left().row();

        passwordField = createPasswordField("Password");
        Table passwordWrapper = new Table();
        passwordWrapper.add(passwordField).width(FIELD_WIDTH).left().row();
        passwordWrapper.add(PasswordToggleHelper.createToggle(passwordField, skin)).left().padLeft(4f).padTop(12f);
        form.add(label("Password")).right().padRight(8f);
        form.add(passwordWrapper).width(FIELD_WIDTH).left().padRight(PAIR_GAP);

        confirmPasswordField = createPasswordField("Confirm Password");
        Table confirmWrapper = new Table();
        confirmWrapper.add(confirmPasswordField).width(FIELD_WIDTH).left().row();
        confirmWrapper.add(PasswordToggleHelper.createToggle(confirmPasswordField, skin)).left().padLeft(4f).padTop(12f);
        form.add(label("Confirm")).right().padRight(8f);
        form.add(confirmWrapper).width(FIELD_WIDTH).left().row();

        // ── Profile section ───────────────────────────────────────────────
        Label sectionProfile = new Label("Profile", skin, "big");
        sectionProfile.setColor(new Color(0xFFD75AFF));
        form.add(sectionProfile).colspan(4).left().padTop(10f).padBottom(6f).row();

        emailField = createField("Email");
        form.add(label("Email")).right().padRight(8f);
        form.add(emailField).width(FIELD_WIDTH).left().padRight(PAIR_GAP);

        genderSelect = createSelectBox("Male", "Female");
        form.add(label("Gender")).right().padRight(8f);
        form.add(genderSelect).width(FIELD_WIDTH).left().row();

        // ── Security section ──────────────────────────────────────────────
        Label sectionSecurity = new Label("Security", skin, "big");
        sectionSecurity.setColor(new Color(0xFFD75AFF));
        form.add(sectionSecurity).colspan(4).left().padTop(10f).padBottom(6f).row();

        String[] questions = new String[SecurityQuestions.QUESTIONS.size()];
        for (int i = 0; i < questions.length; i++) {
            questions[i] = SecurityQuestions.QUESTIONS.get(i);
        }
        questionSelect = createSelectBox(questions);
        form.add(label("Question")).right().padRight(8f);
        form.add(questionSelect).width(FIELD_WIDTH + PAIR_GAP + 200f).colspan(2).left().row();

        answerField = createField("Answer");
        form.add(label("Answer")).right().padRight(8f);
        form.add(answerField).width(FIELD_WIDTH).left().padRight(PAIR_GAP);

        confirmAnswerField = createField("Confirm Answer");
        form.add(label("Confirm")).right().padRight(8f);
        form.add(confirmAnswerField).width(FIELD_WIDTH).left().row();

        // ── Status ────────────────────────────────────────────────────────
        statusLabel = new Label("", skin, "big");
        statusLabel.setWrap(true);
        statusLabel.setAlignment(Align.center);
        form.add(statusLabel).colspan(4).width(FIELD_WIDTH * 2 + PAIR_GAP).padTop(8f).row();

        // ── Buttons ───────────────────────────────────────────────────────
        TextButton registerBtn = new TextButton("Register", skin, "purple");
        registerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                handleRegister();
            }
        });
        form.add(registerBtn).colspan(4).width(260f).height(58f).padTop(6f).row();

        TextButton loginBtn = new TextButton("Already have an account? Login", skin, "brown");
        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new LoginMenu(game));
            }
        });
        form.add(loginBtn).colspan(4).width(320f).height(50f).padTop(2f).row();

        panel.add(form);
        root.add(panel).row();

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

    @SafeVarargs
    private final SelectBox<String> createSelectBox(String... items) {
        SelectBox<String> sb = new SelectBox<>(skin);
        Label.LabelStyle bigStyle = skin.get("big", Label.LabelStyle.class);
        SelectBox.SelectBoxStyle old = sb.getStyle();
        SelectBox.SelectBoxStyle ts = new SelectBox.SelectBoxStyle(
                bigStyle.font, old.fontColor, old.background,
                old.scrollStyle, old.listStyle);
        ts.overFontColor = old.overFontColor;
        sb.setStyle(ts);
        sb.setItems(items);
        return sb;
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

    private void showStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setColor(color);
    }

    // ── Register logic ───────────────────────────────────────────────────────

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String passwordConfirm = confirmPasswordField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String email = emailField.getText().trim();
        int genderIndex = genderSelect.getSelectedIndex();
        int questionIndex = questionSelect.getSelectedIndex();
        String answer = answerField.getText().trim();
        String confirmAnswer = confirmAnswerField.getText().trim();

        String usernameValidation = PatternManager.validateUsername(username);
        if (usernameValidation != null) {
            showStatus(usernameValidation, ERROR_COLOR);
            return;
        }

        String passwordValidation = PatternManager.validatePassword(password, passwordConfirm);
        if (passwordValidation != null) {
            showStatus(passwordValidation, ERROR_COLOR);
            return;
        }

        String nicknameValidation = PatternManager.validateNickname(nickname);
        if (nicknameValidation != null) {
            showStatus(nicknameValidation, ERROR_COLOR);
            return;
        }

        String emailValidation = PatternManager.validateEmail(email);
        if (emailValidation != null) {
            showStatus(emailValidation, ERROR_COLOR);
            return;
        }

        Gender gender = Gender.getGender(genderSelect.getSelected().toLowerCase());
        if (gender == null) {
            showStatus("Please select a valid gender.", ERROR_COLOR);
            return;
        }

        if (!answer.equals(confirmAnswer)) {
            showStatus("Security answers don't match!", ERROR_COLOR);
            return;
        }

        String passwordHash = PasswordUtils.hashPassword(password);
        User user = new User(username, passwordHash, nickname, email, gender);
        user.setSecurityQuestion(String.valueOf(questionIndex + 1));
        user.setSecurityAnswer(answer);
        grantStarterProgress(user);

        showStatus("Connecting...", new Color(0xCCCCCCFF));
        NetworkClient.getInstance().register(user, response -> {
            if (!response.success) {
                showStatus(response.errorMessage, ERROR_COLOR);
                return;
            }
            showStatus("Registration successful! Please login.", SUCCESS_COLOR);
            game.setScreen(new LoginMenu(game));
        });
    }

    /** Unlocks the default starter plants and first chapter for a brand-new account. */
    private void grantStarterProgress(User user) {
        user.getProfile().getCollection().unlockPlant(PlantType.Peashooter);
        user.getProfile().getCollection().unlockPlant(PlantType.Sunflower);
        user.getProfile().getCollection().unlockPlant(PlantType.Cabbagepult);
        user.getProfile().getCollection().unlockPlant(PlantType.BonkChoy);
        user.getProfile().getCollection().unlockPlant(PlantType.Repeater);
        user.getProfile().getCollection().unlockPlant(PlantType.TwinSunflower);
        user.getProfile().getCollection().unlockPlant(PlantType.Jalapeno);
        user.getProfile().getCollection().unlockPlant(PlantType.SnowPea);
        user.getProfile().getCollection().unlockPlant(PlantType.CherryBomb);
        user.getProfile().getCollection().unlockPlant(PlantType.Wallnut);

        Season ancientEgypt = new Season("Ancient Egypt");
        ancientEgypt.unlock();
        ancientEgypt.unlockLevel(1);
        user.getProfile().getSeasons().add(ancientEgypt);
        user.getProfile().getSeasons().add(new Season("Frostbite Caves"));
        user.getProfile().getSeasons().add(new Season("Dark Ages"));
        user.getProfile().getSeasons().add(new Season("Big Wave Beach"));
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
