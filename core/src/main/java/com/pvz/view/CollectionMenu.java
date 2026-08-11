package com.pvz.view;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pvz.PvZ2;
import com.pvz.controller.CollectionController;
import com.pvz.enums.GameAsset;

import pvz.skin.PvzSkin;

/**
 * Rey-style Collection screen: a dimmed backdrop, a bordered panel holding the Plants / Zombies
 * tabs, a close button and a grid of collectible plant cards. Only the Plants tab is populated;
 * the Zombies tab is a placeholder until zombie data lands. Opening a card (details) is the only
 * interaction the player has inside.
 */
public class CollectionMenu extends ScreenAdapter {

    private static final int COLS = 8;
    private static final float PANEL_WIDTH = 1520f;
    private static final float PANEL_HEIGHT = 780f;
    private static final float PANEL_X = (1920f - PANEL_WIDTH) / 2f;
    private static final float TAB_W = 128f;
    private static final float TAB_H = 96f;
    private static final float FACE_W = 68f;
    private static final float FACE_H = 68f;
    private static final Color PAGE_BG = new Color(0x1A0E06A0);
    private static final Color PANEL_BG = new Color(0x6B4226FF);

    private enum Tab {
        PLANTS("IMAGE_UI_ALMANAC_TABS_PLANTS_ACTIVE", "IMAGE_UI_ALMANAC_TABS_PLANTS_DOWN"),
        ZOMBIES("IMAGE_UI_ALMANAC_TABS_ZOMBIES_ACTIVE", "IMAGE_UI_ALMANAC_TABS_ZOMBIES_DOWN");

        final String activeId;
        final String downId;

        Tab(String activeId, String downId) {
            this.activeId = activeId;
            this.downId = downId;
        }
    }

    private final PvZ2 game;
    private final Screen previous;
    private final List<PlantCard> cards = new ArrayList<>();
    private final CollectionController controller = new CollectionController();

    private Stage stage;
    private Table root;
    private Table contentLayer;
    private Table cardsGrid;
    private Table detailsOverlay;
    private Table toastOverlay;
    private Table walletBar;
    private Tab currentTab = Tab.PLANTS;

    public CollectionMenu(PvZ2 game, Screen previous) {
        this.game = game;
        this.previous = previous;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);
        Skin skin = PvzSkin.get();

        // Full-screen menu background (same as Game Modes / Greenhouse), with the bordered
        // collection panel sitting on top of it so it reads as a table on the screen.
        Stack rootStack = new Stack();
        rootStack.setFillParent(true);
        stage.addActor(rootStack);
        Texture bgTexture = GameAsset.MAIN_MENU_BG.get(game.getGlobalAssetManager());
        rootStack.add(new Image(bgTexture));

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.newDrawable("white_pixel", PAGE_BG));
        rootStack.add(root);
        this.root = root;

        BorderedPanel panel = new BorderedPanel(PANEL_BG);
        root.add(panel).size(PANEL_WIDTH, PANEL_HEIGHT);

        contentLayer = panel.contentLayer;
        buildHeader();
        buildGrid(Tab.PLANTS);
        buildTabs();
        buildWalletBar();
        refreshWallet();
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
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    // ── header: close button ──────────────────────────────────────────────────

    private void buildHeader() {
        Skin skin = PvzSkin.get();

        ImageButton closeBtn = new ImageButton(new ImageButton.ImageButtonStyle(
                skin.get("almanac", ImageButton.ImageButtonStyle.class)));
        closeBtn.getStyle().imageUp = PlantData.regionDrawable("IMAGE_UI_ALMANAC_TABS_CLOSE_TAB");
        closeBtn.getStyle().imageOver = PlantData.regionDrawable("IMAGE_UI_ALMANAC_TABS_CLOSE_TAB");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(previous != null ? previous : new GameModesMenu(game));
            }
        });

        Table header = new Table();
        header.add().expandX();
        header.add(closeBtn).size(64f, 64f).padRight(8f).padTop(6f);

        contentLayer.add(header).growX().padTop(4f);
        contentLayer.row();
    }

    /** Coin + gem pills in the screen's top-right corner, rebuilt whenever the balances change. */
    private void buildWalletBar() {
        walletBar = new Table();
        root.addActor(walletBar);
        refreshWallet();
    }

    private void refreshWallet() {
        if (walletBar == null) return;
        Skin skin = PvzSkin.get();
        walletBar.clearChildren();
        walletBar.add(MenuUiKit.resourceWidget(skin, "textures/ui/coin_icon.png",
                new Color(0.95f, 0.78f, 0.15f, 1f), String.valueOf(controller.getCoins()), 195, 63)).padRight(15);
        walletBar.add(MenuUiKit.resourceWidget(skin, "textures/ui/diamond_icon.png",
                new Color(0.35f, 0.75f, 0.95f, 1f), String.valueOf(controller.getDiamonds()), 195, 63));
        walletBar.pack();
        walletBar.setPosition(1920f - walletBar.getPrefWidth() - 30f, 1080f - walletBar.getPrefHeight() - 18f);
    }

    // ── floating tabs: badge + face, straddling the panel's top border ────────

    private void buildTabs() {
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(1);
        group.setMaxCheckCount(1);

        ImageButton plantsTab = tabButton(Tab.PLANTS, group, () -> showTab(Tab.PLANTS));
        plantsTab.setChecked(true);
        currentTab = Tab.PLANTS;

        float panelTop = (1080f + PANEL_HEIGHT) / 2f;
        float tabY = panelTop - 4f - TAB_H;

        Group plants = wrapTab(plantsTab, "IMAGE_UI_STORE_TABICONS_PLANTS");
        plants.setPosition(PANEL_X + 30f, tabY);

        Group zombies = wrapTab(tabButton(Tab.ZOMBIES, group, () -> showTab(Tab.ZOMBIES)),
                "IMAGE_UI_STORE_TABICONS_ZOMBIES");
        zombies.setPosition(PANEL_X + 30f + TAB_W + 10f, tabY);

        root.addActor(plants);
        root.addActor(zombies);
    }

    private Group wrapTab(ImageButton button, String faceId) {
        button.setSize(TAB_W, TAB_H);
        Image face = new Image(PlantData.regionDrawable(faceId));
        face.setTouchable(Touchable.disabled);
        face.setSize(FACE_W, FACE_H);
        face.setPosition((TAB_W - FACE_W) / 2f, (TAB_H - FACE_H) / 2f);
        Group g = new Group();
        g.setSize(TAB_W, TAB_H);
        g.addActor(button);
        g.addActor(face);
        return g;
    }

    private ImageButton tabButton(Tab tab, ButtonGroup<ImageButton> group, Runnable onSelect) {
        Skin skin = PvzSkin.get();
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(
                skin.get("almanac", ImageButton.ImageButtonStyle.class));
        style.imageUp = PlantData.regionDrawable(tab.downId);
        style.imageChecked = PlantData.regionDrawable(tab.activeId);
        ImageButton button = new ImageButton(style);
        button.setProgrammaticChangeEvents(false);
        group.add(button);
        if (onSelect != null) {
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onSelect.run();
                }
            });
        }
        return button;
    }

    // ── grid ──────────────────────────────────────────────────────────────────

    private void buildGrid(Tab tab) {
        cards.clear();
        if (cardsGrid != null) {
            cardsGrid.clearChildren();
        } else {
            cardsGrid = new Table();
            ScrollPane gridScroll = new ScrollPane(cardsGrid);
            gridScroll.setFlickScroll(true);
            gridScroll.setFadeScrollBars(false);
            gridScroll.setScrollingDisabled(true, false);
            gridScroll.setOverscroll(false, true);
            contentLayer.add(gridScroll).expand().fill();
        }

        if (tab == Tab.ZOMBIES) {
            cardsGrid.clearChildren();
            Label placeholder = new Label("Zombies collection is coming soon", PvzSkin.get(), "big");
            placeholder.setColor(Color.WHITE);
            cardsGrid.add(placeholder).pad(60f);
            return;
        }

        List<PlantData> plants = PlantData.loadAll();
        ButtonGroup<PlantCard> group = new ButtonGroup<>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(0);

        int column = 0;
        for (PlantData data : plants) {
            PlantCard card = new PlantCard(data);
            cards.add(card);
            group.add(card);
            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!card.isDisabled()) openDetails(card.data);
                }
            });
            cardsGrid.add(card).pad(8f);
            if (++column == COLS) {
                cardsGrid.row();
                column = 0;
            }
        }
    }

    private void showTab(Tab tab) {
        if (tab == currentTab) return;
        currentTab = tab;
        buildGrid(tab);
    }

    private void openDetails(PlantData data) {
        if (detailsOverlay != null) return;
        detailsOverlay = new Table();
        detailsOverlay.setFillParent(true);
        detailsOverlay.setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.45f)));
        stage.addActor(detailsOverlay);

        PlantDetailsTable details = new PlantDetailsTable(data,
                () -> closeDetails(),
                () -> tryPurchase(data),
                () -> tryUpgrade(data));
        detailsOverlay.add(details).size(840f, 920f);
    }

    private void closeDetails() {
        if (detailsOverlay == null) return;
        detailsOverlay.remove();
        detailsOverlay = null;
        for (PlantCard card : cards) card.update();
    }

    /** Buys a locked plant; shows a toast on failure, or refreshes everything and reopens on success. */
    private void tryPurchase(PlantData data) {
        String error = controller.purchasePlant(data.type);
        if (error != null) {
            showToast(error);
            return;
        }
        refreshWallet();
        for (PlantCard card : cards) card.update();
        showToast("Unlocked " + data.getName() + "!");
        closeDetails();
        openDetails(data);
    }

    /** Spends seed packets to level an owned plant up; refreshes the grid and reopens on success. */
    private void tryUpgrade(PlantData data) {
        String error = controller.upgradePlant(data.type);
        if (error != null) {
            showToast(error);
            return;
        }
        for (PlantCard card : cards) card.update();
        showToast(data.getName() + " is now Level " + data.getLevel() + "!");
        closeDetails();
        openDetails(data);
    }

    /** Centered, auto-dismissing message used for purchase feedback and errors. */
    private void showToast(String message) {
        if (toastOverlay != null) {
            toastOverlay.remove();
            toastOverlay = null;
        }
        Skin skin = PvzSkin.get();
        Table toast = new Table();
        toast.setBackground(skin.newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.88f)));
        Label label = new Label(message, skin, "medium");
        label.setColor(Color.WHITE);
        label.setWrap(true);
        label.setAlignment(Align.center);
        toast.add(label).width(720f).pad(18f);

        toastOverlay = new Table();
        toastOverlay.setFillParent(true);
        toastOverlay.bottom().padBottom(60f);
        toastOverlay.add(toast);
        stage.addActor(toastOverlay);
        toastOverlay.setTouchable(Touchable.disabled);
        toastOverlay.addAction(Actions.sequence(Actions.delay(2.8f), Actions.fadeOut(0.4f), Actions.removeActor()));
    }
}
