package Models.Seasons.Levels;

import Models.Seasons.Levels.SpecialLevels.SpecialLevelType;

public abstract class SpecialLevel extends Level {
    private SpecialLevelType specialType;

    public SpecialLevelType getSpecialType() {
        return specialType;
    }
}
