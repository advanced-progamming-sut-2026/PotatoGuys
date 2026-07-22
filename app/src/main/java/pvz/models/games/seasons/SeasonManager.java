package pvz.models.games.seasons;

import java.util.List;

import pvz.models.user.Profile;
import pvz.models.user.User;

import java.io.File;

public class SeasonManager {
    private static final String BASE_PATH = "app/src/main/resources/data/seasons/";

    public void unlockNextLevel(User user, String currentSeasonName, int currentLevelNumber) {
        Profile profile = user.getProfile();
        List<Season> seasons = profile.getSeasons();
        
        // 1. Find current season
        Season currentSeason = null;
        int currentSeasonIndex = -1;
        
        for (int i = 0; i < seasons.size(); i++) {
            if (seasons.get(i).getName().equalsIgnoreCase(currentSeasonName)) {
                currentSeason = seasons.get(i);
                currentSeasonIndex = i;
                break;
            }
        }
        
        if (currentSeason == null) return;
        
        // 2. Try to unlock next level in current season
        int nextLevel = currentLevelNumber + 1;
        String nextLevelPath = BASE_PATH + currentSeasonName.toLowerCase() + "/level_" + nextLevel + ".json";
        File nextLevelFile = new File(nextLevelPath);
        
        if (nextLevelFile.exists()) {
            currentSeason.unlockLevel(nextLevel);
        } else {
            // 3. If no next level, try to unlock first level of next season
            if (currentSeasonIndex + 1 < seasons.size()) {
                Season nextSeason = seasons.get(currentSeasonIndex + 1);
                nextSeason.unlock();
                nextSeason.unlockLevel(1);
            }
        }
    }
}
