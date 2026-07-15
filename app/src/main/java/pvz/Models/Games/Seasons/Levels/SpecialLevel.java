package pvz.Models.Seasons.Levels;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Games.map.GameMap;
import pvz.Models.Seasons.Levels.SpecialLevels.SpecialLevelType;

import java.util.List;

public abstract class SpecialLevel extends Level {
    private SpecialLevelType specialType;

    public SpecialLevel(GameEngine engine, GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(engine, gameMap, levelNumber, type, initialSun, waves);
    }

    public SpecialLevelType getSpecialType() {
        return specialType;
    }
}
