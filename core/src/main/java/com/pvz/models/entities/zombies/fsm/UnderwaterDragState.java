package com.pvz.models.entities.zombies.fsm;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Tangle Kelp's grab effect: the zombie is pulled straight down (below the
 * tile) while its sprite fades out over {@link #fadeDuration}, then it is
 * removed from the world entirely. While this state is active the zombie's
 * {@code underwaterGrabbed} flag is set so it neither walks nor eats nor takes
 * further damage.
 */
public class UnderwaterDragState extends ZombieState {

    private final float fadeDuration;
    private final float sinkDepth;
    private boolean removed;

    public UnderwaterDragState(float fadeDuration, float sinkDepth) {
        super(null);
        this.fadeDuration = fadeDuration;
        this.sinkDepth = sinkDepth;
        this.removed = false;
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        super.onEnter(zombie, ctx);
        zombie.setUnderwaterGrabbed(true);
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        if (stateTime >= fadeDuration && !removed) {
            removed = true;
            ctx.getGameStats().onZombieKilled();
            ctx.getGameStats().onZombieKilledInSeason(ctx.getSeasonName());
            ctx.removeZombie(zombie);
            return this;
        }
        return this;
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        float t = Math.min(1f, stateTime / fadeDuration);
        float alpha = 1f - t;
        float y = zombie.getY() - sinkDepth * t;

        FrameConfig fc = zombie.drawClip("walk");
        if (fc != null) {
            fc.position.set(zombie.getPosition().x, y);
            fc.a = alpha;
            fc.r = 1f;
            fc.g = 1f;
            fc.b = 1f;
        }
        return fc;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        super.onExit(zombie, ctx);
        zombie.setUnderwaterGrabbed(false);
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
        return "UnderwaterDrag";
    }
}
