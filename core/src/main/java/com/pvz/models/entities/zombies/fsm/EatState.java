package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PumpkinShield;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieActionConfig;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.games.GameContext;

public class EatState extends ZombieState {

    private Plant target;
    private boolean isGargantuar;
    private boolean smashPhase;

    public EatState(Plant target) {
        super(null);
        this.target = target;
    }

    public EatState(Plant target, boolean isGargantuar) {
        super(null);
        this.target = target;
        this.isGargantuar = isGargantuar;
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
        return "";
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        ctx.log(zombie.getSheet().getAlias()
                + " is eating plant at (" + target.getCol() + "," + target.getLane() + ")");
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;

        if (isGargantuar) {
            float eatDur = getClipDuration(zombie, "eat");
            float smashDur = getClipDuration(zombie, "smash_left");

            if (!smashPhase) {
                if (stateTime >= eatDur) {
                    smashPhase = true;
                    stateTime = 0f;
                }
            } else {
                if (stateTime >= smashDur) {
                    if (!target.isDead() && ctx.getPlants().contains(target)) {
                        Plant shield = PumpkinShield.shieldFor(target, ctx);
                        if (shield != null) target = shield;
                        float totalDmg = zombie.getEatDpsPerTick() * Zombie.TICKS_PER_SECOND * (eatDur + smashDur);
                        target.takeDamage(totalDmg, DamageKind.FIXED);
                    }
                    return new WalkState();
                }
            }
            return this;
        }

        if (target.isDead() || target.isFrozen() || !ctx.getPlants().contains(target)) {
            ctx.log("Plant at (" + target.getCol() + "," + target.getLane() + ") is destroyed.");
            return new WalkState();
        }
        Plant shield = PumpkinShield.shieldFor(target, ctx);
        if (shield != null) {
            target = shield;
        }
        float dps = zombie.getEatDpsPerTick() * Zombie.TICKS_PER_SECOND;
        target.takeDamage(dps * dt, DamageKind.FIXED);
        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
    }

    @Override
    public String getLabel() {
        return "Eating@(" + target.getCol() + "," + target.getLane() + ")";
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        if (isGargantuar) {
            String clip = smashPhase ? "smash_left" : "eat";
            return zombie.drawClip(clip, stateTime, false);
        }
        ZombieActionConfig eat = zombie.getSheet().eatConfig;
        return zombie.drawClip(eat != null ? eat.label : "eat");
    }

    private float getClipDuration(Zombie zombie, String clipName) {
        ZombieAnimationConfig anim = zombie.getSheet().getAnimationConfig();
        if (anim != null && anim.pamFilePath != null) {
            AnimationCatalog catalog = AnimationCatalog.getInstance();
            if (catalog != null) {
                float dur = catalog.getClipDuration(anim.pamFilePath, clipName);
                if (dur > 0f) return dur;
            }
        }
        return 1f;
    }

    public Plant getTarget() { return target; }
}
