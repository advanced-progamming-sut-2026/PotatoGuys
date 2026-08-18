package com.pvz.view.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
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
import com.pvz.models.games.card.Card;
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
 * slot bar and Rey's in-match tray — with a cooldown overlay on top.
 *
 * <p>Slot size is tuned to the 1280x720 gameplay viewport (see
 * GameController's FitViewport) rather than the 1920x1080 menus use, so all
 * 7 cards fit under the top bar without running off the bottom of the screen.
 *
 * <p>The plant-food bank (dots + "+" cheat button) sits at normalized (0.25, 0.1).
 */
public class GameUiModal extends Table {

    private static final float SLOT_WIDTH = 135f;
    private static final float SLOT_HEIGHT = 95f;

    private static final int CHEAT_SUN_AMOUNT = 25;
    private static final int MAX_PLANT_FOOD = 3;

    private final Label sunLabel;
    private final Label coinLabel;
    private final Label gemLabel;
    private final Image coinIcon;
    private final Image gemIcon;
    private final Image[] plantFoodDots = new Image[MAX_PLANT_FOOD];
    private final ImageButton addSunButton;
    private final ImageButton addFoodButton;
    private final Table cardsBarTable;
    private PlantCard selectedCard = null;
    private ZombieCard selectedZombieCard = null;
    private boolean shovelSelected = false;
    private Runnable onShovelRequested = null;
    private ImageButton shovelButton = null;

    private final Map<PlantCard, Table> slotByCard = new HashMap<>();
    private final Map<PlantCard, Image> cooldownOverlayByCard = new HashMap<>();
    private final Map<ZombieCard, Table> zombieSlotByCard = new HashMap<>();
    private final Map<ZombieCard, Image> zombieCooldownOverlayByCard = new HashMap<>();
    private Map<PlantType, PlantData> dataByType = new HashMap<>();

    public GameUiModal(Runnable onPauseRequested) {
        super();
        setFillParent(true);
        top();
        setVisible(false);

        Table topBar = new Table();
        topBar.top().left();
        topBar.pad(10);

        // Sun bank with a +25 cheat button, matching the reference HUD (phase-0-group-51).
        Table sunRow = new Table();
        addSunButton = createCheatButton();
        addSunButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isDebugModeEnabled()) return;
                GameContext context = AppContext.getInstance().getGameContext();
                if (context == null) return;
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
        topBar.add(sunRow).left().row();

        // Plant food bank with a +1 cheat button, matching the reference HUD (phase-0-group-51).
        Table foodRow = new Table();
        addFoodButton = createCheatButton();
        addFoodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isDebugModeEnabled()) return;
                GameContext context = AppContext.getInstance().getGameContext();
                if (context == null) return;
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
        rightControls.add(pauseButton).right().size(58f);

        add(topBar).top().left().expandX().fillX();
        add(rightControls).top().right().padTop(15).padRight(15);
        row();

        // Seed-packet tray: vertical column pinned to the left edge, below the top bar.
        cardsBarTable = new Table();
        cardsBarTable.top();
        cardsBarTable.defaults().pad(-2f).size(SLOT_WIDTH, SLOT_HEIGHT);

        Table leftColumnWrapper = new Table();
        leftColumnWrapper.top().left();
        leftColumnWrapper.add(cardsBarTable);

        // Seed-packet tray tucked right under the sun bank: the top bar only keeps
        // the sun row, and padTop(10) starts the first card just under the sun
        // amount display (the food row moved to the bottom overlay).
        add(leftColumnWrapper).left().top().colspan(2).padLeft(15).padTop(-17);

        // Plant food pinned at normalized (0.25, 0.1): centered on x = 0.25 * 1280 = 320
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
            if (shovelButton != null) shovelButton.setChecked(false);
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
     * origin) coordinates — the target the flying diamond-drop animation arcs toward.
     */
    public com.badlogic.gdx.math.Vector2 getGemWalletStagePosition() {
        if (gemIcon == null) {
            return new com.badlogic.gdx.math.Vector2(980f, 695f);
        }
        return gemIcon.localToStageCoordinates(
            new com.badlogic.gdx.math.Vector2(gemIcon.getWidth() / 2f, gemIcon.getHeight() / 2f));
    }

    /**
     * The "+" cheat button used next to the sun and plant food banks, using the same
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

    private boolean isDebugModeEnabled() {
        User user = AppContext.getInstance().getCurrentUser();
        return user != null && user.getSetting() != null && user.getSetting().isDebugMode();
    }

    public void setSelectedCard(PlantCard card) {
        this.selectedCard = card;
        if (card != null) {
            this.shovelSelected = false;
            if (shovelButton != null) shovelButton.setChecked(false);
        }
        updateCardStyles();
    }

    public void setOnShovelRequested(Runnable onShovelRequested) {
        this.onShovelRequested = onShovelRequested;
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

    public void initCards() {
        cardsBarTable.clearChildren();
        slotByCard.clear();
        cooldownOverlayByCard.clear();
        zombieSlotByCard.clear();
        zombieCooldownOverlayByCard.clear();
        selectedCard = null;
        selectedZombieCard = null;

        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null) return;

        List<PlantData> allData = PlantData.loadAll();
        dataByType = new HashMap<>();
        for (PlantData data : allData) {
            dataByType.put(data.type, data);
        }

        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc) {
                cardsBarTable.add(buildSlot(pc, false)).row();
            } else if (card instanceof ZombieCard zc) {
                cardsBarTable.add(buildZombieSlot(zc)).row();
            }
        }
        updateCardStyles();
    }

    public void syncCards() {
        GameContext context = AppContext.getInstance().getGameContext();
        if (context == null) return;

        boolean changed = false;
        for (Card card : context.getCards()) {
            if (card instanceof PlantCard pc && !slotByCard.containsKey(pc)) {
                cardsBarTable.add(buildSlot(pc, true)).row();
                changed = true;
            }
        }
        if (changed) {
            updateCardStyles();
        }
    }

    private Table buildSlot(PlantCard pc, boolean isConveyor) {
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

        Image cooldownOverlay = new Image(new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0f, 0f, 0f, 0.6f))));
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

    private Table buildZombieSlot(ZombieCard zc) {
        Skin skin = PvzSkin.get();
        Drawable fallback = skin.newDrawable("white_pixel", new Color(0.35f, 0.15f, 0.15f, 1f));

        Table slot = new Table();
        com.badlogic.gdx.graphics.g2d.TextureRegion bgRegion = PvZ2.textureBank.region("IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY");
        if (bgRegion != null) {
            slot.setBackground(new TextureRegionDrawable(bgRegion));
        } else {
            slot.setBackground(new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0.4f, 0.1f, 0.1f, 0.85f))));
        }

        Stack stack = new Stack();

        String artPath = "textures/zombies/" + zc.getZombieType().name() + ".png";
        if (com.badlogic.gdx.Gdx.files.internal(artPath).exists()) {
            com.badlogic.gdx.graphics.Texture tex = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal(artPath));
            tex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear, com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
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

        Image cooldownOverlay = new Image(new TextureRegionDrawable(MenuUiKit.solidTexture(new Color(0f, 0f, 0f, 0.6f))));
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
        if (context == null) return;

        syncCards();

        // The + buttons only work while Debug Mode is enabled in settings.
        boolean debug = isDebugModeEnabled();
        addSunButton.setTouchable(debug ? Touchable.enabled : Touchable.disabled);
        addFoodButton.setTouchable(debug ? Touchable.enabled : Touchable.disabled);
        addSunButton.setColor(debug ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 0.6f));
        addFoodButton.setColor(debug ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 0.6f));

        sunLabel.setText(String.valueOf(context.getCurrentSun()));

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
    }

    private void updateCardStyles() {
        for (Map.Entry<PlantCard, Table> entry : slotByCard.entrySet()) {
            PlantCard pc = entry.getKey();
            Table slot = entry.getValue();
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
