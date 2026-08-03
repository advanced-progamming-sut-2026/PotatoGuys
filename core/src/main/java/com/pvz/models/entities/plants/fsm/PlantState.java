package com.pvz.models.entities.plants.fsm;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;

public interface PlantState {

    void onEnter(Plant plant, GameContext ctx);

    PlantState tick(Plant plant, GameContext ctx);

    void onExit(Plant plant, GameContext ctx);

    String getLabel();
}
