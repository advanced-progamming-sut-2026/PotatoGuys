package com.pvz.models.games.effects;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.Water;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.enums.PlantTag;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.map.GameMap;
import com.pvz.models.games.map.behaviors.TileBehavior;
import com.pvz.models.games.map.behaviors.WaterBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BigWaveBeachEffect implements ChapterEffect {
    private static final String WATER_TIDE_LINE_PAM = "768/FULL/BACKGROUNDS/WATER_TIDE_LINE/WATER_TIDE_LINE.PAM";
    private static final String WATER_TIDE_LINE_CLIP = "idle";

    private float stateTime;

    private final int minWaterCol;
    private final int maxWaterCol;
    private int currentWaterCol;
    private final Random rand = new Random();

    private Water water;

    public BigWaveBeachEffect(int minWaterCol, int maxWaterCol) {
        this.minWaterCol = minWaterCol > 0 ? minWaterCol : 6;
        this.maxWaterCol = maxWaterCol > 0 ? maxWaterCol : 4;
        this.currentWaterCol = this.minWaterCol;
        this.stateTime = 0;
    }

    @Override
    public void first(GameContext ctx) {
        water = new Water(ctx, new Vector2(1000, 900));
        ctx.addEffect(water);
    }

    @Override
    public void onWaveStart(Wave wave, GameContext ctx) {
        if (!"big wave beach".equalsIgnoreCase(ctx.getSeasonName()))
            return;

        // Randomly shift water level between maxWaterCol and minWaterCol
        int span = Math.abs(minWaterCol - maxWaterCol) + 1;
        int lastCol = currentWaterCol;
        currentWaterCol = Math.min(minWaterCol, maxWaterCol) + rand.nextInt(span);
        ctx.log("[BigWaveBeach] Water tide shifts! Water level rises to column " + currentWaterCol);

        // updating water underlayer
        if (water != null) {
            float distance = (currentWaterCol - lastCol) * GameMap.TILE_WIDTH;
            Vector2 pos = new Vector2(water.getPos().x + distance, water.getPos().y);
            water.setPos(pos);
        }

        // clear all waters from map before updating them
        for (int i = 0; i < ctx.getMap().getColumns(); i++) {
            for (int j = 0; j < ctx.getMap().getLanes(); j++) {
                Tile tile = ctx.getMap().getTileAt(i, j);
                tile.getTags().removeAll(tile.getTags().stream().filter(t -> t.equals(TileTags.WATER)).toList());
                for (TileBehavior behavior : new ArrayList<>(tile.getBehaviors())) {
                    if (behavior instanceof WaterBehavior waterBehavior) {
                        tile.removeBehavior(waterBehavior);
                    }
                }
            }
        }

        updateWaterAndTides(wave, ctx);
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();

        FrameConfig frameConfig = new FrameConfig(WATER_TIDE_LINE_PAM, WATER_TIDE_LINE_CLIP, stateTime,
                new Vector2(GameController.colToWorldX(maxWaterCol), GameController.laneToWorldY(2)),
                new Vector2(0.65f, 0.65f), null, true);

        frameConfigs.add(frameConfig);
        return frameConfigs;
    }

    private void updateWaterAndTides(Wave wave, GameContext ctx) {
        int cols = ctx.getMap().getColumns();
        int lanes = ctx.getMap().getLanes();

        for (int col = 0; col < cols; col++) {
            boolean isWaterColumn = (col >= currentWaterCol);
            for (int lane = 0; lane < lanes; lane++) {
                Tile tile = ctx.getTileAt(col, lane);
                if (tile == null)
                    continue;

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
                        boolean hasLilyPadUnderneath = tile.getPlants().stream()
                                .anyMatch(pl -> pl.getType() == PlantType.LilyPad);

                        if (!hasWaterTag && !hasLilyPad && !hasLilyPadUnderneath) {
                            ctx.removePlant(p);
                            ctx.log("Water tide rose and washed away " + p.getSheet().getName() + " at (" + col + ","
                                    + lane + ")!");
                        }
                    }
                } else {
                    // Tide receded. Lily Pad floats, so it — and anything planted
                    // on it — survives even when the water drops. Only aquatic
                    // plants standing directly on now-dry water get washed away.
                    List<Plant> plantsAt = ctx.getPlantsAt(col, lane);
                    for (Plant p : new ArrayList<>(plantsAt)) {
                        boolean isLilyPad = p.getType() == PlantType.LilyPad;
                        boolean hasLilyPadUnderneath = tile.getPlants().stream()
                                .anyMatch(pl -> pl.getType() == PlantType.LilyPad);
                        if (isLilyPad || hasLilyPadUnderneath) {
                            continue;
                        }
                        if (p.getSheet().getTags().contains(PlantTag.WATER)) {
                            ctx.removePlant(p);
                            ctx.log("Water tide receded and destroyed " + p.getSheet().getName() + " at (" + col + ","
                                    + lane + ")!");
                        }
                    }
                }

                if (wave.getWaveNumber() > 1) {
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
        Zombie z = new ZombieFactory().create(type.getAlias(), GameController.colToWorldX(col), lane, ctx,
                wave.getWaveNumber(), 5);
        if (z != null) {
            ctx.spawnZombie(z);
            ctx.log("A Snorkel Zombie emerged from the low tide at (" + col + "," + lane + ")!");
        }
    }

    @Override
    public void update(GameContext ctx, float dt) {
        stateTime += dt;
    }
}
