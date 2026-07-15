package pvz.Models.Games.Levels;

import java.util.List;

import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.GameMap;

public abstract class Level {
    private GameMap gameMap;
    private GameModeType gameMode;

    private final LevelType type;
    private final int initialSun;
    private final int levelNumber;
    private final List<Wave> waves;
    private int currentWaveIndex;

    public Level(GameModeType gameMode, GameMap gameMap, int levelNumber, LevelType type, int initialSun,
                 List<Wave> waves){
        this.gameMode=gameMode;
        this.gameMap=gameMap;
        this.levelNumber=levelNumber;
        this.type=type;
        this.initialSun=initialSun;
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

    public int getInitialSun() {
        return initialSun;
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

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public GameModeType getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModeType gameMode) {
        this.gameMode = gameMode;
    }

    public void setCurrentWaveIndex(int currentWaveIndex) {
        this.currentWaveIndex = currentWaveIndex;
    }
}
