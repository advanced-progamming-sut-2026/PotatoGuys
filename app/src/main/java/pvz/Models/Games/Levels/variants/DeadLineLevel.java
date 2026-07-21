package pvz.models.games.levels.variants;

import java.util.List;

import pvz.models.games.levels.Level;
import pvz.models.games.levels.LevelType;
import pvz.models.games.levels.Wave;
import pvz.models.games.map.data.GameMapDefinition;
import pvz.models.games.modes.GameModeType;

public class DeadLineLevel extends Level {
    private final List<Wave> waves;
    private final int deadlineColumn;

    public DeadLineLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves, int deadlineColumn) {
        super(GameModeType.DEADLINE, gameMap, levelNumber, type, initialSun);
        this.waves = waves;
        this.deadlineColumn = deadlineColumn;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public int getDeadlineColumn() {
        return deadlineColumn;
    }
}