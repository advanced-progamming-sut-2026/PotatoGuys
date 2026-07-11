package pvz.Models.Entities.Zombies.skills;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieGameContext;

/**
 * Ice-Age Hunter Zombie — throws snowballs that build up frost levels on plants.
 *
 * <p>From JSON {@code ZombieIceAgeHunterProps}:
 * <ul>
 *   <li>{@code SnowballsPerBarrage = 3} — each barrage fires 3 snowballs at one plant.</li>
 *   <li>{@code FarAttackRange = 4} — attacks plants up to 4 cells ahead.</li>
 *   <li>{@code NearAttackRange = 1} — minimum distance before eating rather than throwing.</li>
 * </ul>
 *
 * <p>Three frost levels freeze a plant solid (same model as the WalkState checks for
 * FROZEN plants in Frostbite Caves). Each snowball in a barrage increments the plant's
 * frost counter via {@link ZombieGameContext#applyFrostToPlant}.
 */
public class HunterSnowballSkill extends CooldownSkill {

    private static final float BARRAGE_INTERVAL_SECONDS = 2.0f;

    private final int snowballsPerBarrage;
    private final int farRange;
    private final int nearRange;

    /**
     * @param snowballsPerBarrage JSON {@code SnowballsPerBarrage} (typically 3)
     * @param farAttackRange      JSON {@code FarAttackRange} (typically 4)
     */
    public HunterSnowballSkill(int snowballsPerBarrage, int farAttackRange) {
        super(BARRAGE_INTERVAL_SECONDS);
        this.snowballsPerBarrage = snowballsPerBarrage;
        this.farRange = farAttackRange;
        this.nearRange = 1;
    }

    @Override
    protected boolean canUse(Zombie zombie, ZombieGameContext ctx) {
        return findTargetCol(zombie, ctx) >= 0;
    }

    @Override
    protected void doExecute(Zombie zombie, ZombieGameContext ctx) {
        int targetCol = findTargetCol(zombie, ctx);
        if (targetCol < 0) return;

        for (int shot = 0; shot < snowballsPerBarrage; shot++) {
            ctx.applyFrostToPlant(targetCol, zombie.getLane());
        }
        ctx.log("Hunter Zombie threw " + snowballsPerBarrage
                + " snowball(s) at (" + targetCol + "," + zombie.getLane()
                + ") — " + snowballsPerBarrage + " frost level(s) added!");
    }

    @Override
    public String getName() {
        return "HunterSnowball";
    }

    // ── Private helper ────────────────────────────────────────────────────────

    /**
     * Returns the column of the nearest plant within [farRange] cells, or -1.
     * The hunter does not throw snowballs if the plant is within nearRange
     * (it eats directly instead).
     */
    private int findTargetCol(Zombie zombie, ZombieGameContext ctx) {
        int col = (int) zombie.getX();
        int minCol = Math.max(0, col - farRange);
        int maxCol = col - nearRange; // do not attack at melee range
        for (int c = maxCol; c >= minCol; c--) {
            if (ctx.isPlantAt(c, zombie.getLane())) return c;
        }
        return -1;
    }
}
