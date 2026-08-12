package com.pvz.models.entities.projectile.effects;

import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/** Plain projectile: deals its damage to whatever it hit, no status effect, no splash. */
public class NormalEffectState extends ProjectileEffectState {

    @Override
    public void onImpact(Projectile projectile, Zombie primaryTarget, GameContext ctx) {
        if (primaryTarget != null) {
            primaryTarget.takeDamage(projectile.getDamage(), bypassesArmor());
        }
    }

    @Override
    public String getLabel() {
        return "Normal";
    }
}
