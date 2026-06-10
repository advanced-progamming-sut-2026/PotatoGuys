package Models.Seasons;

import java.util.List;
import java.util.Map;

public class SeasonManager {
    private List<Season> allSeasons;
    private Map<String,SeasonProgress> userProgress;

    public Season getSeason(String name){
        return null;
    }

    public void unlockLevel(String username, String seasonName, int levelNumber){

    }

    public boolean isLevelUnlocked(String username, String seasonName, int levelNumber){
        return true;
    }

    public void saveProgress(){

    }

    public List<Season> getAllSeasons() {
        return allSeasons;
    }

    public Map<String, SeasonProgress> getUserProgress() {
        return userProgress;
    }
}
