package com.pvz.models.entities.plants.fsm;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.games.GameContext;

public class PlantActionState implements PlantState {

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
        action.execute(plant, ctx);
    }

    @Override
    public PlantState tick(Plant plant, GameContext ctx) {
        if (plant.isDead()) return this;
        if (pauseTicksRemaining > 0) {
            pauseTicksRemaining--;
            return this;
        }
        return new PlantIdleState();
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Action[" + action.getName() + "]";
    }
}
