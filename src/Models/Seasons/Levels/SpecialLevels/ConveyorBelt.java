package Models.Seasons.Levels.SpecialLevels;

import Models.Plants.Plant;
import Models.Seasons.Levels.SpecialLevel;

import java.util.List;

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
