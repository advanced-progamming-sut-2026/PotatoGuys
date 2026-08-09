package com.pvz.models.entities.plants.actions.shooters;

import com.pvz.models.engine.TickAware;
import com.pvz.models.games.GameContext;

/** One-off tick countdown used to honor a {@code ProjectilePattern}'s delaySeconds before firing. */
final class DelayedShot implements TickAware {

    private final GameContext ctx;
    private final Runnable onFire;
    private int ticksRemaining;

    DelayedShot(int ticksRemaining, GameContext ctx, Runnable onFire) {
        this.ticksRemaining = ticksRemaining;
        this.ctx = ctx;
        this.onFire = onFire;
    }

    @Override
    public void enter() {
        // nothing to do until the countdown elapses
    }

    @Override
    public void update(float dt) {
        if (--ticksRemaining <= 0) {
            onFire.run();
            ctx.getEngine().unRegister(this);
        }
    }

    @Override
    public void dispose() {
        // no owned resources
    }
}
