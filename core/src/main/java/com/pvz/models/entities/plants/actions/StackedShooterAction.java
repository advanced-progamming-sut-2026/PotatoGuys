package com.pvz.models.entities.plants.actions;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.config.AnimationCatalog;
import com.pvz.models.entities.plants.config.PamAnimationConfig;
import com.pvz.models.entities.plants.config.ShooterActionConfig;
import com.pvz.models.entities.plants.config.StackedShooterConfig;
import com.pvz.models.entities.plants.fsm.PlantIdleState;
import com.pvz.models.games.GameContext;

/**
 * A {@link ShooterAction} variant for plants whose visual appearance and projectile
 * count scale with the number of stacked instances on the same tile (e.g. Pea Pod).
 *
 * <p>Behaviour:
 * <ul>
 *   <li>Only the first {@code stackCount} patterns from the config are fired each
 *       attack cycle, where {@code stackCount} starts at 1 and grows as more
 *       instances are planted on the same tile.</li>
 *   <li>The attack and idle PAM clip labels are resolved from per-stack arrays
 *       defined in {@link StackedShooterConfig}.</li>
 * </ul>
 */
public class StackedShooterAction extends ShooterAction {

    private final StackedShooterConfig stackedConfig;
    private int stackCount = 1;

    public StackedShooterAction(StackedShooterConfig config) {
        super(config);
        this.stackedConfig = config;
    }

    // ── Stack management ─────────────────────────────────────────────────────

    /** Called by the stacking logic when another instance is planted on the same tile. */
    public void incrementStack() {
        stackCount = Math.min(stackCount + 1, 5);
    }

    public int getStackCount() {
        return stackCount;
    }

    // ── Pattern handling ─────────────────────────────────────────────────────

    @Override
    public void onEnter(Plant plant, GameContext ctx) {
        stateTime = 0f;
        patterns.clear();

        int limit = Math.min(stackCount, stackedConfig.patterns.size);
        for (int i = 0; i < limit; i++) {
            ShooterActionConfig.ProjectilePattern src = stackedConfig.patterns.get(i);
            ShooterActionConfig.ProjectilePattern dst = new ShooterActionConfig.ProjectilePattern();
            dst.projectileType = src.projectileType;
            dst.damage = src.damage;
            dst.laneOffset = src.laneOffset;
            dst.allLanes = src.allLanes;
            dst.positionOffset.set(src.positionOffset);
            dst.velocity.set(src.velocity);
            dst.delaySeconds = src.delaySeconds;
            patterns.add(dst);
        }

        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        String attackClip = resolveAttackLabel();
        animTime = AnimationCatalog.getInstance().getClipDuration(pam.pamFilePath, attackClip);
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    @Override
    public FrameConfig draw(Plant plant, GameContext ctx) {
        PamAnimationConfig pam = plant.getSheet().pamAnimationConfig;
        float x = GameController.colToWorldX(plant.getCol());
        float y = GameController.laneToWorldY(plant.getLane());
        Vector2 pos = new Vector2(x, y);
        Vector2 scale = new Vector2(0.7f, 0.7f);
        return new FrameConfig(pam.pamFilePath, resolveAttackLabel(), stateTime, pos, scale, null, true);
    }

    // ── Idle label helper (used by PlantIdleState) ──────────────────────────

    /** Returns the correct idle clip label for the current stack count. */
    public String getIdleLabel() {
        return resolveIdleLabel();
    }

    // ── Label resolvers ──────────────────────────────────────────────────────

    private String resolveIdleLabel() {
        int idx = Math.min(stackCount - 1, stackedConfig.idleLabels.length - 1);
        return stackedConfig.idleLabels[Math.max(0, idx)];
    }

    private String resolveAttackLabel() {
        int idx = Math.min(stackCount - 1, stackedConfig.attackLabels.length - 1);
        return stackedConfig.attackLabels[Math.max(0, idx)];
    }
}
