package pvz.Models.Seasons.Levels.SpecialLevels;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Seasons.Levels.SpecialLevel;

import java.util.List;

public class SaveOurSeeds extends SpecialLevel {
    private List<Vector2> protectedPositions;

    public List<Vector2> getProtectedPositions() {
        return protectedPositions;
    }
}
