package com.pvz.models.entities.projectile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.Entity;
import com.pvz.models.entities.Hitbox;
import com.pvz.models.entities.projectile.effects.NormalEffectState;
import com.pvz.models.entities.projectile.effects.ProjectileEffectState;
import com.pvz.models.entities.projectile.fsm.ProjectileMotionState;
import com.pvz.models.entities.projectile.fsm.StraightMotionState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.effects.SplatEffect;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

/**
 * A single in-flight projectile. Movement and on-impact behavior are no longer
 * hard-coded booleans (poison/ice/fire/bouncing) — they're delegated to a pair
 * of
 * composable states, mirroring how {@code Plant} delegates to
 * {@code PlantState}:
 *
 * <ul>
 * <li>{@link ProjectileMotionState} — HOW it moves each tick (straight line vs.
 * the parabolic arc used by lobbers).</li>
 * <li>{@link ProjectileEffectState} — WHAT happens the moment it connects
 * (plain damage, fire, area fire, chill, area+chill, pass-through, ...).</li>
 * </ul>
 *
 * <p>
 * {@link ProjectileFactory} is the single place that decides which pair of
 * states a given {@link ProjectileType} gets, so adding a new projectile flavor
 * never requires touching this class.
 */
public class Projectile extends Entity {

    private final GameContext ctx;
    private final ProjectileType type;

    private float stateTime = 0f;

    private int lastCol;
    private int lastLane;

    private final float damage;
    private boolean isDead = false;

    private final Set<Zombie> hitZombies = new HashSet<>();

    private boolean lobbed = false;
    private int launchLane = -1;

    private ProjectileMotionState motionState;
    private ProjectileEffectState effectState;

    public Projectile(GameContext ctx, ProjectileType type, float startX, float startY,
            float velX, float velY, float damage) {
        this.ctx = ctx;
        this.type = type;
        this.position.set(startX, startY);
        this.velocity.set(velX, velY);
        this.damage = damage;

        setHitbox(new Hitbox(this, position.x, position.y, 28f, 28f) {
            @Override
            public void onCollision(Hitbox onHit) {
                if (isDead) {
                    return;
                }
                if (onHit.getOwner() instanceof Zombie zombie && !zombie.isDead()) {
                    if (lobbed && GameController.worldYtoLane(zombie.getY()) != launchLane) {
                        return;
                    }
                    onHitZombie(zombie);
                }
            }
        });

        // Sensible defaults; ProjectileFactory swaps these for the real states.
        this.motionState = new StraightMotionState();
        this.effectState = new NormalEffectState();
    }

    // ── State wiring ──────────────────────────────────────────────────────────

    public void setMotionState(ProjectileMotionState motionState) {
        this.motionState = motionState;
        this.motionState.onEnter(this);
    }

    public void setEffectState(ProjectileEffectState effectState) {
        this.effectState = effectState;
    }

    public void setLobbed(int launchLane) {
        this.lobbed = true;
        this.launchLane = launchLane;
    }

    public ProjectileMotionState getMotionState() {
        return motionState;
    }

    public ProjectileEffectState getEffectState() {
        return effectState;
    }

    // ── TickAware ─────────────────────────────────────────────────────────────

    @Override
    public void enter() {
        stateTime = 0f;
        lastCol = GameController.worldXtoCol(position.x);
        lastLane = GameController.worldYtoLane(position.y);
        motionState.onEnter(this);
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (isDead) {
            return;
        }

        motionState.update(this, dt);
        syncHitbox();
        // A lobbed projectile may have just landed and called land() -> destroy()
        // from inside motionState.update(); bail out before touching tiles/zombies.
        if (isDead) {
            return;
        }

        int col = GameController.worldXtoCol(position.x);
        int lane = GameController.worldYtoLane(position.y);

        int cols = ctx.getMap().getColumns();
        int lanes = ctx.getMap().getLanes();

        boolean inLane = lane >= 0 && lane < lanes;
        boolean inColRange = col >= -2 && col < cols + 3;

        if (!inColRange || lane < -1 || lane > lanes) {
            destroy();
            return;
        }

        if (col != lastCol || lane != lastLane) {
            lastCol = col;
            lastLane = lane;

            if (inLane && col >= 0 && col < cols && (!lobbed || lane == launchLane)) {
                try {
                    Tile tile = ctx.getTileAt(lastCol, lastLane);
                    tile.processHit(this);
                } catch (Exception ex) {
                    destroy();
                    return;
                }

                if (isDead) {
                    return;
                }
            }
        }
    }

    @Override
    public List<FrameConfig> draw() {
        List<FrameConfig> frameConfigs = new ArrayList<>();
        frameConfigs.add(
                new FrameConfig(type.path, type.lable, stateTime, position, new Vector2(type.scale, type.scale), null, true));
        return frameConfigs;
    }

    @Override
    public void dispose() {
    }

    // ── Collision ─────────────────────────────────────────────────────────────

    private void onHitZombie(Zombie zombie) {
        hitZombies.add(zombie);

        effectState.onImpact(this, zombie, ctx);
        ctx.log("[Projectile] " + type + " (" + effectState.getLabel() + ") hit zombie at ("
                + zombie.getX() + ", " + zombie.getY() + ")");

        if (!effectState.piercesThrough()) {
            destroy();
        }
    }

    /**
     * Called by a {@link ProjectileMotionState} (currently only the lobbed/aerial
     * one) once its flight completes. Resolves the impact at the current position
     * without requiring a live collision — area-effect states (Pepper-pult,
     * Melon-pult family) look up their own targets from {@link GameContext}, while
     * single-target effect states fall back to the nearest zombie at the landing
     * tile.
     */
    public void land() {
        if (isDead) {
            return;
        }
        Zombie nearest = nearestZombieAt(position.x, position.y);
        effectState.onImpact(this, nearest, ctx);
        ctx.log("[Projectile] " + type + " (" + effectState.getLabel() + ") landed at ("
                + position.x + ", " + position.y + ")");
        destroy();
    }

    private Zombie nearestZombieAt(float x, float y) {
        Zombie nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (Zombie z : ctx.getZombies()) {
            if (z.isDead()) {
                continue;
            }
            double distance = Math.hypot(z.getX() - x, z.getY() - y);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = z;
            }
        }
        return nearest;
    }

    public void destroy() {
        if (isDead) {
            return;
        }
        this.isDead = true;
        if (type.hasSplat()) {
            ctx.addEffect(new SplatEffect(ctx, position, type.splatPamPath, type.splatClip, type.splatScale));
        }
        ctx.removeProjectile(this);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean isDead() {
        return isDead;
    }

    public Vector2 getPos() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getDamage() {
        return damage;
    }

    public ProjectileType getType() {
        return type;
    }
}
