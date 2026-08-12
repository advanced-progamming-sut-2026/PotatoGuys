package com.pvz.models.entities.projectile.effects;

import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.effects.EffectType;
import com.pvz.models.entities.zombies.effects.StatusEffect;
import com.pvz.models.games.GameContext;

/**
 * Simple fire projectile (Fire Peashooter, etc.): deals damage to the single
 * zombie it hit and sets it BURNING. No splash — for area fire see
 * {@link AreaFireEffectState} (Pepper-pult).
 */
public class FireEffectState extends ProjectileEffectState {

    /** 20 ticks == 2 in-game seconds at Zombie.TICKS_PER_SECOND (10). */
    private static final int BURN_TICKS = 20;

    @Override
    public void onImpact(Projectile projectile, Zombie primaryTarget, GameContext ctx) {
        if (primaryTarget == null) {
            return;
        }
        primaryTarget.takeDamage(projectile.getDamage(), false);
        primaryTarget.applyEffect(new StatusEffect(EffectType.BURNING, BURN_TICKS));
    }

    @Override
    public String getLabel() {
        return "Fire";
    }
}
