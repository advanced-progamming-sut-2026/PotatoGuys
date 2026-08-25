package com.pvz.models.entities.plants.actions;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.MultiStageShooterConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig.ProjectilePattern;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.TileTags;

/**
 * A {@link ShooterAction} that cycles through N stages, firing one pattern
 * per stage and switching the idle / attack clip accordingly.
 * After the last stage it plays a sequence of reload clips, then resets.
 *
 * <p>Plant-food sub-stages are handled through a separate feed action
 * created by {@code MultiStageFeedAction}.
 */
public class MultiStageShooterAction extends ShooterAction {

    private final MultiStageShooterConfig msConfig;
    private int currentStage = 0;
    private boolean reloading = false;
    private int reloadIndex = 0;
    private float clipTimer = 0f;

    public MultiStageShooterAction(MultiStageShooterConfig config) {
        super(config);
        this.msConfig = config;
    }

    // ── Query helpers for PlantIdleState ─────────────────────────────────────

    /** Current idle clip label. */
    public String getCurrentIdleLabel() {
        if (reloading) {
            return msConfig.reloadLabels[reloadIndex];
        }
        return msConfig.stageLabels[currentStage];
    }

    /** Reload clips do not loop; stage clips loop while waiting for a target. */
    public boolean shouldLoopIdle() {
        return !reloading;
    }

    /** Elapsed time within the current clip (resets on clip change). */
    public float getClipElapsedTime() {
        return clipTimer;
    }

    // ── Trigger logic ────────────────────────────────────────────────────────

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;

        // ── Reloading phase: advance through reload clips ──
        if (reloading) {
            PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
            float clipDuration = AnimationCatalog.getInstance()
                    .getClipDuration(pam.pamFilePath, msConfig.reloadLabels[reloadIndex]);
            clipTimer += dt;
            if (clipDuration > 0 && clipTimer >= clipDuration) {
                clipTimer = 0f;
                reloadIndex++;
                if (reloadIndex >= msConfig.reloadLabels.length) {
                    reloading = false;
                    currentStage = 0;
                    stateTime = 0f;
                }
            }
            return false;
        }

        // ── Normal phase: fire when a target is present ──
        return laneHasTarget(plant, ctx);
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        clipTimer = 0f;

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        animTime = AnimationCatalog.getInstance()
                .getClipDuration(pam.pamFilePath, msConfig.stageLabels[currentStage]);

        // Fire only the pattern matching the current stage
        patterns.clear();
        if (currentStage < msConfig.patterns.size) {
            var src = msConfig.patterns.get(currentStage);
            var dst = new ProjectilePattern();
            dst.projectileType = src.projectileType;
            dst.damage = src.damage;
            dst.laneOffset = src.laneOffset;
            dst.allLanes = src.allLanes;
            dst.positionOffset.set(src.positionOffset);
            dst.velocity.set(src.velocity);
            dst.delaySeconds = src.delaySeconds;
            patterns.add(dst);
        }
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (patterns.isEmpty() && stateTime >= animTime) {
            currentStage++;
            if (currentStage >= msConfig.stageLabels.length) {
                // All stages done → start reload
                reloading = true;
                reloadIndex = 0;
                clipTimer = 0f;
                stateTime = 0f;
            }
            plant.changeState(new com.pvz.models.entities.plants.fsm.PlantIdleState());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        // clipTimer is managed by shouldTrigger for reload, reset only on full cycle
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x, y);
        Vector2 scale = new Vector2(0.7f, 0.7f);
        String label = reloading ? msConfig.reloadLabels[reloadIndex] : msConfig.stageLabels[currentStage];
        return new FrameConfig(pam.pamFilePath, label, stateTime, pos, scale, null, true);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean laneHasTarget(Plant plant, GameContext ctx) {
        boolean hasZombie = ctx.getZombiesInLane(plant.getLane()).stream()
                .anyMatch(z -> z.getX() >= GameController.colToWorldX(plant.getCol()));
        if (hasZombie) return true;

        for (int i = plant.getCol(); i < ctx.getMap().getColumns(); i++) {
            List<TileTags> tags = ctx.getTileAt(i, plant.getLane()).getTags();
            if (tags.contains(TileTags.GRAVE) || tags.contains(TileTags.ICE_BLOCK)) {
                return true;
            }
        }
        return false;
    }
}
