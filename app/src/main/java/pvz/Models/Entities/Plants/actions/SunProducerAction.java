package pvz.Models.Entities.Plants.actions;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.data.ProductionKind;
import pvz.Models.Entities.Sun.Sun;
import pvz.Models.Entities.Sun.SunType;
import pvz.Models.Games.GameContext;

/**
 * Behaviour for {@code SUN_PRODUCER} plants: periodically spawns a collectible
 * {@link Sun} at the plant's own cell. Handles fixed (Sunflower), staged
 * (Sun-shroom growth), and one-shot (Gold Bloom) production kinds uniformly
 * via {@link Plant#getEffectiveProductionAmount()}.
 */
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
