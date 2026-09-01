package com.pvz.models.entities.plants.config.explosive;

import com.pvz.models.entities.plants.config.PlantActionConfig;

/**
 * Tangle Kelp's water trap: grabs a zombie that steps onto its tile, drags it
 * underwater (fading it out) and then re-emerges.
 */
public class TangleKelpConfig extends PlantActionConfig {
    public String submergeClip = "attack_submerge";
    public String attackClip = "attack";
    public String emergeClip = "attack_emerge";

    public float submergeDuration = 2.1333f;
    public float attackDuration = 2.4667f;
    public float emergeDuration = 1.8333f;

    /** Seconds into {@link #attackClip} before the zombie is dragged down. */
    public float attackTriggerSeconds = 1.0f;

    /** Seconds over which the grabbed zombie is pulled down and faded out. */
    public float fadeDuration = 1.0f;

    /** How far down the zombie is pulled below the tile centre (world units). */
    public float sinkDepth = 90.0f;

    public float radarRangeCoefficient = 1.0f;
    public float baseDamage = 9999.0f;
}
