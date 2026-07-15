package pvz.Models.Seasons.Levels.SpecialLevels;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Engine.GameEngine;
import pvz.Models.Games.map.GameMap;
import pvz.Models.Seasons.Levels.LevelType;
import pvz.Models.Seasons.Levels.SpecialLevel;
import pvz.Models.Seasons.Levels.Wave;

import java.util.List;

public class SaveOurSeeds extends SpecialLevel {
    private List<Vector2> protectedPositions;

    public SaveOurSeeds(GameEngine engine, GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(engine, gameMap, levelNumber, type, initialSun, waves);
    }

    public List<Vector2> getProtectedPositions() {
        return protectedPositions;
    }
}
