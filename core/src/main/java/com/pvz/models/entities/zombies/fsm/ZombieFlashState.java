package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Temporary damage-flash wrapper around any {@link ZombieState}.
 * Delegates all logic to the wrapped state while applying a white
 * semi-transparent tint to the draw output for {@code FLASH_DURATION} seconds.
 */
public class ZombieFlashState extends ZombieState {

    private static final float FLASH_DURATION = 0.28f;
    private static final float FLASH_R = 5f;
    private static final float FLASH_G = 5f;
    private static final float FLASH_B = 5f;
    private static final float FLASH_A = 0.6f;

    private ZombieState underlying;

    public ZombieFlashState(ZombieState underlying) {
        super(null);
        this.underlying = underlying;
        this.stateTime = 0f;
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        stateTime = 0f;
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        if (stateTime >= FLASH_DURATION) {
            return underlying;
        }
        ZombieState prev = underlying;
        ZombieState next = underlying.update(zombie, ctx, dt);
        if (next != prev) {
            underlying = next;
        }
        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        // underlying state is still alive — do NOT call its onExit
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        FrameConfig fc = underlying.draw(zombie, ctx);
        if (fc != null) {
            fc.setColor(FLASH_R, FLASH_G, FLASH_B, FLASH_A);
        }
        return fc;
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
        return "Flash";
    }
}
