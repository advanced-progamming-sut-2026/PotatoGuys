package com.pvz.models.entities.zombies.config;

import java.util.List;

/**
 * Per-zombie PAM animation configuration — the zombie counterpart of
 * {@link com.pvz.models.entities.plants.config.PamAnimationConfig}.
 *
 * <p>Holds the PAM file path plus the shared {@link #idleLabel}; the per-state
 * clip labels (walk/eat/die/skill) live in the {@link ZombieActionConfig}
 * subclasses attached to each zombie.
 */
public class ZombieAnimationConfig {

    /** PAM file path, e.g. {@code "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM"}. */
    public String pamFilePath;

    /** Idle / neutral clip label (used by non-walking states such as flying imps). */
    public String idleLabel = "idle";

    /** Animation playback speed multiplier (1.0 = normal). */
    public float animSpeed = 1.0f;

    /** Visual scale multiplier; falls back to 0.65 when unset. */
    public Float scale;

    /**
     * All clip labels the sheet actually defines, in PAM order. Populated from
     * the baked config data at load time so the model can resolve a requested
     * state label (e.g. {@code walk}) to an existing clip (e.g. {@code idle})
     * without touching any graphics engine — the entity layer must stay
     * server-friendly.
     */
    public List<String> availableClips;
}
