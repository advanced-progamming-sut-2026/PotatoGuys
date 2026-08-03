package com.pvz.models.entities.zombies.skills;

import com.pvz.models.Constants;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.behaviors.DestructibleBehavior;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private final Random random = new Random();


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
        List<int[]> emptyCells = new ArrayList<>();

        for (int lane = 0; lane < ctx.getLanes(); lane++) {
            for (int col = 0; col < ctx.getColumns(); col++) {
                if (isCellEmpty(ctx, col, lane)) {
                    emptyCells.add(new int[]{col, lane});
                }
            }
        }

        if (emptyCells.isEmpty()) {
            return null;
        }

        return emptyCells.get(random.nextInt(emptyCells.size()));
    }

    private boolean isCellEmpty(GameContext ctx, int col, int lane) {
        // Check for plants
        if (ctx.isPlantAt(col, lane)) {
            return false;
        }

        // Check for graves (tile tags)
        if (ctx.getTileAt(col, lane).getTags().contains(TileTags.GRAVE)) {
            return false;
        }

        return true;
    }

    private void raiseTomb(GameContext ctx , int[] cell){
        Tile tile=ctx.getTileAt(cell[0],cell[1]);
        tile.getTags().add(TileTags.GRAVE);
        tile.addBehavior(new DestructibleBehavior(Constants.DEFAULT_GRAVE_HP,"Grave"));
    }

    @Override
    public String getName() {
        return "RaiseTomb";
    }
}
