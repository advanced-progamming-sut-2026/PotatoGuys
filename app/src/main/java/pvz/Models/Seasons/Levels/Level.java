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

    public Level(GameEngine engine, GameMap gameMap, int levelNumber, LevelType type, int initialSun,
                 List<Wave> waves){
        this.engine=engine;
        this.gameMap=gameMap;
        this.levelNumber=levelNumber;
        this.type=type;
        this.currentSun=initialSun;
        this.waves=waves;
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

    public List<Wave> getWaves() {
        return waves;
    }

    public GameEngine getEngine() {
        return engine;
    }
}
