
package com.pvz.models.entities.zombies.fsm;

import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.SandstormCloudEffect;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Initial FSM state for zombies delivered by a sandstorm (see
 * {@code Wave#spawnZombie}, Ancient Egypt final-wave bursts).
 *
 * <p>The zombie is still spawned off-map exactly like any other zombie (so it
 * never just materializes over the lawn), but instead of immediately walking
 * it is wrapped in a {@link SandstormCloudEffect} and carried smoothly to its
 * landing column:
 * <ol>
 *   <li>The cloud plays its intro clip while the zombie sits still at its
 *       spawn point — nothing is on screen yet but the storm forming.</li>
 *   <li>Once the intro finishes ({@link SandstormCloudEffect#isReadyToMove()}),
 *       this state starts sliding the zombie toward {@link #targetX}, hidden
 *       inside the (looping) cloud the whole way.</li>
 *   <li>On arrival, the cloud is told to dissipate
 *       ({@link SandstormCloudEffect#beginOutro()}) and the zombie hands off
 *       to {@link WalkState} immediately — it becomes visible and starts
 *       walking normally while the storm fades out behind it.</li>
 * </ol>
 *
 * <p>Movement here intentionally does not reuse {@code Zombie}'s normal walk
 * speed — it's a distinct "carried by the storm" speed, tunable below.
 */
public class SandstormCarryState extends ZombieState {

    // TODO: tune to taste — this is a placeholder carry speed (world units/sec),
    // independent of the zombie's own walk speed.
    private static final float CARRY_SPEED_PER_SECOND = 150f;
    private static final float ARRIVAL_EPSILON = 1f;

    private final float targetX;
    private SandstormCloudEffect cloud;

    /** @param targetX world-space X of the column this zombie should land on. */
    public SandstormCarryState(float targetX) {
        super(null);
        this.targetX = targetX;
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        stateTime = 0f;
        cloud = new SandstormCloudEffect(ctx, zombie);
        ctx.addEffect(cloud);
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;

        if (cloud.isReadyToMove()) {
            advanceTowardTarget(zombie, dt);
            if (hasArrived(zombie)) {
                zombie.setX(targetX);
                cloud.beginOutro();
                ctx.log("[Sandstorm] " + zombie.getSheet().getAlias() + " dropped at x="
                        + String.format("%.1f", targetX));
                return new WalkState();
            }
        }
        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        // The cloud is intentionally left alive — it keeps playing its own
        // outro/dispose cycle independently of the zombie's FSM from here on.
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        // The zombie itself stays invisible while inside the storm; the
        // SandstormCloudEffect (rendered separately via GameContext#getEffects)
        // is the only thing drawn during this state.
        return null;
    }

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        return false;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
        // no-op — all logic lives in onEnter()/update()
    }

    @Override
    public String getName() {
        return "SandstormCarry";
    }

    @Override
    public String getLabel() {
        return "Carried by Sandstorm";
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void advanceTowardTarget(Zombie zombie, float dt) {
        float step = CARRY_SPEED_PER_SECOND * dt;
        float newX = Math.max(zombie.getX() - step, targetX);
        zombie.setX(newX);
    }

    private boolean hasArrived(Zombie zombie) {
        return zombie.getX() <= targetX + ARRIVAL_EPSILON;
    }
}
