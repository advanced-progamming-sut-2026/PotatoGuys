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
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pvz.PvZ2;
import com.pvz.controller.CollectionController;
import com.pvz.enums.GameAsset;
import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantCategory;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.user.Collection;
import com.pvz.models.user.User;

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
    private static final Color FILTER_DIALOG_BG = new Color(0x6B4226FF);

    // Same convention SettingsMenu already uses for its tick checkboxes — falls back to
    // a drawn green/gray checkmark until these PNGs are actually added.
    private static final String CHECKBOX_ON_PATH = "textures/ui/checkbox_on.png";
    private static final String CHECKBOX_OFF_PATH = "textures/ui/checkbox_off.png";

    // Filter button icon (funnel), up/down press states — drop the two PNGs at these
    // paths; falls back to a plain tinted funnel-less button if they're not there yet.
    private static final String FILTER_ICON_UP_PATH = "textures/ui/filter_button_up.png";
    private static final String FILTER_ICON_DOWN_PATH = "textures/ui/filter_button_down.png";

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
    private final List<ZombieCard> zombieCards = new ArrayList<>();
    private final CollectionController controller = new CollectionController();

    private Stage stage;
    private Table root;
    private Table contentLayer;
    private Table cardsGrid;
    private Table detailsOverlay;
    private Table toastOverlay;
    private Table walletBar;
    private Table filterOverlay;
    private Label plantsCollectedLabel;
    private ImageTextButton filterButton;
    private Tab currentTab = Tab.PLANTS;

    // ── filter state: exactly one filter can be active at a time (Locked / Unlocked /
    // Upgrade Ready / a single Family) — picking a new one replaces whichever was active,
    // and re-picking the active one clears it back to "show all". The other categories in
    // the reference screenshot (worlds/mints/favorites/etc.) are deliberately not implemented.
    private enum FilterKind { NONE, LOCKED, UNLOCKED, UPGRADABLE, CATEGORY }

    private FilterKind activeFilterKind = FilterKind.NONE;
    private PlantCategory activeCategory = null;

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
        buildFilterBar();
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

        ImageButton closeBtn = new ImageButton(skin, "generic_close_circle");
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
        MenuUiKit.PlusResourceWidget coinWidget = MenuUiKit.resourceWidgetWithPlus(skin, "textures/ui/coin_icon.png",
            new Color(0.95f, 0.78f, 0.15f, 1f), String.valueOf(controller.getCoins()), 195, 63);
        MenuUiKit.PlusResourceWidget gemWidget = MenuUiKit.resourceWidgetWithPlus(skin, "textures/ui/diamond_icon.png",
            new Color(0.35f, 0.75f, 0.95f, 1f), String.valueOf(controller.getDiamonds()), 195, 63);
        MenuUiKit.wirePlusButton(coinWidget, true, 200);
        MenuUiKit.wirePlusButton(gemWidget, false, 10);
        walletBar.add(coinWidget.widget).padRight(15);
        walletBar.add(gemWidget.widget);
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
            contentLayer.row();
        }

        if (tab == Tab.ZOMBIES) {
            buildZombieGrid();
            refreshFilterBar();
            return;
        }

        List<PlantData> plants = PlantData.loadAll();
        ButtonGroup<PlantCard> group = new ButtonGroup<>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(0);

        int column = 0;
        for (PlantData data : plants) {
            if (!passesFilters(data)) continue;

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

        if (column == 0 && cardsGrid.getChildren().isEmpty()) {
            Label empty = new Label("No plants match these filters.", PvzSkin.get(), "big");
            empty.setColor(Color.WHITE);
            cardsGrid.add(empty).pad(60f);
        }

        refreshFilterBar();
    }

    private void buildZombieGrid() {
        zombieCards.clear();
        cardsGrid.clearChildren();

        User user = AppContext.getInstance().getCurrentUser();
        if (user == null) return;
        Collection collection = user.getProfile().getCollection();

        ButtonGroup<ZombieCard> group = new ButtonGroup<>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(0);

        int column = 0;
        for (ZombieType type : ZombieType.values()) {
            boolean unlocked = collection.getUnlockedZombies().contains(type);
            ZombieCard card = new ZombieCard(new ZombieCard.ViewData(type, unlocked));
            zombieCards.add(card);
            group.add(card);
            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!card.isDisabled()) openZombieDetails(card.getData());
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

    // ── bottom filter bar: stretched strip with the filter button + collected count ──

    private void buildFilterBar() {
        Skin skin = PvzSkin.get();

        Table bar = new Table();
        bar.setBackground(skin.newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.35f)));
        bar.pad(10f, 16f, 10f, 16f);

        filterButton = new ImageTextButton("Filters", filterButtonStyle(skin));
        filterButton.getLabel().setFontScale(0.85f);
        filterButton.getLabelCell().padLeft(8f);
        filterButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                openFilterDialog();
            }
        });
        bar.add(filterButton).height(46f).left();

        bar.add().expandX();

        plantsCollectedLabel = new Label("", skin);
        plantsCollectedLabel.setColor(Color.WHITE);
        plantsCollectedLabel.setFontScale(0.9f);
        bar.add(plantsCollectedLabel).right();

        contentLayer.add(bar).growX().padTop(8f);
    }

    /** Funnel icon (your two PNGs) + label, no background box — matches the reference
     *  bar (icon+text sitting directly on the strip). Falls back to a plain tinted
     *  funnel-less button if the PNGs aren't added yet, same as everywhere else. */
    private ImageTextButton.ImageTextButtonStyle filterButtonStyle(Skin skin) {
        ImageTextButton.ImageTextButtonStyle style = new ImageTextButton.ImageTextButtonStyle();
        style.imageUp = new TextureRegionDrawable(MenuUiKit.loadTextureSafe(FILTER_ICON_UP_PATH));
        style.imageDown = new TextureRegionDrawable(MenuUiKit.loadTextureSafe(FILTER_ICON_DOWN_PATH));
        style.font = skin.get("medium", Label.LabelStyle.class).font;
        style.fontColor = Color.WHITE;
        return style;
    }

    /** Updates the "Plants Collected: X of Y" count and the Filters button label. Called
     *  after every grid rebuild, so it always reflects the real (unfiltered) totals. */
    private void refreshFilterBar() {
        if (plantsCollectedLabel == null) return;

        int unlocked = 0;
        List<PlantData> all = PlantData.loadAll();
        for (PlantData data : all) {
            if (data.isUnlocked()) unlocked++;
        }
        plantsCollectedLabel.setText("Plants Collected: " + unlocked + " of " + all.size());

        int activeFilters = activeFilterKind == FilterKind.NONE ? 0 : 1;
        filterButton.setText(activeFilters == 0 ? "Filters" : "Filters (1)");
    }

    private boolean isUpgradable(PlantData data) {
        return data.isUnlocked() && !data.isMaxLevel() && data.seedPackets() >= data.requiredSeedPackets();
    }

    private boolean passesFilters(PlantData data) {
        switch (activeFilterKind) {
            case LOCKED:
                return !data.isUnlocked();
            case UNLOCKED:
                return data.isUnlocked();
            case UPGRADABLE:
                return isUpgradable(data);
            case CATEGORY:
                return data.getCategory() == activeCategory;
            case NONE:
            default:
                return true;
        }
    }

    // ── "Select Filters" popup ───────────────────────────────────────────────────

    private void openFilterDialog() {
        if (filterOverlay != null) return;
        Skin skin = PvzSkin.get();

        filterOverlay = new Table();
        filterOverlay.setFillParent(true);
        filterOverlay.setBackground(skin.newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.6f)));
        filterOverlay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getTarget() == filterOverlay) closeFilterDialog();
            }
        });
        stage.addActor(filterOverlay);

        BorderedPanel dialogPanel = new BorderedPanel(FILTER_DIALOG_BG);
        Table content = dialogPanel.contentLayer;
        content.top().left();
        content.pad(20f);

        Table titleRow = new Table();
        Label title = new Label("Select Filter", skin, "big");
        title.setColor(Color.WHITE);
        // Generic close, same convention as every other popup in the project (news modal,
        // settings, etc.) instead of a one-off plain "X" text button.
        ImageButton closeBtn = new ImageButton(skin, "generic_close_circle");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeFilterDialog();
            }
        });
        // Spacer matching the close button's width so the title lands truly centered in the
        // row (not just centered in the leftover space next to the button).
        titleRow.add().width(44f);
        titleRow.add(title).expandX().center();
        titleRow.add(closeBtn).size(44f);
        content.add(titleRow).growX().padBottom(6f).row();

        TextButton showAllBtn = new TextButton("Show All Plants", skin, "green");
        showAllBtn.getLabel().setFontScale(0.9f);
        showAllBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                activeFilterKind = FilterKind.NONE;
                activeCategory = null;
                closeFilterDialog();
                buildGrid(currentTab);
                openFilterDialog();
            }
        });
        content.add(showAllBtn).width(280f).height(46f).left().padBottom(14f).row();

        ScrollPane scrollPane = buildFilterOptionsScrollPane(skin);
        content.add(scrollPane).grow();

        filterOverlay.add(dialogPanel).size(900f, 800f);
    }

    private ScrollPane buildFilterOptionsScrollPane(Skin skin) {
        Table options = new Table();
        options.top().left();
        options.defaults().left().padBottom(6f);

        // One shared group across every option below (Status + Upgrade + Family) — that's
        // what makes the whole dialog single-select: checking any option here silently
        // unchecks whichever was active before, in every section, not just its own.
        ButtonGroup<CheckBox> group = new ButtonGroup<>();
        group.setMinCheckCount(0);
        group.setMaxCheckCount(1);

        Label statusHeader = new Label("    Status", skin, "medium");
        statusHeader.setColor(new Color(1f, 0.85f, 0.5f, 1f));
        options.add(statusHeader).padTop(4f).row();
        options.add(filterOption(group, "Locked", activeFilterKind == FilterKind.LOCKED,
            () -> activeFilterKind = FilterKind.LOCKED)).row();
        options.add(filterOption(group, "Unlocked", activeFilterKind == FilterKind.UNLOCKED,
            () -> activeFilterKind = FilterKind.UNLOCKED)).row();

        Label upgradeHeader = new Label("    Upgrade", skin, "medium");
        upgradeHeader.setColor(new Color(1f, 0.85f, 0.5f, 1f));
        options.add(upgradeHeader).padTop(14f).row();
        options.add(filterOption(group, "Upgrade Ready only", activeFilterKind == FilterKind.UPGRADABLE,
            () -> activeFilterKind = FilterKind.UPGRADABLE)).row();

        Label familyHeader = new Label("    Family", skin, "medium");
        familyHeader.setColor(new Color(1f, 0.85f, 0.5f, 1f));
        options.add(familyHeader).padTop(14f).row();
        for (PlantCategory category : PlantCategory.values()) {
            boolean checked = activeFilterKind == FilterKind.CATEGORY && activeCategory == category;
            options.add(filterOption(group, categoryLabel(category), checked, () -> {
                activeFilterKind = FilterKind.CATEGORY;
                activeCategory = category;
            })).row();
        }

        ScrollPane scrollPane = new ScrollPane(options, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, true);
        return scrollPane;
    }

    /** One filter row: a label + a tick checkbox in the dialog's single shared ButtonGroup, so
     *  checking it clears whatever the previous selection was (across every section) and applies
     *  immediately. Re-checking the currently active one unchecks it — back to "show all".
     *  Indented a bit from the section headers so it doesn't hug the panel's left edge. */
    private Table filterOption(ButtonGroup<CheckBox> group, String label, boolean initiallyChecked, Runnable onSelect) {
        Table row = new Table();
        row.padLeft(18f);
        CheckBox checkBox = new CheckBox("  " + label, tickCheckBoxStyle());
        checkBox.setChecked(initiallyChecked);
        checkBox.getLabel().setColor(Color.WHITE);
        group.add(checkBox);
        checkBox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (checkBox.isChecked()) {
                    onSelect.run();
                } else {
                    activeFilterKind = FilterKind.NONE;
                    activeCategory = null;
                }
                buildGrid(currentTab);
            }
        });
        row.add(checkBox).left();
        return row;
    }

    private void closeFilterDialog() {
        if (filterOverlay == null) return;
        filterOverlay.remove();
        filterOverlay = null;
    }

    private String categoryLabel(PlantCategory category) {
        String[] words = category.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /** Green/gray tick checkbox, same convention SettingsMenu already uses. */
    private CheckBox.CheckBoxStyle tickCheckBoxStyle() {
        Skin skin = PvzSkin.get();
        CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle();
        style.checkboxOn = new TextureRegionDrawable(loadTickTexture(CHECKBOX_ON_PATH, true));
        style.checkboxOff = new TextureRegionDrawable(loadTickTexture(CHECKBOX_OFF_PATH, false));
        style.font = skin.get("medium", Label.LabelStyle.class).font;
        style.fontColor = Color.WHITE;
        return style;
    }

    private Texture loadTickTexture(String path, boolean on) {
        if (path != null && !path.isEmpty() && Gdx.files.internal(path).exists()) {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return texture;
        }
        return drawnCheckTexture(on);
    }

    private Texture drawnCheckTexture(boolean on) {
        int size = 34;
        com.badlogic.gdx.graphics.Pixmap pixmap =
            new com.badlogic.gdx.graphics.Pixmap(size, size, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        Color tileColor = on ? new Color(0.20f, 0.65f, 0.25f, 1f) : new Color(0.55f, 0.55f, 0.5f, 1f);
        pixmap.setColor(tileColor);
        pixmap.fillRectangle(2, 2, size - 4, size - 4);
        pixmap.setColor(Color.WHITE);
        drawThickLine(pixmap, 8, 18, 14, 24, 3);
        drawThickLine(pixmap, 14, 24, 26, 10, 3);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void drawThickLine(com.badlogic.gdx.graphics.Pixmap pixmap, int x1, int y1, int x2, int y2, int thickness) {
        for (int t = -thickness / 2; t <= thickness / 2; t++) {
            pixmap.drawLine(x1, y1 + t, x2, y2 + t);
            pixmap.drawLine(x1 + t, y1, x2 + t, y2);
        }
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

    private void openZombieDetails(ZombieCard.ViewData data) {
        if (detailsOverlay != null) return;
        detailsOverlay = new Table();
        detailsOverlay.setFillParent(true);
        detailsOverlay.setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.45f)));
        stage.addActor(detailsOverlay);

        ZombieDetailsTable details = new ZombieDetailsTable(data, this::closeDetails);
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
