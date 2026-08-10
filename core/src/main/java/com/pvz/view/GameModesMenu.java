package com.pvz.view;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;
import com.pvz.enums.AudioPaths;
import com.pvz.enums.GameAsset;
import com.pvz.models.AppContext;
import com.pvz.models.user.Profile;

import pvz.skin.PvzSkin;

/**
 * Mode select screen (Adventure / Penny's Pursuit / Arena), restyled to match the
 * hub look: top bar with a back button + title + live currency, then a smooth
 * flick-scrollable card row below (same carousel behavior as AdventureMenu/Dani's).
 * <p>
 * Optional art (falls back to a tinted card if missing — check the console for a
 * "MenuUiKit" log line telling you exactly which path it looked for and didn't find):
 * textures/ui/mode_adventure.png, textures/ui/mode_pennys_pursuit.png, textures/ui/mode_arena.png
 */
public class GameModesMenu extends ScreenAdapter {
    private static final float CARD_WIDTH = 500f;
    private static final float CARD_HEIGHT = 200f;
    private static final float CARD_PAD = 30f;
    private static final float CAROUSEL_VIEWPORT_WIDTH = 1550f;

    Stage stage;
    PvZ2 game;
    Skin skin;
    MenuUiKit.Carousel carousel;

    public GameModesMenu(PvZ2 game) {
        this.game = game;
    }

    @Override
    public void show() {
        super.show();
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        skin = PvzSkin.get();

        //stack
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        //background image
        Texture bgTexture = GameAsset.MAIN_MENU_BG.get(game.getGlobalAssetManager());
        Image bgImage = new Image(bgTexture);
        stack.add(bgImage);

        //root layout
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stack.add(rootTable);

        rootTable.add(buildTopBar()).fillX().top().padTop(30).padLeft(40).padRight(40).row();
        rootTable.add(buildTitle()).padTop(10).row();
        rootTable.add(buildCenter()).expand().center().row();

        AudioManager.getInstance().playMusic(AudioPaths.MAIN_MENU,true,0.7f);
    }

    private Table buildTitle() {
        Table titleRow = new Table();
        Label title = new Label("Choose a Mode", skin, "big");
        title.setFontScale(1.3f);
        titleRow.add(title);
        return titleRow;
    }

    private Table buildTopBar() {
        Table topBar = new Table();

        Table topLeft = new Table();
        topLeft.add(MenuUiKit.backButton(MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)), () -> game.setScreen(new MainMenu(game))))
            .size(75, 70).padRight(24);
        ImageButton leaderboardBtn = new ImageButton(MenuUiKit.textureDrawable(game.getGlobalAssetManager().get("textures/ui/leaderboard.png")));
        leaderboardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new LeaderboardMenu(game));
            }
        });
        topLeft.add(leaderboardBtn).size(75, 70);

        ImageButton greenhouseBtn = new ImageButton(MenuUiKit.textureDrawable(MenuUiKit.loadTextureSafe("textures/greenhouse/greenhouse.png")));
        greenhouseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                game.setScreen(new GreenHouseMenu(game));
            }
        });
        topLeft.add(greenhouseBtn).size(75, 70).padLeft(24);

        Table topRight = new Table();
        Profile profile = currentProfile();
        int coins = profile != null ? profile.getCoins() : 0;
        int diamonds = profile != null ? profile.getDiamonds() : 0;
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/coin_icon.png",
            new Color(0.95f, 0.78f, 0.15f, 1f), String.valueOf(coins), 220, 70)).padRight(15);
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/diamond_icon.png",
            new Color(0.35f, 0.75f, 0.95f, 1f), String.valueOf(diamonds), 220, 70));

        topBar.add(topLeft).left().expandX();
        topBar.add(topRight).right();
        return topBar;
    }

    private Table buildCenter() {
        Table centerTable = new Table();
        centerTable.add(buildModeCarousel());
        return centerTable;
    }

    private Table buildModeCarousel() {
        List<Supplier<Actor>> cardFactories = Arrays.asList(
            () -> MenuUiKit.bigCard(skin, "textures/ui/mode_adventure.png",
                new Color(0.55f, 0.32f, 0.14f, 0.9f), "Adventure", "Unlocked", false,
                () -> game.setScreen(new AdventureMenu(game)), CARD_WIDTH, CARD_HEIGHT),
            () -> MenuUiKit.bigCard(skin, "textures/ui/mode_pennys_pursuit.png",
                new Color(0.2f, 0.3f, 0.55f, 0.9f), "Penny's Pursuit", "Locked", true, null,
                CARD_WIDTH, CARD_HEIGHT),
            () -> MenuUiKit.bigCard(skin, "textures/ui/mode_arena.png",
                new Color(0.5f, 0.18f, 0.22f, 0.9f), "Arena", "Locked", true, null,
                CARD_WIDTH, CARD_HEIGHT)
        );

        carousel = MenuUiKit.buildCarousel(cardFactories, CARD_WIDTH, CARD_HEIGHT, CARD_PAD, CAROUSEL_VIEWPORT_WIDTH);
        return carousel.viewport;
    }

    private Profile currentProfile() {
        if (AppContext.getInstance().getCurrentUser() == null) return null;
        return AppContext.getInstance().getCurrentUser().getProfile();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (carousel != null) carousel.update();

        stage.act(delta);
        stage.draw();
    }
}
