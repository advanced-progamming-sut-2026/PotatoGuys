package com.pvz.models.entities.projectile.effects;

import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Kernel-pult's butter: single-target damage plus a 4.5-second stun.
 * The butter part is shown on the zombie's head via partVisibility in ButterStunState.
 */
public class ButterEffectState extends ProjectileEffectState {

    private static final float STUN_DURATION = 4.5f;

    @Override
    public void onImpact(Projectile projectile, Zombie primaryTarget, GameContext ctx) {
        if (primaryTarget == null) {
            return;
        }
        primaryTarget.takeDamage(projectile.getDamage(), bypassesArmor());
        primaryTarget.setButterStunned(STUN_DURATION);
    }

    @Override
    public String getLabel() {
        return "Butter";
    }
}
