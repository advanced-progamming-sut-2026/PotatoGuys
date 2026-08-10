// package com.pvz.models.entities.plants.actions;

// import java.util.List;

// import com.pvz.models.entities.plants.Plant;
// import com.pvz.models.entities.plants.enums.PlantTag;
// import com.pvz.models.entities.zombies.Zombie;
// import com.pvz.models.entities.zombies.effects.EffectType;
// import com.pvz.models.entities.zombies.effects.StatusEffect;
// import com.pvz.models.games.GameContext;

// public class LobberAction extends CooldownPlantAction {

//     private static final int DEFAULT_SPLASH_TARGETS = 3;

//     public LobberAction(float intervalSeconds) {
//         super(intervalSeconds);
//     }

//     @Override
//     protected boolean canUse(Plant plant, GameContext ctx) {
//         return !ctx.getZombiesInLane(plant.getLane()).isEmpty();
//     }

//     @Override
//     protected void doExecute(Plant plant, GameContext ctx) {
//         List<Zombie> zombies = ctx.getZombiesInLane(plant.getLane());
//         boolean aoe = plant.getSheet().hasTag(PlantTag.AOE);
//         boolean chills = plant.getSheet().hasTag(PlantTag.ICE);
//         int targets = aoe ? Math.min(zombies.size(), DEFAULT_SPLASH_TARGETS) : 1;

//         int hit = 0;
//         for (Zombie z : zombies) {
//             if (hit >= targets) break;
//             z.takeDamage(plant.getEffectiveDamage() , false);
//             if (chills) {
//                 z.applyEffect(new StatusEffect(
//                         EffectType.CHILL, 3 * Plant.TICKS_PER_SECOND));
//             }
//             hit++;
//         }
//         ctx.log("[Action] " + plant.getSheet().getName() + " lobbed damage onto " + hit
//                 + " zombie(s) in lane " + plant.getLane() + ".");
//     }

//     @Override
//     public String getName() { return "Lob"; }
// }
