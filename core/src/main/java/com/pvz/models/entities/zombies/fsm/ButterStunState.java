package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Zombie is stunned by Kernel-pult butter — frozen in place with a yellow
 * tint for the duration, then returns to whatever state it was in before.
 * The butter part is shown via buildPartsVisibility() in Zombie.
 */
public class ButterStunState extends ZombieState {

    private final ZombieState lastState;
    private final float lastStateFrameTime;
    private final float duration;
    private final String pamPath;
    private final String clip;

    public ButterStunState(ZombieState lastState, float lastStateFrameTime, float duration,
                           String pamPath, String clip) {
        super(null);
        this.lastState = lastState;
        this.lastStateFrameTime = lastStateFrameTime;
        this.duration = duration;
        this.pamPath = pamPath;
        this.clip = clip;
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        super.onEnter(zombie, ctx);
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        if (stateTime >= duration) {
            return lastState;
        }
        return this;
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        FrameConfig fc = zombie.drawClip(clip, lastStateFrameTime, false);
        if (fc != null) {
            fc.setColor(1f, 0.9f, 0.5f, 0.85f);
        }
        return fc;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        super.onExit(zombie, ctx);
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
        return "ButterStun";
    }

    public float getRemainingDuration() {
        return Math.max(0, duration - stateTime);
    }
}
