package com.pvz.models.entities.projectile.fsm;

import com.pvz.models.entities.projectile.Projectile;

/**
 * Default straight-line travel used by peas, spikes, fumes, stars, and any other
 * projectile type without a special motion pattern. This is exactly the movement
 * {@code Projectile.update(dt)} used to do inline in Phase 1, extracted into its own state.
 */
public class StraightMotionState extends ProjectileMotionState {

    /** Matches the multiplier previously hard-coded in Projectile.update(). */
    private static final float SPEED_SCALE = 80f;

    @Override
    public void update(Projectile projectile, float dt) {
        stateTime += dt;
        projectile.getPos().x += projectile.getVelocity().x * SPEED_SCALE * dt;
        projectile.getPos().y += projectile.getVelocity().y * SPEED_SCALE * dt;
    }

    @Override
    public String getLabel() {
        return "Straight";
    }
}
