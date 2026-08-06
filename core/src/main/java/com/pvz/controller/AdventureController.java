package com.pvz.controller;

import com.pvz.models.AppContext;
import com.pvz.models.games.seasons.Season;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdventureController {
    public AdventureController(){

    }

    public boolean isSeasonLocked(String seasonName){
        List<Season> seasons=AppContext.getInstance().getCurrentUser().getProfile().getSeasons();
        for (Season s: seasons){
            if (s.getName().equalsIgnoreCase(seasonName)){
                return s.isLocked();
            }
        }
        return true;
    }
}
