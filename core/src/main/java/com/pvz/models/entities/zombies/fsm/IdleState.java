package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Stationary state — the zombie stands in place playing its idle clip.
 * Used by IZombie sun-producers which must not walk.
 */
public class IdleState extends ZombieState {

    public IdleState() {
        super(null);
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        stateTime = 0f;
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
    }

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        return false;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
    }

    @Override
    public String getName() {
        return "Idle";
    }

    @Override
    public String getLabel() {
        return "Idle";
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        return zombie.drawClip("idle");
    }
}
