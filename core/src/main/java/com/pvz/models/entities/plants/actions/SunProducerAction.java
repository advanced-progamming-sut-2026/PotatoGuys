package com.pvz.models.entities.plants.actions;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.SunProducerActionConfig;
import com.pvz.models.entities.plants.data.ProductionKind;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.sun.SunType;
import com.pvz.models.games.GameContext;

public class SunProducerAction extends CooldownPlantAction {

    private final SunProducerActionConfig config;

    public SunProducerAction(float intervalSeconds, SunProducerActionConfig config) {
        super(intervalSeconds);
        this.config = config;
    }

    @Override
    protected boolean canUse(Plant plant, GameContext ctx) {
        return true; // autonomous — no zombie precondition
    }

    @Override
    protected void doExecute(Plant plant, GameContext ctx) {
        produce(config, plant, ctx);
        if (config.productionKind == ProductionKind.ONESHOT) {
            plant.kill();
        }
    }

    @Override
    public String getName() { return "ProduceSun"; }

    /** Spawns one sun burst from {@code config}, honoring growth-stage amounts. Reused by Plant Food. */
    public static void produce(SunProducerActionConfig config, Plant plant, GameContext ctx) {
        int amount = (int)config.amount;
        if (amount <= 0) return;
        if (plant.getUnlockedFlags().contains("Double Sun Chance") && Math.random() < 0.5) {
            amount *= 2;
        }
        Sun sun = new Sun(SunType.NORMAL, plant.getCol(), plant.getLane(), amount, false, ctx);
        ctx.spawnSun(sun);
        ctx.log("[Action] " + plant.getSheet().getName() + " produced " + amount + " sun.");
    }
}

