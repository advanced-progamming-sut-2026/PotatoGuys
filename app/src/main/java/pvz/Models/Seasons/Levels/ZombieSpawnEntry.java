package pvz.Models.Seasons.Levels;

import pvz.Models.Entities.Zombies.ZombieType;

public class ZombieSpawnEntry {
    private ZombieType type;
    private int cost;
    private int minLane;
    private int maxLane;
    private int spawnDelayTicks;

    public ZombieType getType() {
        return type;
    }

    public int getCost() {
        return cost;
    }

    public int getMinLane() {
        return minLane;
    }

    public int getMaxLane() {
        return maxLane;
    }

    public int getSpawnDelayTicks() {
        return spawnDelayTicks;
    }
}
