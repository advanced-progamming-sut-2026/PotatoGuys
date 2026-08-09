package com.pvz.models.entities.plants.config;

/**
 * Maps a plant's PAM (PopCap Animation) timeline to the engine's state machine.
 * No TextureAtlas/sprite-sheet frames are involved; {@link #pamFilePath} points
 * directly at the {@code .pam} binary and the label fields name timelines inside it.
 */
public class PamAnimationConfig {

    public String pamFilePath;
    public String idleLabel;
    public String attackActionLabel;
    public String plantFoodLabel;
    public float animSpeed = 1.0f;

    /** Frame index (within {@link #actionLabel}) at which the actual game effect fires. Mutually optional with {@link #actionTriggerLabel}. */
    public Integer actionTriggerFrame;
    /** Named PAM sub-label marking the trigger point, used instead of a raw frame index when the timeline exposes one. */
    public String actionTriggerLabel;
}
