package com.pvz.models.entities.zombies.skills;

import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieSkillConfig;
import com.pvz.models.entities.zombies.fsm.ZombieState;
import com.pvz.models.games.GameContext;

/**
 * Abstract base for skills that fire on a repeating cooldown (e.g. Ra's
 * sun-steal, TombRaiser's casts, Wizard/Hunter attacks).
 *
 * <p>{@link #shouldTrigger} accumulates real seconds while the zombie walks and
 * returns {@code true} as soon as the cooldown has elapsed and {@link #canUse}
 * allows it. The cooldown timer resets when the skill fires (on state entry).
 */
public abstract class CooldownSkill extends ZombieState {

    private final float cooldownSeconds;
    private float cooldownTimer = 0f;

    /** @param config           the skill's JSON config (label, duration, …) */
    /** @param cooldownSeconds  minimum interval between uses, in seconds */
    protected CooldownSkill(ZombieSkillConfig config, float cooldownSeconds) {
        super(config);
        this.cooldownSeconds = Math.max(0f, cooldownSeconds);
    }

    @Override
    public final boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        cooldownTimer += dt;
        if (cooldownTimer < cooldownSeconds) return false;
        return canUse(zombie, ctx);
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        super.onEnter(zombie, ctx);
        cooldownTimer = 0f;
    }

    /**
     * Additional world-state condition checked after the cooldown is ready.
     * Override to add pre-conditions (e.g. ammo check, range check).
     */
    protected abstract boolean canUse(Zombie zombie, GameContext ctx);
}
