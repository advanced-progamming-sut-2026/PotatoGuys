package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * The zombie is eating a plant at a fixed grid cell.
 *
 * <p>Each tick it deals {@link Zombie#getEatDpsPerTick()} damage to the
 * plant at ({@link #targetCol}, {@link #targetLane}).
 *
 * <p><b>Transitions:</b>
 * <ul>
 *   <li>→ {@link WalkState} when the target cell becomes empty (plant destroyed or removed).</li>
 * </ul>
 */
public class EatState implements ZombieState {

    private final int targetCol;
    private final int targetLane;

    /**
     * @param col  column of the plant being eaten (integer grid index)
     * @param lane row of the plant being eaten
     */
    public EatState(int col, int lane) {
        this.targetCol = col;
        this.targetLane = lane;
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        ctx.log(zombie.getSheet().getAlias()
                + " is eating plant at (" + targetCol + "," + targetLane + ")");
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        if (!ctx.isPlantAt(targetCol, targetLane)) {
            // Plant was destroyed — return to walking
            ctx.log("Plant at (" + targetCol + "," + targetLane + ") is destroyed.");
            return new WalkState();
        }
        // Deal eat-DPS damage (not poisonous — regular bite)
        ctx.getPlantsAt(targetCol, targetLane).get(0).takeDamage(zombie.getEatDpsPerTick(), DamageKind.FIXED);
        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Eating@(" + targetCol + "," + targetLane + ")";
    }

    public int getTargetCol()  { return targetCol; }
    public int getTargetLane() { return targetLane; }
}
