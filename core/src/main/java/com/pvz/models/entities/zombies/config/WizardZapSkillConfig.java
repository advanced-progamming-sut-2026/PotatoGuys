package com.pvz.models.entities.zombies.config;

/**
 * Dark-Ages Wizard Zombie config.
 *
 * <p>The wizard transforms the nearest plant into a cat roughly every
 * {@code cooldownSeconds}.
 */
public class WizardZapSkillConfig extends ZombieSkillConfig {

    /** Seconds between zaps. */
    public float cooldownSeconds = 3f;
}
