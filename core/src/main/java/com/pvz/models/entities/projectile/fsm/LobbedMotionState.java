package com.pvz.models.entities.projectile.fsm;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.entities.projectile.Projectile;

/**
 * Parabolic ("aerial") flight from the launching plant's position onto a fixed
 * target point (typically the targeted zombie's position at launch time), used by
 * lobbers like Melon-pult, Winter Melon-pult, and Pepper-pult.
 *
 * <p>The target point is captured once at launch rather than tracked live — matches
 * how real PvZ lobbers behave (they don't home in on a moving zombie mid-arc). The
 * horizontal/vertical position is linearly interpolated over {@link #flightSeconds},
 * with a sine-shaped vertical offset layered on top to create the arc. When the
 * flight completes, {@link Projectile#land()} is invoked so the projectile's
 * {@code ProjectileEffectState} can resolve its impact (area damage, etc.) exactly
 * at the landing spot instead of at the first zombie touched along the way.
 */
public class LobbedMotionState extends ProjectileMotionState {

    private final Vector2 startPos = new Vector2();
    private final Vector2 targetPos;
    private final float flightSeconds;
    private final float arcHeight;
    private boolean landed = false;

    /**
     * @param targetPos      world-space landing point (usually the target zombie's position at launch)
     * @param flightSeconds  total time-of-flight, in seconds
     * @param arcHeight      how high (in world units) the arc rises above the straight line
     *                       between start and target; tune per plant for a flatter/steeper lob
     */
    public LobbedMotionState(Vector2 targetPos, float flightSeconds, float arcHeight) {
        this.targetPos = new Vector2(targetPos);
        this.flightSeconds = Math.max(0.1f, flightSeconds);
        this.arcHeight = arcHeight;
    }

    @Override
    public void onEnter(Projectile projectile) {
        super.onEnter(projectile);
        startPos.set(projectile.getPos());
        landed = false;
    }

    @Override
    public void update(Projectile projectile, float dt) {
        if (landed) {
            return;
        }
        stateTime += dt;
        float t = Math.min(1f, stateTime / flightSeconds);

        float x = startPos.x + (targetPos.x - startPos.x) * t;
        float baseY = startPos.y + (targetPos.y - startPos.y) * t;
        // Sine hump: 0 at takeoff, peaks at the midpoint, back to 0 at landing.
        float arcOffset = arcHeight * (float) Math.sin(t * Math.PI);
        projectile.getPos().set(x, baseY + arcOffset);

        if (t >= 1f) {
            landed = true;
            projectile.land();
        }
    }

    @Override
    public String getLabel() {
        return "Lobbed";
    }
}
