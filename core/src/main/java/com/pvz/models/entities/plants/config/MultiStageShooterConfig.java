package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.utils.Array;

/**
 * Extension of {@link ShooterActionConfig} for plants that fire one projectile
 * per stage and cycle through idle/attack clips (e.g. BowlingBulb).
 *
 * <p>Each stage fires the {@code patterns.get(stageIndex)} entry.  After the
 * last stage the plant plays {@code reloadLabels} in order, then resets to
 * stage&nbsp;0.
 *
 * <p>Plant-food mode uses {@code plantfoodLabels} (one clip per sub-stage,
 * all firing {@code plantfoodPatterns}) and returns to the normal cycle
 * afterwards.
 */
public class MultiStageShooterConfig extends ShooterActionConfig {

    /** Idle / attack clip label for each stage (e.g. ["special","special2","special3"]). */
    public String[] stageLabels;

    /** Reload clips played in order after the last stage (e.g. ["reload","reload2","reload3"]). */
    public String[] reloadLabels;

    /** Clip played once at the start of plant-food (may be null). */
    public String plantfoodOnLabel;

    /** Idle clip during plant-food (may be null). */
    public String plantfoodIdleLabel;

    /** One clip per plant-food sub-stage (e.g. ["plantfood1","plantfood2","plantfood3"]). */
    public String[] plantfoodLabels;

    /** Patterns fired during plant-food (one per sub-stage). */
    public Array<ProjectilePattern> plantfoodPatterns = new Array<>();
}
