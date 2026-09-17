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
import com.pvz.view.game.GameScreen;
import com.pvz.PvZ2;
import com.pvz.controller.ChapterController;
import com.pvz.enums.GameAsset;
import com.pvz.models.AppContext;
import com.pvz.models.user.Profile;
import com.pvz.view.MenuUiKit;

import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

/**
 * Dark Ages level-select ("stage map") screen. Ported from the reference
 * DarkAgesStagesScreen: 3 islands plus a separate boss node, tap a stage to
 * select it, then hit Play in the bottom bar - same interaction pattern as
 * {@link EgyptChapterMenu} and {@link FrostbiteCavesChapterMenu}.
 * <p>
 * Genuinely unique things ported from  actual Dark Ages code (not just
 * reused from Egypt/Frostbite): drifting clouds that continuously scroll
 * across the map, a lightning effect near the danger node, fireflies
 * instead of stars/crystals, and Day 2's island rendered taller and
 * top-anchored instead of centered like the others. The boss node itself
 * is rendered using the actual Zomboss PAM clip rather than a static island
 * texture, since her Dark Ages file never defines a separate boss island
 * PNG - the animation IS the boss node's art.
 * <p>
 * Real asset paths and PAM engine calls below are copied directly from her
 * working code. Lock/complete state comes from our own
 * {@link ChapterController}/{@code Season}, not her Level model. Level 4
 * (the boss) is appearance-only for now, same as Egypt/Frostbite - no real
 * level-4 data exists yet, so it isn't clickable.
 */
public class DarkAgesChapterMenu extends ScreenAdapter {

    private static final String CHAPTER_NAME = "Dark Ages";

    private static final String CHAPTER_BACKGROUND = "textures/backgrounds/dark_ages_bg.png";

    private static final String[] STAGE_ISLAND_TEXTURES = {
        "images/chapters/darkage/island7.png",
        "images/chapters/darkage/anim9_373x659.png",
        "images/chapters/darkage/anim10_352x358.png"
    };

    private static final float NODE_WIDTH = 170f;
    private static final float NODE_HEIGHT = 130f;
    private static final float BOSS_NODE_WIDTH = 470f;
    private static final float BOSS_NODE_HEIGHT = 540f;

    private static final float HOUSE_ISLAND_WIDTH = 270f;
    private static final float HOUSE_ISLAND_HEIGHT = 300f;
    private static final float HOUSE_ISLAND_OFFSET_X = -25f;

    private static final float PATH_WIDTH = 1700f;
    private static final float PATH_HEIGHT = 700f;
    private static final float LAYOUT_SCALE_X = PATH_WIDTH / 1080f;
    private static final float LAYOUT_SCALE_Y = PATH_HEIGHT / 380f;

    private static final Color TRAIL_COLOR = new Color(0.55f, 0.32f, 0.85f, 0.85f);

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

    private static final DecorTuning LEVEL_NODE_TUNING = new DecorTuning(260f, 260f, 0.34f, 27f, 34f);
    private static final DecorTuning BOSS_LEVEL_NODE_TUNING = new DecorTuning(260f, 260f, 0.45f, 25f, 70f);
    private static final DecorTuning DANGER_NODE_TUNING = new DecorTuning(350f, 300f, 0.50f, 45f, 130f);
    private static final DecorTuning ZOMBOSS_TUNING = new DecorTuning(560f, 760f, 0.45f, 70f, 135f);
    private static final DecorTuning FIREFLY_TUNING = new DecorTuning(25f, 25f, 0.15f, 0f, 0f);
    private static final DecorTuning CLOUD_TUNING = new DecorTuning(300f, 200f, 0.05f, 0f, 0f);
    private static final DecorTuning LIGHTNING_TUNING = new DecorTuning(300f, 300f, 0.28f, 0f, 0f);
    private static final DecorTuning ROCK_TUNING = new DecorTuning(100f, 100f, 0.22f, 0f, 0f);

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
        DECOR_HOUSE_ISLAND("images/chapters/darkage/anim1_1201x1413.png", false),

        SMALL_ISLAND_1("images/chapters/darkage/island6.png", false),
        SMALL_ISLAND_2("images/chapters/darkage/island8.png", false),
        SMALL_ISLAND_3("images/chapters/darkage/island9.png", false),

        FLOATING_ROCK_1("images/chapters/darkage/anim16_55x63.png", false),
        FLOATING_ROCK_2("images/chapters/darkage/anim15_102x97.png", false),

        PARTICLE("images/chapters/darkage/anim4_23x23.png", false),

        LEVEL_NODE("768/INITIAL/WORLDMAP/LEVEL_NODE/LEVEL_NODE.PAM", true),

        ZOMBOSS_BOSS_ISLAND("768/FULL/WORLDMAP/ZOMBOSS_NODE_DARK/ZOMBOSS_NODE_DARK.PAM", true),

        DANGER_NODE_ANIM("768/FULL/WORLDMAP/DANGER_NODE_DARK/DANGER_NODE_DARK.PAM", true),

        CLOUD_ANIM_1("768/FULL/WORLDMAP/DARK/ANIM6/ANIM6.PAM", true),
        CLOUD_ANIM_2("768/FULL/WORLDMAP/DARK/ANIM7/ANIM7.PAM", true),

        FIREFLY_ANIM("768/FULL/WORLDMAP/DARK/ANIM5/ANIM5.PAM", true),

        LIGHTNING_ANIM("768/FULL/EFFECTS/ZOMBIE_DARK_WIZARD_PROJECTILE_HIT"
                + "/ZOMBIE_DARK_WIZARD_PROJECTILE_HIT.PAM", true);

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

    public DarkAgesChapterMenu(PvZ2 game) {
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
                FileHandle rootHandle = Gdx.files.internal("pvz-assets");
                textureBank = new TextureBank("768", rootHandle);
                pamPlayer = new PamPlayer(textureBank, rootHandle);
                Gdx.app.log("PAM_INIT", "PAM system initialized for the Dark Ages stage map.");
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

    // --- Top bar (same convention as AdventureMenu/MainMenu/EgyptChapterMenu) -

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
            selectionLabel.setText(CHAPTER_NAME + " - " + (boss ? "Zomboss" : "Day " + selectedLevel)
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
        private float dangerNodeAnchorX, dangerNodeAnchorY;
        private float bridgeX, bridgeY;

        private float dangerRenderHeight() {
            return DANGER_NODE_TUNING.nativeH * DANGER_NODE_TUNING.scale;
        }

        StagePath() {
            setSize(PATH_WIDTH, PATH_HEIGHT);

            for (int i = 0; i < PATH_NODE_COUNT; i++) {
                float progress = PATH_NODE_COUNT <= 1 ? 0.5f : (float) i / (PATH_NODE_COUNT - 1);
                centerX[i] = PATH_WIDTH * (0.12f + 0.76f * progress);
                centerY[i] = PATH_HEIGHT * (0.52f + 0.26f * (float) Math.sin(progress * Math.PI * 1.3f));
            }
            centerX[0] += 60f * LAYOUT_SCALE_X;

            houseX = centerX[0] - 170f * LAYOUT_SCALE_X;
            houseY = centerY[0] + 70f * LAYOUT_SCALE_Y;
            houseAnchorX = houseX + 75f * LAYOUT_SCALE_X;
            houseAnchorY = houseY + 20f * LAYOUT_SCALE_Y;

            dangerNodeAnchorX = (centerX[1] + centerX[2]) / 2f;
            dangerNodeAnchorY = Math.max(centerY[1], centerY[2]) - 200f * LAYOUT_SCALE_Y;

            // The trail forks here: Day 2 -> bridge -> Day 3, instead of one
            // straight segment - matches Egypt/Frostbite's crossing-lines look.
            bridgeX = dangerNodeAnchorX;
            bridgeY = dangerNodeAnchorY + dangerRenderHeight() / 2f;

            addBackgroundDecorations();
            addActor(new TrailActor());
            addMapDecorations();

            for (int i = 0; i < PATH_NODE_COUNT; i++) {
                buildNode(i, i + 1);
            }

            addForegroundEffects();
            addClouds();
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
            float[][] fireflyCoords = {
                {110f, 45f}, {320f, 330f}, {540f, 50f}, {760f, 310f}, {910f, 70f},
                {210f, 270f}, {460f, 190f}, {650f, 35f}, {870f, 330f}, {140f, 170f},
                {380f, 85f}, {590f, 320f}, {830f, 175f}, {260f, 345f}, {980f, 220f}
            };
            for (float[] coord : fireflyCoords) {
                addActor(createAnchoredAnimation(MapObjectType.FIREFLY_ANIM, FIREFLY_TUNING, "idle",
                    coord[0] * LAYOUT_SCALE_X, coord[1] * LAYOUT_SCALE_Y));
            }

            MapObjectType[] rockTypes = { MapObjectType.FLOATING_ROCK_1, MapObjectType.FLOATING_ROCK_2 };
            float[][] rockCoords = {
                {180f, 310f}, {480f, 320f}, {750f, 300f},
                {120f, 150f}, {350f, 450f}, {600f, 120f},
                {820f, 480f}, {1020f, 280f}, {400f, 250f},
                {250f, 550f}, {680f, 500f}, {920f, 550f}
            };
            for (int i = 0; i < rockCoords.length; i++) {
                MapObjectType selectedRock = rockTypes[i % rockTypes.length];
                //addActor(createAnchoredAnimation(selectedRock, ROCK_TUNING, "idle",
                    //rockCoords[i][0] * LAYOUT_SCALE_X, rockCoords[i][1] * LAYOUT_SCALE_Y));
            }

            for (int i = 0; i < 8; i++) {
                MapDecorationActor particle = new MapDecorationActor(MapObjectType.PARTICLE, 14f, 14f, "idle");
                particle.setPosition(rockCoords[i][0] * LAYOUT_SCALE_X + 20f, rockCoords[i][1] * LAYOUT_SCALE_Y - 20f);
                addActor(particle);
            }
        }

        private void addMapDecorations() {
            MapObjectPlacement[] placements = {
                //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_2, 70, 260, 50, 38),
                //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_2, 270, 150, 55, 40),
               //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_3, 620, 280, 60, 45),
                //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_1, 760, 110, 100, 70),
               // new MapObjectPlacement(MapObjectType.SMALL_ISLAND_2, 970, 240, 55, 40),
                //new MapObjectPlacement(MapObjectType.SMALL_ISLAND_3, 410, 170, 60, 45),
               // new MapObjectPlacement(MapObjectType.SMALL_ISLAND_1, 300, 200, 100, 70)
            };
            for (MapObjectPlacement p : placements) {
                MapDecorationActor actor = new MapDecorationActor(p.type, p.width, p.height, "idle");
                actor.setPosition(p.x * LAYOUT_SCALE_X, p.y * LAYOUT_SCALE_Y);
                addActor(actor);
            }

            // Purely decorative starting island - not clickable (no Greenhouse hookup here).
            MapDecorationActor houseIsland = new MapDecorationActor(MapObjectType.DECOR_HOUSE_ISLAND,
                HOUSE_ISLAND_WIDTH, HOUSE_ISLAND_HEIGHT, "idle");
            houseIsland.setPosition(houseX + HOUSE_ISLAND_OFFSET_X, houseY - 20f);
            addActor(houseIsland);
        }

        private void addForegroundEffects() {
            DangerNodeState dState = calculateDangerNodeState();
            addActor(createAnchoredAnimation(MapObjectType.DANGER_NODE_ANIM, DANGER_NODE_TUNING, dState.pamState,
                dangerNodeAnchorX, dangerNodeAnchorY));

            addActor(createAnchoredAnimation(MapObjectType.LIGHTNING_ANIM, LIGHTNING_TUNING, "idle",
                dangerNodeAnchorX + 60f * LAYOUT_SCALE_X, dangerNodeAnchorY + 40f * LAYOUT_SCALE_Y));
        }

        private void addClouds() {
            float travel = PATH_WIDTH + CLOUD_TUNING.nativeW * CLOUD_TUNING.scale * 2f;
            for (int i = 0; i < 4; i++) {
                float startOffset = travel * i / 4f;
                float y = PATH_HEIGHT * (0.65f + 0.08f * i);
                float speed = 40f + i * 6f;
                addActor(new DriftingCloud(MapObjectType.CLOUD_ANIM_1, CLOUD_TUNING, startOffset, y, speed));
            }
            for (int i = 0; i < 4; i++) {
                float startOffset = travel * (i + 0.5f) / 4f;
                float y = PATH_HEIGHT * (0.20f + 0.08f * i);
                float speed = 30f + i * 5f;
                addActor(new DriftingCloud(MapObjectType.CLOUD_ANIM_2, CLOUD_TUNING, startOffset, y, speed));
            }
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

            if (boss) {
                // Dark Ages never defines a separate static boss island
                // texture - the Zomboss PAM clip itself IS the boss node's art.
                String zombossState = (status == StageStatus.COMPLETED) ? "defeated" : "active";
                addActor(createAnchoredAnimation(MapObjectType.ZOMBOSS_BOSS_ISLAND, ZOMBOSS_TUNING, zombossState,
                    centerX[index], centerY[index]));
            } else {
                String islandPath = STAGE_ISLAND_TEXTURES[index % STAGE_ISLAND_TEXTURES.length];

                // Day 2's island renders taller and top-anchored instead of
                // centered - a quirk specific to the Dark Ages layout.
                float islandWidth = width;
                float islandHeight = (index == 1) ? height * 1.5f : height;

                Image islandImage = new Image(getTextureDrawable(islandPath, (int) islandWidth, (int) islandHeight));
                islandImage.setSize(islandWidth, islandHeight);

                float islandX = centerX[index] - islandWidth / 2f;
                float islandY;
                if (index == 1) {
                    float topY = centerY[index] + height / 2f;
                    islandY = topY - islandHeight + 16f;
                } else {
                    islandY = centerY[index] - islandHeight / 2f;
                }
                islandImage.setPosition(islandX, islandY);
                addActor(islandImage);
            }

            DecorTuning tuning = boss ? BOSS_LEVEL_NODE_TUNING : LEVEL_NODE_TUNING;
            addActor(createAnchoredAnimation(MapObjectType.LEVEL_NODE, tuning, nodeState.pamState,
                centerX[index], centerY[index]));

            Stack stack = new Stack();
            stack.setSize(width, height);

            Label numberLabel = new Label(boss ? "BOSS" : String.valueOf(stageNumber), skin, "big");
            numberLabel.setFontScale(boss ? 1.2f : 1.4f);
            numberLabel.setAlignment(Align.center);
            stack.add(numberLabel);

            Table column = new Table();
            column.add(stack).size(width, height).row();

            String captionText = boss ? "Dark Ages - Zomboss" : "Dark Ages - Day " + stageNumber;
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
        }

        private Drawable getTextureDrawable(String path, int w, int h) {
            if (Gdx.files.internal(path).exists()) {
                return new TextureRegionDrawable(MenuUiKit.loadTextureSafe(path));
            }
            return circleDrawable(Math.min(w, h), new Color(0.45f, 0.30f, 0.6f, 1f), Color.WHITE, 2);
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

        /** A cloud that continuously drifts left-to-right and wraps around, per the Dark Ages map. */
        private class DriftingCloud extends Group {
            private final float renderWidth;
            private final float baseY;
            private final float startOffset;
            private final float speed;
            private final float travel;
            private float elapsed = 0f;

            DriftingCloud(MapObjectType type, DecorTuning tuning, float startOffset, float y, float speed) {
                setTransform(true);
                setScale(tuning.scale);
                setSize(tuning.nativeW, tuning.nativeH);

                this.renderWidth = tuning.nativeW * tuning.scale;
                this.baseY = y;
                this.startOffset = startOffset;
                this.speed = speed;
                this.travel = PATH_WIDTH + renderWidth * 2f;

                MapDecorationActor actor = new MapDecorationActor(type, tuning.nativeW, tuning.nativeH, "idle");
                actor.setSize(tuning.nativeW, tuning.nativeH);
                addActor(actor);

                setPosition(currentX(), baseY);
            }

            private float currentX() {
                float x = (startOffset + speed * elapsed) % travel;
                return x - renderWidth;
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                elapsed += delta;
                setPosition(currentX(), baseY);
            }
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
                    + "', then active/idle/default/\"\") - check this file actually exists under assets/pvz-assets/"
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
