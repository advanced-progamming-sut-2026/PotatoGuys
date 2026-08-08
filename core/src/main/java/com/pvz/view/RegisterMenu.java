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
import com.pvz.controller.user.PatternManager;
import com.pvz.enums.SecurityQuestions;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.seasons.Season;
import com.pvz.models.user.Gender;
import com.pvz.models.user.Message;
import com.pvz.models.user.User;
import com.pvz.utils.PasswordUtils;
import com.pvz.utils.SaveManager;
import pvz.skin.PvzSkin;

import java.util.UUID;

public class RegisterMenu extends ScreenAdapter {
    private final PvZ2 game;
    private Stage stage;
    private Skin skin;
    private TextField usernameField;
    private TextField passwordField;
    private TextField confirmPasswordField;
    private TextField nicknameField;
    private TextField emailField;
    private TextField genderField;
    private TextField questionField;
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

        Texture bgTexture = game.getGlobalAssetManager().get("textures/backgrounds/MainMenu.png");
        Image bgImage = new Image(bgTexture);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().space(10);
        table.center();
        stage.addActor(table);

        Label titleLabel = new Label("Register", skin);
        titleLabel.setFontScale(2f);
        table.add(titleLabel).padBottom(90).row();


        usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");
        table.add(usernameField).width(300).height(45).row();

        passwordField = new TextField("", skin);
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        table.add(passwordField).width(300).height(45).row();

        confirmPasswordField = new TextField("", skin);
        confirmPasswordField.setMessageText("Confirm Password");
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');
        table.add(confirmPasswordField).width(300).height(45).row();

        nicknameField = new TextField("", skin);
        nicknameField.setMessageText("Nickname");
        table.add(nicknameField).width(300).height(45).row();

        emailField = new TextField("", skin);
        emailField.setMessageText("Email");
        table.add(emailField).width(300).height(45).row();

        genderField = new TextField("", skin);
        genderField.setMessageText("Gender (Male/Female)");
        table.add(genderField).width(300).height(45).row();

        questionField = new TextField("", skin);
        questionField.setMessageText("Security Question # (1-" + SecurityQuestions.QUESTIONS.size() + ")");
        table.add(questionField).width(300).height(45).row();

        answerField = new TextField("", skin);
        answerField.setMessageText("Security Answer");
        table.add(answerField).width(300).height(45).row();

        confirmAnswerField = new TextField("", skin);
        confirmAnswerField.setMessageText("Confirm Security Answer");
        table.add(confirmAnswerField).width(300).height(45).row();

        statusLabel = new Label("", skin);
        table.add(statusLabel).padTop(10).row();

        TextButton registerBtn = new TextButton("Register", skin, "purple");
        registerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                handleRegister();
            }
        });
        table.add(registerBtn).width(200).height(60).row();

        TextButton loginBtn = new TextButton("Already have an account? Login", skin, "brown");
        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new LoginMenu(game));
            }
        });
        table.add(loginBtn).width(300).height(60);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String passwordConfirm = confirmPasswordField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String email = emailField.getText().trim();
        String genderString = genderField.getText().trim();
        String questionText = questionField.getText().trim();
        String answer = answerField.getText().trim();
        String confirmAnswer = confirmAnswerField.getText().trim();

        String usernameValidation = PatternManager.validateUsername(username);
        if (usernameValidation != null) {
            statusLabel.setText(usernameValidation);
            return;
        }

        String passwordValidation = PatternManager.validatePassword(password, passwordConfirm);
        if (passwordValidation != null) {
            statusLabel.setText(passwordValidation);
            return;
        }

        String nicknameValidation = PatternManager.validateNickname(nickname);
        if (nicknameValidation != null) {
            statusLabel.setText(nicknameValidation);
            return;
        }

        String emailValidation = PatternManager.validateEmail(email);
        if (emailValidation != null) {
            statusLabel.setText(emailValidation);
            return;
        }

        if (!answer.equals(confirmAnswer)) {
            statusLabel.setText("Security answers don't match!");
            return;
        }

        String passwordHash = PasswordUtils.hashPassword(password);
        Gender gender = Gender.getGender(genderString.toLowerCase());
        if (gender == null) {
            statusLabel.setText("Invalid gender. Use Male or Female.");
            return;
        }

        int questionIndex;
        try {
            questionIndex = Integer.parseInt(questionText);
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid question number.");
            return;
        }
        if (questionIndex < 1 || questionIndex > SecurityQuestions.QUESTIONS.size()) {
            statusLabel.setText("Question number must be between 1 and " + SecurityQuestions.QUESTIONS.size());
            return;
        }

        User user = new User(username, passwordHash, nickname, email, gender);
        user.setSecurityQuestion(String.valueOf(questionIndex));
        user.setSecurityAnswer(answer);

        SaveManager saveManager = SaveManager.getInstance();
        String id = UUID.randomUUID().toString();
        user.setId(id);

        grantStarterProgress(user);

        user.getProfile().getNews().getMessages()
            .add(new Message("Welcome   to   Plants   vs   Zombies 2   " + user.getNickName() + " ! "));

        HashMap<String, String> usernames = saveManager.load("users/username.json", HashMap.class);
        if (usernames == null) {
            usernames = new HashMap<>();
        }
        usernames.put(user.getUsername(), user.getId());
        saveManager.save(usernames, "users/username.json");
        saveManager.save(user, "users/" + user.getId() + ".json");

        statusLabel.setText("Registration successful! Please login.");
        Gdx.app.postRunnable(() -> game.setScreen(new LoginMenu(game)));
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
        skin.dispose();
    }
}
