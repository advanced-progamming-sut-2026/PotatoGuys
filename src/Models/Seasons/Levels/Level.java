package Models.Seasons.Levels;

import Models.Engine.GameEngine;

import java.util.List;

public abstract class Level {
    private GameEngine engine;
    private GameMap gameMap;
    private int levelNumber;
    private LevelType type;
    private int initialSun;
    private int currentSun;
    private List<WaveConfig> waves;

    public GameMap getGameMap() {
        return gameMap;
    }
    public int getLevelNumber() {
        return levelNumber;
    }

    public LevelType getType() {
        return type;
    }

    public int getInitialSun() {
        return initialSun;
    }

    public int getCurrentSun() {
        return currentSun;
    }

    public List<WaveConfig> getWaves() {
        return waves;
    }

    public GameEngine getEngine() {
        return engine;
    }
}
