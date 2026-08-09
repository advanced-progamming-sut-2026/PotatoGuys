package com.pvz.models.entities.plants.fsm;

import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.games.GameContext;

public class PlantActionState extends PlantState {

    private final PlantAction action;
    private int pauseTicksRemaining;

    public PlantActionState(PlantAction action, int pauseTicks) {
        this.action = action;
        this.pauseTicksRemaining = pauseTicks;
    }

    public PlantActionState(PlantAction action) {
        this(action, 0);
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        action.execute(plant, ctx);
    }

    @Override
    public void update(Plant plant, GameContext ctx , float dt) {
        super.update(plant, ctx, dt);
        if (plant.isDead()) return;
        if (pauseTicksRemaining > 0) {
            pauseTicksRemaining--;
            return;
        }
        // return new PlantIdleState();
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Action[" + action.getName() + "]";
    }

    @Override
    public void draw(Plant plant, GameContext ctx) {
        PamAnimationConfig config = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        PvZ2.pamPlayer.draw(PvZ2.batch, config.pamFilePath , config.attackActionLabel, stateTime, x, y, true);
    }
}
