// package com.pvz.models.entities.plants.actions;

// import com.pvz.models.entities.plants.Plant;
// import com.pvz.models.entities.plants.data.DamageKind;
// import com.pvz.models.entities.projectile.Projectile;
// import com.pvz.models.entities.projectile.ProjectileType;
// import com.pvz.models.entities.zombies.Zombie;
// import com.pvz.models.games.GameContext;

// public class HomingAction extends CooldownPlantAction {

//     public HomingAction(float intervalSeconds) {
//         super(intervalSeconds);
//     }

//     @Override
//     protected boolean canUse(Plant plant, GameContext ctx) {
//         return !ctx.getZombies().isEmpty();
//     }

//     @Override
//     protected void doExecute(Plant plant, GameContext ctx) {
//         Zombie target = getNearestZombieAhead(plant.getCol(), plant.getLane(), ctx);
//         if (target == null) target = ctx.getZombies().isEmpty() ? null : ctx.getZombies().get(0);
//         if (target == null) return;

//         boolean instaKill = plant.getSheet().getDamage().getKind()
//                 == DamageKind.INSTA_KILL;
//         float dmg = instaKill ? Float.MAX_VALUE : plant.getEffectiveDamage();

//         // Projectile bolt = new Projectile(ctx, ProjectileType.HOMING_BOLT, plant.getLane(),
//         //         plant.getCol(), dmg, false, false, false,0, target);
//         // ctx.spawnProjectile(bolt);
//         ctx.log("[Action] " + plant.getSheet().getName() + " locked onto a zombie in lane "
//                 + target.getLane() + ".");
//     }

//     private Zombie getNearestZombieAhead(int col, int lane, GameContext ctx) {
//         return ctx.getZombiesInLane(lane).stream()
//                 .filter(z -> z.getX() >= col && !z.isDead())
//                 .min((z1, z2) -> Integer.compare((int)z1.getX(), (int)z2.getX()))
//                 .orElse(null);
//     }

//     @Override
//     public String getName() { return "Homing"; }
// }
