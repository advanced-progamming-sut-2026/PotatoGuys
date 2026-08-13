package com.pvz.models.entities.zombies.config;

/**
 * Gargantuar Imp-throw config.
 *
 * <p>From JSON {@code ZombieGargantuarProps}:
 * <ul>
 *   <li>{@code HealthPercentThrowImp = 0.5} — imp is thrown at 50 % HP.</li>
 *   <li>{@code ImpType} — registry alias of the imp to spawn.</li>
 * </ul>
 */
public class GargantuarSkillConfig extends ZombieSkillConfig {

    /** HP fraction (0..1) of max HP that triggers the throw. */
    public float throwHpFraction = 0.5f;

    /** Full registry alias of the imp zombie to spawn. */
    public String impType;
}
