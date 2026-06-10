package Models.Seasons;

import java.util.Map;

public class SeasonProgress {
    private String userName;
    private Season season;
    private Map<Integer,Boolean> unlockedLevels;

    public boolean isLevelUnlocked(int levelNumber){
        return true;
    }

    public String getUserName() {
        return userName;
    }

    public Season getSeason() {
        return season;
    }

    public Map<Integer, Boolean> getUnlockedLevels() {
        return unlockedLevels;
    }
}
