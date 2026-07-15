package pvz.Models.Entities.Zombies.skills;

import javax.imageio.IIOException;

import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;

/**
 * TombRaiser Zombie — throws bones to raise tombs on random grid cells.
 *
 * <p>From JSON {@code ZombieTombRaiserProps}:
 * <ul>
 *   <li>{@code Ammo = 5} — maximum casts before the zombie runs out of bones.</li>
 *   <li>{@code NumberOfTombsToSpawn = 2} — tombs raised per cast.</li>
 *   <li>{@code TimeBetweenRaisings = 6} — seconds between casts.</li>
 * </ul>
 */
public class TombRaiserSkill extends CooldownSkill {

    private final int tombsPerCast;
    private int ammoLeft;

    /**
     * @param castIntervalSeconds seconds between casts (JSON {@code TimeBetweenRaisings})
     * @param tombsPerCast        tombs raised per cast (JSON {@code NumberOfTombsToSpawn})
     * @param ammo                total casts available (JSON {@code Ammo})
     */
    public TombRaiserSkill(float castIntervalSeconds, int tombsPerCast, int ammo) {
        super(castIntervalSeconds);
        this.tombsPerCast = tombsPerCast;
        this.ammoLeft = ammo;
    }

    @Override
    protected boolean canUse(Zombie zombie, GameContext ctx) {
        return ammoLeft > 0;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
        for (int i = 0; i < tombsPerCast; i++) {
            int[] cell = getRandomEmptyCell(ctx);
            if (cell != null) {
                raiseTomb(ctx , cell);
                ctx.log("TombRaiser raised a tomb at (" + cell[0] + "," + cell[1] + ")");
            }
        }
        ammoLeft--;
        ctx.log("TombRaiser has " + ammoLeft + " ammo left.");
    }

    private int[] getRandomEmptyCell(GameContext ctx){
        return null;
    }

    private void raiseTomb(GameContext ctx , int[] cell){
    }

    @Override
    public String getName() {
        return "RaiseTomb";
    }
}
