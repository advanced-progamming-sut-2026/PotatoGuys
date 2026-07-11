package pvz.Models.Entities.Zombies.skills;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieGameContext;

/**
 * Ra Zombie — magnetically steals sun from the player's reserve.
 *
 * <p>From JSON {@code ZombieRaProps}: {@code MaxClaimedSunCurrency = 250}.
 * Every second (10 ticks), Ra steals 25 sun — up to the configured maximum.
 * When Ra dies all stolen sun is returned to the player (handled in
 * {@link Zombie#onDeath()} via {@link ZombieGameContext#returnSun}).
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
    protected boolean canUse(Zombie zombie, ZombieGameContext ctx) {
        return ctx.getSunAmount() > 0 && zombie.getStolenSun() < maxStealable;
    }

    @Override
    protected void doExecute(Zombie zombie, ZombieGameContext ctx) {
        int headroom = maxStealable - zombie.getStolenSun();
        int toSteal = Math.min(SUN_PER_STEAL, Math.min(headroom, ctx.getSunAmount()));
        ctx.stealSun(toSteal);
        zombie.addStolenSun(toSteal);
        ctx.log("Ra Zombie stole " + toSteal + " sun! (total stolen: "
                + zombie.getStolenSun() + "/" + maxStealable + ")");
    }

    @Override
    public String getName() {
        return "StealSun";
    }
}
