package pvz.Models.Entities.Plants.actions;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Games.GameContext;

/**
 * Abstract base for actions that fire on a repeating cooldown — the plant
 * counterpart of {@link pvz.Models.Entities.Zombies.skills.CooldownSkill}.
 *
 * <p>The cooldown is the plant's own (level-resolved) action interval, so it
 * is supplied per-instance by {@link pvz.Models.Entities.Plants.PlantFactory}
 * rather than hard-coded per plant kind.
 */
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
