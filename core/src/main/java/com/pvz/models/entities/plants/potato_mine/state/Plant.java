package com.pvz.models.entities.plants.potato_mine.state;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.fsm.PlantState;
import com.pvz.models.entities.plants.grave_buster.GraveBuster;
import com.pvz.models.entities.plants.potato_mine.PotatoMine;
import com.pvz.models.games.GameContext;

public class Plant extends PlantState {
    public static final String CLIP="plant";

    @Override
    public void onEnter(com.pvz.models.entities.plants.Plant plant, GameContext ctx) {

    }

    @Override
    public void update(com.pvz.models.entities.plants.Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (stateTime> PotatoMine.PLANT_DURATION) plant.changeState(new Idle());
    }

    @Override
    public void onExit(com.pvz.models.entities.plants.Plant plant, GameContext ctx) {

    }

    @Override
    public FrameConfig draw(com.pvz.models.entities.plants.Plant plant, GameContext ctx) {
        Vector2 position= new Vector2(GameController.colToWorldX(plant.getCol()),GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f,0.65f);
        return new FrameConfig(PotatoMine.PAM_PATH,CLIP,stateTime,position,scale,null,false);
    }

    @Override
    public String getLabel() {
        return "";
    }
}
