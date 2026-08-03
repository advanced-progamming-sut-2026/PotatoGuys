package com.pvz.models.entities.zombies.skills;

import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Ra Zombie — magnetically steals sun from the player's reserve.
 *
 * <p>From JSON {@code ZombieRaProps}: {@code MaxClaimedSunCurrency = 250}.
 * Every second (10 ticks), Ra steals 25 sun — up to the configured maximum.
 * When Ra dies all stolen sun is returned to the player (handled in
 * {@link Zombie#onDeath()} via {@link GameContext#returnSun}).
 */
public class RaStealSunSkill extends CooldownSkill {

    private static final float STEAL_INTERVAL_SECONDS = 1.0f;
    private static final int SUN_PER_STEAL = 25;

    private final int maxStealable;

    /**
     * @param maxClaimedSun the JSON {@code MaxClaimedSunCurrency} value (typically 250)
     */
    public RaStealSunSkill(int maxClaimedSun) {
        super(STEAL_INTERVAL_SECONDS);
        this.maxStealable = maxClaimedSun;
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
        int toSteal = Math.min(SUN_PER_STEAL, Math.min(headroom, sun.getAmount()));
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
