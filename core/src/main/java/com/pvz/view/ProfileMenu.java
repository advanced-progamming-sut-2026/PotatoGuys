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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.models.AppContext;
import com.pvz.models.games.seasons.Season;
import com.pvz.models.user.User;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

public class ProfileMenu extends ScreenAdapter {
    private final PvZ2 game;
    private Stage stage;
    private Skin skin;
    private User currentUser;

    public ProfileMenu(PvZ2 game) {
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
        mainPanel.setSize(600, 800);
        mainPanel.setPosition((1920 - 600) / 2, (1080 - 800) / 2);
        mainPanel.defaults().space(15);
        mainPanel.center();
        stage.addActor(mainPanel);

        Label titleLabel = new Label("Profile", skin, "big");
        titleLabel.setFontScale(2.0f);
        titleLabel.setColor(Color.BLACK);
        mainPanel.add(titleLabel).padBottom(30).row();

        Label.LabelStyle descStyle = new Label.LabelStyle(skin.getFont("AVENIRNEXTLTPRO-DEMICN"), Color.BLACK);

        if (currentUser != null) {
            Label usernameLabel = new Label("Username: " + currentUser.getUsername(), descStyle);
            usernameLabel.setFontScale(1.3f);
            mainPanel.add(usernameLabel).left().row();

            Label nicknameLabel = new Label("Nickname: " + currentUser.getNickName(), descStyle);
            nicknameLabel.setFontScale(1.3f);
            mainPanel.add(nicknameLabel).left().row();

            Label emailLabel = new Label("Email: " + currentUser.getEmail(), descStyle);
            emailLabel.setFontScale(1.3f);
            mainPanel.add(emailLabel).left().row();

            Label gamesLabel = new Label("Games Played: " + currentUser.getProfile().getGamePlayed(), descStyle);
            gamesLabel.setFontScale(1.3f);
            mainPanel.add(gamesLabel).left().row();

            Label coinsLabel = new Label("Coins: " + currentUser.getProfile().getCoins(), descStyle);
            coinsLabel.setFontScale(1.3f);
            mainPanel.add(coinsLabel).left().row();

            Label diamondsLabel = new Label("Diamonds: " + currentUser.getProfile().getDiamonds(), descStyle);
            diamondsLabel.setFontScale(1.3f);
            mainPanel.add(diamondsLabel).left().row();

            int completedLevels = 0;
            for (Season season : currentUser.getProfile().getSeasons()) {
                completedLevels += season.getUnlockedLevelCount();
            }
            Label levelsLabel = new Label("Completed Levels: " + completedLevels, descStyle);
            levelsLabel.setFontScale(1.3f);
            mainPanel.add(levelsLabel).left().row();

            Label miopointLabel = new Label("Highest Miopoint: " + currentUser.getProfile().getMaxMiopoint(), descStyle);
            miopointLabel.setFontScale(1.3f);
            mainPanel.add(miopointLabel).left().row();
        }

        TextButton changeInfoBtn = new TextButton("Change Info", skin, "green");
        changeInfoBtn.getLabel().setFontScale(1.3f);
        changeInfoBtn.getLabel().setColor(Color.BLACK);
        changeInfoBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new ProfileEditMenu(game));
            }
        });
        mainPanel.add(changeInfoBtn).width(250).height(60).padTop(20).row();

        TextButton changePasswordBtn = new TextButton("Change Password", skin, "brown");
        changePasswordBtn.getLabel().setFontScale(1.3f);
        changePasswordBtn.getLabel().setColor(Color.BLACK);
        changePasswordBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new PasswordChangeMenu(game));
            }
        });
        mainPanel.add(changePasswordBtn).width(250).height(60).padTop(10).row();

        TextButton backBtn = new TextButton("Back", skin, "purple");
        backBtn.getLabel().setFontScale(1.3f);
        backBtn.getLabel().setColor(Color.BLACK);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new MainMenu(game));
            }
        });
        mainPanel.add(backBtn).width(250).height(60).padTop(20).row();
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
