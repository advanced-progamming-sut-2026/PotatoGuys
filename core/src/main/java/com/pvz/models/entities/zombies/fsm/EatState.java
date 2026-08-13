package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.config.ZombieActionConfig;
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
public class EatState extends ZombieState {

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
        // Deal eat-DPS damage scaled by real dt: the engine runs at render-frame
        // rate (~60 fps), not at TICKS_PER_SECOND, so a flat per-tick amount would
        // make chewing ~6× too fast. dps == sheet eatDps (damage per second).
        float dps = zombie.getEatDpsPerTick() * Zombie.TICKS_PER_SECOND;
        target.takeDamage(dps * dt, DamageKind.FIXED);
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

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        ZombieActionConfig eat = zombie.getSheet().eatConfig;
        return zombie.drawClip(eat != null ? eat.label : "eat");
    }

    public Plant getTarget()  { return target; }
}
