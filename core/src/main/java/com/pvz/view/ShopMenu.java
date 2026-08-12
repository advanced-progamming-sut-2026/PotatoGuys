package com.pvz.view;

import java.time.Duration;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import com.pvz.controller.ShopController;
import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.shop.Currency;
import com.pvz.models.shop.DailyOffer;
import com.pvz.models.shop.Price;
import com.pvz.models.shop.ShopItem;
import com.pvz.models.shop.items.CurrencyExchangeItem;
import com.pvz.models.shop.items.PlantFoodItem;
import com.pvz.models.shop.items.PotSlotItem;
import com.pvz.models.shop.items.RandomSeedPacketItem;
import com.pvz.models.shop.items.SelectableSeedPacketItem;
import com.pvz.models.user.User;

import pvz.skin.PvzSkin;

/**
 * Graphical Shop menu — structurally mirrors Dani's ShopScreen (daily offer
 * panel + scrollable item cards, confirm-purchase modal, plant-picker modal
 * for the selectable seed packet, inline error display) but built entirely
 * on this project's own Shop/ShopItem/ShopController models.
 *
 * <p>Same overlay convention as {@link NewsModal}/{@link CollectionMenu}:
 * constructed once by MainMenu, toggled via setVisible().
 *
 * <p>Icon asset paths this expects (missing files fall back to a plain
 * colored square, never crash):
 * <pre>
 *   textures/ui/shop/pot.png
 *   textures/ui/shop/plant_food.png
 *   textures/ui/shop/random_seed.png
 *   textures/ui/shop/selectable_seed.png
 *   textures/ui/shop/currency_exchange.png
 *   textures/ui/diamond_icon.png
 *   textures/plants/cards/&lt;PlantType name&gt;.png   (daily offer + plant picker; shared with Collection)
 * </pre>
 */
public class ShopMenu extends Table {

    private static final float PANEL_WIDTH = 1500f;
    private static final float PANEL_HEIGHT = 760f;
    private static final float ITEM_GAP = 35f;
    private static final float ITEM_WIDTH = 210f;
    private static final float ICON_SIZE = 110f;
    private static final float PNG_SIZE = 138f;
    private static final float POT_SIZE = 300f;
    private static final float PLANT_FOOD_SIZE = 225f;
    private static final float RANDOM_SEED_SIZE = 250f;
    private static final float SELECTABLE_SEED_SIZE = 225f;
    private static final float CURRENCY_EXCHANGE_SIZE = 300f;
    private static final float BUTTON_WIDTH = 180f;
    private static final float BUTTON_HEIGHT = 50f;
    private static final float BUTTON_X = 15f;
    private static final float BUTTON_Y = 15f;
    private static final float DAILY_OFFER_WIDTH = 260f;
    private static final float DAILY_OFFER_HEIGHT = 500f;
    private static final float DAILY_OFFER_PLANT_WIDTH = 200f;
    private static final float DAILY_OFFER_PLANT_HEIGHT = 200f;

    private static final float POT_CARD_WIDTH = 260f;
    private static final float POT_CARD_HEIGHT = 500f;
    private static final float PLANT_FOOD_CARD_WIDTH = 260f;
    private static final float PLANT_FOOD_CARD_HEIGHT = 500f;
    private static final float RANDOM_SEED_CARD_WIDTH = 260f;
    private static final float RANDOM_SEED_CARD_HEIGHT = 500f;
    private static final float SELECTABLE_SEED_CARD_WIDTH = 260f;
    private static final float SELECTABLE_SEED_CARD_HEIGHT = 500f;
    private static final float CURRENCY_EXCHANGE_CARD_WIDTH = 260f;
    private static final float CURRENCY_EXCHANGE_CARD_HEIGHT = 500f;

    private static final float POT_BUTTON_WIDTH = 180f;
    private static final float POT_BUTTON_HEIGHT = 50f;
    private static final float PLANT_FOOD_BUTTON_WIDTH = 180f;
    private static final float PLANT_FOOD_BUTTON_HEIGHT = 50f;
    private static final float RANDOM_SEED_BUTTON_WIDTH = 180f;
    private static final float RANDOM_SEED_BUTTON_HEIGHT = 50f;
    private static final float SELECTABLE_SEED_BUTTON_WIDTH = 180f;
    private static final float SELECTABLE_SEED_BUTTON_HEIGHT = 50f;
    private static final float CURRENCY_EXCHANGE_BUTTON_WIDTH = 180f;
    private static final float CURRENCY_EXCHANGE_BUTTON_HEIGHT = 50f;
    private static final float POT_BUTTON_X = 35f;
    private static final float POT_BUTTON_Y = 15f;
    private static final float PLANT_FOOD_BUTTON_X = 35f;
    private static final float PLANT_FOOD_BUTTON_Y = 15f;
    private static final float RANDOM_SEED_BUTTON_X = 35f;
    private static final float RANDOM_SEED_BUTTON_Y = 15f;
    private static final float SELECTABLE_SEED_BUTTON_X = 35f;
    private static final float SELECTABLE_SEED_BUTTON_Y = 15f;
    private static final float CURRENCY_EXCHANGE_BUTTON_X = 35f;
    private static final float CURRENCY_EXCHANGE_BUTTON_Y = 15f;

    private static final Color PANEL_BG = new Color(0x6B4226FF);
    private static final Color CARD_BG = new Color(0x5A3A1EFF);

    private static final String ICON_POT = "textures/shop/pot.png";
    private static final String ICON_PLANT_FOOD = "textures/shop/plant_food.png";
    private static final String ICON_RANDOM_SEED = "textures/shop/random_seed.png";
    private static final String ICON_SELECTABLE_SEED = "textures/shop/selectable_seed.png";
    private static final String ICON_CURRENCY_EXCHANGE = "textures/shop/currency_exchange.png";
    private static final String DAILY_OFFER_PLANT_PATH = "textures/greenhouse/plants/";
    private static final String CLOSE_BUTTON = "textures/shop/close_button.png";
    private static final String CLOSE_BUTTON_DOWN = "textures/shop/close_button_down.png";

    private final ShopController controller = new ShopController();

    private final Table itemsRow;
    private final Table dailyOfferSlot;
    private Table walletDisplay;
    private Runnable onHide;

    public ShopMenu() {
        setFillParent(true);
        setTouchable(Touchable.enabled);
        setVisible(false);
        setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.55f)));
        pad(30f);

        BorderedPanel panel = new BorderedPanel(PANEL_BG);
        panel.contentLayer.top();
        panel.contentLayer.pad(20f);

        Label title = new Label("                                                                            Shop", PvzSkin.get(), "big");
        title.setFontScale(1.5f);
        title.setColor(Color.BLACK);

        Table header = new Table();
        header.add(title).left();
        header.add(buildWalletDisplay()).expandX().right();
        panel.contentLayer.add(header).growX().padBottom(5f).row();

        Table body = new Table();
        dailyOfferSlot = new Table();
        body.add(dailyOfferSlot).width(DAILY_OFFER_WIDTH).top().padRight(ITEM_GAP);

        itemsRow = new Table();
        itemsRow.top().left();
        ScrollPane itemsScroll = new ScrollPane(itemsRow, PvzSkin.get());
        itemsScroll.setFadeScrollBars(false);
        itemsScroll.setScrollingDisabled(false, true);
        itemsScroll.setOverscroll(false, false);
        body.add(itemsScroll).grow();

        panel.contentLayer.add(body).grow().minHeight(360f).row();

        Stack menuStack = new Stack();
        menuStack.add(panel);

        ImageButton closeButton = createCloseButton();
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hideShop();
            }
        });
        Table closeLayer = new Table();
        closeLayer.top().right();
        closeLayer.add(closeButton).padTop(10f).padRight(10f);
        menuStack.add(closeLayer);

        add(menuStack).size(PANEL_WIDTH, PANEL_HEIGHT);
    }

    private Table buildWalletDisplay() {
        walletDisplay = new Table();
        refreshWallet();
        return walletDisplay;
    }

    /** Call this instead of setVisible(true) directly — refreshes everything
     *  from the current user's live data every time it opens. */
    public void showShop() {
        setVisible(true);
        clearError();
        refresh();
    }

    /** Hides the shop and notifies the owning screen via {@link #setOnHide}. */
    private void hideShop() {
        setVisible(false);
        if (onHide != null) onHide.run();
    }

    /** Registers a callback fired every time the shop is hidden (close button).
     *  Lets the owning screen refresh itself after a purchase changed the wallet. */
    public void setOnHide(Runnable onHide) {
        this.onHide = onHide;
    }

    private void refresh() {
        refreshWallet();
        refreshDailyOffer();
        refreshItems();
    }

    private void refreshWallet() {
        User user = AppContext.getInstance().getCurrentUser();
        if (user == null) return;
        walletDisplay.clearChildren();
        walletDisplay.add(MenuUiKit.resourceWidget(PvzSkin.get(), "textures/ui/coin_icon.png",
            new Color(0.95f, 0.78f, 0.15f, 1f), String.valueOf(user.getProfile().getCoins()), 195, 63)).padRight(15);
        walletDisplay.add(MenuUiKit.resourceWidget(PvzSkin.get(), "textures/ui/diamond_icon.png",
            new Color(0.35f, 0.75f, 0.95f, 1f), String.valueOf(user.getProfile().getDiamonds()), 195, 63));
    }

    private void refreshDailyOffer() {
        dailyOfferSlot.clearChildren();
        DailyOffer offer = controller.getDailyOffer();
        if (offer == null) {
            dailyOfferSlot.add(new Label("No daily offer today.", PvzSkin.get()));
            return;
        }

        Table card = new Table();
        card.setBackground(PvzSkin.get().newDrawable("white_pixel", CARD_BG));
        card.top();
        card.pad(10f);

        Label header = new Label("Daily Offer", PvzSkin.get(), "big");
        header.setFontScale(0.9f);
        header.setColor(Color.BLACK);
        header.setAlignment(Align.center);
        card.add(header).growX().padBottom(6f).row();

        Label nameLabel = new Label(offer.getPlantType().name(), PvzSkin.get());
        nameLabel.setColor(Color.BLACK);
        nameLabel.setAlignment(Align.center);
        card.add(nameLabel).growX().padBottom(6f).row();

        Image plantImage = safeImage(DAILY_OFFER_PLANT_PATH + offer.getPlantType().name() + ".png");
        card.add(plantImage).size(DAILY_OFFER_PLANT_WIDTH, DAILY_OFFER_PLANT_HEIGHT).padBottom(6f).row();

        Duration remaining = controller.getDailyOfferTimeRemaining();
        if (remaining != null) {
            String timeText = "Resets in " + remaining.toHours() + "h " + (remaining.toMinutes() % 60) + "m";
            Label timeLabel = new Label(timeText, PvzSkin.get());
            timeLabel.setColor(Color.BLACK);
            timeLabel.setFontScale(0.85f);
            card.add(timeLabel).padBottom(8f).row();
        }

        TextButton buyBtn = new TextButton(
            offer.isPurchasedToday() ? "Bought" : priceLabel(offer.getPrice()), PvzSkin.get(), "green");
        buyBtn.setDisabled(offer.isPurchasedToday());
        buyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!buyBtn.isDisabled()) {
                    confirmPurchase(offer, null);
                }
            }
        });
        buyBtn.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        buyBtn.setPosition(BUTTON_X, BUTTON_Y);
        card.addActor(buyBtn);

        dailyOfferSlot.add(card).size(DAILY_OFFER_WIDTH, DAILY_OFFER_HEIGHT);
    }

    private void refreshItems() {
        itemsRow.clearChildren();
        for (ShopItem item : controller.getPermanentItems()) {
            itemsRow.add(buildItemCard(item)).size(cardWidthFor(item), cardHeightFor(item)).top().padRight(ITEM_GAP);
        }
    }

    private Table buildItemCard(ShopItem item) {
        Skin skin = PvzSkin.get();
        Table card = new Table();
        card.setBackground(skin.newDrawable("white_pixel", CARD_BG));
        card.top();
        card.pad(10f);

        Label nameLabel = new Label(item.getName(), PvzSkin.get());
        nameLabel.setColor(Color.BLACK);
        nameLabel.setWrap(true);
        nameLabel.setAlignment(Align.center);
        nameLabel.setFontScale(1.6f);
        card.add(nameLabel).width(cardWidthFor(item) - 20f).align(Align.center).padBottom(6f).row();

        Image icon = safeImage(iconPathFor(item));
        card.add(icon).size(iconSizeFor(item)).padBottom(6f).row();

        Label descLabel = new Label(item.getDescription(), PvzSkin.get());
        descLabel.setColor(Color.BLACK);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        descLabel.setFontScale(1.2f);
        card.add(descLabel).width(cardWidthFor(item) - 20f).align(Align.center).padBottom(6f).row();

        int remaining = controller.getRemainingCapacity(item);
        if (remaining >= 0) {
            Label capacityLabel = new Label("Remaining: " + remaining, PvzSkin.get());
            capacityLabel.setColor(Color.BLACK);
            capacityLabel.setFontScale(0.8f);
            card.add(capacityLabel).padBottom(6f).row();
        }

        TextButton buyBtn = new TextButton(priceLabel(item.getPrice()), PvzSkin.get(), "green");
        buyBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (item instanceof SelectableSeedPacketItem) {
                    openPlantPicker(item);
                } else {
                    confirmPurchase(item, null);
                }
            }
        });
        buyBtn.setSize(buyButtonWidthFor(item), buyButtonHeightFor(item));
        buyBtn.setPosition(buyButtonXFor(item), buyButtonYFor(item));
        card.addActor(buyBtn);

        return card;
    }

    private String iconPathFor(ShopItem item) {
        if (item instanceof PotSlotItem) return ICON_POT;
        if (item instanceof PlantFoodItem) return ICON_PLANT_FOOD;
        if (item instanceof RandomSeedPacketItem) return ICON_RANDOM_SEED;
        if (item instanceof SelectableSeedPacketItem) return ICON_SELECTABLE_SEED;
        if (item instanceof CurrencyExchangeItem) return ICON_CURRENCY_EXCHANGE;
        return null;
    }

    private float iconSizeFor(ShopItem item) {
        if (item instanceof PotSlotItem) return POT_SIZE;
        if (item instanceof PlantFoodItem) return PLANT_FOOD_SIZE;
        if (item instanceof RandomSeedPacketItem) return RANDOM_SEED_SIZE;
        if (item instanceof SelectableSeedPacketItem) return SELECTABLE_SEED_SIZE;
        if (item instanceof CurrencyExchangeItem) return CURRENCY_EXCHANGE_SIZE;
        return ICON_SIZE;
    }

    private float cardWidthFor(ShopItem item) {
        if (item instanceof PotSlotItem) return POT_CARD_WIDTH;
        if (item instanceof PlantFoodItem) return PLANT_FOOD_CARD_WIDTH;
        if (item instanceof RandomSeedPacketItem) return RANDOM_SEED_CARD_WIDTH;
        if (item instanceof SelectableSeedPacketItem) return SELECTABLE_SEED_CARD_WIDTH;
        if (item instanceof CurrencyExchangeItem) return CURRENCY_EXCHANGE_CARD_WIDTH;
        return ITEM_WIDTH;
    }

    private float cardHeightFor(ShopItem item) {
        if (item instanceof PotSlotItem) return POT_CARD_HEIGHT;
        if (item instanceof PlantFoodItem) return PLANT_FOOD_CARD_HEIGHT;
        if (item instanceof RandomSeedPacketItem) return RANDOM_SEED_CARD_HEIGHT;
        if (item instanceof SelectableSeedPacketItem) return SELECTABLE_SEED_CARD_HEIGHT;
        if (item instanceof CurrencyExchangeItem) return CURRENCY_EXCHANGE_CARD_HEIGHT;
        return ITEM_WIDTH;
    }

    private float buyButtonWidthFor(ShopItem item) {
        if (item instanceof PotSlotItem) return POT_BUTTON_WIDTH;
        if (item instanceof PlantFoodItem) return PLANT_FOOD_BUTTON_WIDTH;
        if (item instanceof RandomSeedPacketItem) return RANDOM_SEED_BUTTON_WIDTH;
        if (item instanceof SelectableSeedPacketItem) return SELECTABLE_SEED_BUTTON_WIDTH;
        if (item instanceof CurrencyExchangeItem) return CURRENCY_EXCHANGE_BUTTON_WIDTH;
        return BUTTON_WIDTH;
    }

    private float buyButtonHeightFor(ShopItem item) {
        if (item instanceof PotSlotItem) return POT_BUTTON_HEIGHT;
        if (item instanceof PlantFoodItem) return PLANT_FOOD_BUTTON_HEIGHT;
        if (item instanceof RandomSeedPacketItem) return RANDOM_SEED_BUTTON_HEIGHT;
        if (item instanceof SelectableSeedPacketItem) return SELECTABLE_SEED_BUTTON_HEIGHT;
        if (item instanceof CurrencyExchangeItem) return CURRENCY_EXCHANGE_BUTTON_HEIGHT;
        return BUTTON_HEIGHT;
    }

    private float buyButtonXFor(ShopItem item) {
        if (item instanceof PotSlotItem) return POT_BUTTON_X;
        if (item instanceof PlantFoodItem) return PLANT_FOOD_BUTTON_X;
        if (item instanceof RandomSeedPacketItem) return RANDOM_SEED_BUTTON_X;
        if (item instanceof SelectableSeedPacketItem) return SELECTABLE_SEED_BUTTON_X;
        if (item instanceof CurrencyExchangeItem) return CURRENCY_EXCHANGE_BUTTON_X;
        return BUTTON_X;
    }

    private float buyButtonYFor(ShopItem item) {
        if (item instanceof PotSlotItem) return POT_BUTTON_Y;
        if (item instanceof PlantFoodItem) return PLANT_FOOD_BUTTON_Y;
        if (item instanceof RandomSeedPacketItem) return RANDOM_SEED_BUTTON_Y;
        if (item instanceof SelectableSeedPacketItem) return SELECTABLE_SEED_BUTTON_Y;
        if (item instanceof CurrencyExchangeItem) return CURRENCY_EXCHANGE_BUTTON_Y;
        return BUTTON_Y;
    }

    private String priceLabel(Price price) {
        String unit = price.getCurrency() == Currency.COIN ? "Coins" : "Diamonds";
        return price.getAmount() + " " + unit;
    }

    // ── plant picker (Selectable Seed Packet) ───────────────────────────

    private void openPlantPicker(ShopItem item) {
        List<PlantType> unlocked = controller.getUnlockedPlantTypes();

        Table modalContent = new Table();
        Label header = new Label("Choose a Plant", PvzSkin.get(), "big");
        header.setColor(Color.BLACK);
        modalContent.add(header).padBottom(10f).row();

        Table list = new Table();
        if (unlocked.isEmpty()) {
            Label empty = new Label("No unlocked plants yet.", PvzSkin.get());
            empty.setColor(Color.BLACK);
            list.add(empty);
        } else {
            for (PlantType type : unlocked) {
                TextButton pick = new TextButton(type.name(), PvzSkin.get(), "brown");
                pick.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        closeModal();
                        confirmPurchase(item, type);
                    }
                });
                list.add(pick).width(260f).height(45f).padBottom(6f).row();
            }
        }

        ScrollPane scroll = new ScrollPane(list, PvzSkin.get());
        scroll.setFadeScrollBars(false);
        modalContent.add(scroll).width(280f).height(220f).row();

        TextButton cancel = new TextButton("Cancel", PvzSkin.get(), "purple");
        cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                closeModal();
            }
        });
        modalContent.add(cancel).width(140f).height(45f).padTop(8f);

        showModal(modalContent);
    }

    // ── confirm-purchase modal ──────────────────────────────────────────

    private void confirmPurchase(ShopItem item, PlantType plantType) {
        Table modalContent = new Table();
        Label header = new Label("Confirm Purchase", PvzSkin.get(), "big");
        header.setColor(Color.BLACK);
        modalContent.add(header).padBottom(10f).row();

        String itemLabel = item.getName() + (plantType != null ? " (" + plantType.name() + ")" : "");
        Label message = new Label("Buy " + itemLabel + " for " + priceLabel(item.getPrice()) + "?", PvzSkin.get());
        message.setColor(Color.BLACK);
        message.setWrap(true);
        modalContent.add(message).width(300f).padBottom(15f).row();

        Table buttons = new Table();
        TextButton cancelBtn = new TextButton("Cancel", PvzSkin.get(), "purple");
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                closeModal();
            }
        });
        TextButton confirmBtn = new TextButton("Confirm", PvzSkin.get(), "green");
        confirmBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                closeModal();
                executePurchase(item, plantType);
            }
        });
        buttons.add(cancelBtn).width(140f).height(45f).padRight(10f);
        buttons.add(confirmBtn).width(140f).height(45f);
        modalContent.add(buttons);

        showModal(modalContent);
    }

    private void executePurchase(ShopItem item, PlantType plantType) {
        String error = controller.purchase(item, 1, plantType);
        if (error != null) {
            showError(error);
        } else {
            clearError();
        }
        refresh(); // always refresh — capacities/wallet may have changed either way
    }

    // ── modal plumbing ──────────────────────────────────────────────────

    private Table activeModal;

    private void showModal(Table content) {
        showModal(content, 0f, 0f);
    }

    private void showModal(Table content, float boxWidth, float boxHeight) {
        closeModal();

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.setTouchable(Touchable.enabled);
        overlay.setBackground(PvzSkin.get().newDrawable("white_pixel", new Color(0f, 0f, 0f, 0.6f)));

        BorderedPanel box = new BorderedPanel(CARD_BG);
        box.contentLayer.add(content);

        if (boxWidth > 0 || boxHeight > 0) {
            overlay.add(box).size(boxWidth, boxHeight);
        } else {
            overlay.add(box);
        }
        activeModal = overlay;
        if (getStage() != null) {
            getStage().addActor(overlay);
        }
    }

    private void closeModal() {
        if (activeModal != null) {
            activeModal.remove();
            activeModal = null;
        }
    }

    private void showError(String message) {
        Table modalContent = new Table();
        modalContent.top();

        Label header = new Label("Purchase Failed", PvzSkin.get(), "big");
        header.setColor(Color.BLACK);
        header.setFontScale(1.6f);
        modalContent.add(header).padBottom(18f).row();

        Label messageLabel = new Label(message, PvzSkin.get());
        messageLabel.setColor(Color.BLACK);
        messageLabel.setWrap(true);
        messageLabel.setAlignment(Align.center);
        messageLabel.setFontScale(1.3f);
        modalContent.add(messageLabel).width(480f).padBottom(20f).row();

        TextButton okBtn = new TextButton("OK", PvzSkin.get(), "green");
        okBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                closeModal();
            }
        });
        modalContent.add(okBtn).width(160f).height(55f);

        showModal(modalContent, 600f, 320f);
    }

    private void clearError() {
        closeModal();
    }

    private ImageButton createCloseButton() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = safeDrawable(CLOSE_BUTTON);
        style.imageDown = safeDrawable(CLOSE_BUTTON_DOWN);
        style.imageOver = style.imageDown;
        return new ImageButton(style);
    }

    // ── local texture helpers, on top of MenuUiKit's actual shared loader ──

    private static Image safeImage(String path) {
        Image image = new Image(MenuUiKit.loadTextureSafe(path));
        image.setScaling(Scaling.fit);
        image.setSize(PNG_SIZE, PNG_SIZE);
        return image;
    }

    private static com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable safeDrawable(String path) {
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(MenuUiKit.loadTextureSafe(path));
    }
}
