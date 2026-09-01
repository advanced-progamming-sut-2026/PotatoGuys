package com.pvz.view.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.variants.IZombieLevel;
import com.pvz.view.MenuUiKit;
import com.pvz.view.PlantData;
import com.pvz.view.ZombieData;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

/**
 * Pre-game zombie selection screen for the online I,Zombie mode's zombie-side
 * player (the guest). Mirrors {@link PlantSelectModal}'s layout: a card grid
 * on the right, a selected-slots bar on the left, a preview panel, and a
 * "LET'S ROCK!" button.
 *
 * <p>The zombie player selects up to 7 zombie types from the level's allowed
 * pool. Unlike plants, zombies have no player-owned levels, boost status, or
 * seed packets — each card just shows the zombie's portrait, name, and sun cost.
 */
public class ZombieSelectModal extends Table {

    private static final int MAX_SELECTED = 7;
    private static final int SLOT_COUNT = 7;

    private static final float SLOT_WIDTH = 103.5f;
    private static final float SLOT_HEIGHT = 63f;

    private static final float PANEL_WIDTH = 820f;
    private static final float PREVIEW_HEIGHT = 120f;
    private static final float SCREEN_PADDING = 16f;
    private static final float SLOTS_TOP_OFFSET = 75f;
    private static final float SLOTS_CELL_TOP_PADDING = SLOTS_TOP_OFFSET - SCREEN_PADDING;

    private final Level level;
    private final Runnable onStartCallback;

    private final List<ZombieType> selectedZombies = new ArrayList<>();
    private final Map<ZombieType, ZombieGridCard> gridCardsByType = new HashMap<>();
    private final Map<ZombieType, ZombieData> dataByType = new HashMap<>();

    private final Table selectedSlotsTable;
    private final Table previewContent;
    private final TextButton startButton;
    private Label previewMessageLabel;

    /** Overlay shown while waiting for the other player to finish selecting. */
    private Table waitingOverlay;

    public ZombieSelectModal(Level level, Runnable onStartCallback) {
        this.level = level;
        this.onStartCallback = onStartCallback;

        setFillParent(true);
        setVisible(false);
        pad(SCREEN_PADDING);

        BorderedTable content = new BorderedTable();
        content.top().left();
        content.pad(20f);

        Label titleLabel = new Label("Choose Your Zombies", PvzSkin.get(), "big");
        titleLabel.setColor(Color.BLACK);

        content.add(titleLabel).padBottom(10f).left().row();

        previewContent = new Table();
        previewContent.top().left();
        content.add(previewContent).growX().height(PREVIEW_HEIGHT).padBottom(10f).row();
        buildPreviewPlaceholder();

        previewMessageLabel = new Label("", PvzSkin.get(), "medium");
        previewMessageLabel.setColor(new Color(0.8f, 0.2f, 0.15f, 1f));
        previewMessageLabel.setWrap(true);
        content.add(previewMessageLabel).width(PANEL_WIDTH - 40f).left().padBottom(10f).row();

        Table cardsGrid = new Table();
        cardsGrid.top();
        cardsGrid.defaults().pad(6f);

        ScrollPane scrollPane = new ScrollPane(cardsGrid, PvzSkin.get());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(true, false);
        content.add(scrollPane).grow().minWidth(0f).minHeight(0f).padBottom(10f).row();

        ButtonGroup<ZombieGridCard> previewGroup = new ButtonGroup<>();
        previewGroup.setMinCheckCount(0);
        previewGroup.setMaxCheckCount(1);
        previewGroup.setUncheckLast(true);

        List<ZombieData> zombieDataList = loadZombieData();
        int column = 0;
        int columnsPerRow = 4;
        for (ZombieData data : zombieDataList) {
            ZombieGridCard card = new ZombieGridCard(data);
            gridCardsByType.put(data.type, card);
            dataByType.put(data.type, data);

            previewGroup.add(card);
            card.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (!card.isChecked()) {
                        return;
                    }
                    showZombiePreview(data);
                    selectZombieIntoSlot(data);
                }
            });

            cardsGrid.add(card);
            column++;
            if (column >= columnsPerRow) {
                cardsGrid.row();
                column = 0;
            }
        }

        startButton = new TextButton("LET'S ROCK!", PvzSkin.get(), "purple");
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!selectedZombies.isEmpty() && onStartCallback != null) {
                    startButton.setDisabled(true);
                    onStartCallback.run();
                }
            }
        });

        selectedSlotsTable = new Table();
        selectedSlotsTable.top().left();
        selectedSlotsTable.defaults().padBottom(2f);
        refreshSlotsBar();

        Table mainLayout = new Table();
        mainLayout.top().left();
        mainLayout.add(selectedSlotsTable).top().left().padTop(SLOTS_CELL_TOP_PADDING).padRight(4f);
        mainLayout.add(content).top().left().width(PANEL_WIDTH).growY().minHeight(0f);
        mainLayout.add().expandX();

        Table rockLayer = new Table();
        rockLayer.bottom().right();
        rockLayer.setTouchable(Touchable.childrenOnly);
        rockLayer.add(startButton).right().bottom().width(220f).height(60f).padRight(20f).padBottom(20f);

        waitingOverlay = new Table();
        waitingOverlay.setFillParent(true);
        waitingOverlay.setTouchable(Touchable.enabled);
        waitingOverlay.setVisible(false);
        Label waitLabel = new Label("Waiting for opponent...", PvzSkin.get(), "big");
        waitLabel.setColor(Color.WHITE);
        Drawable dimBg = new TextureRegionDrawable(
                MenuUiKit.solidTexture(new Color(0f, 0f, 0f, 0.55f)));
        waitingOverlay.setBackground(dimBg);
        waitingOverlay.add(waitLabel).center();

        Stack rootStack = new Stack();
        rootStack.setTouchable(Touchable.childrenOnly);
        rootStack.add(mainLayout);
        rootStack.add(rockLayer);
        rootStack.add(waitingOverlay);

        top().left();
        add(rootStack).grow().minWidth(0f).minHeight(0f);
    }

    // ── zombie data loading ───────────────────────────────────────────────────

    private List<ZombieData> loadZombieData() {
        if (level instanceof IZombieLevel iZombieLevel) {
            return ZombieData.loadForLevel(iZombieLevel.getBasedZombies());
        }
        return List.of();
    }

    // ── preview ───────────────────────────────────────────────────────────────

    private void buildPreviewPlaceholder() {
        previewContent.clearChildren();
        Label label = new Label("Select a zombie", PvzSkin.get());
        label.setColor(Color.BLACK);
        previewContent.add(label).left().padLeft(10f);
    }

    private void showZombiePreview(ZombieData data) {
        previewContent.clearChildren();

        Table portrait = data.portraitWidget();

        Label nameLabel = new Label(data.getName(), PvzSkin.get(), "big");
        nameLabel.setColor(Color.BLACK);

        Label costLabel = new Label("Sun cost: " + data.getSunCost(), PvzSkin.get(), "medium");
        costLabel.setColor(new Color(0.9f, 0.75f, 0.1f, 1f));

        Table info = new Table();
        info.top().left();
        info.add(nameLabel).left().row();
        info.add(costLabel).left().padTop(4f);

        previewContent.add(portrait).size(96f, 96f).padRight(15f);
        previewContent.add(info).top();
    }

    private void showPreviewMessage(String message, boolean success) {
        if (previewMessageLabel != null) {
            previewMessageLabel.setText(message);
            previewMessageLabel.setColor(success
                    ? new Color(0.15f, 0.7f, 0.15f, 1f)
                    : new Color(0.8f, 0.2f, 0.15f, 1f));
        }
    }

    // ── selection logic ───────────────────────────────────────────────────────

    private void selectZombieIntoSlot(ZombieData data) {
        if (selectedZombies.contains(data.type)) {
            return;
        }
        if (selectedZombies.size() >= MAX_SELECTED) {
            ZombieGridCard card = gridCardsByType.get(data.type);
            if (card != null) card.setChecked(false);
            return;
        }
        selectedZombies.add(data.type);
        refreshSlotsBar();
    }

    private void removeZombieFromSlot(ZombieType type) {
        selectedZombies.remove(type);
        ZombieGridCard card = gridCardsByType.get(type);
        if (card != null) {
            card.setChecked(false);
        }
        refreshSlotsBar();
    }

    private void refreshSlotsBar() {
        selectedSlotsTable.clearChildren();
        for (int i = 0; i < SLOT_COUNT; i++) {
            Table slotCell = new Table();
            slotCell.setBackground(new TextureRegionDrawable(
                    MenuUiKit.solidTexture(new Color(0f, 0f, 0f, 0.45f))));
            if (i < selectedZombies.size()) {
                ZombieType type = selectedZombies.get(i);
                slotCell.add(buildMiniCard(dataByType.get(type), type)).size(SLOT_WIDTH, SLOT_HEIGHT);
            }
            selectedSlotsTable.add(slotCell).size(SLOT_WIDTH, SLOT_HEIGHT).row();
        }
    }

    private Table buildMiniCard(ZombieData data, ZombieType type) {
        Skin skin = PvzSkin.get();
        Drawable fallback = skin.newDrawable("white_pixel", new Color(0.25f, 0.25f, 0.25f, 1f));

        Table card = new Table();
        Drawable readyBg = PlantData.regionDrawableOr("IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY", fallback);
        card.setBackground(readyBg);
        card.setTouchable(Touchable.enabled);
        card.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                removeZombieFromSlot(type);
            }
        });

        Image portrait = new Image(data.portraitDrawable());
        portrait.setScaling(Scaling.fit);
        Table portraitLayer = new Table();
        portraitLayer.center();
        portraitLayer.add(portrait).size(SLOT_HEIGHT - 12f);

        Label cost = new Label(String.valueOf(data.getSunCost()), skin, "medium");
        cost.setFontScale(0.7f);
        cost.setColor(Color.WHITE);
        Table costLayer = new Table();
        costLayer.bottom().left();
        costLayer.add(cost).pad(1f, 3f, 1f, 3f);

        Stack stack = new Stack();
        stack.add(portraitLayer);
        stack.add(costLayer);

        card.add(stack).size(SLOT_WIDTH, SLOT_HEIGHT);
        return card;
    }

    // ── public API ────────────────────────────────────────────────────────────

    public List<ZombieType> getSelectedZombies() {
        return selectedZombies;
    }

    /**
     * Shows or hides the "Waiting for opponent..." overlay on top of the
     * selection screen. While waiting, the entire modal is touch-disabled
     * so the player cannot change their selection.
     */
    public void setWaiting(boolean waiting) {
        if (waitingOverlay != null) {
            waitingOverlay.setVisible(waiting);
        }
        if (waiting) {
            setTouchable(Touchable.disabled);
        } else {
            setTouchable(Touchable.childrenOnly);
        }
    }

    // ── grid card widget ──────────────────────────────────────────────────────

    /**
     * A clickable zombie card for the selection grid. Shows the zombie's
     * portrait on a styled background; highlights when checked/selected.
     * All zombies in the allowed pool are selectable (no lock state).
     */
    private static class ZombieGridCard extends com.badlogic.gdx.scenes.scene2d.ui.Button {
        private static final String READY_BG = "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY";
        private static final String SELECTED_BG = "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_SELECTED";

        private final ZombieData data;
        private final Image stateBackground;

        ZombieGridCard(ZombieData data) {
            super(new ButtonStyle());
            this.data = data;

            setSize(160f, 105f);
            setProgrammaticChangeEvents(true);

            Skin skin = PvzSkin.get();
            Drawable fallback = skin.newDrawable("white_pixel", new Color(0.15f, 0.15f, 0.18f, 1f));

            Stack cardStack = new Stack();
            cardStack.setTouchable(Touchable.disabled);

            stateBackground = new Image(PlantData.regionDrawableOr(READY_BG, fallback));
            stateBackground.setScaling(Scaling.stretch);
            cardStack.add(stateBackground);

            Table portraitLayer = new Table();
            portraitLayer.pad(8f);
            Image portrait = new Image(data.portraitDrawable());
            portrait.setScaling(Scaling.fit);
            portraitLayer.add(portrait).grow();
            cardStack.add(portraitLayer);

            Label nameLabel = new Label(data.getName(), skin, "medium");
            nameLabel.setColor(Color.WHITE);
            nameLabel.setFontScale(0.65f);
            nameLabel.setWrap(true);
            nameLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
            Table nameLayer = new Table();
            nameLayer.bottom().center();
            nameLayer.add(nameLabel).width(140f).padBottom(2f);
            cardStack.add(nameLayer);

            Label costLabel = new Label(String.valueOf(data.getSunCost()), skin, "medium");
            costLabel.setColor(new Color(0.95f, 0.85f, 0.1f, 1f));
            costLabel.setFontScale(0.75f);
            Table costLayer = new Table();
            costLayer.top().left();
            costLayer.add(costLabel).pad(4f, 6f, 0f, 0f);
            cardStack.add(costLayer);

            add(cardStack).size(160f, 105f);

            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    refreshVisual();
                }
            });
        }

        private void refreshVisual() {
            boolean highlighted = isChecked();
            String asset = highlighted ? SELECTED_BG : READY_BG;
            Color fallback = highlighted
                    ? new Color(0.35f, 0.25f, 0.45f, 1f)
                    : new Color(0.15f, 0.15f, 0.18f, 1f);
            Drawable d = PlantData.regionDrawableOr(asset,
                    new TextureRegionDrawable(MenuUiKit.solidTexture(fallback)));
            stateBackground.setDrawable(d);
        }

        @Override
        public float getPrefWidth() { return 160f; }

        @Override
        public float getPrefHeight() { return 105f; }
    }
}
