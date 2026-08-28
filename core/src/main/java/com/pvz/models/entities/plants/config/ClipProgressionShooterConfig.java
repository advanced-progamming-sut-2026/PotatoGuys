package com.pvz.models.entities.plants.config;

/**
 * Extension of {@link ShooterActionConfig} for plants whose charge-up is
 * visualised by cycling through a sequence of idle clips rather than a single
 * charge animation (e.g. Caulipower, Electric Blueberry).
 *
 * <p>The plant cycles through {@code chargeIdleLabels} in order.  Once the
 * last clip has played, the plant is "fully charged" and uses
 * {@code readyIdleLabel} while waiting for a target.  After firing it
 * resets back to the first charge clip.
 *
 * <p>When {@code allTargets} is {@code true} the attack damages
 * <em>every</em> zombie in the lane (Electric Blueberry) and optionally
 * spawns a cloud effect ({@code cloudPamPath}/{@code cloudClip}) on each
 * target.
 */
public class ClipProgressionShooterConfig extends ShooterActionConfig {

    /** Ordered idle clip labels played during the charge phase. */
    public String[] chargeIdleLabels;

    /** Idle clip used once fully charged (waiting for a target). */
    public String readyIdleLabel;

    /** Attack clip label. */
    public String attackLabel;

    /** When {@code true} every zombie in the lane is hit (not just the first). */
    public boolean allTargets;

    /** PAM path for the per-target cloud effect (Electric Blueberry). May be null. */
    public String cloudPamPath;

    /** Clip inside {@code cloudPamPath} to play for the cloud effect. */
    public String cloudClip;
}
