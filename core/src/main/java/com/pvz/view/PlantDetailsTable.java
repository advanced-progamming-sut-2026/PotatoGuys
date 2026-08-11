package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import com.pvz.controller.CollectionController;

import pvz.skin.PvzSkin;

/**
 * Full details view for a single plant: header with back button, the plant's idle PAM animation
 * over a card background on the left, and a scrollable column of stat rows, family and a wrapped
 * description on the right. Locked plants get an UNLOCK (buy) button here instead of in the
 * collection grid.
 */
public class PlantDetailsTable extends Table {

    private static final Color DETAILS_BG = new Color(0x183A78FF);
    private static final Color TITLE_COLOR = new Color(0xB7C7E8FF);
    private static final Color FALLBACK_TINT = new Color(0.25f, 0.25f, 0.25f, 1f);

    public final PlantData data;
    public final ImageButton backButton;
    private final ProgressBar xpBar;
    private final Label xpLabel;
    private final Table actionArea;

    public PlantDetailsTable(PlantData data, Runnable onClose, Runnable onPurchase, Runnable onUpgrade) {
        this.data = data;
        Skin skin = PvzSkin.get();
        setBackground(skin.newDrawable("white_pixel", DETAILS_BG));
        Drawable fallback = skin.newDrawable("white_pixel", FALLBACK_TINT);

        backButton = new ImageButton(skin, "almanac");
        Drawable backNormal = PlantData.regionDrawable("IMAGE_UI_ALMANAC_BUTTONS_HUD_BACK_NORMAL");
        Drawable backSelected = PlantData.regionDrawable("IMAGE_UI_ALMANAC_BUTTONS_HUD_BACK_SELECTED");
        if (backNormal != null) {
            backButton.getStyle().imageUp = backNormal;
            backButton.getStyle().imageDown = backSelected;
            backButton.getStyle().imageOver = backSelected;
        }
        if (onClose != null) backButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                onClose.run();
                return true;
            }
        });

        Label name = new Label(data.getName(), skin, "big");
        name.setColor(Color.WHITE);
        name.setAlignment(Align.center);

        // ── header ───────────────────────────────────────────────────────────────
        pad(20f);
        top().left();
        add(backButton).size(75f, 70f).top().left().padRight(20f);
        add(name).top().center().expandX();
        row();
        add().height(30f).colspan(2);
        row();

        // ── left: animated preview (card background + idle PAM, no packet art) ──
        Table left = new Table();
        left.top().left();

        Stack preview = new Stack();
        Image cardBg = new Image(PlantData.regionDrawableOr(
                "IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_MODERN", fallback));
        preview.add(cardBg);
        PamIdleActor pam = PamIdleActor.forPlant(data);
        if (pam != null) {
            Table pamWrap = new Table();
            pamWrap.add(pam).size(390f, 390f);
            preview.add(pamWrap);
        }

        xpBar = new ProgressBar(0f, 1f, 0.01f, false, skin, "xp_yellow");

        xpLabel = new Label("", skin, "medium");
        xpLabel.setColor(Color.WHITE);

        actionArea = new Table();
        refreshState(onPurchase, onUpgrade);

        left.add(preview).size(470f, 470f);
        left.row();
        left.add(xpBar).width(430f).height(18f).padTop(14f);
        left.row();
        left.add(xpLabel).padTop(6f).left();
        left.row();
        left.add(actionArea).padTop(14f).left();

        // ── right: scrollable stats + family + description ──────────────────────
        Table right = new Table();
        right.top().left();
        addStatRow(right, statBlock("IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_SUNCOST",
                "SUN COST", String.valueOf(data.sunCost())));
        addStatRow(right, statBlock("IMAGE_UI_ALMANAC_PLANTS_RECHARGE_ICON",
                "RECHARGE", rechargeText()));
        addStatRow(right, statBlock("IMAGE_UI_ALMANAC_PLANTS_TOUGHNESS_ICON",
                "TOUGHNESS", String.valueOf((int) data.toughness())));
        addStatRow(right, statBlock("IMAGE_UI_ALMANAC_PLANTS_DAMAGE_ICON",
                "DAMAGE", data.damageExpression()));
        addStatRow(right, statBlock("IMAGE_UI_ALMANAC_PLANTS_RANGE_ICON",
                "RANGE", rangeText()));
        addStatRow(right, statBlock("IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_SPECIAL",
                "SPECIAL", data.tagsText()));
        addStatRow(right, familyBlock());

        String description = data.sheet.getDescription();
        if (description != null && !description.isBlank()) {
            Label desc = new Label(description, skin, "medium");
            desc.setWrap(true);
            desc.setFontScale(0.85f);
            desc.setColor(new Color(0xD8E6FFFF));
            desc.setAlignment(Align.topLeft);
            right.add(desc).width(RIGHT_W).left().padTop(18f).row();
        }

        ScrollPane cardsScroll = new ScrollPane(right, skin);
        cardsScroll.setFadeScrollBars(false);
        cardsScroll.setOverscroll(false, false);
        cardsScroll.setScrollingDisabled(true, false);

        Table content = new Table();
        content.top().left().padTop(10f);
        content.add(left).width(470f).top();
        content.add(cardsScroll).grow().padLeft(40f);
        add(content).colspan(2).growX().growY().top().left();
    }

    /** Fixed width of the right-hand stat column (matches Rey's almanac). */
    private static final float RIGHT_W = 500f;

    // ── right side building blocks ─────────────────────────────────────────────

    private void addStatRow(Table right, Actor stat) {
        right.add(stat).width(RIGHT_W).left().row();
    }

    /** Icon + gray title over a big white value, like the reference almanac page. */
    private Table statBlock(String iconId, String title, String value) {
        Table block = new Table();
        block.left();

        Drawable icon = PlantData.regionDrawableOr(iconId,
                PvzSkin.get().newDrawable("white_pixel", FALLBACK_TINT));

        Table text = new Table();
        text.left();
        Label titleLabel = new Label(title, PvzSkin.get(), "medium");
        titleLabel.setColor(TITLE_COLOR);
        Label valueLabel = new Label(value == null || value.isBlank() ? "—" : value,
                PvzSkin.get(), "big");
        valueLabel.setColor(Color.WHITE);
        text.add(titleLabel).left().row();
        text.add(valueLabel).left();

        block.add(new Image(icon)).size(48f, 48f).padRight(10f);
        block.add(text).left();
        return block;
    }

    /** Family badge + display name. */
    private Table familyBlock() {
        Table block = new Table();
        block.left();
        Drawable icon = PlantData.regionDrawableOr(data.familyImageId(),
                PvzSkin.get().newDrawable("white_pixel", FALLBACK_TINT));
        Label family = new Label(data.familyName(), PvzSkin.get(), "big");
        family.setColor(Color.WHITE);
        block.add(new Image(icon)).size(48f, 48f).padRight(10f);
        block.add(family).left();
        return block;
    }

    /**
     * Refreshes the progress bar, packet text and the action button from the live save:
     * locked plants get an UNLOCK button (with price), unlocked ones get a LEVEL UP button
     * showing how many packets the next level costs, and max-level plants get a disabled
     * MAX LEVEL marker.
     */
    private void refreshState(Runnable onPurchase, Runnable onUpgrade) {
        xpBar.setValue(data.xpFraction());
        xpLabel.setText(xpText());
        actionArea.clearChildren();
        if (!data.isUnlocked()) {
            TextButton buy = new TextButton(
                    "UNLOCK · " + CollectionController.PURCHASE_COIN_COST + " COINS", PvzSkin.get(), "green");
            buy.setColor(Color.WHITE);
            if (onPurchase != null) {
                buy.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        onPurchase.run();
                        return true;
                    }
                });
            } else {
                buy.setDisabled(true);
            }
            actionArea.add(buy).size(260f, 50f).left();
        } else if (data.isMaxLevel()) {
            TextButton max = new TextButton("MAX LEVEL", PvzSkin.get(), "green");
            max.setDisabled(true);
            max.setColor(TITLE_COLOR);
            actionArea.add(max).size(200f, 50f).left();
        } else {
            TextButton levelUp = new TextButton(
                    "LEVEL UP · " + data.requiredSeedPackets() + " PACKETS", PvzSkin.get(), "green");
            levelUp.setColor(Color.WHITE);
            if (onUpgrade != null) {
                levelUp.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        onUpgrade.run();
                        return true;
                    }
                });
            } else {
                levelUp.setDisabled(true);
            }
            actionArea.add(levelUp).size(320f, 50f).left();
        }
    }

    private String rangeText() {
        switch (data.getCategory()) {
            case SHOOTER:
            case STRIKE_THROUGH: return "Straight";
            case LOBBER: return "Lobbed";
            case MELEE: return "Close";
            case EXPLOSIVE: return "Area";
            case HOMING: return "Any lane";
            default: return "—";
        }
    }

    private String rechargeText() {
        Float recharge = data.rechargeSeconds();
        if (recharge == null) return "—";
        if (recharge <= 0.05f) return "Instant";
        if (recharge < 1f) return "Fast";
        return ((int) (float) recharge) + "s";
    }

    private String xpText() {
        if (!data.isUnlocked()) return "Locked — buy it to collect packets";
        if (data.isMaxLevel()) return "Level " + data.getLevel() + " · Max Level";
        return "Level " + data.getLevel() + " · " + data.seedPackets() + " / " + data.requiredSeedPackets() + " packets";
    }
}
