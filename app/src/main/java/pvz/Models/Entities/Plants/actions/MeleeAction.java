package pvz.Models.Entities.Plants.actions;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantContext;
import pvz.Models.Entities.Zombies.Zombie;

/**
 * Behaviour for {@code MELEE} plants: strikes the zombie directly in front of
 * (or, per {@code DamageKind.INSTA_KILL}, swallows) the plant's own cell.
 * {@code AOE}-tagged melee plants (Phat Beet, Kiwibeast) hit every zombie in
 * range instead of just the front-most one.
 */
public class MeleeAction extends CooldownPlantAction {

    public MeleeAction(float intervalSeconds) {
        super(intervalSeconds);
    }

    @Override
    protected boolean canUse(Plant plant, PlantContext ctx) {
        return ctx.hasZombieInLane(plant.getLane());
    }

    @Override
    protected void doExecute(Plant plant, PlantContext ctx) {
        boolean aoe = plant.getSheet().hasTag(pvz.Models.Entities.Plants.Enums.PlantTag.AOE);
        boolean instaKill = plant.getSheet().getDamage().getKind()
                == pvz.Models.Entities.Plants.data.DamageKind.INSTA_KILL;
        float dmg = instaKill ? Float.MAX_VALUE : plant.getEffectiveDamage();

        int hit = 0;
        for (Zombie z : ctx.getZombiesInLane(plant.getLane())) {
            ctx.dealDamageToZombie(z, dmg, false);
            hit++;
            if (!aoe) break; // single-target melee only hits the nearest zombie
        }
        ctx.log("[Action] " + plant.getSheet().getName() + " struck " + hit
                + " zombie(s) in lane " + plant.getLane() + ".");
    }

    @Override
    public String getName() { return "Melee"; }
}
