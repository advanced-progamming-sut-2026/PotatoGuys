package com.pvz.models.engine;

import com.pvz.models.entities.LawnMower;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Detects collisions between all entities in the world.
 *
 * <p>Runs once per engine tick, <b>after</b> every entity has updated its
 * position. It scans each pair of overlapping entities and calls
 * {@code onCollision} on <b>both</b> sides, letting each entity react based on
 * what the other one is (a projectile hits a zombie, a zombie starts eating a
 * plant, ...).
 *
 * <p>Collisions are cheap broad-phase checks over the per-type lists held by
 * {@link GameContext}; entities remove themselves from those lists on death, so
 * each pair is inspected only while both members are still alive.
 */
public class CollisionSystem {

    private static CollisionSystem instance;

    public static CollisionSystem getInstance() {
        if (instance == null) {
            instance = new CollisionSystem();
        }
        return instance;
    }

    public void detect(GameContext ctx) {
        detectProjectileZombie(ctx);
        detectZombiePlant(ctx);
        detectLawnMowerZombie(ctx);
    }

    private void detectProjectileZombie(GameContext ctx) {
        for (Projectile p : ctx.getProjectiles()) {
            if (p.isDead()) {
                continue;
            }
            for (Zombie z : ctx.getZombies()) {
                if (z.isDead()) {
                    continue;
                }
                if (p.overlaps(z)) {
                    p.onCollision(z);
                    z.onCollision(p);
                    if (p.isDead()) {
                        break;
                    }
                }
            }
        }
    }

    private void detectZombiePlant(GameContext ctx) {
        for (Zombie z : ctx.getZombies()) {
            if (z.isDead()) {
                continue;
            }
            for (Plant plant : ctx.getPlants()) {
                if (plant.isDead()) {
                    continue;
                }
                if (z.overlaps(plant)) {
                    z.onCollision(plant);
                    plant.onCollision(z);
                    if (z.isDead()) {
                        break;
                    }
                }
            }
        }
    }

    private void detectLawnMowerZombie(GameContext ctx) {
        for (LawnMower m : ctx.getLawnMowers()) {
            if (m == null) {
                continue;
            }
            for (Zombie z : ctx.getZombies()) {
                if (z.isDead()) {
                    continue;
                }
                if (m.overlaps(z)) {
                    m.onCollision(z);
                    z.onCollision(m);
                    if (z.isDead()) {
                        break;
                    }
                }
            }
        }
    }
}
