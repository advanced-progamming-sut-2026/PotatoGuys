package com.pvz.models.entities.zombies.config;

/**
 * TombRaiser Zombie config.
 *
 * <p>From JSON {@code ZombieTombRaiserProps}:
 * <ul>
 *   <li>{@code Ammo = 5} — maximum casts before the zombie runs out of bones.</li>
 *   <li>{@code NumberOfTombsToSpawn = 2} — tombs raised per cast.</li>
 *   <li>{@code TimeBetweenRaisings = 6} — seconds between casts.</li>
 * </ul>
 */
public class TombRaiserSkillConfig extends ZombieSkillConfig {

    /** Seconds between casts (JSON {@code TimeBetweenRaisings}). */
    public float castIntervalSeconds = 6f;

    /** Tombs raised per cast (JSON {@code NumberOfTombsToSpawn}). */
    public int tombsPerCast = 2;

    /** Total casts available (JSON {@code Ammo}). */
    public int ammo = 5;
}
