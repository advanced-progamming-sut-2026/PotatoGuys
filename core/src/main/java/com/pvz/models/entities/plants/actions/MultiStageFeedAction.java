package com.pvz.models.entities.plants.actions;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.MultiStageShooterConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig.ProjectilePattern;
import com.pvz.models.entities.projectile.ProjectileFactory;
import com.pvz.models.games.GameContext;

/**
 * Plant-food action for {@link MultiStageShooterConfig} plants (e.g. BowlingBulb).
 *
 * <p>Plays {@code plantfood_on} → then cycles through {@code plantfoodLabels},
 * firing one {@code plantfoodPattern} per sub-stage.  After the last sub-stage
 * the plant returns to idle (which resets the normal stage cycle).
 */
public class MultiStageFeedAction extends ShooterAction {

    private final MultiStageShooterConfig msConfig;
    private int feedStage = 0;
    private float clipTimer = 0f;
    private boolean introPlayed = false;

    public MultiStageFeedAction(MultiStageShooterConfig config) {
        super(config);
        this.msConfig = config;
    }

    // ── Query helpers for PlantIdleState ─────────────────────────────────────

    public String getCurrentIdleLabel() {
        if (!introPlayed) return msConfig.plantfoodOnLabel;
        return msConfig.plantfoodLabels[feedStage];
    }

    public boolean shouldLoopIdle() {
        return false;
    }

    public float getClipElapsedTime() {
        return clipTimer;
    }

    // ── Trigger logic ────────────────────────────────────────────────────────

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        return false; // only invoked directly
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        clipTimer = 0f;
        feedStage = 0;
        introPlayed = false;

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        animTime = AnimationCatalog.getInstance()
                .getClipDuration(pam.pamFilePath, msConfig.plantfoodOnLabel);

        patterns.clear();
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;
        clipTimer += dt;

        // ── Intro phase: play plantfood_on clip once ──
        if (!introPlayed) {
            if (clipTimer >= animTime) {
                introPlayed = true;
                clipTimer = 0f;
                stateTime = 0f;

                PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
                animTime = AnimationCatalog.getInstance()
                        .getClipDuration(pam.pamFilePath, msConfig.plantfoodLabels[feedStage]);

                // Fire the first plant-food pattern
                if (feedStage < msConfig.plantfoodPatterns.size) {
                    spawnPattern(plant, ctx, msConfig.plantfoodPatterns.get(feedStage));
                }
            }
            return;
        }

        // ── Sub-stage phase: advance through plantfood clips ──
        if (clipTimer >= animTime) {
            feedStage++;
            clipTimer = 0f;
            stateTime = 0f;

            if (feedStage >= msConfig.plantfoodLabels.length) {
                // All sub-stages done → return to idle (normal cycle resets)
                plant.changeState(new com.pvz.models.entities.plants.fsm.PlantIdleState());
                return;
            }

            PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
            animTime = AnimationCatalog.getInstance()
                    .getClipDuration(pam.pamFilePath, msConfig.plantfoodLabels[feedStage]);

            // Fire the next plant-food pattern
            if (feedStage < msConfig.plantfoodPatterns.size) {
                spawnPattern(plant, ctx, msConfig.plantfoodPatterns.get(feedStage));
            }
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
        String label = introPlayed ? msConfig.plantfoodLabels[feedStage] : msConfig.plantfoodOnLabel;
        return new FrameConfig(pam.pamFilePath, label, clipTimer, pos, scale, null, false);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void spawnPattern(Plant plant, GameContext ctx, ProjectilePattern pat) {
        float plantX = GameController.colToWorldX(plant.getCol());
        float plantY = GameController.laneToWorldY(plant.getLane());
        Vector2 startPos = new Vector2(plantX + pat.positionOffset.x, plantY + pat.positionOffset.y);
        Vector2 vel = new Vector2(pat.velocity.x, pat.velocity.y);
        var proj = ProjectileFactory.create(pat.projectileType, ctx, startPos, vel, pat.damage);
        proj.setSourcePlantType(plant.getType());
        ctx.spawnProjectile(proj);
        ctx.log("[Action] " + plant.getSheet().getName() + " PF fired " + pat.projectileType);
    }
}
