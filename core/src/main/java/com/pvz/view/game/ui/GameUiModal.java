package com.pvz.view.game.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.pvz.PvZ2;
import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.card.ZombieCard;
import com.pvz.models.user.User;
import com.pvz.view.MenuUiKit;
import com.pvz.view.PlantData;

import pvz.skin.PvzSkin;

/**
 * In-game HUD: top bar (sun/plant food/wallet) plus the seed-packet tray,
 * a vertical column hugging the left edge of the screen showing full cards
 * (packet background, family badge, sun cost) — same look as the pregame
 * slot bar and the in-match tray — with a cooldown overlay on top.
 *
 * <p>
 * Slot size is tuned to the 1280x720 gameplay viewport (see
 * GameController's FitViewport) rather than the 1920x1080 menus use, so all
 * 7 cards fit under the top bar without running off the bottom of the screen.
 *
 * <p>
 * The plant-food bank (dots + "+" cheat button) sits at normalized (0.25, 0.1).
 */
public class GameUiModal extends Table {

    protected static final float SLOT_WIDTH = 135f;
    protected static final float SLOT_HEIGHT = 95f;

    private static final int CHEAT_SUN_AMOUNT = 25;
    private static final int MAX_PLANT_FOOD = 3;

    private static final float WAVE_BAR_WIDTH = 260f;
    private static final float WAVE_BAR_HEIGHT = 36f;
    private static final float TRACK_LEFT = 8f;
    private static final float TRACK_RIGHT = 248f;
    private static final float WAVE_FILL_Y = 9f;
    private static final float WAVE_FILL_HEIGHT = 20f;
    private static final float FLAG_WIDTH = 27f;
    private static final float FLAG_HEIGHT = 21f;
    private static final float FLAG_POLE_WIDTH = 8f;
    private static final float FLAG_POLE_HEIGHT = 46f;
    private static final float FLAG_DOWN_Y = 28f;
    private static final float FLAG_X_OFFSET = 22f;

    private static final float OBJ_BAR_WIDTH = 220f;
    private static final float OBJ_BAR_HEIGHT = 36f;
    private static final float OBJ_TRACK_HEIGHT = 20f;
    private static final float OBJ_TRACK_Y = 9f;
    private static final String CHECK_MARK_PAM = "768/INITIAL/UI/GENERIC/CHECK_MARK_ANIM/CHECK_MARK_ANIM.PAM";
    private static final String CHECK_MARK_CLIP = "check_idle";

    private final Label sunLabel;
    private final Label coinLabel;
    private final Label gemLabel;
    private Group waveGroup;
    private Image waveFill;
    private Image waveZombieHead;
    private Image[] waveFlagImages;
    private int lastCompletedWaves = -1;
    private Group objectiveGroup;
    private Image objectiveFill;
    private Image objectiveFrame;
    private PamClipActor objectiveTick;
    private float lastObjectiveProgress = -1f;
    private boolean lastObjectiveComplete = false;
    private final Image coinIcon;
    private final Image gemIcon;
    private final Image[] plantFoodDots = new Image[MAX_PLANT_FOOD];
    private final ImageButton addSunButton;
    private final ImageButton addFoodButton;
    protected PlantCard selectedCard = null;
    protected ZombieCard selectedZombieCard = null;
    private boolean shovelSelected = false;
    private Runnable onShovelRequested = null;
    private ImageButton shovelButton = null;
    private boolean plantFoodSelected = false;
    private Runnable onPlantFoodRequested = null;

    private Label speedLabel = null;
    private Image speedImage = null;

    protected final Map<PlantCard, Table> slotByCard = new HashMap<>();
    protected final Map<PlantCard, Image> cooldownOverlayByCard = new HashMap<>();
    protected final Map<ZombieCard, Table> zombieSlotByCard = new HashMap<>();
    protected final Map<ZombieCard, Image> zombieCooldownOverlayByCard = new HashMap<>();
    protected Map<PlantType, PlantData> dataByType = new HashMap<>();

    private Label timerLabel;
    private Stack timerBank;

    public GameUiModal(Runnable onPauseRequested) {
        super();
        setFillParent(true);
        top();
        setVisible(false);

        Table topBar = new Table();
        topBar.top().left();
        topBar.pad(10);

        // Sun bank with a +25 cheat button, matching the reference HUD
        // (phase-0-group-51).
        Table sunRow = new Table();
        addSunButton = createCheatButton();
        addSunButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isDebugModeEnabled())
                    return;
                GameContext context = AppContext.getInstance().getGameContext();
                if (context == null)
                    return;
                context.addSun(CHEAT_SUN_AMOUNT);
                updateHud();
            }
        });

        Stack sunBank = new Stack();
        Image sunBackground = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_background_3slice"));
        sunBackground.setScaling(Scaling.stretch);
        Table sunContent = new Table();
        Image sunIcon = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_sun"));
        sunIcon.setScaling(Scaling.fit);

        sunLabel = new Label("0", PvzSkin.get(), "big_outline");
        sunLabel.setColor(Color.WHITE);
        sunLabel.setFontScale(1.25f);
        sunLabel.setAlignment(Align.center);
        sunContent.add(sunIcon).size(55f).padLeft(-9f);
        sunContent.add(sunLabel).expandX().center().padRight(8f);
        sunBank.add(sunBackground);
        sunBank.add(sunContent);
        sunRow.add(addSunButton).size(50f).padRight(3f);
        sunRow.add(sunBank).width(150f).height(54f);
        topBar.add(sunRow).left();

        buildWaveProgress();
        topBar.add(waveGroup).left().padLeft(15f).size(WAVE_BAR_WIDTH + 50f, 50f);

        timerBank = new Stack();
        Image timerBg = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_background_3slice"));
        timerBg.setScaling(Scaling.stretch);
        Table timerContent = new Table();
        timerLabel = new Label("2:00", PvzSkin.get(), "big_outline");
        timerLabel.setColor(Color.RED);
        timerLabel.setFontScale(1.25f);
        timerLabel.setAlignment(Align.center);
        timerContent.add(timerLabel).expandX().center().padRight(8f).padLeft(8f);
        timerBank.add(timerBg);
        timerBank.add(timerContent);
        timerBank.setVisible(false);
        topBar.add(timerBank).left().padLeft(10f).width(110f).height(54f);

        topBar.row();

        // Plant food bank with a +1 cheat button, matching the reference HUD
        // (phase-0-group-51).
        Table foodRow = new Table();
        addFoodButton = createCheatButton();
        addFoodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isDebugModeEnabled())
                    return;
                GameContext context = AppContext.getInstance().getGameContext();
                if (context == null)
                    return;
                context.addPlantFood(1);
                updateHud();
            }
        });

        Stack foodDisplay = new Stack();
        Stack dotsBank = new Stack();
        Image dotsBackground = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_demo"));
        dotsBackground.setScaling(Scaling.stretch);
        dotsBank.add(dotsBackground);

        Table dots = new Table();
        dots.center();
        for (int i = 0; i < MAX_PLANT_FOOD; i++) {
            Image dot = new Image(PvzSkin.get().getDrawable("image_ui_generic_navdot_fill"));
            plantFoodDots[i] = dot;
            dots.add(dot).size(17f).pad(2f);
        }
        dotsBank.add(dots);

        Table rectangleLayer = new Table();
        rectangleLayer.right();
        rectangleLayer.add(dotsBank).width(104f).height(40f);
        foodDisplay.add(rectangleLayer);

        Image foodIcon = new Image(PvzSkin.get().getDrawable("image_ui_almanac_plant_food_stat_icon"));
        foodIcon.setScaling(Scaling.fit);
        Table iconLayer = new Table();
        iconLayer.left();
        iconLayer.add(foodIcon).size(72f);
        foodDisplay.add(iconLayer);

        foodDisplay.setTouchable(Touchable.enabled);
        foodDisplay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onPlantFoodRequested != null) {
                    onPlantFoodRequested.run();
                }
            }
        });

        foodRow.add(addFoodButton).size(50f).padRight(3f);
        foodRow.add(foodDisplay).width(150f).height(46f);

        Table walletTable = new Table();
        walletTable.top().right();

        Table coinCell = new Table();
        coinIcon = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_coin"));
        coinLabel = new Label("0", PvzSkin.get(), "medium_outline");
        coinLabel.setColor(Color.YELLOW);
        coinLabel.setFontScale(1.4f);
        coinCell.add(coinIcon).size(64, 64);
        coinCell.add(coinLabel).padLeft(8);
        walletTable.add(coinCell).padRight(20);

        Table gemCell = new Table();
        gemIcon = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_gem"));
        gemLabel = new Label("0", PvzSkin.get(), "medium_outline");
        gemLabel.setColor(Color.CYAN);
        gemLabel.setFontScale(1.4f);
        gemCell.add(gemIcon).size(64, 64);
        gemCell.add(gemLabel).padLeft(8);
        walletTable.add(gemCell);

        // Pause button pinned to the very top-right corner (same look as the
        // in-match HUD), pushing the coin/gem counters a bit to the left.
        ImageButton pauseButton = new ImageButton(PvzSkin.get(), "ingame_pause");
        pauseButton.setTouchable(Touchable.enabled);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onPauseRequested != null) {
                    onPauseRequested.run();
                }
            }
        });

        // Shovel button: uses the same art and checked-state behaviour as the
        // phase-0 reference HUD. The imageChecked drawable keeps the "down"
        // texture visible while the shovel is armed. It lives in its own
        // bottom-right overlay so it stays pinned to the corner.
        shovelButton = createShovelButton();
        shovelButton.setTouchable(Touchable.enabled);
        shovelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onShovelRequested != null) {
                    onShovelRequested.run();
                }
            }
        });

        Table rightControls = new Table();
        rightControls.top().right();
        rightControls.add(walletTable).right().padRight(10);
        rightControls.add(buildSpeedButton()).right().padRight(10).top();
        rightControls.add(pauseButton).right().size(58f).top();

        add(topBar).top().left().expandX().fillX();
        add(rightControls).top().right().padTop(15).padRight(15);
        row();

        // Plant food pinned at normalized (0.25, 0.1): centered on x = 0.25 * 1280 =
        // 320
        // and its bottom at y = 0.1 * 720 = 72. 0.25*1280 - (50 + 3 + 150)/2 = 218.5
        // pushes its center onto x = 320; padBottom(72) lifts it off the bottom edge.
        //
        // It lives in its own fill-parent overlay added via addActor(), so it plays
        // no part in this table's cell layout and can't shift anything else.
        Table foodOverlay = new Table();
        foodOverlay.setFillParent(true);
        foodOverlay.bottom().left();
        foodOverlay.add(foodRow).padLeft(218.5f).padBottom(15f);
        addActor(foodOverlay);

        // Shovel pinned to the bottom-right corner (58x58, matching the top-bar
        // buttons), in its own overlay so it never affects the table layout.
        Table shovelOverlay = new Table();
        shovelOverlay.setFillParent(true);
        shovelOverlay.bottom().right();
        shovelOverlay.add(shovelButton).size(58f).padRight(20f).padBottom(15f);
        addActor(shovelOverlay);

        // Objective progress bar for Timed War: kept out of the top bar table so
        // the HUD keeps the exact known-good Normal-mode layout. It sits in its
        // own fill-parent overlay just under the sun bank (below the wave bar,
        // clear of the seed-packet column).
        buildObjectiveProgress();
        Table objectiveOverlay = new Table();
        objectiveOverlay.setFillParent(true);
        objectiveOverlay.top().left();
        objectiveOverlay.add(objectiveGroup).size(OBJ_BAR_WIDTH + 46f, 46f).padLeft(228f).padTop(70f);
        addActor(objectiveOverlay);
    }

    public PlantCard getSelectedCard() {
        return selectedCard;
    }

    public ZombieCard getSelectedZombieCard() {
        return selectedZombieCard;
    }

    public void setSelectedZombieCard(ZombieCard card) {
        this.selectedZombieCard = card;
        if (card != null) {
            this.shovelSelected = false;
            if (shovelButton != null)
                shovelButton.setChecked(false);
        }
        updateCardStyles();
    }

    /**
     * Where the coin wallet icon sits in stage (1280x720, bottom-left origin)
     * coordinates — the target the flying coin-drop animation arcs toward.
     */
    public com.badlogic.gdx.math.Vector2 getCoinWalletStagePosition() {
        if (coinIcon == null) {
            return new com.badlogic.gdx.math.Vector2(1120f, 695f);
        }
        return coinIcon.localToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(coinIcon.getWidth() / 2f, coinIcon.getHeight() / 2f));
    }

    /**
     * Where the gem (diamond) wallet icon sits in stage (1280x720, bottom-left
     * origin) coordinates — the target the flying diamond-drop animation arcs
     * toward.
     */
    public com.badlogic.gdx.math.Vector2 getGemWalletStagePosition() {
        if (gemIcon == null) {
            return new com.badlogic.gdx.math.Vector2(980f, 695f);
        }
        return gemIcon.localToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(gemIcon.getWidth() / 2f, gemIcon.getHeight() / 2f));
    }

    /**
     * The "+" cheat button used next to the sun and plant food banks, using the
     * same
     * coin-buy asset the reference HUD (phase-0-group-51) uses for both buttons.
     */
    private ImageButton createCheatButton() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = PvzSkin.get().getDrawable("image_ui_hud_ingame_coin_buy");
        style.imageDown = PvzSkin.get().getDrawable("image_ui_hud_ingame_coin_buy_down");
        style.imageOver = PvzSkin.get().getDrawable("image_ui_hud_ingame_coin_buy_down");
        style.imageChecked = PvzSkin.get().getDrawable("image_ui_hud_ingame_coin_buy_down");
        return new ImageButton(style);
    }

    /**
     * Shovel tool button mirroring the phase-0 reference HUD: the up/down art is
     * taken from the shared PvZ skin, and {@code imageChecked} is wired to the
     * "down" drawable so {@code setChecked(true)} keeps the pressed look while
     * the shovel is armed.
     */
    private ImageButton createShovelButton() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = PvzSkin.get().getDrawable("image_ui_hud_ingame_shovel_button");
        style.imageDown = PvzSkin.get().getDrawable("image_ui_hud_ingame_shovel_button_down");
        style.imageOver = PvzSkin.get().getDrawable("image_ui_hud_ingame_shovel_button_down");
        style.imageChecked = PvzSkin.get().getDrawable("image_ui_hud_ingame_shovel_button_down");
        return new ImageButton(style);
    }

    /**
     * Game-speed button pinned to the left of the pause button. Cycles the saved
     * Game Speed setting through 1x -> 2x -> 3x -> 1x on each tap, with the current
     * multiplier shown in a label just under the {@code images/2x.png} art.
     */
    private Table buildSpeedButton() {
        com.badlogic.gdx.graphics.Texture tex = new com.badlogic.gdx.graphics.Texture(
                com.badlogic.gdx.Gdx.files.internal("images/2x.png"));
        speedImage = new Image(tex);
        speedImage.setScaling(Scaling.fit);

        speedLabel = new Label("", PvzSkin.get(), "medium");
        speedLabel.setColor(Color.BLACK);

        Table button = new Table();
        button.add(speedImage).size(58f).row();
        button.add(speedLabel).padTop(2f);
        refreshSpeedButton();
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cycleGameSpeed();
            }
        });
        return button;
    }

    /** Applies the current Game Speed multiplier to the on-screen label. */
    private void refreshSpeedButton() {
        if (speedLabel == null)
            return;
        int speed = currentGameSpeed();
        speedLabel.setText(speed + "x");
    }

    /** Reads the logged-in user's clamped Game Speed setting (1/2/3). */
    private int currentGameSpeed() {
        com.pvz.models.user.User user = AppContext.getInstance().getCurrentUser();
        int speed = 1;
        if (user != null && user.getSetting() != null) {
            speed = user.getSetting().getGameSpeed();
        }
        return Math.max(1, Math.min(3, speed));
    }

    /** Advances 1x -> 2x -> 3x -> 1x and persists the new speed to the save. */
    private void cycleGameSpeed() {
        int next = currentGameSpeed() + 1;
        if (next > 3) {
            next = 1;
        }
        com.pvz.models.user.User user = AppContext.getInstance().getCurrentUser();
        if (user != null && user.getSetting() != null) {
            user.getSetting().setGameSpeed(next);
            user.saveUser();
        }
        refreshSpeedButton();
    }

    private void buildObjectiveProgress() {
        objectiveGroup = new Group();
        objectiveGroup.setSize(OBJ_BAR_WIDTH + 46f, 46f);

        Drawable fillDrawable = safeSkinDrawable("image_ui_hud_ingame_progress_meter_fill",
                PvzSkin.get().newDrawable("white_pixel", Color.valueOf("65B83B")));
        objectiveFill = new Image(fillDrawable);
        objectiveFill.setBounds(0f, OBJ_TRACK_Y, 0f, OBJ_TRACK_HEIGHT);
        objectiveGroup.addActor(objectiveFill);

        Drawable frameDrawable = safeSkinDrawable("image_ui_hud_ingame_progress_meter",
                PvzSkin.get().newDrawable("white_pixel", new Color(0.15f, 0.15f, 0.15f, 0.85f)));
        objectiveFrame = new Image(frameDrawable);
        objectiveFrame.setScaling(Scaling.stretch);
        objectiveFrame.setBounds(0f, 0f, OBJ_BAR_WIDTH, OBJ_BAR_HEIGHT);
        objectiveGroup.addActor(objectiveFrame);

        objectiveTick = null;

        objectiveGroup.setVisible(false);
    }

    private void ensureObjectiveTick() {
        if (objectiveTick == null) {
            objectiveTick = new PamClipActor(CHECK_MARK_PAM, CHECK_MARK_CLIP, 0.55f, 0f, true);
            objectiveTick.setBounds(OBJ_BAR_WIDTH + 4f, 0f, 40f, 40f);
            objectiveGroup.addActor(objectiveTick);
        }
        objectiveTick.setVisible(true);
        objectiveTick.toFront();
    }

    private void updateObjectiveProgress(com.pvz.models.games.modes.variants.TimedWarMode timedWar) {
        objectiveGroup.setVisible(true);

        boolean complete = timedWar.isObjectiveComplete();
        float progress = timedWar.getObjectiveProgress();

        if (complete && !lastObjectiveComplete) {
            ensureObjectiveTick();
            lastObjectiveComplete = true;
        }

        if (progress != lastObjectiveProgress) {
            lastObjectiveProgress = progress;
            float fillWidth = OBJ_BAR_WIDTH * MathUtils.clamp(progress, 0f, 1f);
            objectiveFill.setBounds(0f, OBJ_TRACK_Y, fillWidth, OBJ_TRACK_HEIGHT);
        }
    }

    private void buildWaveProgress() {
        waveGroup = new Group();
        waveGroup.setSize(WAVE_BAR_WIDTH + 70f, 85f);

        Drawable fillDrawable = safeSkinDrawable("image_ui_hud_ingame_progress_meter_fill",
                PvzSkin.get().newDrawable("white_pixel", Color.valueOf("65B83B")));
        waveFill = new Image(fillDrawable);
        waveFill.setBounds(WAVE_BAR_WIDTH, WAVE_FILL_Y, 0f, WAVE_FILL_HEIGHT);
        waveGroup.addActor(waveFill);

        Drawable frameDrawable = safeSkinDrawable("image_ui_hud_ingame_progress_meter",
                PvzSkin.get().newDrawable("white_pixel", new Color(0.15f, 0.15f, 0.15f, 0.85f)));
        Image progressFrame = new Image(frameDrawable);
        progressFrame.setScaling(Scaling.stretch);
        progressFrame.setBounds(0f, 0f, WAVE_BAR_WIDTH, WAVE_BAR_HEIGHT);
        waveGroup.addActor(progressFrame);

        Drawable zombieDrawable = null;
        com.badlogic.gdx.graphics.g2d.TextureRegion zombieRegion = PvZ2.textureBank
                .region("IMAGE_UI_HUD_INGAME_PROGRESS_METER_ZOMBIEHEAD");
        if (zombieRegion != null) {
            zombieDrawable = new TextureRegionDrawable(zombieRegion);
        } else if (Gdx.files.internal("textures/ui/zombie.png").exists()) {
            Texture zombieTex = new Texture(Gdx.files.internal("textures/ui/zombie.png"));
            zombieTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            zombieDrawable = new TextureRegionDrawable(zombieTex);
        }
        waveZombieHead = zombieDrawable != null
                ? new Image(zombieDrawable)
                : new Image(PvzSkin.get().newDrawable("white_pixel", new Color(0.8f, 0.2f, 0.2f, 1f)));
        waveZombieHead.setScaling(Scaling.fit);
        waveZombieHead.setBounds(TRACK_RIGHT - 12f, -7f, 52f, 52f);
        waveGroup.addActor(waveZombieHead);

        waveGroup.setVisible(false);
    }

    private Drawable safeSkinDrawable(String name, Drawable fallback) {
        try {
            return PvzSkin.get().getDrawable(name);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void rebuildWaveFlags(int totalWaves) {
        if (waveFlagImages != null) {
            for (Image flag : waveFlagImages) {
                if (flag != null)
                    flag.remove();
            }
        }
        waveFlagImages = new Image[totalWaves];
        float trackWidth = TRACK_RIGHT - TRACK_LEFT;
        for (int i = 0; i < totalWaves; i++) {
            float fraction = (i + 1f) / totalWaves;
            float poleX = TRACK_RIGHT - trackWidth * fraction;

            Drawable poleDrawable = PvzSkin.get().getDrawable("image_ui_hud_ingame_progress_meter_flag_pole");
            if (poleDrawable == null) {
                poleDrawable = PvzSkin.get().newDrawable("white_pixel", new Color(0.55f, 0.4f, 0.2f, 1f));
            }
            Image pole = new Image(poleDrawable);
            pole.setScaling(Scaling.stretch);
            pole.setBounds(poleX - FLAG_POLE_WIDTH / 2f, 0f, FLAG_POLE_WIDTH, FLAG_POLE_HEIGHT);
            waveGroup.addActor(pole);

            Drawable flagDrawable = PvzSkin.get().getDrawable("image_ui_hud_ingame_progress_meter_flag_default");
            if (flagDrawable == null) {
                flagDrawable = PvzSkin.get().newDrawable("white_pixel", Color.RED);
            }
            Image flag = new Image(flagDrawable);
            flag.setScaling(Scaling.fit);
            flag.setBounds(poleX - FLAG_WIDTH + FLAG_X_OFFSET, FLAG_DOWN_Y, FLAG_WIDTH, FLAG_HEIGHT);
            waveGroup.addActor(flag);
            waveFlagImages[i] = flag;
        }
        lastCompletedWaves = -1;
    }

    private void updateWaveFlags(int completedWaves, int totalWaves) {
        if (waveFlagImages == null || waveFlagImages.length != totalWaves) {
            rebuildWaveFlags(totalWaves);
        }
        completedWaves = MathUtils.clamp(completedWaves, 0, totalWaves);
        if (completedWaves == lastCompletedWaves)
            return;
        for (int i = 0; i < waveFlagImages.length; i++) {
            waveFlagImages[i].setVisible(i >= completedWaves);
        }
        lastCompletedWaves = completedWaves;
    }

    private void updateWaveFill(float progress, int completedWaves, int totalWaves) {
        progress = MathUtils.clamp(progress, 0f, 1f);
        float trackWidth = TRACK_RIGHT - TRACK_LEFT;
        float fillWidth = trackWidth * progress;
        float progressX = TRACK_RIGHT - fillWidth;

        if (completedWaves > 0 && totalWaves > 0) {
            float flagX = TRACK_RIGHT - trackWidth * (completedWaves / (float) totalWaves);
            float minX = flagX - 12f;
            float headX = progressX - 12f;
            if (headX > minX) {
                progressX = minX + 12f;
            }
        }

        waveFill.setBounds(progressX, WAVE_FILL_Y, fillWidth, WAVE_FILL_HEIGHT);
        waveZombieHead.setPosition(progressX - 12f, waveZombieHead.getY());
    }

    private boolean isDebugModeEnabled() {
        User user = AppContext.getInstance().getCurrentUser();
        return user != null && user.getSetting() != null && user.getSetting().isDebugMode();
    }

    public void setSelectedCard(PlantCard card) {
        this.selectedCard = card;
        if (card != null) {
            this.shovelSelected = false;
            if (shovelButton != null)
                shovelButton.setChecked(false);
            this.plantFoodSelected = false;
        }
        updateCardStyles();
    }

    public void setOnShovelRequested(Runnable onShovelRequested) {
        this.onShovelRequested = onShovelRequested;
    }

    public boolean isPlantFoodSelected() {
        return plantFoodSelected;
    }

    public void setPlantFoodSelected(boolean selected) {
        this.plantFoodSelected = selected;
        if (selected) {
            this.selectedCard = null;
            this.selectedZombieCard = null;
            this.shovelSelected = false;
            if (shovelButton != null)
                shovelButton.setChecked(false);
            updateCardStyles();
        }
    }

    public void setOnPlantFoodRequested(Runnable onPlantFoodRequested) {
        this.onPlantFoodRequested = onPlantFoodRequested;
    }

    public boolean isShovelSelected() {
        return shovelSelected;
    }

    public void setShovelSelected(boolean selected) {
        this.shovelSelected = selected;
        if (shovelButton != null) {
            shovelButton.setChecked(selected);
        }
        if (selected) {
            this.selectedCard = null;
            this.selectedZombieCard = null;
            updateCardStyles();
        }
    }

    /**
     * Loads the static plant artwork/label data shared by every card slot.
     * Exposed as a separate hook so subclasses (e.g. the conveyor-belt HUD)
     * can reuse it while managing their own tray layout.
     */
    protected void loadPlantData() {
        List<PlantData> allData = PlantData.loadAll();
        dataByType = new HashMap<>();
        for (PlantData data : allData) {
            dataByType.put(data.type, data);
        }
    }

    /**
     * Builds the per-mode seed/card tray. The base {@link GameUiModal} has no
     * tray of its own; each mode's HUD (e.g. {@link NormalUiModal}'s static
     * tray, {@link ConveyorBeltUiModal}'s belt) implements this with its own
     * layout. Called once per game session from {@code GameController}.
     */
    public void initCards() {
    }

    /**
     * Refreshes the per-mode tray each frame. The base has no tray, so this is
     * a no-op hook that tray-bearing subclasses override.
     */
    public void syncCards() {
    }

    protected Table buildSlot(PlantCard pc, boolean isConveyor) {
        PlantType type = pc.getPlant().getType();
        PlantData data = dataByType.get(type);

        Skin skin = PvzSkin.get();
        Drawable fallback = skin.newDrawable("white_pixel", new Color(0.25f, 0.25f, 0.25f, 1f));

        Table slot = new Table();
        if (data != null) {
            slot.setBackground(PlantData.regionDrawableOr(
                    data.isBoosted() ? "IMAGE_UI_PACKETS_BOOST" : "IMAGE_UI_PACKETS_READY", fallback));
        } else {
            slot.setBackground(new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0f, 0f, 0f, 0.45f))));
        }

        Stack stack = new Stack();

        if (data != null) {
            Image plantImage = new Image(PlantData.regionDrawableOr(data.cardImageId(), fallback));
            plantImage.setScaling(Scaling.fit);
            Table plantLayer = new Table();
            plantLayer.center();
            plantLayer.add(plantImage).size(SLOT_HEIGHT - 8f);
            stack.add(plantLayer);

            Image badge = new Image(PlantData.regionDrawableOr(data.familyImageId(), fallback));
            Table badgeLayer = new Table();
            badgeLayer.top().left();
            badgeLayer.add(badge).size(18f).pad(2f);
            stack.add(badgeLayer);
        }

        Image cooldownOverlay = new Image(
                new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0f, 0f, 0f, 0.6f))));
        cooldownOverlay.setTouchable(Touchable.disabled);
        stack.add(cooldownOverlay);
        cooldownOverlayByCard.put(pc, cooldownOverlay);

        Label costLabel = new Label(isConveyor ? "FREE" : String.valueOf(pc.getCost()), skin, "medium");
        costLabel.setFontScale(0.9f);
        costLabel.setColor(Color.WHITE);
        Table costRow = new Table();
        costRow.bottom().left();
        costRow.add(costLabel).pad(1f, 4f, 1f, 4f);
        stack.add(costRow);

        slot.add(stack).grow();
        slot.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!pc.canUse()) {
                    return;
                }
                if (selectedCard == pc) {
                    selectedCard = null;
                } else {
                    selectedCard = pc;
                    selectedZombieCard = null;
                }
                updateCardStyles();
            }
        });

        slotByCard.put(pc, slot);
        return slot;
    }

    protected Table buildZombieSlot(ZombieCard zc) {
        Skin skin = PvzSkin.get();

        Table slot = new Table();
        com.badlogic.gdx.graphics.g2d.TextureRegion bgRegion = PvZ2.textureBank
                .region("IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY");
        if (bgRegion != null) {
            slot.setBackground(new TextureRegionDrawable(bgRegion));
        } else {
            slot.setBackground(new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0.4f, 0.1f, 0.1f, 0.85f))));
        }

        Stack stack = new Stack();

        String artPath = "textures/zombies/" + zc.getZombieType().name() + ".png";
        if (com.badlogic.gdx.Gdx.files.internal(artPath).exists()) {
            com.badlogic.gdx.graphics.Texture tex = new com.badlogic.gdx.graphics.Texture(
                    com.badlogic.gdx.Gdx.files.internal(artPath));
            tex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
                    com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
            Image zombieImg = new Image(new TextureRegionDrawable(tex));
            zombieImg.setScaling(Scaling.fit);
            Table imgLayer = new Table();
            imgLayer.center();
            imgLayer.add(zombieImg).size(SLOT_HEIGHT - 8f);
            stack.add(imgLayer);
        } else {
            String zombieName = zc.getZombieType().name().replace("_", " ");
            Label nameLabel = new Label(zombieName, skin, "medium");
            nameLabel.setFontScale(0.65f);
            nameLabel.setColor(Color.WHITE);
            nameLabel.setWrap(true);
            nameLabel.setAlignment(Align.center);
            Table nameLayer = new Table();
            nameLayer.center();
            nameLayer.add(nameLabel).size(SLOT_HEIGHT - 10f);
            stack.add(nameLayer);
        }

        Image cooldownOverlay = new Image(
                new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0f, 0f, 0f, 0.6f))));
        cooldownOverlay.setTouchable(Touchable.disabled);
        stack.add(cooldownOverlay);
        zombieCooldownOverlayByCard.put(zc, cooldownOverlay);

        Label costLabel = new Label(String.valueOf(zc.getCost()), skin, "medium");
        costLabel.setFontScale(0.9f);
        costLabel.setColor(Color.WHITE);
        Table costRow = new Table();
        costRow.bottom().left();
        costRow.add(costLabel).pad(1f, 4f, 1f, 4f);
        stack.add(costRow);

        slot.add(stack).grow();
        slot.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!zc.canUse()) {
                    return;
                }
                if (selectedZombieCard == zc) {
                    selectedZombieCard = null;
                } else {
                    selectedCard = null;
                    selectedZombieCard = zc;
                }
                updateCardStyles();
            }
        });

        zombieSlotByCard.put(zc, slot);
        return slot;
    }

    public void updateHud() {
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null)
            return;

        syncCards();
        refreshSpeedButton();

        // The + buttons only work while Debug Mode is enabled in settings.
        boolean debug = isDebugModeEnabled();
        addSunButton.setTouchable(debug ? Touchable.enabled : Touchable.disabled);
        addFoodButton.setTouchable(debug ? Touchable.enabled : Touchable.disabled);
        addSunButton.setColor(debug ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 0.6f));
        addFoodButton.setColor(debug ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 0.6f));

        if (context.getMode() instanceof com.pvz.models.games.modes.variants.IZombieMode izMode) {
            com.pvz.models.MatchSession ms = com.pvz.models.AppContext.getInstance().getMatchSession();
            boolean isZombieSide = ms != null && ms.getMyRole() == com.pvz.network.PlayerRole.ZOMBIE;
            if (isZombieSide) {
                sunLabel.setText(String.valueOf(izMode.getZombieSun()));
            } else {
                sunLabel.setText(String.valueOf(context.getCurrentSun()));
            }
        } else {
            sunLabel.setText(String.valueOf(context.getCurrentSun()));
        }

        int foodCount = MathUtils.clamp(context.getPlantFoodCount(), 0, MAX_PLANT_FOOD);
        for (int i = 0; i < plantFoodDots.length; i++) {
            if (i < foodCount) {
                plantFoodDots[i].setColor(Color.valueOf("65D44B"));
            } else {
                plantFoodDots[i].setColor(new Color(0.20f, 0.30f, 0.20f, 0.30f));
            }
        }

        User user = AppContext.getInstance().getCurrentUser();
        if (user != null && user.getProfile() != null) {
            coinLabel.setText(String.valueOf(user.getProfile().getCoins()));
            gemLabel.setText(String.valueOf(user.getProfile().getDiamonds()));
        }

        for (Map.Entry<PlantCard, Image> entry : cooldownOverlayByCard.entrySet()) {
            PlantCard pc = entry.getKey();
            Image overlay = entry.getValue();
            float base = pc.getBaseCooldown();
            float fraction = base > 0 ? pc.getCooldown() / base : 0f;
            overlay.setVisible(fraction > 0f);
            overlay.setHeight(SLOT_HEIGHT * fraction);
        }

        for (Map.Entry<ZombieCard, Image> entry : zombieCooldownOverlayByCard.entrySet()) {
            ZombieCard zc = entry.getKey();
            Image overlay = entry.getValue();
            float base = zc.getBaseCooldown();
            float fraction = base > 0 ? zc.getCooldown() / base : 0f;
            overlay.setVisible(fraction > 0f);
            overlay.setHeight(SLOT_HEIGHT * fraction);
        }

        com.pvz.models.games.modes.GameMode mode = context.getMode();
        if (mode != null && mode.hasProgressBar()) {
            waveGroup.setVisible(true);
            int totalWaves = mode.getTotalWaves();
            int totalZombies = mode.getTotalZombieCount();
            int killed = context.getGameStats().getZombiesKilled();

            int completed;
            if (context.isGameOver()) {
                completed = totalWaves;
            } else {
                completed = MathUtils.clamp(mode.getCurrentWaveIndex(), 0, totalWaves);
            }
            updateWaveFlags(completed, totalWaves);

            float killProgress = totalZombies > 0
                    ? MathUtils.clamp(killed / (float) totalZombies, 0f, 1f)
                    : 0f;
            float waveProgress = totalWaves > 0
                    ? completed / (float) totalWaves
                    : 0f;
            float progress = Math.max(killProgress, waveProgress);
            updateWaveFill(progress, completed, totalWaves);
        } else {
            waveGroup.setVisible(false);
        }

        if (mode instanceof com.pvz.models.games.modes.variants.TimedWarMode timedWar) {
            updateObjectiveProgress(timedWar);
        } else {
            objectiveGroup.setVisible(false);
        }

        if (mode instanceof com.pvz.models.games.modes.variants.TimedWarMode timedWar) {
            updateObjectiveProgress(timedWar);
        } else {
            objectiveGroup.setVisible(false);
        }

        float survivalSeconds = -1f;
        if (context.getMode() instanceof com.pvz.models.games.modes.variants.IZombieMode izMode) {
            survivalSeconds = izMode.getPlantSurvivalSecondsRemaining();
        } else if (context.getMode() instanceof com.pvz.models.games.modes.variants.IZombieLocalMode izLocalMode) {
            survivalSeconds = izLocalMode.getPlantSurvivalSecondsRemaining();
        }

        if (survivalSeconds >= 0f) {
            timerBank.setVisible(true);
            float seconds = Math.max(0f, survivalSeconds);
            int mins = (int) (seconds / 60);
            int secs = (int) (seconds % 60);
            timerLabel.setText(String.format("%d:%02d", mins, secs));
            if (seconds <= 15f) {
                timerLabel.setColor(Color.RED);
            } else {
                timerLabel.setColor(Color.WHITE);
            }
        } else {
            timerBank.setVisible(false);
        }
    }

    protected void updateCardStyles() {
        Skin skin = PvzSkin.get();
        Drawable boostBg = skin.newDrawable("white_pixel", new Color(0.25f, 0.25f, 0.25f, 1f));
        for (Map.Entry<PlantCard, Table> entry : slotByCard.entrySet()) {
            PlantCard pc = entry.getKey();
            Table slot = entry.getValue();
            com.pvz.view.PlantData dataLookup = dataByType.get(pc.getPlant().getType());
            if (dataLookup != null) {
                slot.setBackground(PlantData.regionDrawableOr(
                        dataLookup.isBoosted() ? "IMAGE_UI_PACKETS_BOOST" : "IMAGE_UI_PACKETS_READY", boostBg));
            }
            if (selectedCard == pc) {
                slot.setColor(0.8f, 0.5f, 1f, 1f);
            } else {
                slot.setColor(1f, 1f, 1f, 1f);
            }
        }
        for (Map.Entry<ZombieCard, Table> entry : zombieSlotByCard.entrySet()) {
            ZombieCard zc = entry.getKey();
            Table slot = entry.getValue();
            if (selectedZombieCard == zc) {
                slot.setColor(0.8f, 0.5f, 1f, 1f);
            } else {
                slot.setColor(1f, 1f, 1f, 1f);
            }
        }
    }
}
