package pvz.Models.Seasons.Levels.SpecialLevels;

import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Seasons.Levels.SpecialLevel;

public class LockedPlants extends SpecialLevel {
    private List<Plant> lockedPlants;
    private List<Plant> forcedPlants;

    public List<Plant> getLockedPlants() {
        return lockedPlants;
    }

    public List<Plant> getForcedPlants() {
        return forcedPlants;
    }
}
