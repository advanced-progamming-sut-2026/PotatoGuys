package pvz.Models.Seasons.Levels;

import java.util.List;

import pvz.Models.Engine.GameEngine;

public abstract class Level {
    private GameEngine engine;
    private GameMap gameMap;
    private int levelNumber;
    private LevelType type;
    private int currentSun;
    private List<Wave> waves;
    private int currentWaveIndex;

    public Level(GameEngine engine, GameMap gameMap, int levelNumber, LevelType type, int initialSun,
                 List<Wave> waves){
        this.engine=engine;
        this.gameMap=gameMap;
        this.levelNumber=levelNumber;
        this.type=type;
        this.currentSun=initialSun;
        this.waves=waves;
        this.currentWaveIndex=0;
    }

    public GameMap getGameMap() {
        return gameMap;
    }
    public int getLevelNumber() {
        return levelNumber;
    }

    public LevelType getType() {
        return type;
    }

    public int getCurrentSun() {
        return currentSun;
    }

    public void addSun(int amount) {
        currentSun += amount;
    }

    public boolean spendSun(int amount) {
        if (currentSun < amount) return false;
        currentSun -= amount;
        return true;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public int getCurrentWaveIndex() {
        return currentWaveIndex;
    }

    public Wave getCurrentWave() {
        if (currentWaveIndex >= waves.size()) return null;
        return waves.get(currentWaveIndex);
    }

    public boolean allWavesDone() {
        return currentWaveIndex >= waves.size();
    }

    public void advanceWave() {
        if (currentWaveIndex < waves.size()) {
            currentWaveIndex++;
        }
    }

    public GameEngine getEngine() {
        return engine;
    }
}
