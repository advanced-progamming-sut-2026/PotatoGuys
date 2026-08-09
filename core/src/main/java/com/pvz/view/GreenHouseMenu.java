package com.pvz.view;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.pvz.controller.GreenHouseController;
import com.pvz.models.AppContext;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.greenhouse.GreenHousePlant;
import com.pvz.models.greenhouse.GreenHousePot;
import com.pvz.models.user.Profile;
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

    private final PvZ2 game;
    private final GreenHouseController controller;
    private Stage stage;
    private Skin skin;

    /** Growing pots whose countdown label needs refreshing every frame. */
    private final List<TimerBinding> timerBindings = new ArrayList<>();

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
        rootTable.add(buildPotGrid()).expand().center().padTop(90).row();
    }

    private Table buildTopBar() {
        Table topBar = new Table();

        Table topLeft = new Table();
        topLeft.add(MenuUiKit.backButton(
            MenuUiKit.textureDrawable(game.getGlobalAssetManager().get(MenuUiKit.BACK_BUTTON_TEX)),
            () -> game.setScreen(new MainMenu(game))
        )).size(70).padRight(24);

        GreenHouse greenHouse = controller.getGreenHouse();
        Table titleBlock = new Table();
        Label title = new Label("Greenhouse", skin, "big");
        titleBlock.add(title).row();
        Label subtitle = new Label(greenHouse.getUnlockedPotCount() + "/" + (GreenHouse.WIDTH * GreenHouse.HEIGHT)
            + " pots unlocked", skin);
        subtitle.setFontScale(0.85f);
        subtitle.setColor(0.8f, 0.8f, 0.8f, 1f);
        titleBlock.add(subtitle).padTop(4);
        topLeft.add(titleBlock);

        Table topRight = new Table();
        Profile profile = currentProfile();
        int coins = profile != null ? profile.getCoins() : 0;
        int diamonds = profile != null ? profile.getDiamonds() : 0;
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/coin_icon.png",
            new Color(0.95f, 0.78f, 0.15f, 1f), String.valueOf(coins))).padRight(15);
        topRight.add(MenuUiKit.resourceWidget(skin, "textures/ui/diamond_icon.png",
            new Color(0.35f, 0.75f, 0.95f, 1f), String.valueOf(diamonds)));

        topBar.add(topLeft).left().expandX();
        topBar.add(topRight).right();
        return topBar;
    }

    private Table buildPotGrid() {
        GreenHouse greenHouse = controller.getGreenHouse();

        Table grid = new Table();
        for (int y = 1; y <= GreenHouse.HEIGHT; y++) {
            for (int x = 1; x <= GreenHouse.WIDTH; x++) {
                GreenHousePot pot = greenHouse.getPot(x, y);
                grid.add(buildPotCell(pot)).size(CELL_SIZE, CELL_SIZE + 34).pad(CELL_PAD);
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
        // into the row below regardless of the PNG's actual aspect ratio. No backing tint
        // panel behind it: your pot PNGs are already transparent outside the pot shape,
        // so a backing color would just show through as an ugly square behind the pot.
        potStack.add(fittedImage(potArt, fallbackTint, Scaling.fit));

        if (locked) {
            potStack.add(centeredIcon(LOCK_ICON, null, 0.5f));
        } else if (plant != null) {
            String plantArt = PLANTS_DIR + (plant.isMariGold() ? "marigold" : plant.getPlantType()) + ".png";
            potStack.add(centeredIcon(plantArt, null, 0.7f));
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
        }
        // still growing: tapping does nothing for now (see GreenHouseController.growNow
        // if you want to add an instant-grow-with-diamonds button later)
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
        boolean hasArt = path != null && !path.isEmpty() && Gdx.files.internal(path).exists();
        Image image;
        if (hasArt) {
            image = new Image(MenuUiKit.loadTextureSafe(path));
        } else {
            logIfMissing(path);
            image = new Image(MenuUiKit.solidDrawable(fallbackTint != null ? fallbackTint : new Color(0f, 0f, 0f, 0f)));
        }
        image.setScaling(scaling);
        image.setAlign(Align.center);
        return image;
    }

    /** An icon (or tinted fallback) shrunk to a fraction of the cell and centered, aspect preserved. */
    private Actor centeredIcon(String path, Color fallbackTint, float sizeFraction) {
        Image image = fittedImage(path, fallbackTint, Scaling.fit);
        Table wrap = new Table();
        wrap.add(image).size(CELL_SIZE * sizeFraction).center();
        return wrap;
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
