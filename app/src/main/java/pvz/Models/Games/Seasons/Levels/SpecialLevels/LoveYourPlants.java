package pvz.Models.Seasons.Levels.SpecialLevels;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Seasons.Levels.GameMap;
import pvz.Models.Seasons.Levels.LevelType;
import pvz.Models.Seasons.Levels.SpecialLevel;
import pvz.Models.Seasons.Levels.Wave;

import java.util.List;

public class LoveYourPlants extends SpecialLevel {
    private int plantsDieLimit;

    public LoveYourPlants(GameEngine engine, GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(engine, gameMap, levelNumber, type, initialSun, waves);
    }

    public int getPlantsDieLimit() {
        return plantsDieLimit;
    }
}
