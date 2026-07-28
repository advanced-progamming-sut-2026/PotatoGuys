package pvz.models.games.levels.variants;

import java.util.List;

import pvz.models.games.levels.Level;
import pvz.models.games.levels.LevelType;
import pvz.models.games.levels.Wave;
import pvz.models.games.map.data.GameMapDefinition;
import pvz.models.games.modes.GameModeType;

public class WallnutBowlingLevel extends Level {
    private final List<Wave> waves;
    private final int deadlineColumn;

    public WallnutBowlingLevel(GameMapDefinition gameMap, int levelNumber, int initialSun,
            List<Wave> waves, int deadlineColumn) {
        super(GameModeType.WALLNUTBOWLING, gameMap, levelNumber, LevelType.MINIGAME, initialSun);
        this.waves = waves;
        this.deadlineColumn = deadlineColumn;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public int getDeadlineColumn() {
        return deadlineColumn;
    }

    @Override
    public boolean hasPreGame() {
        return false;
    }
}
