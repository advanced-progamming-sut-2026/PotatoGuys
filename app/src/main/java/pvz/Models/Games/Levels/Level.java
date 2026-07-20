package pvz.Models.Games.Levels;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.data.GameMapDefinition;

public abstract class Level {
    protected GameModeType gameType;
    protected GameMapDefinition gameMap;
    protected final LevelType levelType;
    protected final int initialSun;
    protected final int levelNumber;
    

    public Level(GameModeType gameMode, GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun){
        this.gameType=gameMode;
        this.gameMap=gameMap;
        this.levelNumber=levelNumber;
        this.levelType=type;
        this.initialSun=initialSun;
    }

    public boolean hasPreGame() {
        return true;
    }

    public boolean isPlantAllowed(PlantType type) {
        return true;
    }

    public List<PlantType> getForcedPlants() {
        return new ArrayList<>();
    }

    public GameMapDefinition getGameMapDefinition() {
        return gameMap;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public LevelType getType() {
        return levelType;
    }

    public int getInitialSun() {
        return initialSun;
    }

    public void setGameMap(GameMapDefinition gameMap) {
        this.gameMap = gameMap;
    }

    public GameModeType getGameMode() {
        return gameType;
    }

    public void setGameMode(GameModeType gameMode) {
        this.gameType = gameMode;
    }
}
