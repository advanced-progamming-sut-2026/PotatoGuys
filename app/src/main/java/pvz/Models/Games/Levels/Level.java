package pvz.Models.Games.Levels;

import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.Seasons.Season;
import pvz.Models.Games.map.GameMap;

public abstract class Level {
    protected GameMap gameMap;
    protected GameModeType gameMode;
    protected final LevelType type;
    protected final int initialSun;
    protected final int levelNumber;
    

    public Level(GameModeType gameMode, GameMap gameMap, int levelNumber, LevelType type, int initialSun){
        this.gameMode=gameMode;
        this.gameMap=gameMap;
        this.levelNumber=levelNumber;
        this.type=type;
        this.initialSun=initialSun;
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

    public int getInitialSun() {
        return initialSun;
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public GameModeType getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModeType gameMode) {
        this.gameMode = gameMode;
    }
}
