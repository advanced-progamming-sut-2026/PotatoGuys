package com.pvz.models.entities.plants.actions;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;

public final class PassiveAction implements PlantAction {

    public static final PassiveAction INSTANCE = new PassiveAction();

    private PassiveAction() { }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx) { return false; }

    @Override
    public void execute(Plant plant, GameContext ctx) { /* never invoked */ }

    @Override
    public String getName() { return "Passive"; }
}
