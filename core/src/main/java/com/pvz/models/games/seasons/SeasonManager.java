package com.pvz.models.games.seasons;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.pvz.models.user.Profile;
import com.pvz.models.user.User;

public class SeasonManager {
    private static final String BASE_PATH = "resources/data/seasons/";

    private static String subDir(String seasonName) {
        return seasonName.equalsIgnoreCase("izombie") ? "izombie" : seasonName.toLowerCase();
    }

    /** Returns true if the given season/level pair has a JSON data file available. */
    public static boolean hasLevel(String seasonName, int levelNumber) {
        return Gdx.files.internal(BASE_PATH + subDir(seasonName) + "/level_" + levelNumber + ".json").exists();
    }

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
        if (hasLevel(currentSeasonName, nextLevel)) {
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