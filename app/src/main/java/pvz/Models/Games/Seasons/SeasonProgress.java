package pvz.Models.Games.Seasons;

import java.util.Map;

public class SeasonProgress {
    private Season season;
    private Map<Integer,Boolean> unlockedLevels;

    public boolean isLevelUnlocked(int levelNumber){
        return true;
    }

    public Season getSeason() {
        return season;
    }

    public Map<Integer, Boolean> getUnlockedLevels() {
        return unlockedLevels;
    }
}
