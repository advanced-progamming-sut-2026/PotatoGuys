package com.pvz.models.entities.zombies.config;

/**
 * Base class for every config-driven zombie skill — the zombie counterpart of
 * the plants' {@code PlantActionConfig} subclasses.
 *
 * <p>The concrete subclass (selected via the LibGDX Json {@code "class"} tag)
 * carries the skill's tunable parameters; {@link #label} names the PAM clip
 * played while the skill's state is active. Tuning a skill = editing its JSON.
 */
public abstract class ZombieSkillConfig extends ZombieActionConfig {

    /**
     * Fallback hold time (seconds) for the skill pose when the PAM clip's
     * duration is unknown to {@code AnimationCatalog}.
     */
    public float durationSeconds = 1f;
}
