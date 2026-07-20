package pvz.Models.Games.Levels.variants;

import java.util.List;

import pvz.Models.Games.Levels.Level;
import pvz.Models.Games.Levels.LevelType;
import pvz.Models.Games.Levels.Wave;
import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.GameMap;

public class PlantWhatYouGetLevel extends Level {
    private final List<Wave> waves;

    public PlantWhatYouGetLevel(GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(GameModeType.PLANTWHATYOUGET, gameMap, levelNumber, type, initialSun);
        this.waves = waves;
    }

    public List<Wave> getWaves() {
        return waves;
    }
}