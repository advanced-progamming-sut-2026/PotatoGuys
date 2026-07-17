package pvz.Models.Games.Levels.Data;

import pvz.Models.Games.Levels.LevelType;

public abstract class LevelDefinition {
    public LevelType type;
    public int levelNumber;
    public int initialSun;
    public MapDefinition map;
}
