package com.pvz.models.entities.plants.actions;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.shooters.ShooterAction;
import com.pvz.models.entities.plants.config.PlantActionConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig;
import com.pvz.models.entities.plants.config.SunProducerActionConfig;
import com.pvz.models.entities.plants.data.PlantFoodExecutor;
import com.pvz.models.games.GameContext;

/**
 * Plant Food effect for one plant, dispatched from {@link Plant#triggerPlantFood}.
 * A non-null {@code config} reuses the same {@link ShooterActionConfig}/{@link SunProducerActionConfig}
 * payload types as the normal attack action; {@code null} falls back to the legacy,
 * sheet-driven {@link PlantFoodExecutor} for plants not yet migrated to {@code plant_actions.json}.
 */
public class ConfigFeedAction implements PlantAction {

    private final PlantActionConfig config;

    public ConfigFeedAction(PlantActionConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx) {
        return false; // only ever invoked directly by Plant.triggerPlantFood
    }

    @Override
    public void execute(Plant plant, GameContext ctx) {
        if (config instanceof ShooterActionConfig shooterConfig) {
            ShooterAction.fire(shooterConfig, plant, ctx);
        } else if (config instanceof SunProducerActionConfig sunConfig) {
            SunProducerAction.produce(sunConfig, plant, ctx);
        } else {
            PlantFoodExecutor.execute(plant, ctx);
        }
    }

    @Override
    public String getName() { return "Feed"; }
}
