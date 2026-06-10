package Models.Seasons.Levels;

import Models.Enums.SpecialLevelType;

public abstract class SpecialLevel extends Level {
    private SpecialLevelType specialType;

    public SpecialLevelType getSpecialType() {
        return specialType;
    }
}
