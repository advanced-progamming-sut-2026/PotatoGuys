package com.pvz.models.entities.plants.config;

/**
 * Extension of {@link ShooterActionConfig} for plants that must charge up before
 * firing (e.g. Citron).
 *
 * <p>Behaviour:
 * <ul>
 *   <li>After being planted the plant plays {@code chargeLabel} for {@code chargeDuration}
 *       seconds.  During this phase it cannot fire.</li>
 *   <li>Once charged it enters a ready idle ({@code idleLabel}) and waits for a zombie
 *       to appear in its lane.</li>
 *   <li>When a zombie is detected it plays {@code attackLabel} and fires the configured
 *       projectile patterns.</li>
 * </ul>
 *
 * <p>Example JSON snippet:
 * <pre>{@code
 * "attackConfig": {
 *   "class": "ChargingShooterConfig",
 *   "label": "attack",
 *   "intervalSeconds": 9.0,
 *   "chargeLabel": "charge",
 *   "chargeDuration": 7.0,
 *   "idleLabel": "idle2",
 *   "patterns": [ ... ]
 * }
 * }</pre>
 */
public class ChargingShooterConfig extends ShooterActionConfig {

    /** PAM clip played while the plant is charging (one-shot, non-looping). */
    public String chargeLabel;

    /** Seconds the charge phase lasts. */
    public float chargeDuration;

    /** Idle clip used after charging is complete (waiting for a zombie). */
    public String idleLabel;
}
