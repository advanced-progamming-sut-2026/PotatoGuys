package com.pvz.models.entities.plants.actions.explosive.tangle;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.actions.PlantAction;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.explosive.TangleKelpConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.fsm.UnderwaterDragState;
import com.pvz.models.games.GameContext;

/**
 * Tangle Kelp's attack: when a zombie steps onto its tile it plays
 * {@code attack_submerge} (fully), then {@code attack}; at a fixed point of the
 * attack clip the grabbed zombie is dragged underwater (fading out), then
 * {@code attack_emerge} plays and the plant returns to idle.
 */
public class TangleKelpAction extends PlantAction {

    private enum Phase { SUBMERGE, ATTACK, EMERGE, END }

    private final TangleKelpConfig config;
    private Phase phase;
    private Zombie target;
    private float phaseTimer;
    private float submergeDuration;
    private float attackDuration;
    private float emergeDuration;
    private boolean grabbed;

    public TangleKelpAction(TangleKelpConfig config) {
        this.config = config;
    }

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        int lane = plant.getLane();
        for (Zombie z : ctx.getZombiesInLane(lane)) {
            if (z.isDead() || z.isUnderwaterGrabbed()) {
                continue;
            }
            // Use the collision/hitbox system so the trap only fires once the
            // zombie is actually on top of the plant (not merely in its grid cell).
            if (plant.overlaps(z)) {
                target = z;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0;
        phaseTimer = 0;
        phase = Phase.SUBMERGE;
        grabbed = false;

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        String pamPath = pam.pamFilePath;
        submergeDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.submergeClip);
        attackDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.attackClip);
        emergeDuration = AnimationCatalog.getInstance().getClipDuration(pamPath, config.emergeClip);

        if (submergeDuration <= 0) submergeDuration = config.submergeDuration;
        if (attackDuration <= 0) attackDuration = config.attackDuration;
        if (emergeDuration <= 0) emergeDuration = config.emergeDuration;
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        phaseTimer += dt;

        switch (phase) {
            case SUBMERGE:
                if (phaseTimer >= submergeDuration) {
                    phase = Phase.ATTACK;
                    phaseTimer = 0;
                }
                break;

            case ATTACK:
                if (!grabbed && target != null && !target.isDead()
                        && phaseTimer >= config.attackTriggerSeconds) {
                    grabbed = true;
                    // Stun/drag the zombie: pull it underwater and fade it out.
                    target.setState(new UnderwaterDragState(config.fadeDuration, config.sinkDepth));
                    target = null;
                }
                if (phaseTimer >= attackDuration) {
                    phase = Phase.EMERGE;
                    phaseTimer = 0;
                }
                break;

            case EMERGE:
                if (phaseTimer >= emergeDuration) {
                    phase = Phase.END;
                    phaseTimer = 0;
                }
                break;

            case END:
                plant.changeState(new PlantIdleState());
                break;
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        target = null;
        phase = Phase.SUBMERGE;
        phaseTimer = 0;
        stateTime = 0;
        grabbed = false;
    }

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        Vector2 position = new Vector2(
                GameController.colToWorldX(plant.getCol()),
                GameController.laneToWorldY(plant.getLane()));
        Vector2 scale = new Vector2(0.65f, 0.65f);
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;

        String clip;
        switch (phase) {
            case ATTACK:
                clip = config.attackClip;
                break;
            case EMERGE:
                clip = config.emergeClip;
                break;
            case SUBMERGE:
            default:
                clip = config.submergeClip;
                break;
        }

        return new FrameConfig(pam.pamFilePath, clip, phaseTimer, position, scale, null, false);
    }

    @Override
    public String getLabel() {
        return "TangleKelp[" + (phase != null ? phase.name() : "?") + "]";
    }
}
