package com.pvz.view.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.Scaling;

import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.Card;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.user.User;
import com.pvz.view.MenuUiKit;
import com.pvz.view.PlantData;

import pvz.skin.PvzSkin;

/**
 * In-game HUD: top bar (sun/plant food/wallet) plus the seed-packet tray,
 * a vertical column hugging the left edge of the screen showing full cards
 * (packet background, family badge, sun cost) — same look as the pregame
 * slot bar and Rey's in-match tray — with a cooldown overlay on top.
 *
 * <p>Slot size is tuned to the 1280x720 gameplay viewport (see
 * GameController's FitViewport) rather than the 1920x1080 menus use, so all
 * 7 cards fit under the top bar without running off the bottom of the screen.
 */
public class GameUiModal extends Table {

    private static final float SLOT_WIDTH = 125f;
    private static final float SLOT_HEIGHT = 75f;

    private final Label sunLabel;
    private final Label plantFoodLabel;
    private final Label coinLabel;
    private final Label gemLabel;
    private final Table cardsBarTable;
    private PlantCard selectedCard = null;

    private final Map<PlantCard, Table> slotByCard = new HashMap<>();
    private final Map<PlantCard, Image> cooldownOverlayByCard = new HashMap<>();
    private Map<PlantType, PlantData> dataByType = new HashMap<>();

    public GameUiModal(Runnable onPauseRequested) {
        super();
        setFillParent(true);
        top();
        setVisible(false);

        Table topBar = new Table();
        topBar.top().left();
        topBar.pad(15);

        sunLabel = new Label("Sun: 50", PvzSkin.get(), "big");
        sunLabel.setColor(Color.YELLOW);
        topBar.add(sunLabel).padRight(25);

        plantFoodLabel = new Label("Plant Food: 0", PvzSkin.get());
        plantFoodLabel.setColor(Color.GREEN);
        topBar.add(plantFoodLabel);

        Table walletTable = new Table();
        walletTable.top().right();

        Table coinCell = new Table();
        Image coinIcon = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_coin"));
        coinLabel = new Label("0", PvzSkin.get(), "medium_outline");
        coinLabel.setColor(Color.YELLOW);
        coinLabel.setFontScale(1.4f);
        coinCell.add(coinIcon).size(64, 64);
        coinCell.add(coinLabel).padLeft(8);
        walletTable.add(coinCell).padRight(20);

        Table gemCell = new Table();
        Image gemIcon = new Image(PvzSkin.get().getDrawable("image_ui_hud_ingame_gem"));
        gemLabel = new Label("0", PvzSkin.get(), "medium_outline");
        gemLabel.setColor(Color.CYAN);
        gemLabel.setFontScale(1.4f);
        gemCell.add(gemIcon).size(64, 64);
        gemCell.add(gemLabel).padLeft(8);
        walletTable.add(gemCell);

        // Pause button pinned to the very top-right corner (same look as Rey's
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

        Table rightControls = new Table();
        rightControls.top().right();
        rightControls.add(walletTable).right().padRight(10);
        rightControls.add(pauseButton).right().size(58f);

        add(topBar).top().left().expandX().fillX();
        add(rightControls).top().right().padTop(15).padRight(15);
        row();

        // Seed-packet tray: vertical column pinned to the left edge, below the top bar.
        cardsBarTable = new Table();
        cardsBarTable.top();
        cardsBarTable.defaults().pad(4f).size(SLOT_WIDTH, SLOT_HEIGHT);

        Table leftColumnWrapper = new Table();
        leftColumnWrapper.top().left();
        leftColumnWrapper.add(cardsBarTable);

        add(leftColumnWrapper).left().top().colspan(2).padLeft(15).padTop(5);
    }

    public PlantCard getSelectedCard() {
        return selectedCard;
    }

    public void setSelectedCard(PlantCard card) {
        this.selectedCard = card;
        updateCardStyles();
    }

    public void initCards() {
        cardsBarTable.clearChildren();
        slotByCard.clear();
        cooldownOverlayByCard.clear();

        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null) return;

        List<PlantData> allData = PlantData.loadAll();
        dataByType = new HashMap<>();
        for (PlantData data : allData) {
            dataByType.put(data.type, data);
        }

        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc) {
                cardsBarTable.add(buildSlot(pc)).row();
            }
        }
        updateCardStyles();
    }

    private Table buildSlot(PlantCard pc) {
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
            plantLayer.add(plantImage).size(SLOT_HEIGHT - 22f);
            stack.add(plantLayer);

            Image badge = new Image(PlantData.regionDrawableOr(data.familyImageId(), fallback));
            Table badgeLayer = new Table();
            badgeLayer.top().left();
            badgeLayer.add(badge).size(16f).pad(2f);
            stack.add(badgeLayer);
        }

        Image cooldownOverlay = new Image(new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0f, 0f, 0f, 0.6f))));
        cooldownOverlay.setTouchable(Touchable.disabled);
        stack.add(cooldownOverlay);
        cooldownOverlayByCard.put(pc, cooldownOverlay);

        Label costLabel = new Label(String.valueOf(pc.getCost()), skin, "medium");
        costLabel.setFontScale(0.8f);
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
                }
                updateCardStyles();
            }
        });

        slotByCard.put(pc, slot);
        return slot;
    }

    public void updateHud() {
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null) return;

        sunLabel.setText("Sun: " + context.getCurrentSun());
        plantFoodLabel.setText("Plant Food: " + context.getPlantFoodCount());

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
            // Sweep the dark overlay's height with the remaining cooldown fraction,
            // same idea as PvZ's classic recharge shade shrinking from the bottom up.
            overlay.setHeight(SLOT_HEIGHT * fraction);
        }
    }

    private void updateCardStyles() {
        for (Map.Entry<PlantCard, Table> entry : slotByCard.entrySet()) {
            PlantCard pc = entry.getKey();
            Table slot = entry.getValue();
            if (selectedCard == pc) {
                slot.setColor(0.8f, 0.5f, 1f, 1f); // highlighted tint when selected
            } else {
                slot.setColor(1f, 1f, 1f, 1f);
            }
        }
    }
}
