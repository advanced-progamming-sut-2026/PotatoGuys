package Models.Seasons;

import Models.Zombies.ZombieType;
import Models.Engine.TileType;
import Models.Seasons.Levels.Level;
import Models.Seasons.SeasonEffects.SeasonEffect;

import java.util.List;

public abstract class Season {
    private String name;
    private int numberOfLevels;
    private List<Level> levels;
    private TileType defaultTileType;
    private List<ZombieType> allowedZombieTypes;
    private List<SeasonEffect> seasonEffects;

    public String getName() {
        return name;
    }

    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public TileType getDefaultTileType() {
        return defaultTileType;
    }

    public List<ZombieType> getAllowedZombieTypes() {
        return allowedZombieTypes;
    }

    public List<SeasonEffect> getSeasonEffects() {
        return seasonEffects;
    }
}
