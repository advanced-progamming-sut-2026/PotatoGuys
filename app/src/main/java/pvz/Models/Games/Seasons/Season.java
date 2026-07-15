package pvz.Models.Games.Seasons;

import java.util.List;

import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Games.Levels.Level;

public abstract class Season {
    private String name;
    private int numberOfLevels;
    private List<Level> levels;
    private List<ZombieType> allowedZombieTypes;

    public String getName() {
        return name;
    }

    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public List<ZombieType> getAllowedZombieTypes() {
        return allowedZombieTypes;
    }

}
