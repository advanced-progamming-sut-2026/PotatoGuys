package com.pvz.models.entities.plants.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;

public abstract class PlantState {

    protected float stateTime = 0f;

    public abstract void onEnter(Plant plant, GameContext ctx);

    public void update(Plant plant, GameContext ctx , float dt){
        stateTime += dt;
    };

    public abstract void onExit(Plant plant, GameContext ctx);

    public abstract FrameConfig draw(Plant plant, GameContext ctx);

    public abstract String getLabel();
}
