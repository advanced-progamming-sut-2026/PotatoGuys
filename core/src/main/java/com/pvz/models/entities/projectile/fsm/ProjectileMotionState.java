package com.pvz.models.entities.projectile.fsm;

import com.pvz.models.entities.projectile.Projectile;

/**
 * State-pattern counterpart of {@code PlantState}, but for how a projectile moves
 * through the world tick-to-tick (as opposed to what happens when it lands/hits —
 * see {@link com.pvz.models.entities.projectile.effects.ProjectileEffectState}).
 *
 * <p>Per Phase 1: plants/zombies launch either
 * <ul>
 *   <li><b>Straight</b> projectiles — travel a fixed direction until they hit something, or</li>
 *   <li><b>Lobbed</b> ("aerial") projectiles — a parabolic arc from the launching plant
 *       onto the targeted zombie's position (Melon-pult family, Pepper-pult, Cabbage-pult...).</li>
 * </ul>
 * Any other flavor of motion mentioned in Phase 1 has no special behavior and simply
 * reuses {@link StraightMotionState}.
 */
public abstract class ProjectileMotionState {

    protected float stateTime = 0f;

    /** Called once when the projectile switches into this motion state. */
    public void onEnter(Projectile projectile) {
        stateTime = 0f;
    }

    /** Advances the projectile's position for this tick. */
    public abstract void update(Projectile projectile, float dt);

    /** Short label used for debugging / CLI status output. */
    public String getLabel() {
        return "Motion";
    }
}
