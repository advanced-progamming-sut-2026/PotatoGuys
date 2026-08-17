package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieActionConfig;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.games.GameContext;

public class DeadState extends ZombieState {

    private float dieDuration;
    private boolean removed;

    public DeadState() {
        super(null);
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        stateTime = 0f;
        removed = false;
        dieDuration = resolveDieDuration(zombie);
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        if (removed) return this;
        stateTime += dt;
        if (stateTime >= dieDuration) {
            removed = true;
            ctx.removeZombie(zombie);
        }
        return this;
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        return zombie.drawClip("die", stateTime, false);
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
        return "Dead";
    }

    @Override
    public String getLabel() {
        return "Dead";
    }

    private float resolveDieDuration(Zombie zombie) {
        ZombieAnimationConfig anim = zombie.getSheet().getAnimationConfig();
        if (anim != null && anim.pamFilePath != null) {
            AnimationCatalog catalog = AnimationCatalog.getInstance();
            if (catalog != null) {
                float dur = catalog.getClipDuration(anim.pamFilePath, "die");
                if (dur > 0f) return dur;
            }
        }
        return 1.5f;
    }
}
