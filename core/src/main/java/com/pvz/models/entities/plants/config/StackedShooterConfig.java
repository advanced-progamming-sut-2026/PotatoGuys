package com.pvz.models.entities.plants.config;

/**
 * Extension of {@link ShooterActionConfig} for plants whose visual appearance and
 * projectile count scale with the number of stacked instances on the same tile
 * (e.g. Pea Pod — 1 through 5 pods).
 *
 * <p>Per-stack PAM clip labels are defined here so the JSON data drives the
 * animation transitions without any hard-coded logic in the action.
 *
 * <p>Example JSON snippet:
 * <pre>{@code
 * "attackConfig": {
 *   "class": "StackedShooterConfig",
 *   "label": "attack",
 *   "intervalSeconds": 1.5,
 *   "idleLabels": ["idle", "idle2", "idle3", "idle4", "idle5"],
 *   "attackLabels": ["attack", "attack 2", "attack 3", "attack 4", "attack 5"],
 *   "patterns": [ ... 5 patterns, one per pod ... ]
 * }
 * }</pre>
 *
 * <p>At runtime the action uses only the first {@code stackCount} patterns from the
 * list, so pattern order matters: pattern 0 is always fired, pattern 1 only when
 * stack >= 2, and so on.
 */
public class StackedShooterConfig extends ShooterActionConfig {

    /** Per-stack idle clip labels indexed by stack count minus one (0 = 1 pod, 1 = 2 pods …). */
    public String[] idleLabels;

    /** Per-stack attack clip labels indexed by stack count minus one. */
    public String[] attackLabels;
}
