package com.pvz.models.entities.plants.actions;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.fsm.PlantState;
import com.pvz.models.games.GameContext;

public abstract class PlantAction extends PlantState {
    public abstract boolean shouldTrigger(Plant plant, GameContext ctx , float dt);

}
