package com.pvz.models.entities.projectile.effects;

import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * State-pattern counterpart of {@code PlantAction}, but for what a projectile does
 * when it actually connects — as opposed to how it travels
 * (see {@link com.pvz.models.entities.projectile.fsm.ProjectileMotionState}).
 *
 * <p>Per Phase 1, projectile types are grouped as:
 * <ul>
 *   <li>{@link NormalEffectState} — plain projectiles, no special effect.</li>
 *   <li>{@link FireEffectState} — simple fire projectiles (single target, sets it ablaze).</li>
 *   <li>{@link AreaFireEffectState} — Pepper-pult: area fire effect.</li>
 *   <li>{@link IceEffectState} — simple ice/snow projectiles (single target, chills it).</li>
 *   <li>{@link MelonEffectState} — Melon-pult / Winter Melon-pult: area damage, optionally chilling.</li>
 *   <li>{@link PiercingEffectState} — Smoke Cloud / Cactus Spike: passes through targets
 *       instead of being consumed by the first hit.</li>
 * </ul>
 */
public abstract class ProjectileEffectState {

    /**
     * Resolves the projectile's impact.
     *
     * @param projectile    the projectile that hit something
     * @param primaryTarget the zombie that was actually touched (collision or, for lobbed
     *                      projectiles, the nearest zombie at the landing spot) — may be
     *                      {@code null} for area effects that pick their own targets from {@code ctx}
     * @param ctx           world context, used to look up neighboring zombies for area effects
     */
    public abstract void onImpact(Projectile projectile, Zombie primaryTarget, GameContext ctx);

    /** True for projectiles (Smoke Cloud, Cactus Spike) that keep flying through zombies. */
    public boolean piercesThrough() {
        return false;
    }

    /** True for damage that should bypass armor entirely (poison-style effects). */
    protected boolean bypassesArmor() {
        return false;
    }

    /** Short label used for debugging / CLI status output. */
    public String getLabel() {
        return "Effect";
    }
}
