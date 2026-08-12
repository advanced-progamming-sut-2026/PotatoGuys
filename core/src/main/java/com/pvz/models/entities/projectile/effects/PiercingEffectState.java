package com.pvz.models.entities.projectile.effects;

import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Smoke Cloud (Fume-shroom) / Cactus Spike: damages every zombie it touches but is
 * never consumed by the hit, so it keeps flying through the lane. {@code Projectile}
 * already tracks {@code hitZombies} so the same zombie isn't damaged twice.
 */
public class PiercingEffectState extends ProjectileEffectState {

    @Override
    public void onImpact(Projectile projectile, Zombie primaryTarget, GameContext ctx) {
        if (primaryTarget != null) {
            primaryTarget.takeDamage(projectile.getDamage(), false);
        }
    }

    @Override
    public boolean piercesThrough() {
        return true;
    }

    @Override
    public String getLabel() {
        return "Piercing";
    }
}
