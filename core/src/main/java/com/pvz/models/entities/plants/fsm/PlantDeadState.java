package com.pvz.models.entities.plants.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;


public class PlantDeadState extends PlantState {

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        // death logging happens inside Plant.kill() so it fires exactly once
    }

    @Override
    public void update(Plant plant, GameContext ctx , float dt) {
        super.update(plant, ctx, dt);
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Dead";
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        System.out.println("ahhh");
        return null;
    }
}
