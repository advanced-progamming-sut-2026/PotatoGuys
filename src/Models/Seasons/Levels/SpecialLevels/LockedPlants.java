package Models.Seasons.Levels.SpecialLevels;

import Models.Plants.Plant;
import Models.Seasons.Levels.SpecialLevel;

import java.util.List;

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
