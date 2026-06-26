package pvz.Models.Seasons;

import java.util.List;

import pvz.Models.Seasons.Levels.Level;
import pvz.Models.Seasons.Levels.TileType;
import pvz.Models.Seasons.SeasonEffects.SeasonEffect;
import pvz.Models.Zombies.ZombieType;

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
