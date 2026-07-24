package pvz.models.entities.plants.actions;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.data.DamageKind;
import pvz.models.entities.plants.enums.PlantTag;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;

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
