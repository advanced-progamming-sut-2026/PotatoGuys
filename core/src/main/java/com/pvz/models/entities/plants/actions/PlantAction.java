package com.pvz.models.entities.plants.actions;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;

public interface PlantAction {

    boolean shouldTrigger(Plant plant, GameContext ctx);

    void execute(Plant plant, GameContext ctx);

    String getName();
}
