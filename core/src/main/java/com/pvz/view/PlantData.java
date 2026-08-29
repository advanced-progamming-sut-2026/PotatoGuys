package com.pvz.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.pvz.PvZ2;
import com.pvz.controller.CollectionController;
import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.config.PlantJsonConfig;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.plants.data.DamageProfile;
import com.pvz.models.entities.plants.data.LevelUpgrade;
import com.pvz.models.entities.plants.data.PlantDescriptions;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.data.PlantStatResolver;
import com.pvz.models.entities.plants.enums.PlantCategory;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.user.Collection;
import com.pvz.models.user.MyPlant;
import com.pvz.models.user.Profile;
import com.pvz.models.user.User;

/**
 * Read-only adapter that flattens everything the Collection UI needs about a single plant:
 * the static profile sheet, the plant_actions config (PAM + labels), and the current
 * player's owned instance / seed packets from the save.
 */
public final class PlantData {

    private static final Map<PlantType, String> PACKET_ID_EXCEPTIONS = new HashMap<>();
    private static final Set<PlantType> NO_PACKET_PLANTS =
            Set.of(PlantType.Rotobaga, PlantType.GooPeashooter, PlantType.Cattail);

    static {
        PACKET_ID_EXCEPTIONS.put(PlantType.MegaGatlingPea, "IMAGE_UI_PACKETS_MEGAGATLING");
        PACKET_ID_EXCEPTIONS.put(PlantType.CherryBomb, "IMAGE_UI_PACKETS_CHERRY_BOMB");
        PACKET_ID_EXCEPTIONS.put(PlantType.IcebergLettuce, "IMAGE_UI_PACKETS_ICEBURG");
    }

    public final PlantType type;
    public final PlantPropertySheet sheet;
    public final PlantJsonConfig config;

    private PlantData(PlantType type, PlantPropertySheet sheet, PlantJsonConfig config) {
        this.type = type;
        this.sheet = sheet;
        this.config = config;
    }

    public static List<PlantData> loadAll() {
        List<PlantData> result = new ArrayList<>();
        PlantRegistry registry = PlantRegistry.getInstance();
        PlantConfigRegistry configs = PlantConfigRegistry.getInstance();
        for (PlantType type : PlantType.values()) {
            PlantPropertySheet sheet = registry.getSheet(type);
            PlantJsonConfig cfg = configs.getConfig(type);
            if (sheet == null && cfg != null) {
                sheet = configs.toSheet(cfg);
            }
            if (sheet == null) continue;
            result.add(new PlantData(type, sheet, cfg));
        }
        result.sort((a, b) -> Integer.compare(a.sheet.getId(), b.sheet.getId()));
        return result;
    }

    /** Resolves the read-only plant data for a single plant type (used by in-game
     *  placement previews to look up the idle PAM path without loading every plant). */
    public static PlantData forType(PlantType type) {
        if (type == null) return null;
        PlantRegistry registry = PlantRegistry.getInstance();
        PlantConfigRegistry configs = PlantConfigRegistry.getInstance();
        PlantPropertySheet sheet = registry.getSheet(type);
        PlantJsonConfig cfg = configs.getConfig(type);
        if (sheet == null && cfg != null) {
            sheet = configs.toSheet(cfg);
        }
        if (sheet == null) return null;
        return new PlantData(type, sheet, cfg);
    }

    private static Profile currentProfile() {
        User user = AppContext.getInstance().getCurrentUser();
        return user != null ? user.getProfile() : null;
    }

    private static Collection currentCollection() {
        Profile profile = currentProfile();
        return profile != null ? profile.getCollection() : null;
    }

    /** The owned plant instance, read live from the save so it is always up to date. */
    public MyPlant myPlant() {
        Collection collection = currentCollection();
        return collection != null ? collection.getPlant(type) : null;
    }

    /** Seed packets owned for this plant, read live from the save. */
    public int seedPackets() {
        Collection collection = currentCollection();
        return collection != null ? collection.getSeedPackets(type) : 0;
    }

    public boolean isUnlocked() {
        return myPlant() != null;
    }

    public boolean isBoosted() {
        MyPlant owned = myPlant();
        return owned != null && owned.isBoosted();
    }

    public int getLevel() {
        MyPlant owned = myPlant();
        return owned != null ? owned.getLevel() : 0;
    }

    public String getName() {
        return sheet.getName();
    }

    public String onPlantFoodDescription() {
        PlantDescriptions.Descriptions d = PlantDescriptions.forType(type);
        if (d != null) return d.plantFood();
        return sheet.getOnPlantFoodDescription();
    }

    public String overallDescription() {
        PlantDescriptions.Descriptions d = PlantDescriptions.forType(type);
        if (d != null) return d.overall();
        return sheet.getOverallDescription();
    }

    public String funDescription() {
        PlantDescriptions.Descriptions d = PlantDescriptions.forType(type);
        if (d != null) return d.fun();
        return sheet.getFunDescription();
    }

    public PlantCategory getCategory() {
        return sheet.getCategory();
    }

    // ── art ───────────────────────────────────────────────────────────────────

    public String cardImageId() {
        String special = PACKET_ID_EXCEPTIONS.get(type);
        if (special != null) return special;
        if (NO_PACKET_PLANTS.contains(type)) {
            switch (type) {
                case GooPeashooter: return "IMAGE_PLANT_GOOPEASHOOTER_GOOPEASHOOTER_206X206";
                case Rotobaga:      return "IMAGE_PLANT_ROTORUTABAGA_ROTORUTABAGA_123X123";
                default:            return "IMAGE_UI_PACKETS_EMPTY_PACKET";
            }
        }
        return "IMAGE_UI_PACKETS_" + type.name().toUpperCase();
    }

    public Drawable cardDrawable() {
        if (type == PlantType.Cattail) {
            Texture tex = new Texture(Gdx.files.internal("textures/greenhouse/plants/cattail.png"));
            return new TextureRegionDrawable(new TextureRegion(tex));
        }
        if (type == PlantType.Piercemint) {
            Texture tex = new Texture(Gdx.files.internal("textures/greenhouse/plants/piercemint.png"));
            return new TextureRegionDrawable(new TextureRegion(tex));
        }
        if (type == PlantType.catTailmint) {
            Texture tex = new Texture(Gdx.files.internal("textures/greenhouse/plants/img.png"));
            return new TextureRegionDrawable(new TextureRegion(tex));
        }
        return regionDrawable(cardImageId());
    }

    public Drawable familyDrawable() {
        return regionDrawable(mintFamImageId(getCategory()));
    }

    public String familyImageId() {
        return mintFamImageId(getCategory());
    }

    public String familyName() {
        switch (getCategory()) {
            case SUN_PRODUCER: return "Sun";
            case SHOOTER:
            case STRIKE_THROUGH: return "Peashooter";
            case HOMING: return "Magic";
            case LOBBER: return "Lobber";
            case EXPLOSIVE: return "Explosive";
            case MELEE: return "Melee";
            case WALL_NUT: return "Defense";
            case MODIFIER: return "Magic";
            default: return "Sun";
        }
    }

    public String tagsText() {
        List<PlantTag> tags = sheet.getTags();
        if (tags == null || tags.isEmpty()) return "—";
        StringBuilder sb = new StringBuilder();
        for (PlantTag tag : tags) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(capitalize(tag.name()));
        }
        return sb.toString();
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    // ── animation ─────────────────────────────────────────────────────────────

    public String pamPath() {
        PamAnimationConfig pam = pamConfig();
        if (pam != null && pam.pamFilePath != null) return pam.pamFilePath;

        String upper = type.name().toUpperCase();
        String[] candidates = {
            "768/INITIAL/PLANT/" + upper + "/" + upper + ".PAM",
            "768/FULL/PLANT/" + upper + "/" + upper + ".PAM",
        };
        for (String path : candidates) {
            if (Gdx.files.internal("assets/pvz-assets/IMAGES/" + path).exists()) return path;
        }
        return null;
    }

    public String idleLabel() {
        PamAnimationConfig pam = pamConfig();
        return pam != null && pam.idleLabel != null ? pam.idleLabel : "idle";
    }

    private PamAnimationConfig pamConfig() {
        if (config != null && config.pamAnimationConfig != null) return config.pamAnimationConfig;
        if (sheet.pamAnimationConfig != null) return sheet.pamAnimationConfig;
        return null;
    }

    // ── stats ─────────────────────────────────────────────────────────────────

    public int sunCost() {
        return PlantStatResolver.resolve(sheet, getLevel()).getSunCost();
    }

    public float toughness() {
        return PlantStatResolver.resolve(sheet, getLevel()).getMaxHp();
    }

    public Float rechargeSeconds() {
        return PlantStatResolver.resolve(sheet, getLevel()).getRechargeSeconds();
    }

    public String damageExpression() {
        DamageProfile damage = sheet.getDamage();
        if (damage == null) return "—";
        float scaledDamage = PlantStatResolver.resolve(sheet, getLevel()).getDamage();
        float delta = scaledDamage - damage.getValue();
        switch (damage.getKind()) {
            case NONE: return "—";
            case FIXED: return String.valueOf((int) scaledDamage);
            case MULTI_SHOT: return ((int) scaledDamage) + " × " + damage.getCount();
            case STAGED: {
                float[] stages = damage.getStages();
                if (stages.length == 0) return String.valueOf((int) scaledDamage);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < stages.length; i++) {
                    if (i > 0) sb.append(" / ");
                    sb.append((int) (stages[i] + delta));
                }
                return sb.toString();
            }
            case FIRE: return String.valueOf((int) scaledDamage) + " fire";
            case INSTA_KILL: return "Instant";
            default: return "—";
        }
    }

    // ── leveling ──────────────────────────────────────────────────────────────

    public int maxLevel() {
        List<LevelUpgrade> upgrades = sheet.getLevelUpgrades();
        return upgrades == null || upgrades.isEmpty() ? 1 : upgrades.size() + 1;
    }

    public boolean isMaxLevel() {
        return getLevel() >= maxLevel();
    }

    public int requiredSeedPackets() {
        if (!isUnlocked() || isMaxLevel()) return 1;
        return CollectionController.requiredPacketsForLevel(getLevel());
    }

    public int requiredCoins() {
        if (!isUnlocked() || isMaxLevel()) return 1;
        return CollectionController.requiredCoinsForLevel(getLevel());
    }

    public float xpFraction() {
        if (!isUnlocked()) return 0f;
        if (isMaxLevel()) return 1f;
        return Math.min(1f, (float) seedPackets() / requiredSeedPackets());
    }

    // ── shared helpers ────────────────────────────────────────────────────────

    public static Drawable regionDrawable(String imageId) {
        if (imageId == null) return null;
        TextureRegion region = PvZ2.textureBank.region(imageId);
        if (region == null) return null;
        return new TextureRegionDrawable(region);
    }

    public static Drawable regionDrawableOr(String imageId, Drawable fallback) {
        Drawable drawable = regionDrawable(imageId);
        return drawable != null ? drawable : fallback;
    }

    public static String mintFamImageId(PlantCategory category) {
        switch (category) {
            case SUN_PRODUCER: return "IMAGE_UI_PACKETS_MINTFAM_SUN";
            case SHOOTER:
            case STRIKE_THROUGH: return "IMAGE_UI_PACKETS_MINTFAM_PEASHOOTER";
            case HOMING: return "IMAGE_UI_PACKETS_MINTFAM_MAGIC";
            case LOBBER: return "IMAGE_UI_PACKETS_MINTFAM_LOBBER";
            case EXPLOSIVE: return "IMAGE_UI_PACKETS_MINTFAM_EXPLOSIVE";
            case MELEE: return "IMAGE_UI_PACKETS_MINTFAM_MELEE";
            case WALL_NUT: return "IMAGE_UI_PACKETS_MINTFAM_DEFENSE";
            case MODIFIER: return "IMAGE_UI_PACKETS_MINTFAM_MAGIC";
            default: return "IMAGE_UI_PACKETS_MINTFAM_SUN";
        }
    }

    public static final List<PlantCategory> ALL_CATEGORIES = Collections.unmodifiableList(
            List.of(PlantCategory.values()));
}
