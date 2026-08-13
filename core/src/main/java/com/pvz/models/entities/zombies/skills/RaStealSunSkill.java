package com.pvz.models.entities.zombies.skills;

import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.RaStealSunSkillConfig;
import com.pvz.models.games.GameContext;

/**
 * Ra Zombie — magnetically steals sun from the player's reserve.
 *
 * <p>From JSON {@code ZombieRaProps}: {@code MaxClaimedSunCurrency = 250}.
 * Every {@code stealIntervalSeconds} seconds Ra steals {@code sunPerSteal} sun
 * — up to the configured maximum. When Ra dies all stolen sun is returned to
 * the player (handled in {@link Zombie#onDeath()}).
 */
public class RaStealSunSkill extends CooldownSkill {

    private final int sunPerSteal;
    private final int maxStealable;

    public RaStealSunSkill(RaStealSunSkillConfig config) {
        super(config, config.stealIntervalSeconds);
        this.sunPerSteal = config.sunPerSteal;
        this.maxStealable = config.maxClaimedSun;
    }

    @Override
    protected boolean canUse(Zombie zombie, GameContext ctx) {
        return ctx.getSuns().size() > 0 && zombie.getStolenSun() < maxStealable;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
        int headroom = maxStealable - zombie.getStolenSun();
        Sun sun = getRandomSun(ctx);
        if (sun == null) return;
        int toSteal = Math.min(sunPerSteal, Math.min(headroom, sun.getAmount()));
        zombie.addStolenSun(toSteal);
        ctx.removeSun(sun);
        ctx.log("Ra Zombie stole sun at (" + sun.getCol() + "," + sun.getLane() + ") (total stolen: "
                + zombie.getStolenSun() + "/" + maxStealable + ")");
    }

    @Override
    public String getName() {
        return "StealSun";
    }

    private Sun getRandomSun(GameContext ctx) {
        var suns = ctx.getSuns();
        if (suns.isEmpty()) return null;
        int idx = (int) (Math.random() * suns.size());
        return suns.get(idx);
    }
}
