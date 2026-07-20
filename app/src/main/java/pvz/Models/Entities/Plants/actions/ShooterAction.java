package pvz.Models.Entities.Plants.actions;

import pvz.Models.Entities.Plants.Enums.PlantTag;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.map.tile.TileTags;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Projectile.ProjectileType;

/**
 * Behaviour for {@code SHOOTER} (and, by extension, {@code STRIKE_THROUGH})
 * plants: fires straight down its own lane whenever a zombie is present.
 *
 * <p>{@link pvz.Models.Entities.Plants.data.DamageProfile#getCount()} drives
 * multi-pellet plants (Repeater "20x2", Threepeater/Rotobaga/Mega Gatling
 * Pea) — each pellet is spawned as its own {@link Projectile}. Tags drive
 * flavor: {@code ICE} chills on impact, {@code POISON} bypasses armour and
 * applies damage-over-time (handled by {@code Zombie}'s own status system).
 */
public class ShooterAction extends CooldownPlantAction {

    public ShooterAction(float intervalSeconds) {
        super(intervalSeconds);
    }

    @Override
    protected boolean canUse(Plant plant, GameContext ctx) {
        boolean isGraveOrIceInLane=false;
        for (int i = plant.getCol()+1; i < ctx.getMap().getColumns(); i++) {
            if (ctx.getTileAt(i,plant.getLane()).getTags().contains(TileTags.GRAVE)) isGraveOrIceInLane=true;
        }
        return (!ctx.getZombiesInLane(plant.getLane()).isEmpty()) || isGraveOrIceInLane;
    }

    @Override
    protected void doExecute(Plant plant, GameContext ctx) {
        int pellets = Math.max(1, plant.getSheet().getDamage().getCount());
        boolean poisonous = plant.getSheet().hasTag(PlantTag.POISON);
        boolean chills = plant.getSheet().hasTag(PlantTag.ICE);
        int pierce = plant.getPierceCount();
        // Strike-through plants get a small base pierce beyond their level upgrades.
        if (plant.getSheet().getCategory() == pvz.Models.Entities.Plants.Enums.PlantCategory.STRIKE_THROUGH) {
            pierce += 2;
        }

        for (int i = 0; i < pellets; i++) {
            Projectile bolt = new Projectile(ctx, ProjectileType.PEA, plant.getLane(), plant.getCol(),
                    plant.getEffectiveDamage(), poisonous, chills, pierce, null);
            ctx.spawnProjectile(bolt);
        }
        ctx.log("[Action] " + plant.getSheet().getName() + " fired " + pellets
                + " shot(s) in lane " + plant.getLane() + ".");
    }

    @Override
    public String getName() { return "Shoot"; }
}
