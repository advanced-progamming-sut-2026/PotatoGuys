package pvz.Models.Entities.Plants.actions;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Games.GameContext;
import pvz.Models.Entities.Plants.Enums.PlantTag;
import pvz.Models.Entities.Plants.data.DamageKind;
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
    protected boolean canUse(Plant plant, GameContext ctx) {
        return !ctx.getZombiesInLane(plant.getLane()).isEmpty();
    }

    @Override
    protected void doExecute(Plant plant, GameContext ctx) {
        boolean aoe = plant.getSheet().hasTag(PlantTag.AOE);
        boolean instaKill = plant.getSheet().getDamage().getKind() == DamageKind.INSTA_KILL;
        float dmg = instaKill ? Float.MAX_VALUE : plant.getEffectiveDamage();

        int hit = 0;
        for (Zombie z : ctx.getZombiesInLane(plant.getLane())) {
            z.takeDamage(dmg, false);
            hit++;
            if (!aoe) break; // single-target melee only hits the nearest zombie
        }
        ctx.log("[Action] " + plant.getSheet().getName() + " struck " + hit
                + " zombie(s) in lane " + plant.getLane() + ".");
    }

    @Override
    public String getName() { return "Melee"; }
}
