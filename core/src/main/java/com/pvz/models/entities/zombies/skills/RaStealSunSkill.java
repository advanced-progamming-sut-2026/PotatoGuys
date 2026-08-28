package com.pvz.models.entities.zombies.skills;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.RaStealSunSkillConfig;
import com.pvz.models.entities.zombies.config.ZombieAnimationConfig;
import com.pvz.models.entities.zombies.fsm.WalkState;
import com.pvz.models.entities.zombies.fsm.ZombieState;
import com.pvz.models.games.GameContext;

/**
 * Ra Zombie — magnetically steals sun from the player's reserve.
 *
 * <p>From JSON {@code ZombieRaProps}: {@code MaxClaimedSunCurrency = 250}.
 * Every {@code stealIntervalSeconds} seconds Ra steals {@code sunPerSteal} sun
 * — up to the configured maximum. When Ra dies all stolen sun is returned to
 * the player (handled in {@link Zombie#onDeath()}).
 *
 * <p>The steal itself is animated: once the cooldown is ready the zombie jumps
 * into this skill state, its target sun plays the {@code transition_red} clip
 * (one shot) followed by the {@code red} clip for {@code redHoldSeconds}, and
 * only when the whole sequence completes does the sun actually get stolen.
 * While the state is active the zombie first plays its {@code power_up} clip
 * and then keeps looping its {@code power} clip (the configured skill label).
 */
public class RaStealSunSkill extends CooldownSkill {

    private static final float POWER_UP_FALLBACK_SECONDS = 0.6667f;

    private final int sunPerSteal;
    private final int maxStealable;
    private final float redHoldSeconds;
    private Sun targetSun;

    public RaStealSunSkill(RaStealSunSkillConfig config) {
        super(config, config.stealIntervalSeconds);
        this.sunPerSteal = config.sunPerSteal;
        this.maxStealable = config.maxClaimedSun;
        this.redHoldSeconds = config.redHoldSeconds;
    }

    @Override
    protected boolean canUse(Zombie zombie, GameContext ctx) {
        return ctx.getSuns().size() > 0 && zombie.getStolenSun() < maxStealable;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
        Sun sun = getRandomSun(ctx);
        if (sun == null) {
            targetSun = null;
            return;
        }
        targetSun = sun;
        sun.startStealAnimation(redHoldSeconds);
        ctx.log("Ra Zombie started stealing sun at (" + sun.getCol() + "," + sun.getLane() + ")");
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        if (targetSun == null || targetSun.isDone() || !ctx.getSuns().contains(targetSun)) {
            abandonTarget();
            return new WalkState();
        }
        if (targetSun.isStealAnimationComplete()) {
            performSteal(zombie, ctx, targetSun);
            targetSun = null;
            return new WalkState();
        }
        return this;
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        float powerUpDuration = getPowerUpDuration(zombie);
        boolean inPowerLoop = stateTime >= powerUpDuration;
        String clip = inPowerLoop ? "power" : "power_up";
        float time = inPowerLoop ? stateTime - powerUpDuration : stateTime;
        return zombie.drawClip(clip, time, inPowerLoop);
    }

    @Override
    public String getName() {
        return "StealSun";
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void performSteal(Zombie zombie, GameContext ctx, Sun sun) {
        int headroom = maxStealable - zombie.getStolenSun();
        if (headroom <= 0) {
            sun.stopStealAnimation();
            ctx.removeSun(sun);
            return;
        }
        int toSteal = Math.min(sunPerSteal, Math.min(headroom, sun.getAmount()));
        zombie.addStolenSun(toSteal);
        sun.stopStealAnimation();
        ctx.removeSun(sun);
        ctx.log("Ra Zombie stole sun at (" + sun.getCol() + "," + sun.getLane() + ") (total stolen: "
                + zombie.getStolenSun() + "/" + maxStealable + ")");
    }

    private void abandonTarget() {
        if (targetSun != null) {
            targetSun.stopStealAnimation();
        }
        targetSun = null;
    }

    private float getPowerUpDuration(Zombie zombie) {
        ZombieAnimationConfig anim = zombie.getSheet().getAnimationConfig();
        if (anim != null && anim.pamFilePath != null) {
            AnimationCatalog catalog = AnimationCatalog.getInstance();
            if (catalog != null) {
                float dur = catalog.getClipDuration(anim.pamFilePath, "power_up");
                if (dur > 0f) return dur;
            }
        }
        return POWER_UP_FALLBACK_SECONDS;
    }

    private Sun getRandomSun(GameContext ctx) {
        var suns = ctx.getSuns();
        if (suns.isEmpty()) return null;
        int idx = (int) (Math.random() * suns.size());
        return suns.get(idx);
    }
}