package com.pvz.models.games.effects;

import com.pvz.models.AppContext;
import com.pvz.models.Constants;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.levels.Wave;
import com.pvz.models.games.map.behaviors.GraveBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DarkAgesEffect implements ChapterEffect {
    private final int intervalTicks;
    private int tickCounter = 0;
    private final Random rand = new Random();

    public DarkAgesEffect(int intervalTicks) {
        this.intervalTicks = intervalTicks > 0 ? intervalTicks : 350; // every ~35 seconds
    }

    @Override
    public void onTick(GameContext ctx) {
        if (!"dark ages".equalsIgnoreCase(ctx.getSeasonName()))
            return;

        tickCounter++;
        if (tickCounter >= intervalTicks) {
            growRandomGrave(ctx);
            tickCounter = 0;
        }
    }

    @Override
    public void onWaveStart(Wave wave, GameContext ctx) {
        if (!"dark ages".equalsIgnoreCase(ctx.getSeasonName()))
            return;

        if (wave != null && wave.getWaveNumber() > 1) {
            triggerNecromancy(ctx, wave);
        }
    }

    private void growRandomGrave(GameContext ctx) {
        int cols = ctx.getColumns();
        int lanes = ctx.getLanes();

        List<int[]> emptyTiles = new ArrayList<>();
        for (int l = 0; l < lanes; l++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = ctx.getTileAt(c, l);
                if (tile != null && tile.getPlants().isEmpty() && !tile.getTags().contains(TileTags.GRAVE)) {
                    emptyTiles.add(new int[] { c, l });
                }
            }
        }

        if (emptyTiles.isEmpty())
            return;

        int[] cell = emptyTiles.get(rand.nextInt(emptyTiles.size()));
        int col = cell[0];
        int lane = cell[1];

        Tile tile = ctx.getTileAt(col, lane);
        tile.getTags().add(TileTags.GRAVE);

        // Determine reward
        double roll = rand.nextDouble();
        GraveBehavior.GraveReward reward = GraveBehavior.GraveReward.NONE;
        if (roll < 0.25) {
            reward = GraveBehavior.GraveReward.SUN_50;
        } else if (roll < 0.40) {
            reward = GraveBehavior.GraveReward.PLANT_FOOD;
        }

        tile.addBehavior(new GraveBehavior(Constants.DEFAULT_GRAVE_HP, "Grave", reward));
        tile.getTags().add(TileTags.GRAVE);
        ctx.log("A dark grave has grown at (" + col + "," + lane + ")!");
    }

    private void triggerNecromancy(GameContext ctx, Wave wave) {
        int cols = ctx.getColumns();
        int lanes = ctx.getLanes();

        for (int l = 0; l < lanes; l++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = ctx.getTileAt(c, l);
                if (tile != null && tile.getTags().contains(TileTags.NECROMANCY)
                        && tile.getTags().contains(TileTags.GRAVE)) {
                    int difficulty = AppContext.getInstance().getCurrentUser().getSetting().getDifficulty();
                    ZombieType type = ZombieType.BASIC;
                    Zombie z = new ZombieFactory().create(type.getAlias(), c, l, ctx, wave.getWaveNumber(), difficulty);
                    if (z != null) {
                        ctx.spawnZombie(z);
                        ctx.log("A zombie dug up and emerged from the necromancy grave at (" + c + "," + l + ")!");
                    }
                }
            }
        }
    }
}
