package pvz.models.entities.plants.actions;

import pvz.models.entities.plants.Plant;
import pvz.models.games.GameContext;

public abstract class CooldownPlantAction implements PlantAction {

    private final int cooldownTicks;
    private int elapsed;

    /** @param cooldownSeconds minimum interval between activations, in in-game seconds. */
    protected CooldownPlantAction(float cooldownSeconds) {
        this.cooldownTicks = Math.max(1, Math.round(cooldownSeconds * Plant.TICKS_PER_SECOND));
        this.elapsed = cooldownTicks; // ready on first check
    }

    @Override
    public final boolean shouldTrigger(Plant plant, GameContext ctx) {
        elapsed++;
        if (elapsed < cooldownTicks) return false;
        return canUse(plant, ctx);
    }

    @Override
    public final void execute(Plant plant, GameContext ctx) {
        elapsed = 0;
        doExecute(plant, ctx);
    }

    /** Skips the remaining cooldown so the very next check fires immediately (Plant-Food rapid-fire hook). */
    public final void forceReady() { elapsed = cooldownTicks; }

    /** Additional world-state precondition checked once the cooldown has elapsed. */
    protected abstract boolean canUse(Plant plant, GameContext ctx);

    /** The action's actual game-world effect. */
    protected abstract void doExecute(Plant plant, GameContext ctx);
}
