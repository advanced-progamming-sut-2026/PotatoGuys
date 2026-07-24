package pvz.models.games.levels.variants;

import java.util.List;

import pvz.models.games.levels.Level;
import pvz.models.games.levels.LevelType;
import pvz.models.games.levels.Wave;
import pvz.models.games.map.data.GameMapDefinition;
import pvz.models.games.modes.GameModeType;

public class ConveyorBeltLevel extends Level {
    private final List<Wave> waves;

    public ConveyorBeltLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun,
            List<Wave> waves) {
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
