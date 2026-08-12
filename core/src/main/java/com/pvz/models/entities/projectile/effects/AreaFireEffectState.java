package com.pvz.models.entities.projectile.effects;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.effects.EffectType;
import com.pvz.models.entities.zombies.effects.StatusEffect;
import com.pvz.models.games.GameContext;

/**
 * Pepper-pult's ball: area-of-effect fire. Damages and sets ablaze every zombie in
 * the 3x3 tile block centered on the impact point, resolved the same way
 * {@code Sun.dealExplosionDamage} and {@code Plant.applyFireAuras} look up
 * neighboring tiles — via {@code GameContext#getZombiesAt(col, lane)}.
 */
public class AreaFireEffectState extends ProjectileEffectState {

    /** 20 ticks == 2 in-game seconds at Zombie.TICKS_PER_SECOND (10). */
    private static final int BURN_TICKS = 20;

    @Override
    public void onImpact(Projectile projectile, Zombie primaryTarget, GameContext ctx) {
        int centerCol = GameController.worldXtoCol(projectile.getX());
        int centerLane = GameController.worldYtoLane(projectile.getY());

        for (int dCol = -1; dCol <= 1; dCol++) {
            for (int dLane = -1; dLane <= 1; dLane++) {
                int col = centerCol + dCol;
                int lane = centerLane + dLane;
                if (col < 0 || lane < 0 || col >= ctx.getMap().getColumns() || lane >= ctx.getMap().getLanes()) {
                    continue;
                }
                for (Zombie zombie : ctx.getZombiesAt(col, lane)) {
                    zombie.takeDamage(projectile.getDamage(), false);
                    zombie.applyEffect(new StatusEffect(EffectType.BURNING, BURN_TICKS));
                }
            }
        }
    }

    @Override
    public String getLabel() {
        return "AreaFire";
    }
}
