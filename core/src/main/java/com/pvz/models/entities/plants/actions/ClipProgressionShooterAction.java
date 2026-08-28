package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.effects.SplatEffect;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.ClipProgressionShooterConfig;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.projectile.ProjectileFactory;
import com.pvz.models.entities.projectile.ProjectileType;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.TileTags;

/**
 * A {@link ShooterAction} whose charge-up is visualised by cycling through a
 * sequence of idle clips (e.g. Caulipower's idle1_1 → idle2_1 → … or
 * Electric Blueberry's idle1_1 → idle1_2 → idle2_1 → …).
 *
 * <p>After the last charge clip has played the plant enters a ready state and
 * waits for a zombie.  On detection it fires and resets back to the first
 * charge clip.
 *
 * <p>When {@link ClipProgressionShooterConfig#allTargets} is {@code true} the
 * attack hits <em>every</em> zombie in the lane and optionally spawns a cloud
 * effect on each target (Electric Blueberry).
 */
public class ClipProgressionShooterAction extends ShooterAction {

    private final ClipProgressionShooterConfig cpConfig;
    private int chargeIndex = 0;
    private float clipTimer = 0f;
    private boolean fullyCharged = false;

    /** Elapsed time within the *current* charge clip, resets on every clip change. */
    private float clipElapsedTime = 0f;

    public ClipProgressionShooterAction(ClipProgressionShooterConfig config) {
        super(config);
        this.cpConfig = config;
    }

    // ── Query helpers for PlantIdleState ─────────────────────────────────────

    /** Returns the idle clip the plant should display right now. */
    public String getCurrentIdleLabel() {
        if (!fullyCharged) {
            return cpConfig.chargeIdleLabels[chargeIndex];
        }
        return cpConfig.readyIdleLabel;
    }

    /** Elapsed time within the current charge clip (resets on each clip change). */
    public float getClipElapsedTime() {
        return clipElapsedTime;
    }

    /** Charge clips should not loop; the ready idle should. */
    public boolean shouldLoopIdle() {
        return fullyCharged;
    }

    // ── Trigger logic ────────────────────────────────────────────────────────

    @Override
    public boolean shouldTrigger(Plant plant, GameContext ctx, float dt) {
        stateTime += dt;

        // ── Phase 1: cycling through charge clips ──
        if (!fullyCharged) {
            PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
            float clipDuration = AnimationCatalog.getInstance()
                    .getClipDuration(pam.pamFilePath, cpConfig.chargeIdleLabels[chargeIndex]);
            clipTimer += dt;
            clipElapsedTime += dt;
            if (clipDuration > 0 && clipTimer >= clipDuration) {
                clipTimer = 0f;
                clipElapsedTime = 0f;
                chargeIndex++;
                if (chargeIndex >= cpConfig.chargeIdleLabels.length) {
                    fullyCharged = true;
                    stateTime = 0f;
                }
            }
            return false;
        }

        // ── Phase 2: fully charged — fire when a target is present ──
        return laneHasTarget(plant, ctx);
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        animTime = AnimationCatalog.getInstance()
                .getClipDuration(pam.pamFilePath, cpConfig.attackLabel);

        if (!cpConfig.allTargets) {
            // ── Normal single-target: copy first pattern ──
            patterns.clear();
            if (cpConfig.patterns.size > 0) {
                var src = cpConfig.patterns.get(0);
                var dst = new com.pvz.models.entities.plants.config.ShooterActionConfig.ProjectilePattern();
                dst.projectileType = src.projectileType;
                dst.damage = src.damage;
                dst.laneOffset = src.laneOffset;
                dst.allLanes = src.allLanes;
                dst.positionOffset.set(src.positionOffset);
                dst.velocity.set(src.velocity);
                dst.delaySeconds = src.delaySeconds;
                patterns.add(dst);
            }
        } else {
            // ── Multi-target: clear patterns; we handle spawning manually ──
            patterns.clear();
            spawnOnAllTargets(plant, ctx);
        }
    }

    @Override
    public void update(Plant plant, GameContext ctx, float dt) {
        super.update(plant, ctx, dt);
        if (!cpConfig.allTargets) {
            // normal projectile spawning handled by super
        }
        if (patterns.isEmpty() && stateTime >= animTime) {
            plant.changeState(new com.pvz.models.entities.plants.fsm.PlantIdleState());
        }
    }

    @Override
    public void onExit(Plant plant, GameContext ctx) {
        chargeIndex = 0;
        clipTimer = 0f;
        clipElapsedTime = 0f;
        fullyCharged = false;
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x, y);
        Vector2 scale = new Vector2(0.7f, 0.7f);
        return new FrameConfig(pam.pamFilePath, cpConfig.attackLabel, stateTime, pos, scale, null, true);
    }

    // ── Multi-target (Electric Blueberry) ────────────────────────────────────

    private void spawnOnAllTargets(Plant plant, GameContext ctx) {
        List<Zombie> allZombies = new ArrayList<>();
        for (int lane = 0; lane < ctx.getMap().getRows(); lane++) {
            allZombies.addAll(ctx.getZombiesInLane(lane));
        }

        for (Zombie z : allZombies) {
            if (cpConfig.cloudPamPath != null && cpConfig.cloudClip != null) {
                ctx.addEffect(new SplatEffect(ctx,
                        new Vector2(z.getX(), z.getY() + 40f),
                        cpConfig.cloudPamPath, cpConfig.cloudClip, 0.7f));
            }

            float dmg = cpConfig.patterns.size > 0 ? cpConfig.patterns.get(0).damage : 0f;
            z.takeDamage(dmg);
            ctx.log("[Action] " + plant.getSheet().getName() + " hit zombie for " + (int) dmg);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean laneHasTarget(Plant plant, GameContext ctx) {
        if (cpConfig.allTargets) {
            for (int lane = 0; lane < ctx.getMap().getRows(); lane++) {
                if (!ctx.getZombiesInLane(lane).isEmpty()) return true;
            }
            return false;
        }

        float plantX = GameController.colToWorldX(plant.getCol());
        boolean hasZombie = ctx.getZombiesInLane(plant.getLane()).stream()
                .anyMatch(z -> z.getX() >= plantX);
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
