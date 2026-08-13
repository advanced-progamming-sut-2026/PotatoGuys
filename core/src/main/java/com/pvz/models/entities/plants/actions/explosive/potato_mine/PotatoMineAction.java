package com.pvz.models.entities.plants.actions.explosive.potato_mine;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.explosive.PotatoMineConfig;
import com.pvz.models.games.GameContext;

public class PotatoMineAction extends PlantAction {
    PotatoMineConfig config;
    float stateTime2;

    public PotatoMineAction(PotatoMineConfig config){
        this.config=config;
        stateTime2=0;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime2+=dt;
        return stateTime2 >= config.plantTime;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime>=config.idleTime) plant.changeState(new PotatoMineReadyAction(config));
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        return null;
    }

    @Override
    public String getLabel() {
        return "";
    }
}
