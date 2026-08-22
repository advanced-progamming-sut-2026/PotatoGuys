package com.pvz.models.entities.zombies.fsm;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.HeadDeathEffect;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieActionConfig;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.games.GameContext;

public class DeadState extends ZombieState {

    private float dieDuration;
    private boolean removed;
    private boolean useAsh;
    private String ashPamPath;

    public DeadState(boolean killedByExplosive) {
        super(null);
        this.useAsh = killedByExplosive;
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        stateTime = 0f;
        removed = false;
        ashPamPath = null;

        if (useAsh) {
            ZombieAnimationConfig anim = zombie.getSheet().getAnimationConfig();
            if (anim != null && anim.ashPamFilePath != null && !anim.ashPamFilePath.isEmpty()) {
                ashPamPath = anim.ashPamFilePath;
            }
        }

        dieDuration = resolveDieDuration(zombie);

        ZombieAnimationConfig anim = zombie.getSheet().getAnimationConfig();
        if (anim != null && anim.pamFilePath != null
                && anim.availableClips != null && anim.availableClips.contains("particles")) {
            float sc = (anim.scale != null) ? anim.scale : 0.65f;
            ctx.addEffect(new HeadDeathEffect(ctx,
                    new Vector2(zombie.getX(), zombie.getY()),
                    anim.pamFilePath, sc));
        }
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
        if (ashPamPath != null) {
            return drawAsh(zombie);
        }
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

    private static final String ASH_CLIP_LABEL = "animation";

    private FrameConfig drawAsh(Zombie zombie) {
        ZombieAnimationConfig anim = zombie.getSheet().getAnimationConfig();
        float scale = (anim != null && anim.scale != null) ? anim.scale : 0.65f;
        return new FrameConfig(ashPamPath, ASH_CLIP_LABEL, stateTime,
            new Vector2(zombie.getX(), zombie.getY()), new Vector2(scale, scale),
            null, false);
    }

    private float resolveDieDuration(Zombie zombie) {
        String pamPath;
        String clipName;
        if (ashPamPath != null) {
            pamPath = ashPamPath;
            clipName = ASH_CLIP_LABEL;
        } else {
            ZombieAnimationConfig anim = zombie.getSheet().getAnimationConfig();
            pamPath = (anim != null && anim.pamFilePath != null)
                ? anim.pamFilePath
                : "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM";
            clipName = "die";
        }
        AnimationCatalog catalog = AnimationCatalog.getInstance();
        if (catalog != null) {
            float dur = catalog.getClipDuration(pamPath, clipName);
            if (dur > 0f) return dur;
        }
        return 1.5f;
    }
}
