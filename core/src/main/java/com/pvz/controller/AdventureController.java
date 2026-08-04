package com.pvz.controller;

import com.pvz.models.AppContext;
import com.pvz.models.games.seasons.Season;

import java.util.HashMap;
import java.util.Map;

public class AdventureController {
    public AdventureController(){

    }

    public Map<String,Boolean> getSeasonsState(){
        Map<String, Boolean> seasonsState=new HashMap<>();
        for (Season s:AppContext.getInstance().getCurrentUser().getProfile().getSeasons()){
            seasonsState.put(s.getName(),s.isLocked());
        }
        return seasonsState;
    }
}
