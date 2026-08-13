package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * The zombie is eating a plant it has collided with.
 *
 * <p>Each tick it deals {@link Zombie#getEatDpsPerTick()} damage to the
 * {@link Plant} it bumped into.
 *
 * <p><b>Transitions:</b>
 * <ul>
 *   <li>→ {@link WalkState} when the target plant is destroyed (or removed/frozen).</li>
 * </ul>
 */
public class EatState implements ZombieState {

    private final Plant target;

    /**
     * @param target the plant this zombie is chewing on (tracked by reference, not
     *               grid cell, so it keeps chewing the same plant as it moves).
     */
    public EatState(Plant target) {
        this.target = target;
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        ctx.log(zombie.getSheet().getAlias()
                + " is eating plant at (" + target.getCol() + "," + target.getLane() + ")");
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        if (target.isDead() || target.isFrozen() || !ctx.getPlants().contains(target)) {
            // Plant was destroyed or removed — return to walking
            ctx.log("Plant at (" + target.getCol() + "," + target.getLane() + ") is destroyed.");
            return new WalkState();
        }
        // Deal eat-DPS damage (not poisonous — regular bite)
        target.takeDamage(zombie.getEatDpsPerTick(), DamageKind.FIXED);
        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        // nothing
    }

    @Override
    public String getLabel() {
        return "Eating@(" + target.getCol() + "," + target.getLane() + ")";
    }

    public Plant getTarget()  { return target; }
}
