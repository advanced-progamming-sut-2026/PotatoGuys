package com.pvz.models.entities.plants.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.games.GameContext;

/**
 * Thin wrapper over a Plant Food action. The action is now self-contained (it carries
 * its own animation label, performs the effect and returns the plant to {@code idle}),
 * so this state only forwards lifecycle calls.
 */
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
        return feedAction.draw(plant, ctx);
    }
}
