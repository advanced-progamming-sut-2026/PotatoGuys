package com.pvz.models.entities.plants.fsm;

import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.games.GameContext;

/** Dedicated state for a Plant Food trigger, parallel to {@link PlantActionState} but forced/immediate. */
public class PlantFeedState extends PlantState {

    private final PlantAction feedAction;

    public PlantFeedState(PlantAction feedAction) {
        this.feedAction = feedAction;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        feedAction.execute(plant, ctx);
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
        return "Feed[" + feedAction.getName() + "]";
    }

    @Override
    public void draw(Plant plant, GameContext ctx) {
        PamAnimationConfig config = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        PvZ2.pamPlayer.draw(PvZ2.batch, config.pamFilePath , config.plantFoodLabel, stateTime, x, y, true);
    }
}
