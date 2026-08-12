package com.pvz.models.entities.projectile.effects;

import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.effects.EffectType;
import com.pvz.models.entities.zombies.effects.StatusEffect;
import com.pvz.models.games.GameContext;

/**
 * Kernel-pult's butter: single-target damage plus a brief stun — the zombie is
 * temporarily buttered and cannot move or attack (see {@link EffectType#STUN}).
 */
public class ButterEffectState extends ProjectileEffectState {

    /** 20 ticks == 2 in-game seconds at Zombie.TICKS_PER_SECOND (10). */
    private static final int STUN_TICKS = 20;

    @Override
    public void onImpact(Projectile projectile, Zombie primaryTarget, GameContext ctx) {
        if (primaryTarget == null) {
            return;
        }
        primaryTarget.takeDamage(projectile.getDamage(), bypassesArmor());
        primaryTarget.applyEffect(new StatusEffect(EffectType.STUN, STUN_TICKS));
    }

    @Override
    public String getLabel() {
        return "Butter";
    }
}
