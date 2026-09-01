package com.pvz.models.entities.plants;

import com.pvz.models.entities.plants.actions.*;
import com.pvz.models.entities.plants.actions.explosive.ExplodeONutAction;
import com.pvz.models.entities.plants.actions.explosive.ExplosiveAction;
import com.pvz.models.entities.plants.actions.explosive.HotPotatoAction;
import com.pvz.models.entities.plants.actions.explosive.squash.SquashAction;
import com.pvz.models.entities.plants.actions.explosive.tangle.TangleKelpAction;
import com.pvz.models.entities.plants.config.*;
import com.pvz.models.entities.plants.config.explosive.ExplosiveConfig;
import com.pvz.models.entities.plants.config.explosive.SquashConfig;
import com.pvz.models.entities.plants.config.explosive.TangleKelpConfig;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.plants.fsm.WallNutState;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.games.GameContext;

public class PlantFactory {

    private static final PlantConfigRegistry CONFIG_REGISTRY = PlantConfigRegistry.getInstance();

    public Plant create(PlantType type, int col, int lane, int level, boolean boosted, GameContext ctx) {
        PlantJsonConfig config = CONFIG_REGISTRY.getConfig(type);
        if (config == null) {
            throw new IllegalArgumentException("Unknown plant type: " + type);
        }
        return createFromConfig(config, col, lane, level, boosted, ctx);
    }

    public Plant create(PlantType type, int col, int lane, GameContext ctx) {
        return create(type, col, lane, 1, false, ctx);
    }

    /**
     * Config-driven path for
     * {@code SHOOTER}/{@code STRIKE_THROUGH}/{@code SUN_PRODUCER} plants (see
     * {@code plant_actions.json}).
     */
    private Plant createFromConfig(PlantJsonConfig config, int col, int lane, int level, boolean boosted,
            GameContext ctx) {
        PlantPropertySheet sheet = CONFIG_REGISTRY.toSheet(config);
        PlantAction attackAction = buildConfigAction(config.attackConfig);
        PlantAction feedAction = buildFeedAction(config.feedConfig);
        return new Plant(sheet, attackAction, feedAction, col, lane, level, boosted, ctx);
    }

    private PlantAction buildConfigAction(PlantActionConfig config) {
        if (config instanceof ChargingShooterConfig chargingConfig) {
            return new ChargingShooterAction(chargingConfig);
        }
        if (config instanceof ClipProgressionShooterConfig cpConfig) {
            return new ClipProgressionShooterAction(cpConfig);
        }
        if (config instanceof MultiStageShooterConfig msConfig) {
            return new MultiStageShooterAction(msConfig);
        }
        if (config instanceof GrowthMeleeConfig growthMeleeConfig) {
            return new GrowthMeleeAction(growthMeleeConfig);
        }
        if (config instanceof StackedShooterConfig stackedConfig) {
            return new StackedShooterAction(stackedConfig);
        }
        if (config instanceof ShooterActionConfig shooterConfig) {
            return new ShooterAction(shooterConfig);
        }
        if (config instanceof LobberActionConfig lobberConfig) {
            return new LobberAction(lobberConfig);
        }
        if (config instanceof PhatBeetConfig phatBeetConfig) {
            return new PhatBeetAction(phatBeetConfig, false);
        }
        if (config instanceof ChomperConfig chomperConfig) {
            return new ChomperAction(chomperConfig);
        }
        if (config instanceof WasabiWhipConfig wasabiWhipConfig) {
            return new WasabiWhipAction(wasabiWhipConfig, false);
        }
        if (config instanceof KiwiBeastConfig kiwiBeastConfig) {
            return new KiwiBeastAction(kiwiBeastConfig, false);
        }
        if (config instanceof SplitPeaConfig splitPeaConfig) {
            return new SplitPeaAction(splitPeaConfig);
        }
        if (config instanceof BonkChoyConfig bonkChoyConfig) {
            return new BonkChoyAttack(bonkChoyConfig);
        }
        if (config instanceof GraveBusterConfig graveBusterConfig) {
            return new GraveBusterAction(graveBusterConfig);
        }
        if (config instanceof GrowthSunProducerConfig growthSunConfig) {
            return new GrowthSunProducerAction(growthSunConfig);
        }
        if (config instanceof SunProducerActionConfig sunConfig) {
            return new SunProducerAction(sunConfig);
        }
        if (config instanceof ExplosiveConfig explosiveConfig) {
            return explosiveConfig.type == null ? new ExplosiveAction(explosiveConfig)
                    : new HotPotatoAction(explosiveConfig);
        }
        if (config instanceof SquashConfig squashConfig) {
            return new SquashAction(squashConfig);
        }
        if (config instanceof TangleKelpConfig tangleKelpConfig) {
            return new TangleKelpAction(tangleKelpConfig);
        }
        if (config instanceof SunBeanConfig sunBeanConfig) {
            return new SunBeanAction(sunBeanConfig);
        }
        if (config instanceof NutConfig nutConfig) {
            return nutConfig.explodesOnDestroy ? new ExplodeONutAction(nutConfig) : new WallNutState(nutConfig);
        }
        if (config instanceof MintActionConfig mintConfig) {
            return new MintAction(mintConfig);
        }
        if (config instanceof PassiveConfig passiveConfig) {
            return new PassiveAction(passiveConfig);
        }
        return null;
    }

    /**
     * Plant Food reuses the same config-driven actions, but a one-shot sun producer
     * must
     * produce without dying (dying is the Gold-Bloom attack behaviour, not a feed
     * behaviour).
     */
    private PlantAction buildFeedAction(PlantActionConfig config) {
        if (config instanceof GrowthSunProducerConfig growthSunConfig) {
            return new GrowthSunProducerAction(growthSunConfig, false);
        }
        if (config instanceof SunProducerActionConfig sunConfig) {
            return new SunProducerAction(sunConfig, false);
        }
        if (config instanceof WallNutFeedConfig wallNutFeedConfig) {
            return new WallNutFeedAction(wallNutFeedConfig);
        }
        if (config instanceof MultiStageShooterConfig msConfig) {
            return new MultiStageFeedAction(msConfig);
        }
        if (config instanceof PhatBeetConfig phatBeetConfig) {
            return new PhatBeetAction(phatBeetConfig, true);
        }
        if (config instanceof ChomperConfig chomperConfig) {
            return new ChomperFeedAction(chomperConfig);
        }
        if (config instanceof WasabiWhipConfig wasabiWhipConfig) {
            return new WasabiWhipAction(wasabiWhipConfig, true);
        }
        if (config instanceof KiwiBeastConfig kiwiBeastConfig) {
            return new KiwiBeastAction(kiwiBeastConfig, true);
        }
        return buildConfigAction(config);
    }
}
