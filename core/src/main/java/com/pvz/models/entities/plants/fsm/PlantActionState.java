package com.pvz.models.entities.plants.fsm;

import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.games.GameContext;

public class PlantActionState extends PlantState {

    private final PlantState action;

    public PlantActionState(PlantState action) {
        this.action = action;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        action.onEnter(plant, ctx);
    }

    @Override
    public void update(Plant plant, GameContext ctx , float dt) {
        super.update(plant, ctx, dt);
        action.update(plant, ctx, dt);
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        action.onExit(plant, ctx);
    }

    @Override
    public String getLabel() {
        return "Action[" + action.getLabel() + "]";
    }

    @Override
    public void draw(Plant plant, GameContext ctx) {
        PamAnimationConfig config = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        PvZ2.pamPlayer.draw(PvZ2.batch, config.pamFilePath , config.attackActionLabel, stateTime, x, y, true);
    }
}
