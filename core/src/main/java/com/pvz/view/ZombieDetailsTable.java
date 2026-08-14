package com.pvz.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.entities.zombies.data.ZombiePropertySheet;
import com.pvz.models.entities.zombies.data.ZombieRegistry;
import com.pvz.models.entities.zombies.data.ZombieStatEntry;

import pvz.skin.PvzSkin;

/**
 * Zombie detail popup, opened from a clicked {@link ZombieCard}. Sized and centered by its
 * caller (CollectionMenu wraps it in a 840x920 cell), same as PlantDetailsTable — it must NOT
 * call setFillParent() itself, since that would make it ignore the caller's size entirely and
 * blow up to match its parent overlay's full-screen bounds instead (that was the bug: the panel
 * came out far too large and badly positioned instead of a neat centered popup).
 *
 * <p>Mirrors Rey's ZombieDetailsTable layout and reuses her real, confirmed
 * texture-bank region IDs (back button, toughness/speed icons) since you
 * have the same asset pack.
 *
 * <p>What's genuinely different: your ZombiePropertySheet has NO flavor text
 * or descriptions at all (unlike PlantPropertySheet) — only mechanical stats.
 * Rather than invent an Almanac-style paragraph to match her white/yellow
 * description rows, this shows the real derived stats instead (health,
 * attack rate, armor).
 *
 * <p>Toughness/speed tier labels are read from real data (ZombieStatEntry's
 * toughness1-8 / speed0-5 tiers), but the English word per tier number is my
 * best-effort guess at the standard PopCap Almanac wording — not confirmed
 * against your actual asset text, so flag it if the wording looks off.
 */
public final class ZombieDetailsTable extends Table {

    private static final String BACK = "IMAGE_UI_ALMANAC_BUTTONS_HUD_BACK_NORMAL";
    private static final String BACK_PRESSED = "IMAGE_UI_ALMANAC_BUTTONS_HUD_BACK_SELECTED";
    private static final String TOUGHNESS_ICON = "IMAGE_UI_ALMANAC_ZOMBIES_ZOMBIETOUGHNESS_ICON";
    private static final String SPEED_ICON = "IMAGE_UI_ALMANAC_ZOMBIES_ZOMBIESPEED_ICON";

    private static final String[] TOUGHNESS_LABELS = {
        "", "Fragile", "Low", "Below Average", "Basic",
        "Protected", "Hardy", "Very High", "Ultimate"
    };
    private static final String[] SPEED_LABELS = {
        "Stationary", "Very Slow", "Slow", "Basic", "Fast", "Very Fast"
    };

    public ZombieDetailsTable(ZombieCard.ViewData data, Runnable onBack) {
        if (data == null) throw new IllegalArgumentException("data cannot be null");
        if (onBack == null) throw new IllegalArgumentException("onBack cannot be null");

        ZombieType type = data.type();
        ZombiePropertySheet sheet = ZombieRegistry.getInstance().getSheet(type.getAlias());

        setTouchable(Touchable.childrenOnly);
        setBackground(PvzSkin.get().newDrawable("white_pixel", Color.valueOf("183A78")));

        ImageButton backButton = createBackButton();
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ZombieDetailsTable.this.remove();
                onBack.run();
            }
        });

        Label nameLabel = new Label(displayName(type), PvzSkin.get(), "big");
        nameLabel.setColor(Color.WHITE);

        Table header = new Table();
        header.add(backButton).left().padLeft(20f).padTop(15f);
        header.add(nameLabel).expandX().center().padTop(15f);
        header.add().width(backButton.getPrefWidth()).padRight(20f);
        add(header).growX().row();

        Table body = new Table();
        body.add(createLeftSide(type)).width(400f).growY().top().padLeft(40f).padTop(25f);

        Table right = createRightSide(sheet);
        ScrollPane scroll = new ScrollPane(right, PvzSkin.get());
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setScrollingDisabled(true, false);
        body.add(scroll).grow();

        add(body).grow();
    }

    private Table createLeftSide(ZombieType type) {
        Table left = new Table();
        left.top();
        left.add(ZombieCard.pamPortrait(type)).size(320f, 400f);
        return left;
    }

    private Table createRightSide(ZombiePropertySheet sheet) {
        Table right = new Table();
        right.top().left();

        if (sheet == null) {
            Label missing = new Label("No data found for this zombie yet.", PvzSkin.get());
            missing.setColor(Color.WHITE);
            right.add(missing);
            return right;
        }

        Table stats = new Table();
        stats.top().left();
        stats.add(statColumn(TOUGHNESS_ICON, "TOUGHNESS", tierLabel(sheet, true))).width(230f).left();
        stats.add(statColumn(SPEED_ICON, "SPEED", tierLabel(sheet, false))).width(230f).left().row();
        right.add(stats).growX().left().row();

        if (!sheet.getArmorAliases().isEmpty()) {
            right.add(textStat("Armor", String.join(", ", sheet.getArmorAliases())))
                .width(500f).left().padTop(18f).row();
        }

        String statsText = "Health: " + Math.round(sheet.getHitPoints())
            + "   Attack: " + Math.round(sheet.getEatDps()) + "/s";
        right.add(textStat("Stats", statsText)).width(500f).left().padTop(18f).row();

        return right;
    }

    private Table statColumn(String iconId, String title, String value) {
        Table column = new Table();
        column.left();

        Image icon = new Image(regionOrSolid(iconId, new Color(0.47f, 0.38f, 0.71f, 1f)));
        icon.setScaling(Scaling.fit);

        Table text = new Table();
        text.left();
        Label titleLabel = new Label(title, PvzSkin.get());
        titleLabel.setColor(Color.LIGHT_GRAY);
        Label valueLabel = new Label(value, PvzSkin.get(), "big");
        valueLabel.setColor(Color.WHITE);
        text.add(titleLabel).left().row();
        text.add(valueLabel).left();

        column.add(icon).size(48f).top().padRight(8f);
        column.add(text).left().top();
        return column;
    }

    private Table textStat(String title, String value) {
        Table row = new Table();
        row.left();
        Label titleLabel = new Label(title + ":", PvzSkin.get(), "big");
        titleLabel.setColor(Color.valueOf("FFD75A"));
        Label valueLabel = new Label(value, PvzSkin.get(), "big");
        valueLabel.setColor(Color.WHITE);
        valueLabel.setWrap(true);
        row.add(titleLabel).top().left().padRight(6f);
        row.add(valueLabel).width(400f).top().left();
        return row;
    }

    private String tierLabel(ZombiePropertySheet sheet, boolean toughness) {
        for (ZombieStatEntry entry : sheet.getZombieStats()) {
            if (toughness && entry.isToughness()) {
                int tier = entry.getTier();
                return (tier >= 1 && tier < TOUGHNESS_LABELS.length) ? TOUGHNESS_LABELS[tier] : "-";
            }
            if (!toughness && entry.isSpeed()) {
                int tier = entry.getTier();
                return (tier >= 0 && tier < SPEED_LABELS.length) ? SPEED_LABELS[tier] : "-";
            }
        }
        return "-";
    }

    private String displayName(ZombieType type) {
        String[] words = type.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(word.charAt(0)).append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private ImageButton createBackButton() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = regionOrSolid(BACK, Color.GRAY);
        style.imageDown = regionOrSolid(BACK_PRESSED, Color.LIGHT_GRAY);
        style.imageOver = style.imageDown;
        return new ImageButton(style);
    }

    private static Drawable regionOrSolid(String regionId, Color fallbackColor) {
        Drawable fallback = new TextureRegionDrawable(MenuUiKit.solidTexture(fallbackColor));
        return PlantData.regionDrawableOr(regionId, fallback);
    }
}
