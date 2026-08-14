package com.pvz.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.PvZ2;

import java.util.Map;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.entities.zombies.armor.ArmorType;
import com.pvz.models.entities.zombies.data.ArmorPropertySheet;
import com.pvz.models.entities.zombies.data.ZombiePropertySheet;
import com.pvz.models.entities.zombies.data.ZombieRegistry;

import pvz.skin.PvzSkin;

/**
 * A single zombie card in the Collection menu's Zombies grid — mirrors Rey's
 * ZombieCard exactly in structure (READY/SELECTED background state, hover
 * highlight, lock state for undiscovered zombies), reusing her actual
 * confirmed texture-bank region IDs for the chrome since you have the same
 * asset pack.
 *
 * <p>Card size is NOT hardcoded — like Rey's version, it's read straight off
 * IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY's real pixel dimensions. That packet
 * art is portrait (taller than wide), unlike the plant packet, which is why
 * a copy-pasted landscape WIDTH/HEIGHT (from PlantCard) stretched it into
 * flat, wrong-shaped gray slabs instead of proper vertical cards.
 *
 * <p>The grid portrait is a static PNG per your latest request, not the PAM idle
 * animation this used to try (that's why most cards showed nothing at all, and the
 * few that did show something showed the wrong zombie — several ZombieType values
 * share/overlap in ZombieRegistry's alias-keyed sheets, so the PAM lookup silently
 * resolved to whichever sheet happened to match first instead of that zombie's own
 * art). Static art is now looked up straight off the ZombieType enum name, which is
 * always unique, so each zombie is guaranteed its own distinct image.
 * {@link ZombieDetailsTable}'s bigger preview still uses {@link #pamPortrait} —
 * that wasn't part of what you asked to change here.
 */
public final class ZombieCard extends Button {

    private static final String READY_BACKGROUND = "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY";
    private static final String SELECTED_BACKGROUND = "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_SELECTED";

    // Drop each zombie's art at textures/zombies/<TYPE_NAME>.png, e.g.
    // textures/zombies/BASIC.png, textures/zombies/CONEHEAD.png (exact ZombieType
    // enum constant name). Falls back to a tinted placeholder + the zombie's name
    // if a given file isn't there yet — never breaks the grid, never shows blank.
    private static final String ZOMBIE_ART_DIR = "textures/zombies/";

    // Used only if the real texture-bank region can't be found at all (e.g. assets not
    // wired in yet) — a portrait fallback so the grid still looks right, not a landscape one.
    private static final float FALLBACK_WIDTH = 130f;
    private static final float FALLBACK_HEIGHT = 190f;

    public record ViewData(ZombieType type, boolean unlocked) {
        public ViewData {
            if (type == null) throw new IllegalArgumentException("type cannot be null");
        }
    }

    private final ViewData data;
    private final Image stateBackground;
    private final float cardWidth;
    private final float cardHeight;
    private boolean hovered;

    public ZombieCard(ViewData data) {
        super(new ButtonStyle());
        if (data == null) throw new IllegalArgumentException("data cannot be null");
        this.data = data;

        TextureRegion backgroundRegion = PvZ2.textureBank.region(READY_BACKGROUND);
        this.cardWidth = backgroundRegion != null ? backgroundRegion.getRegionWidth() : FALLBACK_WIDTH;
        this.cardHeight = backgroundRegion != null ? backgroundRegion.getRegionHeight() : FALLBACK_HEIGHT;

        setSize(cardWidth, cardHeight);
        setProgrammaticChangeEvents(true);
        pad(0f);

        Stack cardStack = new Stack();
        cardStack.setTouchable(Touchable.disabled);

        stateBackground = new Image(regionOrSolid(READY_BACKGROUND, new Color(0.15f, 0.15f, 0.18f, 1f)));
        stateBackground.setScaling(Scaling.stretch);
        cardStack.add(stateBackground);

        if (data.unlocked()) {
            Table portraitLayer = new Table();
            portraitLayer.pad(10f);
            portraitLayer.add(pngPortrait(data.type())).grow();
            cardStack.add(portraitLayer);
        } else {
            setChecked(false);
            setDisabled(true);
            setTouchable(Touchable.disabled);
        }

        add(cardStack).size(cardWidth, cardHeight);
        refreshVisualState();

        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshVisualState();
            }
        });
        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hovered = true;
                refreshVisualState();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hovered = false;
                refreshVisualState();
            }
        });
    }

    public ViewData getData() {
        return data;
    }

    /** Static PNG portrait for the grid card — see the class doc for the asset path
     *  convention and why this replaced the old PAM-based one for the grid. */
    public static Table pngPortrait(ZombieType type) {
        Table wrap = new Table();
        String path = ZOMBIE_ART_DIR + type.name() + ".png";

        if (Gdx.files.internal(path).exists()) {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Image image = new Image(new TextureRegionDrawable(texture));
            image.setScaling(Scaling.fit);
            wrap.add(image).grow();
        } else {
            Gdx.app.log("ZombieCard", "art not found, looked at internal path: \"" + path
                + "\" (resolved to " + Gdx.files.internal(path).file().getAbsolutePath() + ")");
            wrap.setBackground(new TextureRegionDrawable(
                MenuUiKit.solidTexture(new Color(0.28f, 0.32f, 0.3f, 1f))));
            Label label = new Label(displayName(type), PvzSkin.get());
            label.setFontScale(0.7f);
            label.setColor(Color.WHITE);
            label.setWrap(true);
            label.setAlignment(com.badlogic.gdx.utils.Align.center);
            wrap.add(label).width(cardArtWidth()).pad(6f);
        }
        return wrap;
    }

    private static float cardArtWidth() {
        TextureRegion backgroundRegion = PvZ2.textureBank.region(READY_BACKGROUND);
        float width = backgroundRegion != null ? backgroundRegion.getRegionWidth() : FALLBACK_WIDTH;
        return width - 20f; // minus the same 10f pad on each side used around the portrait
    }

    /** "ICE_AGE_CONE" -> "Ice Age Cone", used as the fallback label until real art exists. */
    private static String displayName(ZombieType type) {
        String[] words = type.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /** Kept for {@link ZombieDetailsTable}'s larger preview, which still uses the PAM
     *  idle animation — not part of the grid fix you asked for here. */
    public static PamActor pamPortrait(ZombieType type) {
        ZombiePropertySheet sheet = ZombieRegistry.getInstance().getSheet(type.getAlias());
        ZombieAnimationConfig anim = sheet != null ? sheet.getAnimationConfig() : null;
        String pamPath = anim != null ? anim.pamFilePath : null;
        String idleLabel = anim != null ? anim.idleLabel : "idle";
        if (pamPath == null || pamPath.isBlank()) return null;
        return new PamActor(pamPath, idleLabel, armorParts(sheet));
    }

    /** Maps the zombie's armour aliases to their intact PAM part names, so the
     *  preview shows the armour that the shared sheet hides by default. */
    private static Map<String, Boolean> armorParts(ZombiePropertySheet sheet) {
        if (sheet == null || sheet.getArmorAliases().isEmpty()) return null;
        Map<String, Boolean> parts = new java.util.HashMap<>();
        for (String alias : sheet.getArmorAliases()) {
            ArmorPropertySheet armor = ZombieRegistry.getInstance().getArmorSheet(alias);
            if (armor == null) continue;
            String partName;
            try {
                partName = ArmorType.fromString(armor.getArmorType()).pamPartName();
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (partName != null) parts.put(partName, Boolean.TRUE);
        }
        return parts.isEmpty() ? null : parts;
    }

    private void refreshVisualState() {
        boolean highlighted = data.unlocked() && (isChecked() || hovered);
        String asset = highlighted ? SELECTED_BACKGROUND : READY_BACKGROUND;
        Color fallback = highlighted
            ? new Color(0.35f, 0.25f, 0.45f, 1f)
            : new Color(0.15f, 0.15f, 0.18f, 1f);
        stateBackground.setDrawable(regionOrSolid(asset, fallback));
    }

    /** Rey's real texture-bank region if it resolves, otherwise a plain solid fallback
     *  (never a crash) — same safety net used throughout the rest of this project. */
    private static Drawable regionOrSolid(String regionId, Color fallbackColor) {
        Drawable fallback = new TextureRegionDrawable(MenuUiKit.solidTexture(fallbackColor));
        return PlantData.regionDrawableOr(regionId, fallback);
    }

    @Override
    public float getPrefWidth() {
        return cardWidth;
    }

    @Override
    public float getPrefHeight() {
        return cardHeight;
    }
}
