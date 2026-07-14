package pvz.Models.Seasons.Levels;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieFactory;
import pvz.Models.Entities.Zombies.ZombieGameContext;

import java.util.Random;

public class LevelGameContext implements ZombieGameContext {
    private final GameEngine engine;
    private final GameMap map;
    private final Level level;
    private final ZombieFactory factory;
    private final Random rand;
    private int localSun = 0;

    public LevelGameContext(GameEngine engine, GameMap map, Level level) {
        this.engine = engine;
        this.map = map;
        this.level = level;
        this.factory = new ZombieFactory();
        this.rand = new Random();
    }

    @Override
    public boolean isPlantAt(int col, int lane) {
        return false;
    }

    @Override
    public void dealDamageToPlant(int col, int lane, float damage, boolean poisonous) {
    }

    @Override
    public boolean isTorchVulnerablePlantAt(int col, int lane) {
        return false;
    }

    @Override
    public void burnPlant(int col, int lane) {
    }

    @Override
    public void transformPlantToCat(int col, int lane) {
    }

    @Override
    public void applyFrostToPlant(int col, int lane) {
    }

    @Override
    public void applyOctopusToPlant(int col, int lane) {
    }

    @Override
    public int getSunAmount() {
        return (level != null) ? level.getCurrentSun() : localSun;
    }

    @Override
    public void stealSun(int amount) {
        if (level != null) {
            level.spendSun(amount);
        } else {
            localSun -= amount;
        }
    }

    @Override
    public void returnSun(int amount) {
        if (level != null) {
            level.addSun(amount);
        } else {
            localSun += amount;
        }
    }

    @Override
    public void raiseTomb(int col, int lane) {
        log("Tomb raised at (" + col + "," + lane + ")");
    }

    @Override
    public int[] getRandomEmptyCell() {
        return new int[]{rand.nextInt(map.getColumns()), rand.nextInt(map.getRows())};
    }

    @Override
    public void spawnZombie(String alias, int col, int lane) {
        try {
            Zombie zombie = factory.create(alias, col, lane, this, 0, 3);
            engine.register(zombie);
        } catch (IllegalArgumentException e) {
            log("Failed to spawn zombie: " + alias + " - " + e.getMessage());
        }
    }

    @Override
    public int getColumns() {
        return map.getColumns();
    }

    @Override
    public int getLanes() {
        return map.getRows();
    }

    @Override
    public boolean isWaterAt(int col, int lane) {
        return false;
    }

    @Override
    public void triggerLawnMower(int lane) {
        log("Lawn mower triggered in lane " + lane + "!");
    }

    @Override
    public void log(String message) {
        System.out.println("  [Level] " + message);
    }
}
