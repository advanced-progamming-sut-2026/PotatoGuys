package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.pvz.PvZ2;
import com.pvz.controller.ChapterController;
import com.pvz.enums.GameAsset;
import com.pvz.models.AppContext;
import com.pvz.models.user.Profile;
import com.pvz.view.MenuUiKit;
import com.pvz.view.game.GameScreen;

import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

/**
 * Big Wave Beach level-select ("stage map") screen. Ported from
 * BigWaveBeachStagesScreen: 3 islands plus a separate boss node, tap a
 * stage to select it, then hit Play in the bottom bar - same interaction
 * pattern as {@link EgyptChapterMenu}, {@link FrostbiteCavesChapterMenu},
 * and {@link DarkAgesChapterMenu}.
 * <p>
 * Structurally this one is closer to Egypt/Frostbite than Dark Ages: it
 * DOES define a real static boss island texture
 * ({@code images/chapters/beach/boss.png}), so the boss node renders that
 * texture plus the LEVEL_NODE marker, with a separate ambient Zomboss PAM
 * decoration floating between Day 2 and Day 3 - not the "PAM clip IS the
 * node" trick Dark Ages uses.
 * <p>
 * One genuinely new thing here: the house island itself is a PAM
 * animation ({@code ANIM27.PAM}), not a static PNG like the other three
 * chapters - so it's drawn through {@code createAnchoredAnimation} instead
 * of a plain {@code MapDecorationActor}.
 * <p>
 * Real asset paths and PAM engine calls below are copied directly from her
 * working code. Lock/complete state comes from our own
 * {@link ChapterController}/{@code Season}, not her Level model. Level 4
 * (the boss) is appearance-only for now, same as the other three chapters -
 * no real level-4 data exists yet, so it isn't clickable.
 */
public class BigWaveBeachChapterMenu extends ScreenAdapter {

    private static final String CHAPTER_NAME = "Big Wave Beach";

    private static final String CHAPTER_BACKGROUND = "textures/backgrounds/beaches_stages.png";

    private static final String[] STAGE_ISLAND_TEXTURES = {
        "images/chapters/beach/anim12_335x420.png",
        "images/chapters/beach/anim13_397x399.png",
        "images/chapters/beach/anim17_321x255.png"
    };
    private static final String BOSS_STAGE_ISLAND_TEXTURE = "images/chapters/beach/boss.png";

    private static final float NODE_WIDTH = 204f;
    private static final float NODE_HEIGHT = 156f;
    private static final float BOSS_NODE_WIDTH = 470f;
    private static final float BOSS_NODE_HEIGHT = 540f;

    private static final float PATH_WIDTH = 1700f;
    private static final float PATH_HEIGHT = 700f;
    private static final float LAYOUT_SCALE_X = PATH_WIDTH / 1080f;
    private static final float LAYOUT_SCALE_Y = PATH_HEIGHT / 380f;

    // Offset of the house island from the first level node (centerX[0], centerY[0]).
    // Tweak these two to move the house (and everything attached to it: the
    // house splash effect, the greenhouse anchor and the first path segment).
    private static final float HOUSE_ISLAND_OFFSET_X = -170f * LAYOUT_SCALE_X;
    private static final float HOUSE_ISLAND_OFFSET_Y = 70f * LAYOUT_SCALE_Y;

    private static final Color TRAIL_COLOR = new Color(0.35f, 0.65f, 0.80f, 0.85f);

    private static final int PLAYABLE_LEVEL_COUNT = 3;
    private static final int BOSS_LEVEL_NUMBER = 4;
    private static final int PATH_NODE_COUNT = 4;

    private static final class DecorTuning {
        final float nativeW, nativeH, scale, offsetX, offsetY;

        DecorTuning(float nativeW, float nativeH, float scale, float offsetX, float offsetY) {
            this.nativeW = nativeW;
            this.nativeH = nativeH;
            this.scale = scale;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    private static final DecorTuning HOUSE_ISLAND_TUNING = new DecorTuning(20f, 400f, 0.18f, -62f, 175f);
    private static final DecorTuning LEVEL_NODE_TUNING = new DecorTuning(260f, 260f, 0.50f, 40f, 65f);
    private static final DecorTuning BOSS_LEVEL_NODE_TUNING = new DecorTuning(260f, 260f, 0.45f, -7f, -15f);
    private static final DecorTuning DANGER_NODE_TUNING = new DecorTuning(350f, 300f, 0.50f, 70f, -215f);
    private static final DecorTuning ZOMBOSS_TUNING = new DecorTuning(560f, 760f, 0.30f, 37f, 140f);
    private static final DecorTuning WAVE_TUNING = new DecorTuning(90f, 180f, 0.05f, -60f, -60f);
    private static final DecorTuning ROCK_TUNING = new DecorTuning(100f, 100f, 0.32f, 32f, -480f);
    private static final DecorTuning SPLASH_TUNING = new DecorTuning(200f, 150f, 0.25f, 20f, 60f);
    private static final DecorTuning STAR_TUNING = new DecorTuning(25f, 25f, 0.30f, 0f, 0f);
    private static final DecorTuning WATER_DROP_TUNING = new DecorTuning(100f, 100f, 0.01f, 0f, -20f);
    private static final DecorTuning WATERFALL_TUNING = new DecorTuning(200f, 250f, 0.01f, 520f, -85f);
    //private static final DecorTuning LARGE_ROCK_BEACH_TUNING = new DecorTuning(180f, 180f, 0.25f, -40f, -30f);
    //private static final DecorTuning SMALL_ROCK_BEACH_TUNING = new DecorTuning(120f, 120f, 0.20f, -10f, 10f);

    private enum DangerNodeState {
        LOCKED_IDLE("locked_idle"), UNLOCKED_ANIMATION("unlocked_animation"), UNLOCKED_IDLE("unlocked_idle");
        final String pamState;
        DangerNodeState(String pamState) { this.pamState = pamState; }
    }

    private enum LevelNodeState {
        LOCKED_IDLE("locked_idle"), LOCKED_ANIMATION("locked_animation"), UNLOCKED("unlocked"),
        UNLOCKED_ANIMATION("unlocked_animation"), FINISHED("finished");
        final String pamState;
        LevelNodeState(String pamState) { this.pamState = pamState; }
    }

    private enum MapObjectType {
        DECOR_HOUSE_ISLAND("768/FULL/WORLDMAP/BEACH/ANIM27/ANIM27.PAM", true),

        SMALL_ISLAND_1("768/FULL/WORLDMAP/DINO/ANIM16/ANIM16.PAM", true),
        SMALL_ISLAND_2("768/FULL/WORLDMAP/BEACH/ANIM6/ANIM6.PAM", true),
        SMALL_ISLAND_3("images/chapters/beach/island42.png", false),
        SMALL_ISLAND_4("images/chapters/beach/island41.png", false),
        SMALL_ISLAND_5("images/chapters/beach/img_1.png", false),

        BEACH_ISLAND_ANIM_12("images/chapters/beach/anim12_335x420.png", false),
        BEACH_ISLAND_ANIM_13("images/chapters/beach/anim13_397x399.png", false),
        BEACH_ISLAND_ANIM_17("images/chapters/beach/anim17_321x255.png", false),

        ZOMBOSS_NODE("768/FULL/WORLDMAP/BEACH/ANIM15/ANIM15.PAM", true),
        LEVEL_NODE("768/INITIAL/WORLDMAP/LEVEL_NODE/LEVEL_NODE.PAM", true),

        FLOATING_ROCK_ANIM_1("768/FULL/WORLDMAP/BEACH/ANIM19/ANIM19.PAM", true),
        FLOATING_ROCK_ANIM_2("768/FULL/WORLDMAP/BEACH/ANIM20/ANIM20.PAM", true),
        FLOATING_ROCK_ANIM_3("768/FULL/WORLDMAP/BEACH/ANIM18/ANIM18.PAM", true),

        DANGER_NODE_ANIM("768/FULL/WORLDMAP/DANGER_NODE_BEACH/DANGER_NODE_BEACH.PAM", true),

        WAVE_ANIM("768/FULL/WORLDMAP/FUTURE/ANIM4/ANIM4.PAM", true),
        TWINKLING_STAR_ANIM("768/FULL/UI/JOUST/SPINNING_GOLD_STAR/SPINNING_GOLD_STAR.PAM", true),
        SPLASH_EFFECT_ANIM("768/FULL/EFFECTS/WATER_SPLASH/WATER_SPLASH.PAM", true),

        WATER_DROP_ANIM("768/FULL/WORLDMAP/BEACH/ANIM35/ANIM35.PAM", true),
        WATERFALL_ANIM("768/FULL/WORLDMAP/BEACH/ANIM32/ANIM32.PAM", true),
        FLOATING_ROCK_BEACH_LARGE_1("768/FULL/WORLDMAP/BEACH/ANIM16/ANIM16.PAM", true),
        FLOATING_ROCK_BEACH_LARGE_2("768/FULL/WORLDMAP/BEACH/ANIM10/ANIM10.PAM", true),
        SMALL_ROCK_BEACH_1("768/FULL/WORLDMAP/BEACH/ANIM4/ANIM4.PAM", true),
        SMALL_ROCK_BEACH_2("768/FULL/WORLDMAP/BEACH/ANIM5/ANIM5.PAM", true);

        final String path;
        final boolean isPamAnimation;

        MapObjectType(String path, boolean isPamAnimation) {
            this.path = path;
            this.isPamAnimation = isPamAnimation;
        }
    }

    private static final class MapObjectPlacement {
        final MapObjectType type;
        final float x, y, width, height;

        MapObjectPlacement(MapObjectType type, float x, float y, float width, float height) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private enum StageStatus { LOCKED, UNLOCKED, COMPLETED, CURRENT }

    private final PvZ2 game;
    private final ChapterController controller;

    private Stage stage;
    private Skin skin;

    private TextureBank textureBank;
    private PamPlayer pamPlayer;

    private Label selectionLabel;
    private TextButton playButton;
    private int selectedLevel = -1;

    public BigWaveBeachChapterMenu(PvZ2 game) {
        this.game = game;
        this.controller = new ChapterController(CHAPTER_NAME);
    }

    // --- Lifecycle ------------------------------------------------------

    @Override
    public void show() {
        super.show();
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        MenuUiKit.installClickSound(stage);
        skin = PvzSkin.get();

        if (textureBank == null) {
            try {
                FileHandle rootHandle = Gdx.files.internal("assets/pvz-assets");
                textureBank = new TextureBank("768", rootHandle);
                pamPlayer = new PamPlayer(textureBank, rootHandle);
                Gdx.app.log("PAM_INIT", "PAM system initialized for the Big Wave Beach stage map.");
            } catch (Throwable t) {
                Gdx.app.error("PAM_INIT", "Failed to initialize PAM system - falling back to static art.", t);
            }
        }

        Stack rootStack = new Stack();
        rootStack.setFillParent(true);
        stage.addActor(rootStack);

        rootStack.add(new Image(chapterBackground()));

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootStack.add(rootTable);

        rootTable.add(buildTopBar()).fillX().padTop(20).padLeft(30).padRight(30).row();
        rootTable.add(buildPathContainer()).expand().fill().row();
        rootTable.add(buildSelectionBar()).fillX().padLeft(30).padRight(30).padBottom(20);
    }

    @Override
    public void render(float delta) {
        if (textureBank != null) {
            try {
                textureBank.update();
            } catch (Throwable ignored) {
            }
        }
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

    // --- Top bar (same convention as AdventureMenu/MainMenu/other chapter screens) -

    private Table buildTopBar() {
        Table topBar = new Table();

        Table titleWrap = new Table();
        titleWrap.add(MenuUiKit.backButton(
                MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)),
                () -> game.setScreen(new AdventureMenu(game))))
            .size(75, 70).padRight(20);
        titleWrap.add(new Label(CHAPTER_NAME, skin, "big"));

        Profile profile = currentProfile();
        int coins = profile != null ? profile.getCoins() : 0;
        int diamonds = profile != null ? profile.getDiamonds() : 0;

        Table topRight = new Table();
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/coin_icon.png",
            new Color(0.95f, 0.78f, 0.15f, 1f), String.valueOf(coins), 180, 58)).padRight(15);
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/diamond_icon.png",
            new Color(0.35f, 0.75f, 0.95f, 1f), String.valueOf(diamonds), 180, 58));

        topBar.add(titleWrap).left().expandX();
        topBar.add(topRight).right();
        return topBar;
    }

    private Profile currentProfile() {
        if (AppContext.getInstance().getCurrentUser() == null) return null;
        return AppContext.getInstance().getCurrentUser().getProfile();
    }

    private Texture chapterBackground() {
        if (Gdx.files.internal(CHAPTER_BACKGROUND).exists()) {
            return MenuUiKit.loadTextureSafe(CHAPTER_BACKGROUND);
        }
        return GameAsset.MAIN_MENU_BG.get(game.getGlobalAssetManager());
    }

    // --- Path container + bottom selection/Play bar ----------------------

    private Table buildPathContainer() {
        Table wrap = new Table();
        StagePath stagePath = new StagePath();
        stagePath.setSize(PATH_WIDTH, PATH_HEIGHT);

        ScrollPane scrollPane = new ScrollPane(stagePath);
        scrollPane.setScrollingDisabled(false, true);
        scrollPane.setFadeScrollBars(true);
        scrollPane.setOverscroll(false, false);

        wrap.add(scrollPane).expand().fill();
        Gdx.app.postRunnable(() -> scrollPane.setScrollX(scrollPane.getMaxX() / 2f));
        return wrap;
    }

    private Table buildSelectionBar() {
        Table bar = new Table();
        bar.setBackground(MenuUiKit.solidDrawable(new Color(0f, 0f, 0f, 0.6f)));
        bar.pad(16);

        selectionLabel = new Label("", skin);
        selectionLabel.setFontScale(1.1f);
        selectionLabel.setWrap(true);
        selectionLabel.setColor(Color.WHITE);

        playButton = new TextButton("Play", skin, "green_small");
        playButton.getLabel().setFontScale(1.15f);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (selectedLevel > 0) {
                    game.setScreen(new GameScreen(CHAPTER_NAME, selectedLevel));
                }
            }
        });

        bar.add(selectionLabel).width(1300).left().expandX();
        bar.add(playButton).width(200).height(64).padLeft(24);

        refreshSelectionBar();
        return bar;
    }

    private void refreshSelectionBar() {
        if (selectedLevel > 0) {
            boolean boss = selectedLevel == BOSS_LEVEL_NUMBER;
            selectionLabel.setText(CHAPTER_NAME + " - " + (boss ? "Tsunami Zomboss" : "Day " + selectedLevel)
                + "\nAdventure - " + (boss ? "Boss" : "Normal"));
            playButton.setDisabled(false);
            playButton.setTouchable(Touchable.enabled);
            playButton.setColor(Color.WHITE);
        } else {
            selectionLabel.setText("Tap an unlocked stage to select it.");
            playButton.setDisabled(true);
            playButton.setTouchable(Touchable.disabled);
            playButton.setColor(1f, 1f, 1f, 0.5f);
        }
    }

    private void selectLevel(int levelNumber) {
        selectedLevel = levelNumber;
        refreshSelectionBar();
    }

    // --- Status -------------------------------------------------------

    private StageStatus statusOf(int levelNumber) {
        if (levelNumber == selectedLevel) {
            return StageStatus.CURRENT;
        }
        if (levelNumber == BOSS_LEVEL_NUMBER) {
            // Appearance-only for now: no real level-4 data exists yet, so
            // this isn't tied to controller.isLevelUnlocked - it's always
            // shown bright/inviting rather than looking broken or locked.
            return StageStatus.UNLOCKED;
        }

        if (!controller.isLevelUnlocked(levelNumber)) {
            return StageStatus.LOCKED;
        }
        if (levelNumber < PLAYABLE_LEVEL_COUNT && controller.isLevelUnlocked(levelNumber + 1)) {
            return StageStatus.COMPLETED;
        }
        if (levelNumber == PLAYABLE_LEVEL_COUNT && controller.isLevelUnlocked(levelNumber)) {
            return StageStatus.COMPLETED;
        }
        return StageStatus.UNLOCKED;
    }

    private LevelNodeState levelNodeStateOf(int index, StageStatus status) {
        switch (status) {
            case COMPLETED:
                return LevelNodeState.FINISHED;
            case CURRENT:
                return LevelNodeState.UNLOCKED_ANIMATION;
            case UNLOCKED:
                return LevelNodeState.UNLOCKED;
            case LOCKED:
            default:
                boolean nextUp = index > 0 && statusOf(index) != StageStatus.LOCKED;
                return nextUp ? LevelNodeState.LOCKED_ANIMATION : LevelNodeState.LOCKED_IDLE;
        }
    }

    // --- The winding path itself -------------------------------------

    private class StagePath extends Group {

        private final float[] centerX = new float[PATH_NODE_COUNT];
        private final float[] centerY = new float[PATH_NODE_COUNT];
        private float houseX, houseY;
        private float houseAnchorX, houseAnchorY;
        private float zombossNodeX, zombossNodeY;
        private float dangerNodeAnchorX, dangerNodeAnchorY;
        private float bridgeX, bridgeY;

        private float zombossRenderHeight() {
            return ZOMBOSS_TUNING.nativeH * ZOMBOSS_TUNING.scale;
        }

        StagePath() {
            setSize(PATH_WIDTH, PATH_HEIGHT);

            for (int i = 0; i < PATH_NODE_COUNT; i++) {
                float progress = PATH_NODE_COUNT <= 1 ? 0.5f : (float) i / (PATH_NODE_COUNT - 1);
                centerX[i] = PATH_WIDTH * (0.12f + 0.76f * progress);
                centerY[i] = PATH_HEIGHT * (0.52f + 0.26f * (float) Math.sin(progress * Math.PI * 1.3f));
            }
            centerX[0] += 60f * LAYOUT_SCALE_X;

            houseX = centerX[0] + HOUSE_ISLAND_OFFSET_X;
            houseY = centerY[0] + HOUSE_ISLAND_OFFSET_Y;
            houseAnchorX = houseX + 75f * LAYOUT_SCALE_X;
            houseAnchorY = houseY + 20f * LAYOUT_SCALE_Y;

            zombossNodeX = (centerX[1] + centerX[2]) / 2f;
            zombossNodeY = Math.max(centerY[1], centerY[2]) - 200f * LAYOUT_SCALE_Y;

            // The trail forks here: Day 2 -> bridge -> Day 3, instead of one
            // straight segment - matches Egypt/Frostbite/Dark Ages.
            bridgeX = zombossNodeX;
            bridgeY = zombossNodeY + zombossRenderHeight() / 2f;

            dangerNodeAnchorX = centerX[1] + DANGER_NODE_TUNING.offsetX * LAYOUT_SCALE_X;
            dangerNodeAnchorY = centerY[1] + 120f * LAYOUT_SCALE_Y;

            addBackgroundDecorations();
            addActor(new TrailActor());
            addMapDecorations();

            for (int i = 0; i < PATH_NODE_COUNT; i++) {
                buildNode(i, i + 1);
            }

            addForegroundEffects();
        }

        private Group createScaledAnimation(MapObjectType type, float nativeWidth, float nativeHeight,
                                            String state, float scale, float x, float y) {
            Group group = new Group();
            group.setTransform(true);
            group.setScale(scale);
            group.setSize(nativeWidth, nativeHeight);
            group.setPosition(x, y);

            MapDecorationActor actor = new MapDecorationActor(type, nativeWidth, nativeHeight, state);
            actor.setSize(nativeWidth, nativeHeight);
            group.addActor(actor);
            return group;
        }

        private Group createAnchoredAnimation(MapObjectType type, DecorTuning tuning, String state,
                                              float anchorX, float anchorY) {
            float renderW = tuning.nativeW * tuning.scale;
            float renderH = tuning.nativeH * tuning.scale;
            float x = anchorX - renderW / 2f + tuning.offsetX * LAYOUT_SCALE_X;
            float y = anchorY - renderH / 2f + tuning.offsetY * LAYOUT_SCALE_Y;
            return createScaledAnimation(type, tuning.nativeW, tuning.nativeH, state, tuning.scale, x, y);
        }

        private void addBackgroundDecorations() {
            addActor(createAnchoredAnimation(MapObjectType.WATERFALL_ANIM, WATERFALL_TUNING, "idle",
                centerX[0] - 40f * LAYOUT_SCALE_X, centerY[0] - 50f * LAYOUT_SCALE_Y));

            float[][] largeRocks1 = { {280f, 320f}, {670f, 110f}, {930f, 280f} };

            float[][] largeRocks2 = { {190f, 90f}, {510f, 330f}, {820f, 130f} };

            float[][] starCoords = {
                {110f, 45f}, {320f, 330f}, {540f, 50f},
                {210f, 270f}, {460f, 190f}, {650f, 35f},
                {380f, 85f}, {590f, 320f}, {830f, 175f}
            };
            for (float[] coord : starCoords) {
                addActor(createAnchoredAnimation(MapObjectType.TWINKLING_STAR_ANIM, STAR_TUNING, "idle",
                    coord[0] * LAYOUT_SCALE_X, coord[1] * LAYOUT_SCALE_Y));
            }
        }

        private void addMapDecorations() {
            MapObjectPlacement[] placements = {
                //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_1, 20, 350, 50, 38),
                //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_2, 310, 15, 55, 40),
                //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_3, 620, 280, 60, 45),
                //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_4, 880, 25, 50, 35),
                //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_5, 970, 240, 55, 40),
                //new MapObjectPlacement(MapObjectType.BEACH_ISLAND_ANIM_12, 130, 120,
                    //335f * BEACH_ISLAND_ANIM_12_SCALE, 420f * BEACH_ISLAND_ANIM_12_SCALE),
                //new MapObjectPlacement(MapObjectType.BEACH_ISLAND_ANIM_13, 470, 70,
                    //397f * BEACH_ISLAND_ANIM_13_SCALE, 399f * BEACH_ISLAND_ANIM_13_SCALE),
                //new MapObjectPlacement(MapObjectType.BEACH_ISLAND_ANIM_17, 750, 300,
                    //321f * BEACH_ISLAND_ANIM_17_SCALE, 255f * BEACH_ISLAND_ANIM_17_SCALE)
            };
            for (MapObjectPlacement p : placements) {
                MapDecorationActor actor = new MapDecorationActor(p.type, p.width, p.height, "idle");
                actor.setPosition(p.x * LAYOUT_SCALE_X, p.y * LAYOUT_SCALE_Y);
                addActor(actor);
            }

            //addActor(createAnchoredAnimation(MapObjectType.SMALL_ROCK_BEACH_1, SMALL_ROCK_BEACH_TUNING, "idle",
                //centerX[2] + 80f * LAYOUT_SCALE_X, centerY[2] - 35f * LAYOUT_SCALE_Y));
            //addActor(createAnchoredAnimation(MapObjectType.SMALL_ROCK_BEACH_2, SMALL_ROCK_BEACH_TUNING, "idle",
                //centerX[2] - 65f * LAYOUT_SCALE_X, centerY[2] + 45f * LAYOUT_SCALE_Y));

            float[][] waterDropCoords = {
                {80f, 120f}, {160f, 340f}, {240f, 80f},
                {380f, 310f}, {440f, 130f}, {510f, 260f},
                {640f, 330f}, {710f, 180f}, {780f, 60f},
                {900f, 140f}
            };
            for (float[] coord : waterDropCoords) {
                addActor(createAnchoredAnimation(MapObjectType.WATER_DROP_ANIM, WATER_DROP_TUNING, "idle",
                    coord[0] * LAYOUT_SCALE_X, coord[1] * LAYOUT_SCALE_Y));
            }

            // The house island is itself a PAM clip here (unlike the other
            // three chapters, which use a static PNG) - purely decorative,
            // not clickable (no Greenhouse hookup here).
            addActor(createAnchoredAnimation(MapObjectType.DECOR_HOUSE_ISLAND, HOUSE_ISLAND_TUNING, "idle",
                houseX + 50f, houseY + 20f));
        }

        private void addForegroundEffects() {
            DangerNodeState dState = calculateDangerNodeState();
            String zombossState = (dState == DangerNodeState.UNLOCKED_IDLE) ? "defeated" : "idle";
            addActor(createAnchoredAnimation(MapObjectType.ZOMBOSS_NODE, ZOMBOSS_TUNING, zombossState,
                zombossNodeX, zombossNodeY));
            addActor(createAnchoredAnimation(MapObjectType.DANGER_NODE_ANIM, DANGER_NODE_TUNING, dState.pamState,
                dangerNodeAnchorX, dangerNodeAnchorY));

            addActor(createAnchoredAnimation(MapObjectType.SPLASH_EFFECT_ANIM, SPLASH_TUNING, "idle",
                centerX[0], centerY[0] - 20f));
            addActor(createAnchoredAnimation(MapObjectType.SPLASH_EFFECT_ANIM, SPLASH_TUNING, "idle",
                centerX[1], centerY[1] - 20f));
            addActor(createAnchoredAnimation(MapObjectType.SPLASH_EFFECT_ANIM, SPLASH_TUNING, "idle",
                centerX[2], centerY[2] - 20f));

            float waveOffsetX = 130f * LAYOUT_SCALE_X;
            float waveOffsetY = 70f * LAYOUT_SCALE_Y;
            addActor(createAnchoredAnimation(MapObjectType.WAVE_ANIM, WAVE_TUNING, "idle",
                centerX[1] + waveOffsetX, centerY[1] + waveOffsetY));
        }

        private DangerNodeState calculateDangerNodeState() {
            StageStatus s2 = statusOf(2);
            StageStatus s3 = statusOf(3);
            if (s3 != StageStatus.LOCKED) {
                return DangerNodeState.UNLOCKED_IDLE;
            } else if (s2 == StageStatus.COMPLETED) {
                return DangerNodeState.UNLOCKED_ANIMATION;
            }
            return DangerNodeState.LOCKED_IDLE;
        }

        private void buildNode(int index, int stageNumber) {
            boolean boss = stageNumber == BOSS_LEVEL_NUMBER;
            float width = boss ? BOSS_NODE_WIDTH : NODE_WIDTH;
            float height = boss ? BOSS_NODE_HEIGHT : NODE_HEIGHT;
            StageStatus status = statusOf(stageNumber);
            LevelNodeState nodeState = levelNodeStateOf(index, status);

            Stack stack = new Stack();
            stack.setSize(width, height);

            String islandPath = boss ? BOSS_STAGE_ISLAND_TEXTURE
                : STAGE_ISLAND_TEXTURES[index % STAGE_ISLAND_TEXTURES.length];
            Image islandImage = new Image(getTextureDrawable(islandPath, (int) width, (int) height));
            stack.add(islandImage);

            Label numberLabel = new Label(boss ? "BOSS" : String.valueOf(stageNumber), skin, "big");
            numberLabel.setFontScale(boss ? 1.2f : 1.4f);
            numberLabel.setAlignment(Align.center);
            stack.add(numberLabel);

            Table column = new Table();
            column.add(stack).size(width, height).row();

            String captionText = boss ? "Big Wave Beach - Tsunami Zomboss" : "Big Wave Beach - Day " + stageNumber;
            Label nameLabel = new Label(captionText, skin);
            nameLabel.setColor(status == StageStatus.LOCKED ? new Color(0.75f, 0.75f, 0.75f, 1f) : Color.WHITE);
            nameLabel.setAlignment(Align.center);
            nameLabel.setFontScale(0.85f);
            nameLabel.setWrap(true);
            column.add(nameLabel).width(width + 60f).padTop(4);

            column.pack();
            column.setPosition(centerX[index] - column.getWidth() / 2f, centerY[index] - column.getHeight() / 2f);

            if (status != StageStatus.LOCKED && !boss) {
                column.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        selectLevel(stageNumber);
                    }
                });
            }

            addActor(column);

            DecorTuning tuning = boss ? BOSS_LEVEL_NODE_TUNING : LEVEL_NODE_TUNING;
            addActor(createAnchoredAnimation(MapObjectType.LEVEL_NODE, tuning, nodeState.pamState,
                centerX[index] + (boss ? 50f * LAYOUT_SCALE_X : 0f), centerY[index]));
        }

        private Drawable getTextureDrawable(String path, int w, int h) {
            if (Gdx.files.internal(path).exists()) {
                return new TextureRegionDrawable(MenuUiKit.loadTextureSafe(path));
            }
            return circleDrawable(Math.min(w, h), new Color(0.2f, 0.55f, 0.75f, 1f), Color.WHITE, 2);
        }

        private Drawable circleDrawable(int diameter, Color fill, Color border, int borderWidth) {
            Pixmap pixmap = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
            pixmap.setColor(border);
            pixmap.fillCircle(diameter / 2, diameter / 2, diameter / 2);
            pixmap.setColor(fill);
            pixmap.fillCircle(diameter / 2, diameter / 2, diameter / 2 - borderWidth);
            Texture texture = new Texture(pixmap);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            pixmap.dispose();
            return new TextureRegionDrawable(texture);
        }

        private class TrailActor extends Actor {
            private final TextureRegion pixel = whitePixelRegion();

            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.setColor(TRAIL_COLOR);
                drawSegment(batch, houseAnchorX, houseAnchorY, centerX[0], centerY[0]);

                for (int i = 0; i < centerX.length - 1; i++) {
                    if (i == 1) {
                        continue;
                    }
                    drawSegment(batch, centerX[i], centerY[i], centerX[i + 1], centerY[i + 1]);
                }
                drawSegment(batch, centerX[1], centerY[1], bridgeX, bridgeY);
                drawSegment(batch, bridgeX, bridgeY, centerX[2], centerY[2]);

                batch.setColor(Color.WHITE);
            }

            private void drawSegment(Batch batch, float x1, float y1, float x2, float y2) {
                float dx = x2 - x1;
                float dy = y2 - y1;
                float length = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
                float thickness = 14f;
                batch.draw(pixel, x1, y1 - thickness / 2f, 0f, thickness / 2f,
                    length, thickness, 1f, 1f, angle);
            }
        }

        /**
         * Renders one map decoration: a plain static image for
         * non-PAM entries, or a real PAM clip (with graceful fallback
         * through idle/default/empty states) when the engine loaded.
         * If PamPlayer failed to initialize, PAM entries simply don't
         * draw - nothing crashes, the map still works.
         */
        private class MapDecorationActor extends Actor {
            private final MapObjectType objectType;
            private Texture texture;
            private final String pamState;
            private float stateTime = 0f;
            private ClipRef clip;
            private float lastClipAttempt = -10f;

            MapDecorationActor(MapObjectType objectType, float width, float height, String state) {
                this.objectType = objectType;
                this.pamState = state;
                setSize(width, height);

                if (!objectType.isPamAnimation && Gdx.files.internal(objectType.path).exists()) {
                    texture = MenuUiKit.loadTextureSafe(objectType.path);
                }
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                stateTime += delta;
            }

            @Override
            public void draw(Batch batch, float parentAlpha) {
                if (!objectType.isPamAnimation && texture != null) {
                    batch.draw(texture, getX(), getY(), getWidth(), getHeight());
                } else if (objectType.isPamAnimation && pamPlayer != null) {
                    try {
                        ClipRef clip = resolveClip();
                        if (clip != null) {
                            pamPlayer.draw(batch, clip, stateTime, getX(), getY(), true);
                        } else {
                            logMissingClipOnce(objectType, pamState);
                        }
                    } catch (Throwable t) {
                        logClipErrorOnce(objectType, pamState, t);
                    }
                }
            }

            private ClipRef resolveClip() {
                if (clip != null) {
                    return clip;
                }
                if (stateTime - lastClipAttempt < 1f) {
                    return null;
                }
                lastClipAttempt = stateTime;
                java.util.List<String> labels = null;
                try {
                    labels = pamPlayer.clips(objectType.path);
                } catch (Throwable ignored) {
                }
                if (labels == null || labels.isEmpty()) {
                    return null;
                }
                String chosen = null;
                if (pamState != null && labels.contains(pamState)) {
                    chosen = pamState;
                }
                if (chosen == null && labels.contains("active")) {
                    chosen = "active";
                }
                if (chosen == null && labels.contains("idle")) {
                    chosen = "idle";
                }
                if (chosen == null && labels.contains("default")) {
                    chosen = "default";
                }
                if (chosen == null && labels.contains("loop")) {
                    chosen = "loop";
                }
                if (chosen == null) {
                    chosen = labels.get(0);
                }
                try {
                    clip = pamPlayer.getClip(objectType.path, chosen);
                } catch (IllegalArgumentException e) {
                    clip = null;
                }
                return clip;
            }
        }

        private final java.util.Set<String> loggedClipIssues = new java.util.HashSet<>();

        private void logMissingClipOnce(MapObjectType objectType, String pamState) {
            String key = objectType.name() + ":" + pamState;
            if (loggedClipIssues.add(key)) {
                Gdx.app.error("PAM_MISSING", "No PAM clip found for " + objectType.name()
                    + " at path '" + objectType.path + "' (tried state '" + pamState
                    + "', then idle/default/\"\") - check this file actually exists under assets/pvz-assets/"
                    + objectType.path);
            }
        }

        private void logClipErrorOnce(MapObjectType objectType, String pamState, Throwable t) {
            String key = objectType.name() + ":error";
            if (loggedClipIssues.add(key)) {
                Gdx.app.error("PAM_ERROR", "Exception drawing " + objectType.name()
                    + " ('" + objectType.path + "', state '" + pamState + "')", t);
            }
        }
    }

    private static TextureRegion whitePixelRegion() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
}
