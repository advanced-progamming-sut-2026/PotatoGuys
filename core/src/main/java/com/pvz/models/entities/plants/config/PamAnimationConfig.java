package com.pvz.models.entities.plants.config;


public class PamAnimationConfig {

    public String pamFilePath;
    public String idleLabel;
    public float animSpeed = 1.0f;

    /** Frame index (within {@link #actionLabel}) at which the actual game effect fires.
     * Mutually optional with {@link #actionTriggerLabel}. */
    public Integer actionTriggerFrame;
    /** Named PAM sub-label marking the trigger point, used instead of a raw frame index
     * when the timeline exposes one. */
    public String actionTriggerLabel;
}
