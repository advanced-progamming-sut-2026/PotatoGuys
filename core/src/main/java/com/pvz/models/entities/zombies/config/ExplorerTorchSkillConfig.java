package com.pvz.models.entities.zombies.config;

/**
 * Explorer Zombie torch config.
 *
 * <p>JSON {@code ZombieExplorerProps} values are folded into the skill config
 * so they can be tuned without touching code:
 * <ul>
 *   <li>{@code MaxTorchReach = 37} (pixels ≈ 1 grid cell forward)</li>
 * </ul>
 */
public class ExplorerTorchSkillConfig extends ZombieSkillConfig {

    /** How far the torch reaches ahead of the zombie, in pixels. */
    public float maxTorchReach = 37f;
}
