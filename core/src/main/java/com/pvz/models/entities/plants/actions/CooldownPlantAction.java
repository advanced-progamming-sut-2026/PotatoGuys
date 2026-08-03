package com.pvz.models.entities.plants.actions;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.games.GameContext;

public abstract class CooldownPlantAction implements PlantAction {

    private final int cooldownTicks;
    private final int onFeedTicks = 3;
    private int elapsed;
    private int onFeed = onFeedTicks;

    /** @param cooldownSeconds minimum interval between activations, in in-game seconds. */
    protected CooldownPlantAction(float cooldownSeconds) {
        this.cooldownTicks = Math.max(1, Math.round(cooldownSeconds * Plant.TICKS_PER_SECOND));
        this.elapsed = cooldownTicks; // ready on first check
    }

    @Override
    public final boolean shouldTrigger(Plant plant, GameContext ctx) {
        if (onFeed > 0) {
            return true;
        }

        elapsed++;
        if (elapsed < cooldownTicks) return false;
        return canUse(plant, ctx);
    }

    @Override
    public final void execute(Plant plant, GameContext ctx) {
        elapsed = 0;
        if (onFeed > 0) onFeed--;
        doExecute(plant, ctx);
    }

    public final void forceReady() {
        elapsed = cooldownTicks;
        onFeed = onFeedTicks;
    }

    protected abstract boolean canUse(Plant plant, GameContext ctx);

    protected abstract void doExecute(Plant plant, GameContext ctx);
}
