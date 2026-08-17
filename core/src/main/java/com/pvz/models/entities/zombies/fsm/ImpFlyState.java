package com.pvz.models.entities.zombies.fsm;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;

/**
 * Imp flying state — the Imp is thrown by a Gargantuar and travels
 * along a parabolic arc from behind the Gargantuar's back to a
 * target column ahead (to the left).
 *
 * <p>During flight the Imp is rendered at the parabolic position
 * while its actual world position is set at the landing spot.
 * Once the flight duration elapses, the Imp lands and transitions
 * to {@link WalkState}.
 */
public class ImpFlyState extends ZombieState {

    private static final float FLIGHT_DURATION = 1.0f;
    private static final float PEAK_HEIGHT = 200f;
    private static final int COLUMNS_AHEAD = 2;

    private final float launchX;
    private final float launchY;
    private final float landX;
    private float flightTime;

    /**
     * @param launchX  world X of the launch point (behind Gargantuar's back)
     * @param launchY  world Y of the launch point (above Gargantuar)
     * @param landX    Gargantuar's world X (used to compute target column)
     * @param landY    Gargantuar's world Y (same lane, for landing)
     * @param lane     the lane the Imp is in
     * @param ctx      game context (for map columns)
     */
    public ImpFlyState(float launchX, float launchY, float landX, float landY, int lane, GameContext ctx) {
        super(null);
        this.launchX = launchX;
        this.launchY = launchY;

        int gargCol = GameController.worldXtoCol(landX);
        int targetCol = Math.max(1, gargCol - COLUMNS_AHEAD);
        this.landX = GameController.colToWorldX(targetCol);
    }

    @Override
    public void onEnter(Zombie zombie, GameContext ctx) {
        stateTime = 0f;
        flightTime = 0f;
    }

    @Override
    public ZombieState update(Zombie zombie, GameContext ctx, float dt) {
        stateTime += dt;
        flightTime += dt;

        if (flightTime >= FLIGHT_DURATION) {
            zombie.setX(landX);
            return new WalkState();
        }
        return this;
    }

    @Override
    public void onExit(Zombie zombie, GameContext ctx) {
        zombie.setX(landX);
    }

    @Override
    public FrameConfig draw(Zombie zombie, GameContext ctx) {
        float t = flightTime / FLIGHT_DURATION;
        float renderX = launchX + (landX - launchX) * t;
        float renderY = launchY + PEAK_HEIGHT * (1f - (2f * t - 1f) * (2f * t - 1f));

        FrameConfig base = zombie.drawClip("fly");
        return new FrameConfig(
            base.pamPath,
            base.label,
            stateTime,
            new Vector2(renderX, renderY),
            base.scale,
            base.partsVisibility,
            false
        );
    }

    @Override
    public boolean shouldTrigger(Zombie zombie, GameContext ctx, float dt) {
        return false;
    }

    @Override
    protected void doExecute(Zombie zombie, GameContext ctx) {
    }

    @Override
    public String getName() {
        return "ImpFly";
    }

    @Override
    public String getLabel() {
        return "Imp Flying";
    }
}
