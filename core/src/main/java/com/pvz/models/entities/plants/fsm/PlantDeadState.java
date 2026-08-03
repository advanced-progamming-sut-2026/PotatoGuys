package com.pvz.models.entities.plants.fsm;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;


public class PlantDeadState implements PlantState {

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        // death logging happens inside Plant.kill() so it fires exactly once
    }

    @Override
    public PlantState tick(Plant plant, GameContext ctx) {
        return this;
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Dead";
    }
}
