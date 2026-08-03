package com.pvz.models.entities.plants.fsm;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;

public class PlantIdleState implements PlantState {

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        // no visual cue needed for idling
    }

    @Override
    public PlantState tick(Plant plant, GameContext ctx) {
        if (plant.getAction().shouldTrigger(plant, ctx)) {
            return new PlantActionState(plant.getAction());
        }
        return this;
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Idle";
    }
}
