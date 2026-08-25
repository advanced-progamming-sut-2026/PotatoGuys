package com.pvz.models.entities.plants.actions;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.GrowthSunProducerConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.games.GameContext;

/**
 * A {@link SunProducerAction} variant for plants that visually grow over multiple
 * stages (e.g. Sun-shroom).
 *
 * <p>Behaviour:
 * <ul>
 *   <li>When the plant's {@code growthStageIndex} increases, this action plays the
 *       corresponding {@code growthLabels[fromStage]} clip without producing sun.</li>
 *   <li>When the normal production interval elapses, it plays the stage-specific
 *       {@code attackLabels[stage]} clip and spawns sun at {@code amounts[stage]}.</li>
 * </ul>
 *
 * <p>All clip labels are data-driven via {@link GrowthSunProducerConfig} so the
 * JSON fully controls which PAM animations are used at each stage.
 */
public class GrowthSunProducerAction extends SunProducerAction {

    private final GrowthSunProducerConfig growthConfig;
    private int lastStage = 0;
    private boolean playingGrowth = false;
    private int growthFromStage = 0;

    public GrowthSunProducerAction(GrowthSunProducerConfig config) {
        super(config);
        this.growthConfig = config;
    }

    /** Plant Food variant — does not kill on one-shot. */
    public GrowthSunProducerAction(GrowthSunProducerConfig config, boolean killOnOneShot) {
        super(config, killOnOneShot);
        this.growthConfig = config;
    }

    // ── Trigger logic ────────────────────────────────────────────────────────

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;

        // Growth takes priority over sun production
        int currentStage = plant.getGrowthStageIndex();
        if (currentStage > lastStage) {
            return true;
        }

        if (stateTime < config.intervalSeconds) {
            return false;
        }
        return true;
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        produced = false;
        playingGrowth = false;

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        int currentStage = plant.getGrowthStageIndex();

        if (currentStage > lastStage) {
            // ── Growth transition ──
            playingGrowth = true;
            growthFromStage = lastStage;
            lastStage = currentStage;
            String growthClip = resolveGrowthLabel(growthFromStage);
            animTime = AnimationCatalog.getInstance().getClipDuration(pam.pamFilePath, growthClip);
        } else {
            // ── Normal sun production ──
            lastStage = currentStage;
            String attackClip = resolveAttackLabel(currentStage);
            animTime = AnimationCatalog.getInstance().getClipDuration(pam.pamFilePath, attackClip);
        }
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (plant.isDead()) {
            return;
        }

        if (!playingGrowth && !produced && stateTime >= config.delaySeconds) {
            produced = true;
            produce(config, plant, ctx);
        }

        if (stateTime >= animTime) {
            plant.changeState(new PlantIdleState());
        }
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x, y);
        Vector2 scale = new Vector2(0.7f, 0.7f);

        String clip;
        if (playingGrowth) {
            clip = resolveGrowthLabel(growthFromStage);
        } else {
            clip = resolveAttackLabel(plant.getGrowthStageIndex());
        }
        return new FrameConfig(pam.pamFilePath, clip, stateTime, pos, scale, null, true);
    }

    // ── Idle label helper (used by PlantIdleState) ──────────────────────────

    /** Returns the correct idle clip label for the plant's current growth stage. */
    public String getIdleLabel(Plant plant) {
        return resolveIdleLabel(plant.getGrowthStageIndex());
    }

    // ── Label resolvers ──────────────────────────────────────────────────────

    private String resolveIdleLabel(int stage) {
        if (growthConfig.idleLabels != null && stage < growthConfig.idleLabels.length) {
            return growthConfig.idleLabels[stage];
        }
        return growthConfig.label;
    }

    private String resolveAttackLabel(int stage) {
        if (growthConfig.attackLabels != null && stage < growthConfig.attackLabels.length) {
            return growthConfig.attackLabels[stage];
        }
        return growthConfig.label;
    }

    private String resolveGrowthLabel(int fromStage) {
        if (growthConfig.growthLabels != null && fromStage < growthConfig.growthLabels.length) {
            return growthConfig.growthLabels[fromStage];
        }
        return growthConfig.label;
    }
}
