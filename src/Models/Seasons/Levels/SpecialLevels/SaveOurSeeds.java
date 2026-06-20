package Models.Seasons.Levels.SpecialLevels;

import Models.DataTypes.Vector2;
import Models.Seasons.Levels.SpecialLevel;

import java.util.List;

public class SaveOurSeeds extends SpecialLevel {
    private List<Vector2> protectedPositions;

    public List<Vector2> getProtectedPositions() {
        return protectedPositions;
    }
}
