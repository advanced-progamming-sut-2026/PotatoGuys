package pvz.Models.Games.Levels;

import java.util.List;

import pvz.Models.Games.Modes.GameModeType;
import pvz.Models.Games.map.GameMap;

public class NormalLevel extends Level {
    private final List<Wave> waves;
    
    public NormalLevel(GameMap gameMap, int levelNumber, LevelType type, int initialSun, List<Wave> waves) {
        super(GameModeType.NORMAL, gameMap, levelNumber, type, initialSun);
        this.waves = waves;
    }
    
    public List<Wave> getWaves() {
        return waves;
    }
}
