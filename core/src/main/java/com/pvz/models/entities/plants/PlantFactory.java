package com.pvz.models.entities.plants;

import com.pvz.models.entities.plants.actions.*;
import com.pvz.models.entities.plants.actions.shooters.ShooterAction;
import com.pvz.models.entities.plants.config.PlantActionConfig;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.config.PlantJsonConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig;
import com.pvz.models.entities.plants.config.SunProducerActionConfig;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.data.PlantRegistry;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.GameContext;

public class PlantFactory {

    private static final PlantRegistry REGISTRY = PlantRegistry.getInstance();
    private static final PlantConfigRegistry CONFIG_REGISTRY = PlantConfigRegistry.getInstance();

    public Plant create(PlantType type, int col, int lane, int level, boolean boosted, GameContext ctx) {
        PlantJsonConfig config = CONFIG_REGISTRY.getConfig(type);
        if (config != null) {
            return createFromConfig(config, col, lane, level, boosted, ctx);
        }

        PlantPropertySheet sheet = REGISTRY.getSheet(type);
        if (sheet == null) {
            throw new IllegalArgumentException("Unknown plant type: " + type);
        }
        PlantAction feedAction = new ConfigFeedAction(null); // legacy, sheet-driven Plant Food
        return new Plant(sheet, buildAction(sheet), feedAction, col, lane, level, boosted, ctx);
    }

    public Plant create(PlantType type, int col, int lane, GameContext ctx) {
        return create(type, col, lane, 1, false, ctx);
    }

    /** Config-driven path for {@code SHOOTER}/{@code STRIKE_THROUGH}/{@code SUN_PRODUCER} plants (see {@code plant_actions.json}). */
    private Plant createFromConfig(PlantJsonConfig config, int col, int lane, int level, boolean boosted, GameContext ctx) {
        PlantPropertySheet sheet = CONFIG_REGISTRY.toSheet(config);
        PlantAction attackAction = buildConfigAction(config.attackConfig, sheet.getActionIntervalSeconds());
        PlantAction feedAction = new ConfigFeedAction(config.feedConfig);
        return new Plant(sheet, attackAction, feedAction, col, lane, level, boosted, ctx);
    }

    private PlantAction buildConfigAction(PlantActionConfig config, Float intervalSeconds) {
        if (intervalSeconds == null) {
            return PassiveAction.INSTANCE;
        }
        if (config instanceof ShooterActionConfig shooterConfig) {
            return new ShooterAction(intervalSeconds, shooterConfig);
        }
        if (config instanceof SunProducerActionConfig sunConfig) {
            return new SunProducerAction(intervalSeconds, sunConfig);
        }
        return PassiveAction.INSTANCE;
    }

    private PlantAction buildAction(PlantPropertySheet sheet) {
        if (sheet.getType() == PlantType.GraveBuster) {
            return new GraveBusterAction();
        }
        if (sheet.getType() == PlantType.HotPotato) {
            return new HotPotatoAction();
        }
        if (sheet.isMint()) {
            return new FamilyBuffAction();
        }
        Float interval = sheet.getActionIntervalSeconds();
        return switch (sheet.getCategory()) {
            case SUN_PRODUCER, SHOOTER, STRIKE_THROUGH ->
                // Migrated to the config-driven path above; only reached when no plant_actions.json entry exists yet.
                PassiveAction.INSTANCE;
            case LOBBER -> interval != null ? new LobberAction(interval) : PassiveAction.INSTANCE;
            case MELEE -> interval != null ? new MeleeAction(interval) : PassiveAction.INSTANCE;
            case HOMING -> interval != null ? new HomingAction(interval) : PassiveAction.INSTANCE;
            case EXPLOSIVE -> new TriggeredExplosiveAction(sheet.hasTag(PlantTag.TRAP));
            case WALL_NUT, MODIFIER -> PassiveAction.INSTANCE;
        };
    }
}


