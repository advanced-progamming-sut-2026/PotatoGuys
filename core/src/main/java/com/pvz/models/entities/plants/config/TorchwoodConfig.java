package com.pvz.models.entities.plants.config;

/**
 * Behaviour config for Torchwood: per-bite retaliation with a hit effect,
 * a whole-lane burn + explosion clip on death, and a global plant-food hit.
 */
public class TorchwoodConfig extends PlantActionConfig {

    /** Damage dealt back to a zombie each time it bites Torchwood. */
    public float biteDamage = 20f;

    /** How often the retaliation ticks while being eaten (seconds). */
    public float biteIntervalSeconds = 1f;

    /** Clip from TORCHWOOD_HIT_EFFECTS.PAM played on the eater (small hit). */
    public String biteHitClip = "hit_normal";

    /** Damage applied to every zombie in the lane when Torchwood dies. */
    public float deathDamage = 1800f;

    /** Clip from TORCHWOOD.PAM played at the plant position on death. */
    public String deathExplosionClip = "explosion";

    /** Damage dealt once to every zombie on the board by plant food. */
    public float plantFoodDamage = 100f;

    /** First clip of the plant-food animation (played once). */
    public String plantFoodOnClip = "plantfood_on_t2";

    /** Second clip of the plant-food animation (played after the intro). */
    public String plantFoodClip = "plantfood_t2";

    /** Clip from TORCHWOOD_HIT_EFFECTS.PAM played on each zombie by plant food. */
    public String plantFoodHitClip = "hit_power";
}