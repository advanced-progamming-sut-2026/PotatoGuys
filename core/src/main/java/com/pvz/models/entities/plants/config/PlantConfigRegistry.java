package com.pvz.models.entities.plants.config;

import java.util.EnumMap;
import java.util.Map;

import com.pvz.models.Constants;
import com.pvz.models.entities.plants.data.GrowthProfile;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
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

    /** Adapts a {@link PlantJsonConfig} into the {@link PlantPropertySheet} shape {@code Plant} already understands. */
    public PlantPropertySheet toSheet(PlantJsonConfig cfg) {
        GrowthProfile growth = null;
        if (cfg.attackConfig instanceof SunProducerActionConfig sunConfig) {
            if (sunConfig.productionKind == ProductionKind.STAGED) {
                growth = new GrowthProfile(sunConfig.stageIntervalSeconds);
            }
        }

        return new PlantPropertySheet.Builder(cfg.id, cfg.name, PlantType.valueOf(cfg.type))
                .category(PlantCategory.valueOf(cfg.category))
                .sunCost(cfg.sunCost)
                .baseHp(cfg.baseHp)
                .actionIntervalSeconds(cfg.actionIntervalSeconds)
                .rechargeSeconds(cfg.rechargeSeconds)
                .growth(growth)
                .description(cfg.description == null ? "" : cfg.description)
                .plantAttackConfig(cfg.attackConfig)
                .plantFeedConfig(cfg.feedConfig)
                .pamAnimationConfig(cfg.pamAnimationConfig)
                .build();
    }
}
