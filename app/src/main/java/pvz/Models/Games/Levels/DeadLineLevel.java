package pvz.Models.Games.Levels;

import java.util.List;
import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.GameMap;

public class DeadLineLevel extends Level {
    private final List<Wave> waves;
    private final int deadlineColumn;

    public DeadLineLevel(GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves, int deadlineColumn) {
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