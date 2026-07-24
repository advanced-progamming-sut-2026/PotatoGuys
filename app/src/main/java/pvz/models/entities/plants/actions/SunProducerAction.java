package pvz.models.entities.plants.actions;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.data.ProductionKind;
import pvz.models.entities.sun.Sun;
import pvz.models.entities.sun.SunType;
import pvz.models.games.GameContext;

public class SunProducerAction extends CooldownPlantAction {

    public SunProducerAction(float intervalSeconds) {
        super(intervalSeconds);
    }

    @Override
    protected boolean canUse(Plant plant, GameContext ctx) {
        return true; // autonomous — no zombie precondition
    }

    @Override
    protected void doExecute(Plant plant, GameContext ctx) {
        int amount = Math.round(plant.getEffectiveProductionAmount());
        if (amount <= 0) return;
        if (plant.getUnlockedFlags().contains("Double Sun Chance") && Math.random() < 0.5) {
            amount *= 2;
        }
        Sun sun = new Sun(SunType.NORMAL, plant.getCol(), plant.getLane(), amount, false, ctx);
        ctx.spawnSun(sun);
        ctx.log("[Action] " + plant.getSheet().getName() + " produced " + amount + " sun.");

        boolean oneShot = plant.getSheet().getProduction() != null
                && plant.getSheet().getProduction().getKind() == ProductionKind.ONESHOT;
        if (oneShot) {
            plant.kill();
        }
    }

    @Override
    public String getName() { return "ProduceSun"; }
}
