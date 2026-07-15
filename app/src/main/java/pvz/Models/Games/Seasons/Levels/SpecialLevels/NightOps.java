package pvz.Models.Seasons.Levels.SpecialLevels;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Games.map.GameMap;
import pvz.Models.Seasons.Levels.LevelType;
import pvz.Models.Seasons.Levels.SpecialLevel;
import pvz.Models.Seasons.Levels.Wave;

import java.util.List;

public class NightOps extends SpecialLevel {
    public NightOps(GameEngine engine, GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(engine, gameMap, levelNumber, type, initialSun, waves);
    }
}
