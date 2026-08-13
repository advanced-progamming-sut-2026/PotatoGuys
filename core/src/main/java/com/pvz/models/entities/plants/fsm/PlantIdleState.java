package com.pvz.models.entities.plants.fsm;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.games.GameContext;

public class PlantIdleState extends PlantState {

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        // no visual cue needed for idling
    }

    @Override
    public void update(Plant plant, GameContext ctx , float dt) {
        super.update(plant, ctx, dt);
        if (plant.getAttackAction().shouldTrigger(plant, ctx , dt)) {
            plant.changeState(plant.getAttackAction());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Idle";
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig config = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x,y);
        Vector2 scale = new Vector2(0.65f,0.65f);
        return new FrameConfig(config.pamFilePath,config.idleLabel,stateTime,pos,scale,null,true);
    }
}
