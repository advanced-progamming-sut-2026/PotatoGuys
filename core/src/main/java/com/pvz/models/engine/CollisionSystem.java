package com.pvz.models.engine;

import java.util.ArrayList;
import java.util.List;

import com.pvz.models.entities.Hitbox;
import com.pvz.models.games.GameContext;

/**
 * Detects collisions between every registered hitbox in the world.
 *
 * <p>Runs once per engine tick, <b>after</b> every entity has updated its
 * position. It sweeps the global hitbox registry maintained by
 * {@link GameContext#getHitboxes()} — no entity-type loops — and for each
 * overlapping pair calls {@link Hitbox#onCollision(Hitbox)} on <b>both</b>
 * sides. The hitbox itself decides how to react (via its overridden
 * {@code onCollision}); the system never cares what entity owns it.
 *
 * <p>The sweep iterates a snapshot so hitboxes can be added/removed by a
 * collision handler (spawning a projectile, killing a zombie, ...) without
 * throwing {@code ConcurrentModificationException}. New hitboxes are picked up
 * on the next tick.
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
        List<Hitbox> snapshot = new ArrayList<>(ctx.getHitboxes());

        for (int i = 0; i < snapshot.size(); i++) {
            Hitbox a = snapshot.get(i);
            for (int j = i + 1; j < snapshot.size(); j++) {
                Hitbox b = snapshot.get(j);
                if (a.overlaps(b)) {
                    a.onCollision(b);
                    b.onCollision(a);
                }
            }
        }
    }
}
