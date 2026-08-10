package com.pvz.models.entities.projectile;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.games.GameContext;

public class ProjectileFactory {
    public static Projectile create(ProjectileType type , GameContext ctx, Vector2 startPos , Vector2 vel , float damage){
        switch (type) {
            case PEA:
                return new Projectile(ctx, type, startPos.x, startPos.y, vel.x, vel.y, damage, false, false, false, 0, null);

            default:
                return null;
        }
    }
}
