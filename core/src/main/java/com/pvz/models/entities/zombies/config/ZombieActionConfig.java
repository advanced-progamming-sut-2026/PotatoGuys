package com.pvz.models.entities.zombies.config;

/**
 * Base class for every config-driven zombie FSM action.
 *
 * <p>Mirrors {@link com.pvz.models.entities.plants.config.PlantActionConfig}: the
 * concrete subclass (selected via the LibGDX Json {@code "class"} tag) carries the
 * behaviour, while {@link #label} names the PAM clip that plays while the state
 * is active (e.g. {@code "walk"}, {@code "eat"}, {@code "die"}, {@code "power"}).
 */
public abstract class ZombieActionConfig {

    /** PAM clip label this action plays while active. */
    public String label;
}
