package com.pvz.models.entities.zombies.skills;

import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Abstract base for skills that fire on a repeating cooldown.
 *
 * <p>Subclasses implement:
 * <ul>
 *   <li>{@link #canUse(Zombie, GameContext)} — world-state guard (e.g. is there sun to steal?)</li>
 *   <li>{@link #doExecute(Zombie, GameContext)} — the actual skill effect</li>
 * </ul>
 *
 * <p>The cooldown counter is incremented every call to {@link #shouldTrigger}.
 * This means it must be called every tick to count correctly — {@link
 * pvz.models.entities.zombies.fsm.WalkState} already does this.
 */
public abstract class CooldownSkill implements ZombieSkill {

    private final int cooldownTicks;
    private int elapsed; // ticks since last use (starts at max so skill is ready immediately)

    /**
     * @param cooldownSeconds the minimum interval between uses in in-game seconds
     *                        (multiply by 10 ticks/s internally)
     */
    protected CooldownSkill(float cooldownSeconds) {
        this.cooldownTicks = Math.max(1, (int) (cooldownSeconds * Zombie.TICKS_PER_SECOND));
        this.elapsed = cooldownTicks; // ready on first check
    }

    @Override
    public final boolean shouldTrigger(Zombie zombie, GameContext ctx) {
        elapsed++;
        if (elapsed < cooldownTicks) return false;
        return canUse(zombie, ctx);
    }

    @Override
    public final void execute(Zombie zombie, GameContext ctx) {
        elapsed = 0;
        doExecute(zombie, ctx);
    }

    /**
     * Additional world-state condition checked after the cooldown is ready.
     * Override to add pre-conditions (e.g. ammo check, range check).
     */
    protected abstract boolean canUse(Zombie zombie, GameContext ctx);

    /** Perform the skill's game-world side-effect. */
    protected abstract void doExecute(Zombie zombie, GameContext ctx);

    // protected abstract void onDeath(Zombie zombie, GameContext ctx);
}
