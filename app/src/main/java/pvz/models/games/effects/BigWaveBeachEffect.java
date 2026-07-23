package pvz.models.games.effects;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.enums.PlantTag;
import pvz.models.entities.plants.enums.PlantType;
import pvz.models.entities.zombies.Zombie;
import pvz.models.entities.zombies.ZombieFactory;
import pvz.models.entities.zombies.ZombieType;
import pvz.models.games.GameContext;
import pvz.models.games.levels.Wave;
import pvz.models.games.map.behaviors.WaterBehavior;
import pvz.models.games.map.tile.Tile;
import pvz.models.games.map.tile.TileTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BigWaveBeachEffect implements ChapterEffect {
    private final int minWaterCol;
    private final int maxWaterCol;
    private int currentWaterCol;
    private final Random rand = new Random();

    public BigWaveBeachEffect(int minWaterCol, int maxWaterCol) {
        this.minWaterCol = minWaterCol > 0 ? minWaterCol : 6;
        this.maxWaterCol = maxWaterCol > 0 ? maxWaterCol : 4;
        this.currentWaterCol = this.minWaterCol;
    }

    @Override
    public void onWaveStart(Wave wave, GameContext ctx) {
        if (!"big wave beach".equalsIgnoreCase(ctx.getSeasonName())) return;

        // Randomly shift water level between maxWaterCol and minWaterCol
        int span = Math.abs(minWaterCol - maxWaterCol) + 1;
        currentWaterCol = Math.min(minWaterCol, maxWaterCol) + rand.nextInt(span);
        ctx.log("[BigWaveBeach] Water tide shifts! Water level rises to column " + currentWaterCol);

        updateWaterAndTides(wave, ctx);
    }

    private void updateWaterAndTides(Wave wave, GameContext ctx) {
        int cols = ctx.getColumns();
        int lanes = ctx.getLanes();

        for (int col = 0; col < cols; col++) {
            boolean isWaterColumn = (col >= currentWaterCol);
            for (int lane = 0; lane < lanes; lane++) {
                Tile tile = ctx.getTileAt(col, lane);
                if (tile == null) continue;

                if (isWaterColumn) {
                    if (!tile.getTags().contains(TileTags.WATER)) {
                        tile.getTags().add(TileTags.WATER);
                    }
                    if (tile.getBehaviors().stream().noneMatch(b -> b instanceof WaterBehavior)) {
                        tile.addBehavior(new WaterBehavior());
                    }

                    // Water destroys plants that cannot be in water (no WATER tag and no LilyPad)
                    List<Plant> plantsAt = ctx.getPlantsAt(col, lane);
                    for (Plant p : new ArrayList<>(plantsAt)) {
                        boolean hasWaterTag = p.getSheet().getTags().contains(PlantTag.WATER);
                        boolean hasLilyPad = p.getType() == PlantType.LilyPad;
                        boolean hasLilyPadUnderneath = tile.getPlants().stream().anyMatch(pl -> pl.getType() == PlantType.LilyPad);

                        if (!hasWaterTag && !hasLilyPad && !hasLilyPadUnderneath) {
                            ctx.removePlant(p);
                            ctx.log("Water tide rose and washed away " + p.getSheet().getName() + " at (" + col + "," + lane + ")!");
                        }
                    }

                    // Low tide zombie emergence
                    if (tile.getTags().contains(TileTags.LOW_TIDE)) {
                        if (rand.nextFloat() < 0.6f) {
                            spawnLowTideZombie(ctx, col, lane, wave);
                        }
                    }
                }
            }
        }
    }

    private void spawnLowTideZombie(GameContext ctx, int col, int lane, Wave wave) {
        ZombieType type = ZombieType.SNORKEL;
        Zombie z = new ZombieFactory().create(type.getAlias(), col, lane, ctx, wave.getWaveNumber(), 5);
        if (z != null) {
            ctx.spawnZombie(z);
            ctx.log("A Snorkel Zombie emerged from the low tide at (" + col + "," + lane + ")!");
        }
    }
}
