package pvz.Models.Entities.Plants.actions;

import java.util.List;

import pvz.Models.Entities.Plants.Enums.PlantTag;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantContext;
import pvz.Models.Entities.Zombies.Zombie;

/**
 * Behaviour for {@code LOBBER} plants: arcs damage into the lane, splashing
 * onto multiple zombies when tagged {@code AOE} (Melon-pult, Winter Melon,
 * Pepper-pult). Lobbers ignore line-of-sight in the real game (they clear
 * obstacles); this simplified grid has no obstacle-blocking model, so the
 * only observable difference from {@link ShooterAction} is the splash.
 */
public class LobberAction extends CooldownPlantAction {

    private static final int DEFAULT_SPLASH_TARGETS = 3;

    public LobberAction(float intervalSeconds) {
        super(intervalSeconds);
    }

    @Override
    protected boolean canUse(Plant plant, PlantContext ctx) {
        return ctx.hasZombieInLane(plant.getLane());
    }

    @Override
    protected void doExecute(Plant plant, PlantContext ctx) {
        List<Zombie> zombies = ctx.getZombiesInLane(plant.getLane());
        boolean aoe = plant.getSheet().hasTag(PlantTag.AOE);
        boolean chills = plant.getSheet().hasTag(PlantTag.ICE);
        int targets = aoe ? Math.min(zombies.size(), DEFAULT_SPLASH_TARGETS) : 1;

        int hit = 0;
        for (Zombie z : zombies) {
            if (hit >= targets) break;
            ctx.dealDamageToZombie(z, plant.getEffectiveDamage(), false);
            if (chills) {
                z.applyEffect(new pvz.Models.Entities.Zombies.effects.StatusEffect(
                        pvz.Models.Entities.Zombies.effects.EffectType.CHILL, 3 * Plant.TICKS_PER_SECOND));
            }
            hit++;
        }
        ctx.log("[Action] " + plant.getSheet().getName() + " lobbed damage onto " + hit
                + " zombie(s) in lane " + plant.getLane() + ".");
    }

    @Override
    public String getName() { return "Lob"; }
}
