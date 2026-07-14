package pvz.Models.Seasons.Levels.SpecialLevels;

import java.util.List;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Seasons.Levels.GameMap;
import pvz.Models.Seasons.Levels.LevelType;
import pvz.Models.Seasons.Levels.SpecialLevel;
import pvz.Models.Seasons.Levels.Wave;

public class LockedPlants extends SpecialLevel {
    private List<Plant> lockedPlants;
    private List<Plant> forcedPlants;

    public LockedPlants(GameEngine engine, GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(engine, gameMap, levelNumber, type, initialSun, waves);
    }

    public List<Plant> getLockedPlants() {
        return lockedPlants;
    }

    public List<Plant> getForcedPlants() {
        return forcedPlants;
    }
}
