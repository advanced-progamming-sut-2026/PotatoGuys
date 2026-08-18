package com.pvz.models.entities.zombies.config;

/**
 * Configuration for the Octopus Zombie's binding skill.
 *
 * <p>The octopus zombie periodically throws an octopus projectile at the
 * nearest active plant. On impact the octopus covers the plant, disabling
 * it and blocking the tile until the overlay is destroyed.
 */
public class OctopusSkillConfig extends ZombieSkillConfig {

    /** Seconds between octopus throws (default 12). */
    public float cooldownSeconds = 12f;

    /** HP of the octopus overlay placed on the target plant (default 100). */
    public float octopusHp = 100f;

    /** Duration of the throw animation in seconds (default 1.0). */
    public float throwDuration = 1.0f;

    /** Arc height of the parabolic octopus projectile (default 120). */
    public float arcHeight = 120f;
}
