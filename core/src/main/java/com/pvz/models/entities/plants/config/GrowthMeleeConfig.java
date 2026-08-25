package com.pvz.models.entities.plants.config;

/**
 * Config for melee plants that grow through stages over time,
 * dealing increasing AoE damage at each stage (e.g. Kiwibeast).
 */
public class GrowthMeleeConfig extends PlantActionConfig {
    public float intervalSeconds = 2f;
    public float baseDamage = 15f;
    public float attackRangeFactor = 1.6f;

    /** Idle clip labels for each growth stage. */
    public String[] idleLabels = {"idle"};
    /** Attack clip labels for each growth stage. */
    public String[] attackLabels = {"attack"};
    /** Growth clip labels played when advancing to the next stage. */
    public String[] growthLabels = {};
    /** Seconds to wait before advancing from stage i to stage i+1. */
    public float[] stageIntervals = {};
    /** Damage multiplier per stage (indexed by stage). */
    public float[] damageMultipliers = {1f};
}
