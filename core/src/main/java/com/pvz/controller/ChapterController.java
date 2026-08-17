package com.pvz.controller;

import com.pvz.models.AppContext;
import com.pvz.models.games.seasons.Season;

public class ChapterController {
    Season season;
    public ChapterController(String chapterName){
        season=AppContext.getInstance().getCurrentUser().getProfile().getSeasonByName(chapterName);
    }

    public boolean isLevelUnlocked(int levelNumber){
        if (season == null) return true;
        return season.isLevelUnlocked(levelNumber);
    }
}
