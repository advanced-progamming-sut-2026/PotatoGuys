package pvz.Models.Games.Levels.variants;

import java.util.List;

import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelType;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.GameMap;

public class ConveyorBeltLevel extends Level {
    private final List<Wave> waves;
    
    public ConveyorBeltLevel(GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(GameModeType.CONVEYORBELT, gameMap, levelNumber, type, initialSun);
        this.waves = waves;
    }
    
    public List<Wave> getWaves() {
        return waves;
    }

    @Override
    public boolean hasPreGame() {
        return false;
    }
}
