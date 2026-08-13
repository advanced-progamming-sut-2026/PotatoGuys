package com.pvz.models.entities.zombies.config;

import com.badlogic.gdx.utils.ObjectMap;

/**
 * Root wire-format DTO of {@code zombie_actions.json}.
 *
 * <p>After the merge with the old {@code zombie_profiles.json}, this one file
 * carries everything a zombie needs: the shared scaling presets, the armour
 * definitions and the per-zombie entries (stats + animations + skills).
 *
 * <p>Collection fields use LibGDX-friendly concrete types ({@link ObjectMap}
 * and arrays) because LibGDX {@code Json} cannot instantiate {@code Map}/{@code List}
 * interfaces or nested generic collections.
 */
public class ZombieRootConfig {

    /** Named scaling recipes referenced by each zombie's {@code scaling} field. */
    public ObjectMap<String, ScaledPropConfig[]> scalingPresets;

    /** Armour definitions referenced via {@code armorAliases}. */
    public ZombieArmorConfig[] armors;

    /** The zombies themselves. */
    public ZombieJsonConfig[] zombies;
}
