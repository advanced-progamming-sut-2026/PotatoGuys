package Models.Seasons.Levels;

import Models.Enums.LevelType;

import java.util.List;

public abstract class Level {
    private int levelNumber;
    private LevelType type;
    private int initialSun;
    private List<WaveConfig> waves;

    public int getLevelNumber() {
        return levelNumber;
    }

    public LevelType getType() {
        return type;
    }

    public int getInitialSun() {
        return initialSun;
    }

    public List<WaveConfig> getWaves() {
        return waves;
    }
}
