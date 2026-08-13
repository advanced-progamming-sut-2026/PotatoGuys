package com.pvz.models.entities.zombies.config;

/**
 * Ice-Age Hunter Zombie config.
 *
 * <p>From JSON {@code ZombieIceAgeHunterProps}:
 * <ul>
 *   <li>{@code SnowballsPerBarrage = 3} — snowballs fired per barrage.</li>
 *   <li>{@code FarAttackRange = 4} — attacks plants up to 4 cells ahead.</li>
 *   <li>{@code NearAttackRange = 1} — minimum distance before eating rather than throwing.</li>
 * </ul>
 */
public class HunterSnowballSkillConfig extends ZombieSkillConfig {

    /** Seconds between barrages. */
    public float cooldownSeconds = 2f;

    /** Snowballs fired per barrage. */
    public int snowballsPerBarrage = 3;

    /** Max cells ahead the hunter can target. */
    public int farRange = 4;
}
