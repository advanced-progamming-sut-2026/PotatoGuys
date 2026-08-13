package com.pvz.models.entities.plants.fsm;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.AnimationCatalog;
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
        feedAction.onEnter(plant, ctx);
    }

    @Override
    public void update(Plant plant, GameContext ctx , float dt) {
        super.update(plant, ctx, dt);
        feedAction.update(plant, ctx, dt);
        if(stateTime >= AnimationCatalog.getInstance().getClipDuration(plant.getSheet().pamAnimationConfig.pamFilePath, plant.getSheet().pamAnimationConfig.plantFoodLabel)) {
            plant.changeState(new PlantIdleState());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        feedAction.onExit(plant, ctx);
    }

    @Override
    public String getLabel() {
        return "Feed[" + feedAction.getLabel() + "]";
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig config = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x,y);
        Vector2 scale = new Vector2(0.7f,0.7f);
        return new FrameConfig(config.pamFilePath,config.plantFoodLabel,stateTime,pos,scale,
            null,true);
    }
}
