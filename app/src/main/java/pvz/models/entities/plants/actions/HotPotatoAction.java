package pvz.models.entities.plants.actions;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;
import pvz.models.games.map.behaviors.IceBlockBehavior;
import pvz.models.games.map.behaviors.TileBehavior;
import pvz.models.games.map.tile.Tile;
import pvz.models.games.map.tile.TileTags;

import java.util.ArrayList;

public class HotPotatoAction implements PlantAction {
    private static final float WORK_DURATION_SECONDS = 1.0f;
    private final int workTicks;
    private int elapsedTicks = 0;

    public HotPotatoAction() {
        this.workTicks = Math.round(WORK_DURATION_SECONDS * Plant.TICKS_PER_SECOND);
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx) {
        elapsedTicks++;
        return elapsedTicks >= workTicks;
    }

    @Override
    public void execute(Plant plant, GameContext ctx) {
        int centerCol = plant.getCol();
        int centerLane = plant.getLane();
        int level = plant.getLevel();

        // Level 3+ melts 3x3 area, lower levels melt only center tile
        int radius = (level >= 3) ? 1 : 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int c = centerCol + dx;
                int l = centerLane + dy;
                if (c >= 0 && c < ctx.getColumns() && l >= 0 && l < ctx.getLanes()) {
                    Tile tile = ctx.getTileAt(c, l);
                    if (tile != null) {
                        // 1. Melt frozen plants
                        for (Plant p : tile.getPlants()) {
                            if (p.isFrozen()) {
                                p.takeIceDamage(Float.MAX_VALUE, true);
                            }
                        }
                        // 2. Destroy IceBlockBehaviors and remove ICE_BLOCK tag
                        for (TileBehavior b : new ArrayList<>(tile.getBehaviors())) {
                            if (b instanceof IceBlockBehavior ice) {
                                ice.takeDamage(Float.MAX_VALUE, true);
                                tile.removeBehavior(ice);
                            }
                        }
                        tile.getTags().remove(TileTags.ICE_BLOCK);
                    }

                    // Level 4 trait: explodes and damages zombies in the area
                    if (level >= 4) {
                        for (Zombie z : ctx.getZombiesAt(c, l)) {
                            z.takeDamage(1800f, false);
                        }
                    }
                }
            }
        }

        ctx.log("[Action] Hot Potato melted ice" + (level >= 4 ? " and exploded" : "") + " around (" + centerCol + "," + centerLane + ")!");
        plant.kill();
        ctx.removePlant(plant);
    }

    @Override
    public String getName() {
        return "MeltIce";
    }
}
