package com.pvz.view;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.modes.variants.IZombieMode;

/**
 * Lightweight read-only data holder for a zombie type shown in the
 * {@link com.pvz.view.game.ZombieSelectModal} pre-game selection screen.
 *
 * <p>Mirrors the role that {@link PlantData} plays for the plant select modal,
 * but far simpler — zombies have no player-owned levels, boost status, or
 * seed packets. The only data needed for the selection grid is the display
 * name, sun cost, and a portrait image.
 */
public final class ZombieData {

    public final ZombieType type;
    private final String name;
    private final int sunCost;

    private ZombieData(ZombieType type, String name, int sunCost) {
        this.type = type;
        this.name = name;
        this.sunCost = sunCost;
    }

    /**
     * Builds a {@link ZombieData} for each zombie type allowed by the level.
     * The order matches the list passed in.
     */
    public static List<ZombieData> loadForLevel(List<ZombieType> allowed) {
        List<ZombieData> result = new ArrayList<>();
        if (allowed == null) return result;
        for (ZombieType type : allowed) {
            if (type == null) continue;
            String name = resolveName(type);
            int cost = IZombieMode.sunCostFor(type);
            result.add(new ZombieData(type, name, cost));
        }
        return result;
    }

    /**
     * Builds a {@link ZombieData} for every playable zombie in the game (those
     * with a defined sun cost), so the zombie player can pick from the full
     * roster rather than only the level's default pool.
     */
    public static List<ZombieData> loadAll() {
        return loadForLevel(IZombieMode.playableZombieTypes());
    }

    public String getName() {
        return name;
    }

    public int getSunCost() {
        return sunCost;
    }

    // ── art ───────────────────────────────────────────────────────────────────

    private static final String ZOMBIE_ART_DIR = "textures/zombies/";

    /**
     * Resolves the portrait path for a type, trying both the enum-name case
     * ("ALLSTAR.png") and the lowercase file name ("allstar.png") since the
     * asset pack is inconsistent. Returns a path that is guaranteed to exist,
     * or {@code null} if no portrait exists for the type.
     */
    private static String portraitPath(ZombieType type) {
        String name = type.name();
        String direct = ZOMBIE_ART_DIR + name + ".png";
        if (Gdx.files.internal(direct).exists())
            return direct;
        String lower = ZOMBIE_ART_DIR + name.toLowerCase() + ".png";
        if (Gdx.files.internal(lower).exists())
            return lower;
        return null;
    }

    /** Loads the static PNG portrait for this zombie type. */
    public Table portraitWidget() {
        Table wrap = new Table();
        String path = portraitPath(type);
        if (path != null) {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Image image = new Image(new TextureRegionDrawable(texture));
            image.setScaling(Scaling.fit);
            wrap.add(image).grow();
        } else {
            Drawable fallback = new TextureRegionDrawable(
                    MenuUiKit.solidTexture(new com.badlogic.gdx.graphics.Color(0.28f, 0.32f, 0.3f, 1f)));
            wrap.setBackground(fallback);
            com.badlogic.gdx.scenes.scene2d.ui.Label label =
                    new com.badlogic.gdx.scenes.scene2d.ui.Label(name, pvz.skin.PvzSkin.get());
            label.setFontScale(0.7f);
            label.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            label.setWrap(true);
            label.setAlignment(com.badlogic.gdx.utils.Align.center);
            wrap.add(label).width(100f).pad(6f);
        }
        return wrap;
    }

    /** Loads the portrait as a {@link Drawable} (for the mini slot cards). */
    public Drawable portraitDrawable() {
        String path = portraitPath(type);
        if (path != null) {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return new TextureRegionDrawable(new TextureRegion(texture));
        }
        return new TextureRegionDrawable(
                MenuUiKit.solidTexture(new com.badlogic.gdx.graphics.Color(0.28f, 0.32f, 0.3f, 1f)));
    }

    // ── name resolution ───────────────────────────────────────────────────────

    /**
     * Resolves a human-readable display name for the zombie type.
     * Falls back to a humanized version of the enum constant name
     * (e.g. "ICE_AGE_CONE" → "Ice Age Cone").
     */
    private static String resolveName(ZombieType type) {
        com.pvz.models.entities.zombies.data.ZombiePropertySheet sheet =
                com.pvz.models.entities.zombies.data.ZombieRegistry.getInstance()
                        .getSheet(type.getAlias());
        if (sheet != null && sheet.getAlias() != null) {
            // The alias is a technical ID like "ZombieMummyDefault" — use
            // the humanized enum name instead for the UI.
        }
        return displayName(type);
    }

    /** "ICE_AGE_CONE" → "Ice Age Cone" */
    private static String displayName(ZombieType type) {
        String[] words = type.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
