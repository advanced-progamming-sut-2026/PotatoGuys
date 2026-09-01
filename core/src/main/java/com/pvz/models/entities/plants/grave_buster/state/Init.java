package com.pvz.models.entities.plants.grave_buster.state;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.fsm.PlantState;
import com.pvz.models.entities.plants.grave_buster.GraveBuster;
import com.pvz.models.games.GameContext;

public class Init extends PlantState {
    public static final String CLIP="attack";

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        System.out.println("init grave buster");
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime>=1f){
            plant.changeState(new Eat());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(
                GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        return new FrameConfig(GraveBuster.PAM_PATH,CLIP,stateTime,position,scale,null,true);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
