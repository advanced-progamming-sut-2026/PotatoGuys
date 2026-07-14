package pvz.Models.Entities.Plants.actions;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantContext;
import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Projectile.ProjectileType;
import pvz.Models.Entities.Zombies.Zombie;

/**
 * Behaviour for {@code HOMING} plants: locks onto a zombie anywhere on the
 * board (not just its own lane) and always connects, mirroring the "magic
 * bullet ignores obstacles" flavor of Caulipower / Electric Blueberry /
 * Cat-tail / Magnet-shroom.
 */
public class HomingAction extends CooldownPlantAction {

    public HomingAction(float intervalSeconds) {
        super(intervalSeconds);
    }

    @Override
    protected boolean canUse(Plant plant, PlantContext ctx) {
        return ctx.getAnyZombieOnBoard() != null;
    }

    @Override
    protected void doExecute(Plant plant, PlantContext ctx) {
        Zombie target = ctx.getNearestZombieAhead(plant.getCol(), plant.getLane());
        if (target == null) target = ctx.getAnyZombieOnBoard();
        if (target == null) return;

        boolean instaKill = plant.getSheet().getDamage().getKind()
                == pvz.Models.Entities.Plants.data.DamageKind.INSTA_KILL;
        float dmg = instaKill ? Float.MAX_VALUE : plant.getEffectiveDamage();

        Projectile bolt = new Projectile(ctx, ProjectileType.HOMING_BOLT, plant.getLane(),
                plant.getCol(), dmg, false, false, 0, target);
        ctx.spawnProjectile(bolt);
        ctx.log("[Action] " + plant.getSheet().getName() + " locked onto a zombie in lane "
                + target.getLane() + ".");
    }

    @Override
    public String getName() { return "Homing"; }
}
