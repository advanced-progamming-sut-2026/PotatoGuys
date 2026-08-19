package com.pvz.models.entities.plants.config;

import java.util.EnumMap;
import java.util.Map;

import com.pvz.models.Constants;
import com.pvz.models.entities.plants.data.GrowthProfile;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.data.ProductionKind;
import com.pvz.models.entities.plants.enums.PlantCategory;
import com.pvz.models.entities.plants.enums.PlantType;

public final class PlantConfigRegistry {

    private static final PlantConfigRegistry INSTANCE = new PlantConfigRegistry();

    private final Map<PlantType, PlantJsonConfig> configs = new EnumMap<>(PlantType.class);

    private PlantConfigRegistry() {
        load();
    }

    public static PlantConfigRegistry getInstance() {
        return INSTANCE;
    }

    public PlantJsonConfig getConfig(PlantType type) {
        return configs.get(type);
    }

    /**
     * Resolves a {@link PlantPropertySheet} for the given type, first trying
     * the profile registry and falling back to the config registry (for mints
     * and other plants that only exist in plant_actions.json).
     */
    public PlantPropertySheet resolveSheet(PlantType type) {
        PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(type);
        if (sheet != null) return sheet;
        PlantJsonConfig cfg = configs.get(type);
        if (cfg != null) return toSheet(cfg);
        return null;
    }

    private void load() {
        for (PlantJsonConfig cfg : PlantActionConfigLoader.loadFromFile(Constants.PLANT_ACTIONS_PATH)) {
            try {
                configs.put(PlantType.valueOf(cfg.type), cfg);
            } catch (IllegalArgumentException e) {
                System.err.println("[PlantConfigRegistry] Skipping entry with unknown type '"
                        + cfg.type + "' (id=" + cfg.id + "): " + e.getMessage());
            }
        }
    }

    /** Adapts a {@link PlantJsonConfig} into the {@link PlantPropertySheet} shape {@code Plant} already understands.
     *  When a profile exists for the same type it also copies the profile's tags, level-upgrades and damage
     *  profile, so level-scaling and flavour flags keep working for config-driven plants. */
    public PlantPropertySheet toSheet(PlantJsonConfig cfg) {
        GrowthProfile growth = null;
        if (cfg.attackConfig instanceof SunProducerActionConfig sunConfig) {
            if (sunConfig.productionKind == ProductionKind.STAGED) {
                growth = new GrowthProfile(sunConfig.stageIntervalSeconds);
            }
        }

        PlantPropertySheet.Builder builder = new PlantPropertySheet.Builder(cfg.id, cfg.name, PlantType.valueOf(cfg.type))
                .category(PlantCategory.valueOf(cfg.category))
                .sunCost(cfg.sunCost)
                .baseHp(cfg.baseHp)
                .actionIntervalSeconds(cfg.actionIntervalSeconds)
                .rechargeSeconds(cfg.rechargeSeconds)
                .growth(growth)
                .description(cfg.description == null ? "" : cfg.description)
                .plantAttackConfig(cfg.attackConfig)
                .plantFeedConfig(cfg.feedConfig)
                .pamAnimationConfig(cfg.pamAnimationConfig);

        PlantPropertySheet profile = PlantRegistry.getInstance().getSheet(PlantType.valueOf(cfg.type));
        if (profile != null) {
            builder.tags(profile.getTags())
                   .levelUpgrades(profile.getLevelUpgrades())
                   .damage(profile.getDamage());
        }

        return builder.build();
    }
}
