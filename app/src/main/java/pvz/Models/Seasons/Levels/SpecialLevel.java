package pvz.Models.Seasons.Levels;

import pvz.Models.Seasons.Levels.SpecialLevels.SpecialLevelType;

public abstract class SpecialLevel extends Level {
    private SpecialLevelType specialType;

    public SpecialLevelType getSpecialType() {
        return specialType;
    }
}
