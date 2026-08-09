package com.pvz.models.entities.plants.fsm;

import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
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
        if (plant.getAttackAction().shouldTrigger(plant, ctx)) {
            plant.changeState(new PlantActionState(plant.getAttackAction()));
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
    public void draw(Plant plant, GameContext ctx) {
        PamAnimationConfig config = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        PvZ2.pamPlayer.draw(PvZ2.batch, config.pamFilePath , config.idleLabel, stateTime, x, y, true);
    }
}
