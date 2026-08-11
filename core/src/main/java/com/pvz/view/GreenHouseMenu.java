package com.pvz.view;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pvz.PvZ2;
import com.pvz.controller.AudioManager;
import com.pvz.controller.GreenHouseController;
import com.pvz.enums.AudioPaths;
import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.config.PlantJsonConfig;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.greenhouse.GreenHousePlant;
import com.pvz.models.greenhouse.GreenHousePot;
import com.pvz.models.user.Profile;
import pvz.libpvz.pam.ClipRef;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

/**
 * Greenhouse screen — grid of pots you can plant and harvest, styled to match Dani's
 * Greenhouse screenshot: back button + title + "x/y pots unlocked" + currency on top,
 * a grid of pot cells below (locked / empty / growing with a live countdown / ready to
 * harvest). The Collection button and the "Visit Shop" button aren't wired in yet —
 * left out on purpose per your last message, easy to add back later (see the README).
 * <p>
 * Optional art (everything below gracefully falls back if missing — check the console
 * for a "GreenHouseMenu" log line telling you exactly which path it looked for):
 * textures/backgrounds/Greenhouse.png,
 * textures/greenhouse/pot_empty.png (also used for locked pots, with a lock icon on top,
 * until you add a dedicated pot_locked.png), textures/greenhouse/pot_growing.png,
 * textures/greenhouse/pot_ready.png, textures/greenhouse/lock_icon.png,
 * textures/greenhouse/plants/marigold.png, textures/greenhouse/plants/&lt;PlantType&gt;.png
 * (PlantType exactly as in the enum, e.g. "Peashooter" -> textures/greenhouse/plants/Peashooter.png)
 */
public class GreenHouseMenu extends ScreenAdapter {
    private static final String BG_PATH = "textures/backgrounds/Greenhouse.png";
    private static final String POT_LOCKED = "textures/greenhouse/pot_locked.png";
    private static final String POT_EMPTY = "textures/greenhouse/pot_empty.png";
    private static final String POT_GROWING = "textures/greenhouse/pot_growing.png";
    private static final String POT_READY = "textures/greenhouse/pot_ready.png";
    private static final String LOCK_ICON = "textures/greenhouse/lock_icon.png";
    private static final String PLANTS_DIR = "textures/greenhouse/plants/";

    private static final float CELL_SIZE = 155f;
    private static final float CELL_PAD = 40f;
    private static final float GRID_VIEWPORT_HEIGHT = 950f;
    /** Whole pot grid offset from the top of its area. Increase to push the grid DOWN, decrease to move it UP. */
    private static final float GRID_UP_OFFSET = -63f;
    /** Row 2 is lifted this many px (via a smaller top pad) so it sits a bit higher on the background. */
    private static final float ROW_2_LIFT = 38f;
    /** Row 3 is lifted a bit more than row 2 (this much extra on top of {@link #ROW_2_LIFT}). */
    private static final float ROW_3_EXTRA_LIFT = 18f;
    /** How many px the plant art's bottom sits above the bottom of its pot cell. Raise it to
     *  make the plant stick further out of the pot, lower it to tuck the plant back in. */
    private static final float PLANT_ART_LIFT = 75f;

    /** How big the plant's animation area is inside the pot, as a fraction of {@link #CELL_SIZE}.
     *  Raise it to make the plant bigger in the pot, lower it to shrink it. */
    private static final float PLANT_ART_SIZE_FRACTION = 0.9f;
    /** Extra px the plant is shifted UP from its normal position inside the pot. Positive = up,
     *  negative = down. */
    private static final float PLANT_ART_SHIFT_Y = -10f;
    /** Extra px the plant is shifted RIGHT from its normal position inside the pot. Positive = right,
     *  negative = left. */
    private static final float PLANT_ART_SHIFT_X = 0f;

    /** Grow-now dialog frame: total width, and the padding between the content and the
     *  decorative border so the buttons/text sit comfortably inside the cadre. */
    private static final float GROW_DIALOG_WIDTH = 820f;
    private static final float GROW_DIALOG_PAD_TOP = 48f;
    private static final float GROW_DIALOG_PAD_BOTTOM = 42f;
    private static final float GROW_DIALOG_PAD_SIDE = 56f;

    private final PvZ2 game;
    private final GreenHouseController controller;
    private Stage stage;
    private Skin skin;

    /** Growing pots whose countdown label needs refreshing every frame. */
    private final List<TimerBinding> timerBindings = new ArrayList<>();

    private Table growModal;
    private Label growPlantLabel;
    private Label growCostLabel;
    private Label growHintLabel;
    private int growTargetX;
    private int growTargetY;

    public GreenHouseMenu(PvZ2 game) {
        this.game = game;
        this.controller = new GreenHouseController();
    }

    @Override
    public void show() {
        Viewport viewport = new FitViewport(1920, 1080);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        skin = PvzSkin.get();
        rebuild();
        AudioManager.getInstance().playMusic(AudioPaths.GREEN_HOUSE,true,AudioManager.getInstance().getUserMusicVolume());
    }

    /** Rebuilds the whole screen from the current model state — called after every plant/collect action. */
    private void rebuild() {
        stage.clear();
        timerBindings.clear();

        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        stack.add(fittedImage(BG_PATH, new Color(0.08f, 0.16f, 0.1f, 1f), Scaling.fill));

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stack.add(rootTable);

        rootTable.add(buildTopBar()).fillX().top().padTop(30).padLeft(40).padRight(40).row();
        rootTable.add(buildTitle()).padTop(20).row();
        rootTable.add(buildPotGrid()).expand().top().padTop(GRID_UP_OFFSET).row();

        stack.add(buildGrowModal());
    }

    private Table buildTitle() {
        GreenHouse greenHouse = controller.getGreenHouse();

        Table titleBlock = new Table();
        Label title = new Label("GreenHouse", skin, "big_outline");
        title.setFontScale(1.6f);
        title.setColor(Color.WHITE);
        titleBlock.add(title).row();

        Label subtitle = new Label(greenHouse.getUnlockedPotCount() + "/" + (GreenHouse.WIDTH * GreenHouse.HEIGHT)
            + " pots unlocked", skin, "medium");
        subtitle.setFontScale(1.0f);
        subtitle.setColor(new Color(0.6f, 1f, 0.65f, 1f));
        titleBlock.add(subtitle).padTop(6);

        return titleBlock;
    }

    private Table buildTopBar() {
        Table topBar = new Table();

        Table topLeft = new Table();
        topLeft.add(MenuUiKit.backButton(
            MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)),
            () -> game.setScreen(new GameModesMenu(game))
        )).size(70).padRight(24);

        Table topRight = new Table();
        Profile profile = currentProfile();
        int coins = profile != null ? profile.getCoins() : 0;
        int diamonds = profile != null ? profile.getDiamonds() : 0;
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/coin_icon.png",
            new Color(0.95f, 0.78f, 0.15f, 1f), String.valueOf(coins), 195, 63)).padRight(15);
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/diamond_icon.png",
            new Color(0.35f, 0.75f, 0.95f, 1f), String.valueOf(diamonds), 195, 63));

        topBar.add(topLeft).left().expandX().top().padTop(40);
        topBar.add(topRight).right().top().padTop(40).padRight(20);
        return topBar;
    }

    private Table buildPotGrid() {
        GreenHouse greenHouse = controller.getGreenHouse();

        Table grid = new Table();
        for (int y = 1; y <= GreenHouse.HEIGHT; y++) {
            float topPad = CELL_PAD;
            if (y >= 3) {
                topPad = CELL_PAD - ROW_2_LIFT - ROW_3_EXTRA_LIFT;
            } else if (y == 2) {
                topPad = CELL_PAD - ROW_2_LIFT;
            }
            for (int x = 1; x <= GreenHouse.WIDTH; x++) {
                GreenHousePot pot = greenHouse.getPot(x, y);
                grid.add(buildPotCell(pot)).size(CELL_SIZE, CELL_SIZE + 34)
                    .padTop(topPad).padBottom(CELL_PAD).padLeft(CELL_PAD).padRight(CELL_PAD);
            }
            grid.row();
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFlickScroll(true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, true);

        Table viewport = new Table();
        viewport.add(scrollPane).width((CELL_SIZE + CELL_PAD * 2) * GreenHouse.WIDTH).height(GRID_VIEWPORT_HEIGHT);
        return viewport;
    }

    private Table buildPotCell(GreenHousePot pot) {
        Table cell = new Table();

        Stack potStack = new Stack();

        boolean locked = pot.isLocked();
        boolean empty = !locked && pot.isEmpty();
        GreenHousePlant plant = (locked || empty) ? null : pot.getPlant();
        boolean ready = plant != null && plant.isReady();

        String potArt;
        Color fallbackTint;
        if (locked) {
            // You don't have a dedicated pot_locked.png yet, so locked pots reuse the
            // normal empty-pot art with a lock icon on top (same as Dani's does) instead
            // of falling back to a flat gray panel. If you add a real pot_locked.png later
            // it'll be picked up automatically — no code change needed.
            potArt = Gdx.files.internal(POT_LOCKED).exists() ? POT_LOCKED : POT_EMPTY;
            fallbackTint = new Color(0.42f, 0.3f, 0.18f, 0.9f);
        } else if (empty) {
            potArt = POT_EMPTY;
            fallbackTint = new Color(0.42f, 0.3f, 0.18f, 0.9f);
        } else if (ready) {
            potArt = POT_READY;
            fallbackTint = new Color(0.78f, 0.62f, 0.14f, 0.95f);
        } else {
            potArt = POT_GROWING;
            fallbackTint = new Color(0.32f, 0.45f, 0.22f, 0.9f);
        }

        // Scaling.fit (not fill) so the art always stays inside its own cell — no bleed
        // into the row below regardless of the PNG's actual aspect ratio. The pot art is
        // bottom-aligned so every pot's base/plank sits on the same line even when the
        // individual pot PNGs have different internal (vertical) padding. No backing tint
        // panel behind it: your pot PNGs are already transparent outside the pot shape,
        // so a backing color would just show through as an ugly square behind the pot.
        potStack.add(fittedImage(potArt, fallbackTint, Scaling.fit, Align.bottom));

        if (locked) {
            potStack.add(centeredIcon(LOCK_ICON, null, 0.5f));
        } else if (plant != null) {
            potStack.add(plantArtActor(plant));
        }

        cell.add(potStack).size(CELL_SIZE, CELL_SIZE).row();

        // status text under the pot
        if (locked) {
            Label lockedLabel = new Label("Locked", skin);
            lockedLabel.setFontScale(1.05f);
            lockedLabel.setColor(new Color(1f, 0.55f, 0.55f, 1f));
            cell.add(lockedLabel).padTop(6);
        } else if (empty) {
            Label emptyLabel = new Label("Tap to plant", skin);
            emptyLabel.setFontScale(1f);
            emptyLabel.setColor(0.75f, 0.85f, 0.75f, 1f);
            cell.add(emptyLabel).padTop(6);
        } else {
            Table textCol = new Table();
            Label nameLabel = new Label(plant.isMariGold() ? "MariGold" : plant.getPlantType(), skin);
            nameLabel.setFontScale(1.05f);
            textCol.add(nameLabel).row();

            Label statusLabel = new Label(ready ? "Ready!" : formatRemaining(plant), skin);
            statusLabel.setFontScale(1f);
            statusLabel.setColor(ready ? new Color(0.6f, 1f, 0.65f, 1f) : new Color(1f, 0.9f, 0.55f, 1f));
            textCol.add(statusLabel);

            if (!ready) {
                timerBindings.add(new TimerBinding(statusLabel, plant));
            }
            cell.add(textCol).padTop(6);
        }

        final int x = pot.getX();
        final int y = pot.getY();
        cell.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float ex, float ey) {
                onPotTapped(x, y);
            }
        });

        return cell;
    }

    private void onPotTapped(int x, int y) {
        GreenHouse greenHouse = controller.getGreenHouse();
        GreenHousePot pot = greenHouse.getPot(x, y);
        if (pot == null || pot.isLocked()) return;

        if (pot.isEmpty()) {
            controller.plant(x, y);
            rebuild();
            return;
        }

        GreenHousePlant plant = pot.getPlant();
        if (plant.isReady()) {
            controller.collect(x, y);
            rebuild();
        } else {
            showGrowDialog(x, y);
        }
    }

    /** Full-screen overlay asking to spend diamonds to finish the growth instantly. */
    private Table buildGrowModal() {
        growModal = new Table();
        growModal.setFillParent(true);
        growModal.setVisible(false);
        growModal.setBackground(MenuUiKit.solidDrawable(new Color(0f, 0f, 0f, 0.55f)));
        growModal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Dismiss only when the dim backdrop itself (not a panel/button) is clicked.
                if (event.getTarget() == growModal) {
                    growModal.setVisible(false);
                }
            }
        });

        float contentW = GROW_DIALOG_WIDTH - GROW_DIALOG_PAD_SIDE * 2;

        BorderedTable panel = new BorderedTable();
        panel.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        panel.pad(GROW_DIALOG_PAD_TOP, GROW_DIALOG_PAD_SIDE, GROW_DIALOG_PAD_BOTTOM, GROW_DIALOG_PAD_SIDE);

        growPlantLabel = new Label("", skin, "big_outline");
        growPlantLabel.setFontScale(1.25f);
        growPlantLabel.setColor(new Color(0.6f, 0.42f, 0.06f, 1f));
        growPlantLabel.setWrap(true);
        growPlantLabel.setAlignment(Align.center);

        growCostLabel = new Label("", skin, "medium");
        growCostLabel.setFontScale(1.1f);
        growCostLabel.setColor(new Color(0.1f, 0.1f, 0.1f, 1f));
        growCostLabel.setWrap(true);
        growCostLabel.setAlignment(Align.center);

        growHintLabel = new Label("", skin, "medium");
        growHintLabel.setFontScale(1.05f);
        growHintLabel.setColor(new Color(0.72f, 0.08f, 0.03f, 1f));
        growHintLabel.setWrap(true);
        growHintLabel.setAlignment(Align.center);

        TextButton.TextButtonStyle buttonStyle = growDialogButtonStyle();

        TextButton growBtn = new TextButton("Grow Now", buttonStyle);
        growBtn.getLabel().setFontScale(1.25f);
        growBtn.getLabel().setColor(Color.WHITE);
        growBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (controller.growNow(growTargetX, growTargetY)) {
                    growModal.setVisible(false);
                    rebuild();
                } else {
                    growHintLabel.setText("Not enough diamonds!");
                }
            }
        });

        TextButton cancelBtn = new TextButton("Cancel", buttonStyle);
        cancelBtn.getLabel().setFontScale(1.25f);
        cancelBtn.getLabel().setColor(new Color(0.9f, 0.9f, 0.9f, 1f));
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                growModal.setVisible(false);
            }
        });

        Table buttonRow = new Table();
        buttonRow.add(growBtn).size(260, 84).padRight(20);
        buttonRow.add(cancelBtn).size(200, 84);

        panel.add(growPlantLabel).width(contentW).padBottom(16).row();
        panel.add(growCostLabel).width(contentW).padBottom(8).row();
        panel.add(growHintLabel).width(contentW).padBottom(30).row();
        panel.add(buttonRow).width(contentW).center();

        growModal.add(panel).width(GROW_DIALOG_WIDTH);
        return growModal;
    }

    private void showGrowDialog(int x, int y) {
        GreenHousePot pot = controller.getGreenHouse().getPot(x, y);
        if (pot == null || pot.isLocked() || pot.isEmpty()) return;

        GreenHousePlant plant = pot.getPlant();
        if (plant.isReady()) return;

        int cost = controller.growNowCost(x, y);
        if (cost < 0) return;

        growTargetX = x;
        growTargetY = y;
        growPlantLabel.setText((plant.isMariGold() ? "MariGold" : plant.getPlantType()) + " is still growing");
        growCostLabel.setText("Finish it now for " + cost + " diamond" + (cost == 1 ? "" : "s") + "?");
        growHintLabel.setText("");
        growModal.setVisible(true);
        growModal.toFront();
    }

    private TextButton.TextButtonStyle growDialogButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = skin.getDrawable("image_ui_generic_bluetab_down");
        style.over = skin.getDrawable("image_ui_generic_bluetab_down");
        style.checked = skin.getDrawable("image_ui_generic_bluetab_active");
        style.font = skin.getFont("FBUSV8C5EI_2");
        style.fontColor = new Color(0.2f, 0.2f, 0.2f, 1f);
        style.checkedFontColor = Color.WHITE;
        return style;
    }

    private String formatRemaining(GreenHousePlant plant) {
        LocalDateTime planted = LocalDateTime.parse(plant.getPlantedTime());
        LocalDateTime finish = planted.plusHours(plant.getGrowthHours());
        Duration remaining = Duration.between(LocalDateTime.now(), finish);
        if (remaining.isNegative()) remaining = Duration.ZERO;
        long totalSeconds = remaining.getSeconds();
        return String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
    }

    /** Full-size art (or a tinted fallback panel of the same size) with the given scaling mode. */
    private Image fittedImage(String path, Color fallbackTint, Scaling scaling) {
        return fittedImage(path, fallbackTint, scaling, Align.center);
    }

    /** Like {@link #fittedImage(String, Color, Scaling)} but pins the art to the given alignment
     *  inside its cell — e.g. {@link Align#bottom} for the pot art so every pot's base/plank
     *  sits on the same line regardless of each PNG's internal padding. */
    private Image fittedImage(String path, Color fallbackTint, Scaling scaling, int alignment) {
        boolean hasArt = path != null && !path.isEmpty() && Gdx.files.internal(path).exists();
        Image image;
        if (hasArt) {
            image = new Image(MenuUiKit.loadTextureSafe(path));
        } else {
            logIfMissing(path);
            image = new Image(MenuUiKit.solidDrawable(fallbackTint != null ? fallbackTint : new Color(0f, 0f, 0f, 0f)));
        }
        image.setScaling(scaling);
        image.setAlign(alignment);
        return image;
    }

    /** An icon (or tinted fallback) shrunk to a fraction of the cell and centered, aspect preserved. */
    private Actor centeredIcon(String path, Color fallbackTint, float sizeFraction) {
        Image image = fittedImage(path, fallbackTint, Scaling.fit);
        Table wrap = new Table();
        wrap.add(image).size(CELL_SIZE * sizeFraction).center();
        return wrap;
    }

    /** Like {@link #centeredIcon} but the icon's bottom edge is pinned {@code lift} px above the
     *  bottom of the cell (used for plant art so it rises out of the pot). */
    private Actor liftedIcon(String path, Color fallbackTint, float sizeFraction, float lift) {
        Image image = fittedImage(path, fallbackTint, Scaling.fit);
        Table wrap = new Table();
        wrap.add(image).size(CELL_SIZE * sizeFraction).expandY().bottom().padBottom(lift);
        return wrap;
    }

    /** The plant's in-pot display: its idle PAM animation when one exists, otherwise the static
     *  PNG. Both are laid out the same way as {@link #liftedIcon} so the plant rises out of the pot. */
    private Actor plantArtActor(GreenHousePlant plant) {
        String[] pam = resolvePlantPam(plant);
        if (pam == null) {
            String plantArt = PLANTS_DIR + (plant.isMariGold() ? "marigold" : plant.getPlantType()) + ".png";
            return liftedIcon(plantArt, null, PLANT_ART_SIZE_FRACTION, PLANT_ART_LIFT);
        }
        String pamPath = pam[0];
        String idleLabel = pam[1];
        String fallbackPng = PLANTS_DIR + (plant.isMariGold() ? "marigold" : plant.getPlantType()) + ".png";
        PlantPamActor actor = new PlantPamActor(pamPath, idleLabel, PLANT_ART_SHIFT_X, PLANT_ART_SHIFT_Y,
                Gdx.files.internal(fallbackPng).exists() ? MenuUiKit.loadTextureSafe(fallbackPng) : null);
        Table wrap = new Table();
        wrap.add(actor).size(CELL_SIZE * PLANT_ART_SIZE_FRACTION).expandY().bottom().padBottom(PLANT_ART_LIFT);
        return wrap;
    }

    /** Returns {pamFilePath, idleLabel} for the plant's idle PAM, or {@code null} when the plant
     *  has no configured animation. MariGold hard-codes to MARIGOLD.PAM; other plants use their
     *  {@code pamAnimationConfig} from plant_actions.json, falling back to a folder-style path
     *  guess (768/{INITIAL,FULL}/PLANT/<TYPE>) only when the PAM actually exists on disk. */
    private String[] resolvePlantPam(GreenHousePlant plant) {
        if (plant.isMariGold()) {
            return new String[]{"768/INITIAL/PLANT/MARIGOLD/MARIGOLD.PAM", "idle"};
        }
        String typeName = plant.getPlantType();
        if (typeName == null || typeName.isEmpty()) return null;

        PlantType type;
        try {
            type = PlantType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
        PlantJsonConfig config = PlantConfigRegistry.getInstance().getConfig(type);
        if (config != null && config.pamAnimationConfig != null && config.pamAnimationConfig.pamFilePath != null) {
            String label = config.pamAnimationConfig.idleLabel;
            return new String[]{config.pamAnimationConfig.pamFilePath, label != null ? label : "idle"};
        }

        String upper = typeName.toUpperCase();
        String[] candidates = {
            "768/INITIAL/PLANT/" + upper + "/" + upper + ".PAM",
            "768/FULL/PLANT/" + upper + "/" + upper + ".PAM",
        };
        for (String path : candidates) {
            if (Gdx.files.internal("assets/pvz-assets/IMAGES/" + path).exists()) {
                return new String[]{path, "idle"};
            }
        }
        return null;
    }

    /** Draws a plant's idle PAM animation inside the pot. Falls back to the static PNG (when given)
     *  until the animation has loaded. */
    private static final class PlantPamActor extends Actor {
        private final String pamPath;
        private final String idleLabel;
        private final float shiftX;
        private final float shiftY;
        private final Texture fallbackTexture;
        private float stateTime;

        PlantPamActor(String pamPath, String idleLabel, float shiftX, float shiftY, Texture fallbackTexture) {
            this.pamPath = pamPath;
            this.idleLabel = idleLabel;
            this.shiftX = shiftX;
            this.shiftY = shiftY;
            this.fallbackTexture = fallbackTexture;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            ClipRef clip = resolveClip();
            if (clip == null) {
                drawFallback(batch, parentAlpha);
                return;
            }
            Rectangle bounds = PvZ2.pamPlayer.bounds(pamPath, idleLabel);
            if (bounds == null) {
                bounds = PvZ2.pamPlayer.bounds(pamPath);
            }
            float target = Math.min(getWidth(), getHeight());
            float scale = 1f;
            if (bounds != null && bounds.width > 0 && bounds.height > 0) {
                scale = Math.min(target / bounds.width, target / bounds.height);
            } else {
                scale = target / 390f; // default PAM canvas is 390x390
            }
            float cx = getX() + getWidth() / 2f + shiftX;
            float cy = getY() + getHeight() / 2f + shiftY;
            PvZ2.pamPlayer.draw(batch, clip, stateTime, cx, cy, scale, scale, true);
        }

        private ClipRef resolveClip() {
            if (idleLabel != null) {
                ClipRef clip = PvZ2.pamPlayer.getClip(pamPath, idleLabel);
                if (clip != null) return clip;
            }
            for (String state : new String[]{"idle", "default", ""}) {
                ClipRef clip = PvZ2.pamPlayer.getClip(pamPath, state);
                if (clip != null) return clip;
            }
            return null;
        }

        private void drawFallback(Batch batch, float parentAlpha) {
            if (fallbackTexture == null) return;
            float w = getWidth(), h = getHeight();
            float imgW = fallbackTexture.getWidth(), imgH = fallbackTexture.getHeight();
            float fit = Math.min(w / imgW, h / imgH);
            float dw = imgW * fit, dh = imgH * fit;
            batch.setColor(1f, 1f, 1f, parentAlpha);
            batch.draw(fallbackTexture, getX() + (w - dw) / 2f, getY() + (h - dh) / 2f, dw, dh);
            batch.setColor(Color.WHITE);
        }
    }

    private void logIfMissing(String path) {
        if (path == null || path.isEmpty()) return;
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.log("GreenHouseMenu", "art not found, looked at internal path: \"" + path
                + "\" (resolved to " + Gdx.files.internal(path).file().getAbsolutePath() + ")");
        }
    }

    private Profile currentProfile() {
        if (AppContext.getInstance().getCurrentUser() == null) return null;
        return AppContext.getInstance().getCurrentUser().getProfile();
    }

    private static final class TimerBinding {
        final Label label;
        final GreenHousePlant plant;

        TimerBinding(Label label, GreenHousePlant plant) {
            this.label = label;
            this.plant = plant;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        for (TimerBinding binding : timerBindings) {
            binding.label.setText(formatRemaining(binding.plant));
        }

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
