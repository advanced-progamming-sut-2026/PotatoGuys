package com.pvz.models.entities.projectile;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.entities.projectile.effects.AreaFireEffectState;
import com.pvz.models.entities.projectile.effects.ButterEffectState;
import com.pvz.models.entities.projectile.effects.FireEffectState;
import com.pvz.models.entities.projectile.effects.IceEffectState;
import com.pvz.models.entities.projectile.effects.MelonEffectState;
import com.pvz.models.entities.projectile.effects.NormalEffectState;
import com.pvz.models.entities.projectile.effects.PiercingEffectState;
import com.pvz.models.entities.projectile.fsm.LobbedMotionState;
import com.pvz.models.entities.projectile.fsm.StraightMotionState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Builds a fully-wired {@link Projectile} for a given {@link ProjectileType}: this is
 * the single place that decides which {@code ProjectileMotionState} (how it moves)
 * and {@code ProjectileEffectState} (what it does on impact) a projectile gets.
 *
 * <p>Per Phase 1's grouping:
 * <ul>
 *   <li>Plain projectiles (PEA, STAR, ROTOBAGA_PROJECTILE, ...) → straight motion, no effect.</li>
 *   <li>Simple fire (FIRE_PEA) → straight motion, single-target burn.</li>
 *   <li>Pepper-pult (PEPPER_BALL) → lobbed motion, area burn.</li>
 *   <li>Simple ice (SNOW_PEA) → straight motion, single-target chill.</li>
 *   <li>Melon-pult / Winter Melon-pult (MELON, WINTER_MELON) → lobbed motion, area damage (+ chill for winter).</li>
 *   <li>Smoke Cloud / Cactus Spike (FUME, SPIKE) → straight motion, passes through targets.</li>
 * </ul>
 */
public class ProjectileFactory {

    /** Default flight time for lobbed projectiles when no better value is supplied. */
    private static final float DEFAULT_LOB_SECONDS = 0.8f;
    /** Default arc height (world units) for lobbed projectiles. */
    private static final float DEFAULT_ARC_HEIGHT = 90f;

    /** Convenience overload for straight-motion plants that don't have a specific target zombie. */
    public static Projectile create(ProjectileType type, GameContext ctx, Vector2 startPos,
                                     Vector2 vel, float damage) {
        return create(type, ctx, startPos, vel, damage, null);
    }

    /**
     * @param target the zombie this projectile is aimed at; required for lobbed types
     *               (Pepper-pult, Melon-pult family) so the arc has somewhere to land.
     *               Ignored by straight-motion types.
     */
    public static Projectile create(ProjectileType type, GameContext ctx, Vector2 startPos,
                                     Vector2 vel, float damage, Zombie target) {
        Projectile projectile = new Projectile(ctx, type, startPos.x, startPos.y, vel.x, vel.y, damage);
        int launchLane = GameController.worldYtoLane(startPos.y);

        switch (type) {
            case FIRE_PEA -> {
                projectile.setMotionState(new StraightMotionState());
                projectile.setEffectState(new FireEffectState());
            }
            case SNOW_PEA -> {
                projectile.setMotionState(new StraightMotionState());
                projectile.setEffectState(new IceEffectState());
            }
            case PEPPER_BALL -> {
                projectile.setMotionState(lobbedTowards(target));
                projectile.setEffectState(new AreaFireEffectState());
                projectile.setLobbed(launchLane);
            }
            case MELON -> {
                projectile.setMotionState(lobbedTowards(target));
                projectile.setEffectState(new MelonEffectState(false));
                projectile.setLobbed(launchLane);
            }
            case WINTER_MELON -> {
                projectile.setMotionState(lobbedTowards(target));
                projectile.setEffectState(new MelonEffectState(true));
                projectile.setLobbed(launchLane);
            }
            case CABBAGE, KERNEL -> {
                projectile.setMotionState(lobbedTowards(target));
                projectile.setEffectState(new NormalEffectState());
                projectile.setLobbed(launchLane);
            }
            case BUTTER -> {
                projectile.setMotionState(lobbedTowards(target));
                projectile.setEffectState(new ButterEffectState());
                projectile.setLobbed(launchLane);
            }
            case FUME, SPIKE -> {
                projectile.setMotionState(new StraightMotionState());
                projectile.setEffectState(new PiercingEffectState());
            }
            default -> {
                projectile.setMotionState(new StraightMotionState());
                projectile.setEffectState(new NormalEffectState());
            }
        }

        return projectile;
    }

    /** Builds the lobbed arc toward {@code target}'s current position, or falls back to a
     *  straight line if no target was supplied (e.g. called before a target is resolved). */
    private static com.pvz.models.entities.projectile.fsm.ProjectileMotionState lobbedTowards(Zombie target) {
        if (target == null) {
            return new StraightMotionState();
        }
        return new LobbedMotionState(new Vector2(target.getX(), target.getY()),
                DEFAULT_LOB_SECONDS, DEFAULT_ARC_HEIGHT);
    }
}
