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

import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.levels.Level;
import com.pvz.models.user.MyPlant;
import com.pvz.view.MenuUiKit;
import com.pvz.view.PlantCard;
import com.pvz.view.PlantData;

import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

/**
 * Pre-game plant selection screen — structurally mirrors Rey's
 * PlantSelectionMenuTable (card grid + preview panel + selected-slots bar +
 * LET'S ROCK button), rebuilt on this project's own PlantData/PlantCard
 * (already matching her IMAGE_UI_PACKETS_* texture-bank convention) instead
 * of her Data/models classes.
 *
 * <p>Kept the same public contract as before (constructor + getSelectedPlants())
 * so {@code GameController} only needed one line changed (passing the level in),
 * not a rewrite.
 */
public class PlantSelectModal extends Table {

    private static final int MAX_SELECTED = 7;
    private static final int SLOT_COUNT = 7;

    // Matches PlantSlotsBar: small slot cells stacked in a plain column
    // (no ScrollPane) so all of them fit on screen at once.
    private static final float SLOT_WIDTH = 103.5f;
    private static final float SLOT_HEIGHT = 63f;

    private static final float PANEL_WIDTH = 820f;
    private static final float PREVIEW_HEIGHT = 120f;
    private static final float SCREEN_PADDING = 16f;
    private static final float PLANT_SLOTS_TOP_OFFSET = 75f;
    private static final float PLANT_SLOTS_CELL_TOP_PADDING = PLANT_SLOTS_TOP_OFFSET - SCREEN_PADDING;

    private final Level level;
    private final Runnable onStartCallback;

    private final List<PlantType> selectedPlants = new ArrayList<>();
    private final Map<PlantType, PlantCard> gridCardsByType = new HashMap<>();
    private final Map<PlantType, PlantData> dataByType = new HashMap<>();

    private final Table selectedSlotsTable;
    private final Table previewContent;
    private final TextButton startButton;

    public PlantSelectModal(Level level, Runnable onStartCallback) {
        this.level = level;
        this.onStartCallback = onStartCallback;

        setFillParent(true);
        setVisible(false);
        pad(SCREEN_PADDING);

        BorderedTable content = new BorderedTable();
        content.top().left();
        content.pad(20f);

        Label titleLabel = new Label("Choose Your Plants", PvzSkin.get(), "big");
        titleLabel.setColor(Color.BLACK);
        content.add(titleLabel).padBottom(10f).row();

        previewContent = new Table();
        previewContent.top().left();
        content.add(previewContent).growX().height(PREVIEW_HEIGHT).padBottom(10f).row();
        buildPreviewPlaceholder();

        Table cardsGrid = new Table();
        cardsGrid.top();
        cardsGrid.defaults().pad(6f);

        ScrollPane scrollPane = new ScrollPane(cardsGrid, PvzSkin.get());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(true, false);
        // grow() instead of a fixed height: the grid now takes all the vertical
        // space the panel has left over (title + preview), same as Rey's
        // cardsScroll.grow() — this is what gets 4 rows on screen instead of ~2.5.
        content.add(scrollPane).grow().minWidth(0f).minHeight(0f).padBottom(10f).row();

        ButtonGroup<PlantCard> previewGroup = new ButtonGroup<>();
        previewGroup.setMinCheckCount(0);
        previewGroup.setMaxCheckCount(1);
        previewGroup.setUncheckLast(true);

        int column = 0;
        int columnsPerRow = 4;
        for (PlantData data : PlantData.loadAll()) {
            if (level != null && !level.isPlantAllowed(data.type)) {
                continue; // e.g. sun/water-only plants on a dry level, matches Level.isPlantAllowed
            }

            PlantCard card = new PlantCard(data);
            gridCardsByType.put(data.type, card);
            dataByType.put(data.type, data);

            if (data.isUnlocked()) {
                previewGroup.add(card);
                card.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (!card.isChecked()) {
                            return;
                        }
                        showPlantPreview(data);
                        selectPlantIntoSlot(data);
                    }
                });
            } else {
                card.setDisabled(true);
                card.setTouchable(Touchable.disabled);
            }

            cardsGrid.add(card);
            column++;
            if (column >= columnsPerRow) {
                cardsGrid.row();
                column = 0;
            }
        }

        // LET'S ROCK! lives OUTSIDE the bordered panel entirely, pinned to the
        // bottom-right corner of the whole screen — matches Rey's rockLayer,
        // stacked on top of mainLayout instead of taking up a row inside the
        // panel (that row was the other thing eating into the grid's height).
        startButton = new TextButton("LET'S ROCK!", PvzSkin.get(), "purple");
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!selectedPlants.isEmpty() && onStartCallback != null) {
                    onStartCallback.run();
                }
            }
        });

        // Selected-plant slots: a plain column of small slot cells (no ScrollPane —
        // at SLOT_HEIGHT=63 each, all 7 comfortably fit the screen without scrolling,
        // same as Rey's PlantSlotsBar).
        selectedSlotsTable = new Table();
        selectedSlotsTable.top().left();
        selectedSlotsTable.defaults().padBottom(2f);
        refreshSlotsBar();

        Table mainLayout = new Table();
        mainLayout.top().left();
        mainLayout.add(selectedSlotsTable).top().left().padTop(PLANT_SLOTS_CELL_TOP_PADDING).padRight(4f);
        mainLayout.add(content).top().left().width(PANEL_WIDTH).growY().minHeight(0f);
        mainLayout.add().expandX();

        Table rockLayer = new Table();
        rockLayer.bottom().right();
        rockLayer.setTouchable(Touchable.childrenOnly);
        rockLayer.add(startButton).right().bottom().width(220f).height(60f).padRight(20f).padBottom(20f);

        Stack rootStack = new Stack();
        rootStack.setTouchable(Touchable.childrenOnly);
        rootStack.add(mainLayout);
        rootStack.add(rockLayer);

        top().left();
        add(rootStack).grow().minWidth(0f).minHeight(0f);
    }

    private void buildPreviewPlaceholder() {
        previewContent.clearChildren();
        Label label = new Label("Select a plant", PvzSkin.get());
        label.setColor(Color.BLACK);
        previewContent.add(label).left().padLeft(10f);
    }

    private void showPlantPreview(PlantData data) {
        previewContent.clearChildren();

        Image plantImage = new Image(data.cardDrawable());
        plantImage.setScaling(Scaling.fit);

        Label nameLabel = new Label(data.getName(), PvzSkin.get(), "big");
        nameLabel.setColor(Color.BLACK);

        Table info = new Table();
        info.top().left();
        info.add(nameLabel).left().row();

        Table buttonsRow = new Table();
        TextButton upgradeButton = new TextButton("UPGRADE", PvzSkin.get(), "brown");
        // Inert, matching Rey's own Upgrade button — not wired to anything on her side either.
        TextButton boostButton = new TextButton(data.isBoosted() ? "BOOSTED" : "BOOST", PvzSkin.get(), "green");
        boostButton.setDisabled(data.isBoosted());
        boostButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                MyPlant owned = data.myPlant();
                if (owned != null && !owned.isBoosted()) {
                    owned.setBoosted(true);
                    boostButton.setText("BOOSTED");
                    boostButton.setDisabled(true);
                    PlantCard card = gridCardsByType.get(data.type);
                    if (card != null) {
                        card.update();
                    }
                }
            }
        });
        buttonsRow.add(upgradeButton).width(120f).height(45f).padRight(10f);
        buttonsRow.add(boostButton).width(120f).height(45f);
        info.add(buttonsRow).left().padTop(8f);

        previewContent.add(plantImage).size(96f, 96f).padRight(15f);
        previewContent.add(info).top();
    }

    private void selectPlantIntoSlot(PlantData data) {
        if (selectedPlants.contains(data.type)) {
            return; // already selected — clicking again just re-previews it
        }
        if (selectedPlants.size() >= MAX_SELECTED) {
            gridCardsByType.get(data.type).setChecked(false);
            return; // slots full — no error popup wired here, matches the old modal's silent-ignore behavior
        }
        selectedPlants.add(data.type);
        refreshSlotsBar();
    }

    private void removePlantFromSlot(PlantType type) {
        selectedPlants.remove(type);
        PlantCard card = gridCardsByType.get(type);
        if (card != null) {
            card.setChecked(false);
        }
        refreshSlotsBar();
    }

    private void refreshSlotsBar() {
        selectedSlotsTable.clearChildren();
        for (int i = 0; i < SLOT_COUNT; i++) {
            Table slotCell = new Table();
            slotCell.setBackground(new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0f, 0f, 0f, 0.45f))));
            if (i < selectedPlants.size()) {
                PlantType type = selectedPlants.get(i);
                slotCell.add(buildMiniCard(dataByType.get(type), type)).size(SLOT_WIDTH, SLOT_HEIGHT);
            }
            selectedSlotsTable.add(slotCell).size(SLOT_WIDTH, SLOT_HEIGHT).row();
        }
    }

    /**
     * A shrunk-down version of {@link PlantCard}'s look (packet background, family badge
     * top-left, sun cost) sized exactly to the slot bar's SLOT_WIDTH/SLOT_HEIGHT, so the
     * left-side slots show the full card like Rey's PlantSlotsBar instead of a bare icon.
     * Built by hand rather than reusing PlantCard itself, since PlantCard is a fixed
     * 160x105 Button and scaling a Table-based widget down via Actor.setScale() clips/
     * misaligns its internal cells instead of shrinking them cleanly.
     */
    private Table buildMiniCard(PlantData data, PlantType type) {
        Skin skin = PvzSkin.get();
        Drawable fallback = skin.newDrawable("white_pixel", new Color(0.25f, 0.25f, 0.25f, 1f));

        Table card = new Table();
        card.setBackground(PlantData.regionDrawableOr(
            data.isBoosted() ? "IMAGE_UI_PACKETS_BOOST" : "IMAGE_UI_PACKETS_READY", fallback));
        card.setTouchable(Touchable.enabled);
        card.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                removePlantFromSlot(type);
            }
        });

        Image packet = new Image(PlantData.regionDrawableOr(data.cardImageId(), fallback));
        packet.setScaling(Scaling.fit);
        Table packetLayer = new Table();
        packetLayer.center();
        packetLayer.add(packet).size(SLOT_HEIGHT - 12f);

        Image badge = new Image(PlantData.regionDrawableOr(data.familyImageId(), fallback));
        Table badgeLayer = new Table();
        badgeLayer.top().left();
        badgeLayer.add(badge).size(16f).pad(2f);

        Label cost = new Label(String.valueOf(data.sunCost()), skin, "medium");
        cost.setFontScale(0.7f);
        cost.setColor(Color.WHITE);
        Table costLayer = new Table();
        costLayer.bottom().left();
        costLayer.add(cost).pad(1f, 3f, 1f, 3f);

        Stack stack = new Stack();
        stack.add(packetLayer);
        stack.add(badgeLayer);
        stack.add(costLayer);

        card.add(stack).size(SLOT_WIDTH, SLOT_HEIGHT);
        return card;
    }

    public List<PlantType> getSelectedPlants() {
        return selectedPlants;
    }
}
