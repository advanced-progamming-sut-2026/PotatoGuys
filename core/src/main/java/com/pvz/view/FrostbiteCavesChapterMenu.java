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

import com.pvz.view.game.GameScreen;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

/**
 * Frostbite Caves level-select ("stage map") screen. Ported from  FrostbiteCavesStagesScreen: 3 icy islands plus a separate boss altar,
 * tap a stage to select it, then hit Play in the bottom bar - same
 * interaction pattern as {@link EgyptChapterMenu}.
 * <p>
 * One genuine structural difference from Egypt, taken directly from her
 * code: the trail forks after Day 2 into a "bridge" point near the boss
 * decoration before reconnecting to Day 3, instead of one straight segment -
 * that's the crossing-lines look in the reference screenshot.
 * <p>
 * Real asset paths and PAM engine calls below are copied directly from her
 * working code (same {@code assets/pvz-assets} root, same
 * {@code pvz.libpvz.pam} classes). Lock/complete state comes from our own
 * {@link ChapterController}/{@code Season}, not her Level model. Level 4
 * (the boss) is appearance-only for now, same as Egypt - no real level-4
 * data exists yet, so it isn't clickable.
 */
public class FrostbiteCavesChapterMenu extends ScreenAdapter {

    private static final String CHAPTER_NAME = "Frostbite Caves";

    private static final String CHAPTER_BACKGROUND = "textures/backgrounds/frostbite_cave_bg.png";

    private static final String[] STAGE_ISLAND_TEXTURES = {
        "images/chapters/frostbite_cave/island2.png",
        "images/chapters/frostbite_cave/island1.png",
        "images/chapters/frostbite_cave/island2.png"
    };
    private static final String BOSS_STAGE_ISLAND_TEXTURE = "images/chapters/frostbite_cave/boss_island.png";

    private static final float NODE_WIDTH = 170f;
    private static final float NODE_HEIGHT = 130f;
    private static final float BOSS_NODE_WIDTH = 470f;
    private static final float BOSS_NODE_HEIGHT = 540f;

    // Decorative starting island (start_island.png, native 837x844 — near square).
    private static final float START_ISLAND_WIDTH = 330f;
    private static final float START_ISLAND_HEIGHT = 330f;
    private static final float START_ISLAND_Y_OFFSET = -20f;

    private static final float PATH_WIDTH = 1700f;
    private static final float PATH_HEIGHT = 700f;
    private static final float LAYOUT_SCALE_X = PATH_WIDTH / 1080f;
    private static final float LAYOUT_SCALE_Y = PATH_HEIGHT / 380f;

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

    private static final DecorTuning LEVEL_NODE_TUNING = new DecorTuning(260f, 260f, 0.34f, 27f, 44f);
    private static final DecorTuning BOSS_LEVEL_NODE_TUNING = new DecorTuning(260f, 260f, 0.53f, 45f, 25f);
    private static final DecorTuning DANGER_NODE_TUNING = new DecorTuning(350f, 300f, 0.40f, 90f, -150f);
    private static final DecorTuning ZOMBOSS_TUNING = new DecorTuning(560f, 760f, 0.50f, 37f, 160f);
    private static final DecorTuning BLIZZARD_TUNING = new DecorTuning(250f, 300f, 0.30f, -83f, -24f);
    private static final DecorTuning ICE_CHUNK_TUNING = new DecorTuning(100f, 100f, 0.22f, 0f, 0f);
    private static final DecorTuning SNOW_DUST_TUNING = new DecorTuning(200f, 150f, 0.50f, 10f, 45f);
    private static final DecorTuning CRYSTAL_TUNING = new DecorTuning(25f, 25f, 0.40f, 0f, 0f);

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
        DECOR_HOUSE_ISLAND("images/chapters/frostbite_cave/start_island.png", false),

        SMALL_ISLAND_1("images/chapters/frostbite_cave/island3.png", false),
        SMALL_ISLAND_2("images/chapters/frostbite_cave/island4.png", false),
        SMALL_ISLAND_3("images/chapters/frostbite_cave/island5.png", false),
        SMALL_ISLAND_4("images/chapters/frostbite_cave/island6.png", false),
        SMALL_ISLAND_5("images/chapters/frostbite_cave/island7.png", false),
        SMALL_ISLAND_6("images/chapters/frostbite_cave/anim28_271x337.png", false),
        SMALL_ISLAND_7("images/chapters/frostbite_cave/island11.png", false),

        BIG_BOSS_DECOR_ISLAND("images/chapters/frostbite_cave/island8.png", false),
        LEVEL_NODE("768/INITIAL/WORLDMAP/LEVEL_NODE/LEVEL_NODE.PAM", true),

        FLOATING_ICE_ANIM_1("768/FULL/WORLDMAP/ICEAGE/ANIM27/ANIM27.PAM", true),
        FLOATING_ICE_ANIM_2("768/FULL/WORLDMAP/ICEAGE/ANIM26/ANIM26.PAM", true),
        FLOATING_ICE_ANIM_3("768/FULL/WORLDMAP/ICEAGE/ANIM17/ANIM17.PAM", true),

        DANGER_NODE_ANIM("768/FULL/WORLDMAP/DANGER_NODE_ICEAGE/DANGER_NODE_ICEAGE.PAM", true),

        BLIZZARD_ANIM("768/FULL/EFFECTS/HOTPOTATO_ICEBLOCK_STEAMFX/HOTPOTATO_ICEBLOCK_STEAMFX.PAM", true),
        TWINKLING_CRYSTAL_ANIM("768/INITIAL/EFFECTS/PRIZE_TWINKLE/PRIZE_TWINKLE.PAM", true),
        SNOW_DUST_ANIM("768/FULL/EFFECTS/SNOWSTORM_REAR/SNOWSTORM_REAR.PAM", true);

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

    public FrostbiteCavesChapterMenu(PvZ2 game) {
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
        skin = PvzSkin.get();

        if (textureBank == null) {
            try {
                FileHandle rootHandle = Gdx.files.internal("assets/pvz-assets");
                textureBank = new TextureBank("768", rootHandle);
                pamPlayer = new PamPlayer(textureBank, rootHandle);
                Gdx.app.log("PAM_INIT", "PAM system initialized for the Frostbite Caves stage map.");
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
            selectionLabel.setText(CHAPTER_NAME + " - " + (boss ? "Ice Age Zomboss" : "Day " + selectedLevel)
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

            houseX = centerX[0] - 170f * LAYOUT_SCALE_X;
            houseY = centerY[0] + 70f * LAYOUT_SCALE_Y;
            houseAnchorX = houseX + 75f * LAYOUT_SCALE_X;
            houseAnchorY = houseY + 20f * LAYOUT_SCALE_Y;

            zombossNodeX = (centerX[1] + centerX[2]) / 2f;
            zombossNodeY = Math.max(centerY[1], centerY[2]) - 200f * LAYOUT_SCALE_Y;

            // The trail forks here: Day 2 -> bridge -> Day 3, instead of one
            // straight segment - matches the crossing-lines look
            // reference art has near the boss decoration.
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
            float[][] crystalCoords = {
                {110f, 45f}, {320f, 330f}, {540f, 50f}, {760f, 310f}, {910f, 70f},
                {210f, 270f}, {460f, 190f}, {650f, 35f}, {870f, 330f}, {140f, 170f},
                {380f, 85f}, {590f, 320f}, {830f, 175f}, {260f, 345f}, {980f, 220f},
                {500f, 400f}, {350f, 180f}
            };
            for (float[] coord : crystalCoords) {
                addActor(createAnchoredAnimation(MapObjectType.TWINKLING_CRYSTAL_ANIM, CRYSTAL_TUNING, "animation",
                    coord[0] * LAYOUT_SCALE_X, coord[1] * LAYOUT_SCALE_Y));
            }

            MapObjectType[] iceTypes = {
                MapObjectType.FLOATING_ICE_ANIM_1,
                MapObjectType.FLOATING_ICE_ANIM_2,
                MapObjectType.FLOATING_ICE_ANIM_3
            };
            float[][] iceCoords = {
                {180f, 310f}, {480f, 320f}, {750f, 300f},
                {120f, 150f}, {350f, 450f}, {600f, 120f},
                {820f, 480f}, {1020f, 280f}, {400f, 250f},
                {250f, 550f}, {680f, 500f}, {920f, 550f},
                {300f, 500f}, {100f, 200f}, {600f, 400f}
            };
            for (int i = 0; i < iceCoords.length; i++) {
                MapObjectType selectedIce = iceTypes[i % iceTypes.length];
                addActor(createAnchoredAnimation(selectedIce, ICE_CHUNK_TUNING, "idle",
                    iceCoords[i][0] * LAYOUT_SCALE_X, iceCoords[i][1] * LAYOUT_SCALE_Y));
            }
        }

        private void addMapDecorations() {
            MapObjectPlacement[] placements = {
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_1, 225, 230, 50, 38),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_2, 530, 190, 55, 40),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_3, 610, 260, 60, 45),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_4, 880, 195, 50, 35),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_5, 770, 240, 40, 60),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_5, 155, 270, 40, 60),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_5, 450, 120, 40, 60),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_6, 270, 160, 55, 42),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_6, 700, 180, 60, 45),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_6, 950, 110, 50, 38),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_7, 460, 300, 60, 120),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_7, 770, 20, 90, 180),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_7, 75, 105, 90, 180),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_6, 600, 80, 52, 40),
                new MapObjectPlacement(MapObjectType.SMALL_ISLAND_7, 350, 70, 100, 200)
            };
            for (MapObjectPlacement p : placements) {
                MapDecorationActor actor = new MapDecorationActor(p.type, p.width, p.height, "idle");
                actor.setPosition(p.x * LAYOUT_SCALE_X, p.y * LAYOUT_SCALE_Y);
                addActor(actor);
            }

            // Purely decorative starting island - not clickable (no Greenhouse hookup here).
            MapDecorationActor houseIsland = new MapDecorationActor(MapObjectType.DECOR_HOUSE_ISLAND, START_ISLAND_WIDTH, START_ISLAND_HEIGHT, "idle");
            houseIsland.setPosition(houseX, houseY + START_ISLAND_Y_OFFSET);
            addActor(houseIsland);
        }

        private void addForegroundEffects() {
            DangerNodeState dState = calculateDangerNodeState();
            addActor(createAnchoredAnimation(MapObjectType.BIG_BOSS_DECOR_ISLAND, ZOMBOSS_TUNING, "idle",
                zombossNodeX - 60f, zombossNodeY - 160f));
            addActor(createAnchoredAnimation(MapObjectType.DANGER_NODE_ANIM, DANGER_NODE_TUNING, dState.pamState,
                dangerNodeAnchorX, dangerNodeAnchorY));

            addActor(createAnchoredAnimation(MapObjectType.SNOW_DUST_ANIM, SNOW_DUST_TUNING, "loop", centerX[0] + 35f, centerY[0] - 30f));
            addActor(createAnchoredAnimation(MapObjectType.SNOW_DUST_ANIM, SNOW_DUST_TUNING, "loop", centerX[2] + 35f, centerY[2] - 30f));

            float blizzardOffsetX = 130f * LAYOUT_SCALE_X;
            float blizzardOffsetY = 70f * LAYOUT_SCALE_Y;
            addActor(createAnchoredAnimation(MapObjectType.BLIZZARD_ANIM, BLIZZARD_TUNING, "animation",
                centerX[1] + blizzardOffsetX - 40f, centerY[1] + blizzardOffsetY + 28f));
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

            String islandPath = boss ? BOSS_STAGE_ISLAND_TEXTURE : STAGE_ISLAND_TEXTURES[index % STAGE_ISLAND_TEXTURES.length];
            Image islandImage = new Image(getTextureDrawable(islandPath, (int) width, (int) height));
            stack.add(islandImage);

            Label numberLabel = new Label(boss ? "BOSS" : String.valueOf(stageNumber), skin, "big");
            numberLabel.setFontScale(boss ? 1.2f : 1.4f);
            numberLabel.setAlignment(Align.center);
            stack.add(numberLabel);

            Table column = new Table();
            column.add(stack).size(width, height).row();

            String captionText = boss ? "Frostbite Caves - Ice Age Zomboss" : "Frostbite Caves - Day " + stageNumber;
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
                centerX[index], centerY[index]));
        }

        private Drawable getTextureDrawable(String path, int w, int h) {
            if (Gdx.files.internal(path).exists()) {
                return new TextureRegionDrawable(MenuUiKit.loadTextureSafe(path));
            }
            return circleDrawable(Math.min(w, h), new Color(0.35f, 0.62f, 0.82f, 1f), Color.WHITE, 2);
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
                batch.setColor(0.72f, 0.85f, 0.95f, 0.85f);

                drawSegment(batch, houseAnchorX, houseAnchorY, centerX[0], centerY[0]);

                // Day 2 -> Day 3 is replaced by a two-part detour through the
                // bridge point near the boss decoration, instead of a direct line.
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

                if (objectType.isPamAnimation && pamPlayer != null) {
                    try {
                        pamPlayer.loadSync(objectType.path);
                    } catch (Throwable ignored) {
                    }
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

            private final java.util.Set<String> loggedClipIssues = new java.util.HashSet<>();

            private void logMissingClipOnce(MapObjectType objectType, String pamState) {
                String key = objectType.name() + ":" + pamState;
                if (loggedClipIssues.add(key)) {
                    Gdx.app.error("PAM_MISSING", "No PAM clip found for " + objectType.name()
                        + " at path '" + objectType.path + "' (tried state '" + pamState
                        + "', then idle/default/\"\") - check this file actually exists under assets/pvz-assets/IMAGES/"
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
