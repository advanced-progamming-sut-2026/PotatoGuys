package com.pvz.models.entities.plants.actions.explosive.potato_mine;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.PotatoMineConfig;
import com.pvz.models.games.GameContext;

public class PotatoMineAction extends PlantAction {
    PotatoMineConfig config;
    float stateTime2;
    private boolean triggred;

    public PotatoMineAction(PotatoMineConfig config){
        this.config=config;
        stateTime2=0;
        triggred=false;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime2+=dt;
        if (stateTime2>=config.plantTime && !triggred){
            triggred=true;
            return true;
        }
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {

    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime>=config.idleTime) {
            plant.changeState(new PotatoMineReadyAction(config));
            System.out.println("changed potato mine state to Ready");
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(GameController.colToWorldX(plant.getCol()),GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        PamAnimationConfig pamAnimationConfig = plant.getSheet().pamAnimationConfig;
        return new FrameConfig(pamAnimationConfig.pamFilePath,"plant_idle",stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
