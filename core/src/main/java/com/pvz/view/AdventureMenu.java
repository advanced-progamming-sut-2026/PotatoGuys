package com.pvz.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AdventureController;
import com.pvz.controller.AudioManager;
import com.pvz.enums.AudioPaths;
import com.pvz.enums.GameAsset;
import com.pvz.models.AppContext;
import com.pvz.models.user.Profile;

import pvz.skin.PvzSkin;

/**
 * "Choose a Chapter" screen — this is the one Dani's screenshot shows directly, so it
 * follows her layout closest: top bar (back + title + currency), then a smooth,
 * flick-scrollable, infinitely-looping row of big chapter cards, same as her carousel.
 * <p>
 * Optional art (falls back to a tinted card if missing — check the console/logcat for a
 * "MenuUiKit" log line telling you exactly which path it looked for and didn't find):
 * textures/chapters/ancient_egypt.png, textures/chapters/frostbite_caves.png,
 * textures/chapters/dark_ages.png, textures/chapters/big_wave_beach.png
 */
public class AdventureMenu extends ScreenAdapter {
    private static final String[] CHAPTER_NAMES = {
        "Ancient Egypt", "Frostbite Caves", "Dark Ages", "Big Wave Beach"
    };
    // keys used by AdventureController.isSeasonLocked(...) / season data files
    private static final String[] CHAPTER_KEYS = {
        "ancient egypt", "frostbite caves", "dark ages", "big wave beach"
    };
    private static final String[] CHAPTER_ART = {
        "textures/chapters/ancient_egypt.png", "textures/chapters/frostbite_caves.png",
        "textures/chapters/dark_ages.png", "textures/chapters/big_wave_beach.png"
    };
    private static final Color[] CHAPTER_TINTS = {
        new Color(0.72f, 0.58f, 0.24f, 0.9f), // sandy
        new Color(0.35f, 0.55f, 0.65f, 0.9f), // icy
        new Color(0.28f, 0.2f, 0.3f, 0.9f),   // dark
        new Color(0.2f, 0.55f, 0.55f, 0.9f)   // aqua
    };
    private static final float CARD_WIDTH = 220f;
    private static final float CARD_HEIGHT = 420f;
    private static final float CARD_PAD = 135f;
    private static final float CAROUSEL_VIEWPORT_WIDTH = 1550f;

    AdventureController controller;
    Stage stage;
    PvZ2 game;
    Skin skin;
    MenuUiKit.Carousel carousel;

    public AdventureMenu(PvZ2 game) {
        this.game = game;
        controller = new AdventureController();
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
        rootTable.add(buildCenter()).expand().center().row();

        AudioManager.getInstance().playMusic(AudioPaths.MAIN_MENU,true,0.7f);
    }

    private Table buildTopBar() {
        Table topBar = new Table();

        Table topLeft = new Table();
        topLeft.add(MenuUiKit.backButton(MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)), () -> game.setScreen(new GameModesMenu(game))))
            .size(75, 70).padRight(24);
        Label title = new Label("Choose a Chapter", skin, "big");
        topLeft.add(title);

        Table topRight = new Table();
        Profile profile = currentProfile();
        int coins = profile != null ? profile.getCoins() : 0;
        int diamonds = profile != null ? profile.getDiamonds() : 0;
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/coin_icon.png",
            new Color(0.95f, 0.78f, 0.15f, 1f), String.valueOf(coins), 180, 58)).padRight(15);
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/diamond_icon.png",
            new Color(0.35f, 0.75f, 0.95f, 1f), String.valueOf(diamonds), 180, 58));

        topBar.add(topLeft).left().expandX();
        topBar.add(topRight).right();
        return topBar;
    }

    private Table buildCenter() {
        Table centerTable = new Table();
        centerTable.add(buildChapterCarousel());
        return centerTable;
    }

    private Table buildChapterCarousel() {
        List<Supplier<Actor>> cardFactories = new ArrayList<>();
        for (int i = 0; i < CHAPTER_NAMES.length; i++) {
            final int index = i;
            cardFactories.add(() -> {
                String chapterName = CHAPTER_NAMES[index];
                // Ancient Egypt is the starting chapter and was never lock-checked before;
                // keep that behavior instead of relying on season data that may not track it.
                boolean locked = index == 0 ? false : controller.isSeasonLocked(CHAPTER_KEYS[index]);
                Runnable openChapter = index == 0
                    ? () -> game.setScreen(new EgyptChapterMenu(game))
                    : () -> game.setScreen(new ChapterMenu(game, chapterName));
                return MenuUiKit.bigCard(skin, CHAPTER_ART[index], CHAPTER_TINTS[index], chapterName,
                    locked ? "Locked" : "Unlocked", locked,
                    openChapter,
                    CARD_WIDTH, CARD_HEIGHT);
            });
        }

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
