package com.pvz.models.entities.plants.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.pvz.models.Constants;
import com.pvz.models.entities.plants.data.GrowthProfile;
import com.pvz.models.entities.plants.data.LevelUpgrade;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.ProductionKind;
import com.pvz.models.entities.plants.data.StatKey;
import com.pvz.models.entities.plants.data.StatModifier;
import com.pvz.models.entities.plants.enums.PlantCategory;
import com.pvz.models.entities.plants.enums.PlantTag;
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
     * Resolves a {@link PlantPropertySheet} for the given type from the config
     * registry (plant_actions.json), or {@code null} if the type is unknown.
     */
    public PlantPropertySheet resolveSheet(PlantType type) {
        PlantJsonConfig cfg = configs.get(type);
        return cfg != null ? toSheet(cfg) : null;
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
                .tags(parseTags(cfg.tags))
                .description(cfg.description == null ? "" : cfg.description)
                .onPlantFoodDescription(cfg.onPlantFoodDescription == null ? "" : cfg.onPlantFoodDescription)
                .overallDescription(cfg.overallDescription == null ? "" : cfg.overallDescription)
                .funDescription(cfg.funDescription == null ? "" : cfg.funDescription)
                .plantAttackConfig(cfg.attackConfig)
                .plantFeedConfig(cfg.feedConfig)
                .pamAnimationConfig(cfg.pamAnimationConfig)
                .levelUpgrades(convertUpgrades(cfg.levelUpgrades))
                .build();
    }

    private List<PlantTag> parseTags(String tagsStr) {
        List<PlantTag> result = new ArrayList<>();
        if (tagsStr == null || tagsStr.isBlank()) return result;
        for (String t : tagsStr.split(",")) {
            String trimmed = t.trim();
            if (trimmed.isEmpty()) continue;
            try {
                result.add(PlantTag.valueOf(trimmed.toUpperCase().replace("-", "_").replace(" ", "_")));
            } catch (IllegalArgumentException e) {
                System.err.println("[PlantConfigRegistry] Unknown tag: '" + trimmed + "'");
            }
        }
        return result;
    }

    private List<LevelUpgrade> convertUpgrades(List<LevelUpgradeConfig> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        List<LevelUpgrade> result = new ArrayList<>();
        for (LevelUpgradeConfig cfg : raw) {
            List<StatModifier> mods = new ArrayList<>();
            if (cfg.stats != null) {
                for (LevelUpgradeStatConfig sc : cfg.stats) {
                    mods.add(new StatModifier(StatKey.valueOf(sc.stat), sc.delta));
                }
            }
            List<String> flags = cfg.flags != null ? List.of(cfg.flags) : List.of();
            result.add(new LevelUpgrade(cfg.level, mods, flags));
        }
        return result;
    }
}
