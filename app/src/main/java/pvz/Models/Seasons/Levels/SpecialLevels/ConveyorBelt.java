package pvz.Models.Seasons.Levels.SpecialLevels;

import java.util.List;

import pvz.Models.Plants.Plant;
import pvz.Models.Seasons.Levels.SpecialLevel;

public class ConveyorBelt extends SpecialLevel {
    private List<Plant> possiblePlants;
    private int initialDelayTicks;
    private int DelayTicks;

    public List<Plant> getPossiblePlants() {
        return possiblePlants;
    }

    public int getInitialDelayTicks() {
        return initialDelayTicks;
    }

    public int getDelayTicks() {
        return DelayTicks;
    }
}
