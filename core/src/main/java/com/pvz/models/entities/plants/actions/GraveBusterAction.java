// package com.pvz.models.entities.plants.actions;

// import com.pvz.models.entities.plants.Plant;
// import com.pvz.models.entities.zombies.Zombie;
// import com.pvz.models.games.GameContext;
// import com.pvz.models.games.map.behaviors.GraveBehavior;
// import com.pvz.models.games.map.behaviors.TileBehavior;
// import com.pvz.models.games.map.tile.Tile;

// import java.util.ArrayList;

// public class GraveBusterAction implements PlantAction {
//     private static final float WORK_DURATION_SECONDS = 3.0f;
//     private final int workTicks;
//     private int elapsedTicks = 0;

//     public GraveBusterAction() {
//         this.workTicks = Math.round(WORK_DURATION_SECONDS * Plant.TICKS_PER_SECOND);
//     }

//     @Override
//     public boolean shouldTrigger(Plant plant, GameContext ctx) {
//         elapsedTicks++;
//         return elapsedTicks >= workTicks;
//     }

//     @Override
//     public void execute(Plant plant, GameContext ctx) {
//         int col = plant.getCol();
//         int lane = plant.getLane();
//         Tile tile = ctx.getTileAt(col, lane);

//         boolean graveFound = false;
//         for (TileBehavior b : new ArrayList<>(tile.getBehaviors())) {
//             if (b instanceof GraveBehavior db) {
//                 db.destroyGrave(tile, ctx);
//                 graveFound = true;
//             }
//         }

//         if (graveFound) {
//             ctx.log("[Action] Grave Buster finished eating the grave at (" + col + "," + lane + ").");
//         }

//         // Level 4 trait: explodes at the end of its work
//         if (plant.getLevel() >= 4) {
//             ctx.log("[Action] Level 4 Grave Buster explodes!");
//             for (Zombie z : ctx.getZombiesAt(col, lane)) {
//                 z.takeDamage(1800f, false);
//             }
//         }

//         plant.kill();
//         ctx.removePlant(plant);
//     }

//     @Override
//     public String getName() {
//         return "EatGrave";
//     }
// }
