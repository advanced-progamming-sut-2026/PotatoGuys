package com.pvz.models.entities.plants.config;

/**
 * Extension of {@link SunProducerActionConfig} for plants that visually grow over
 * multiple stages (e.g. Sun-shroom's small / medium / large forms).
 *
 * <p>Per-stage PAM clip labels are defined here so the JSON data drives the
 * animation transitions without any hard-coded logic in the action.
 *
 * <p>Example JSON snippet:
 * <pre>{@code
 * "attackConfig": {
 *   "class": "GrowthSunProducerConfig",
 *   "label": "special_stage1",
 *   "intervalSeconds": 24.0,
 *   "delaySeconds": 0.6,
 *   "productionKind": "STAGED",
 *   "amounts": [25, 50, 75],
 *   "stageIntervalSeconds": [24.0, 72.0],
 *   "spawnOffset": { "x": 0.0, "y": 0.2 },
 *   "idleLabels": ["idle_stage1", "idle_stage2", "idle_stage3"],
 *   "attackLabels": ["special_stage1", "special_stage2", "special_stage3"],
 *   "growthLabels": ["growth_stage1", "growth_stage2"]
 * }
 * }</pre>
 */
public class GrowthSunProducerConfig extends SunProducerActionConfig {

    /** Per-stage idle clip labels indexed by growth stage (0, 1, 2, ...). */
    public String[] idleLabels;

    /** Per-stage attack/sun-production clip labels indexed by growth stage. */
    public String[] attackLabels;

    /** Growth transition clip labels — one fewer than the number of stages.
     *  {@code growthLabels[0]} plays when transitioning from stage 0 → 1, etc. */
    public String[] growthLabels;
}
