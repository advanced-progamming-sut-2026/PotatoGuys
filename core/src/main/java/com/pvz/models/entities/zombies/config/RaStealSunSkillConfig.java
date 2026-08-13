package com.pvz.models.entities.zombies.config;

/**
 * Ra Zombie sun-steal config.
 *
 * <p>From JSON {@code ZombieRaProps}:
 * <ul>
 *   <li>{@code MaxClaimedSunCurrency = 250} — cap on total sun stolen.</li>
 *   <li>{@code SunPerSteal = 25} — stolen per cast.</li>
 *   <li>{@code StealIntervalSeconds = 1.0} — cooldown between steals.</li>
 * </ul>
 */
public class RaStealSunSkillConfig extends ZombieSkillConfig {

    /** Interval between steals, in seconds. */
    public float stealIntervalSeconds = 1.0f;

    /** Sun amount stolen per cast. */
    public int sunPerSteal = 25;

    /** Maximum total sun this Ra may steal. */
    public int maxClaimedSun = 250;
}
