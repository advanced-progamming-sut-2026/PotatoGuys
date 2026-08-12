package com.pvz.models.entities.projectile.effects;

import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.effects.EffectType;
import com.pvz.models.entities.zombies.effects.StatusEffect;
import com.pvz.models.games.GameContext;

/**
 * Simple ice projectile (Snow Pea, etc.): deals damage to the single zombie it hit
 * and chills it (halves its speed via {@code Zombie.getEffectiveSpeedPerTick()}).
 */
public class IceEffectState extends ProjectileEffectState {

    /** 100 ticks == 10 in-game seconds at Zombie.TICKS_PER_SECOND (10). */
    private static final int CHILL_TICKS = 100;

    @Override
    public void onImpact(Projectile projectile, Zombie primaryTarget, GameContext ctx) {
        if (primaryTarget == null) {
            return;
        }
        primaryTarget.takeDamage(projectile.getDamage(), false);
        primaryTarget.applyEffect(new StatusEffect(EffectType.CHILL, CHILL_TICKS));
    }

    @Override
    public String getLabel() {
        return "Ice";
    }
}
