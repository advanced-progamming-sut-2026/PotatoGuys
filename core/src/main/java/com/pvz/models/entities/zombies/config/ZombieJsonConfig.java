package com.pvz.models.entities.zombies.config;

/**
 * Wire-format DTO for a single zombie in {@code zombie_actions.json} — the
 * zombie counterpart of {@link com.pvz.models.entities.plants.config.PlantJsonConfig}.
 *
 * <p>{@code zombie_actions.json} is the single source of truth for zombies: it
 * carries the stats that used to live in {@code zombie_profiles.json}
 * (hitPoints/eatDps/speed/…), the armour aliases, the per-state animation
 * configs and the skill config. Deleting {@code zombie_profiles.json} is safe.
 *
 * <p>{@link #alias} is the exact alias used by {@link
 * com.pvz.models.entities.zombies.data.ZombieRegistry} (and {@link
 * com.pvz.models.entities.zombies.ZombieFactory}).
 */
public class ZombieJsonConfig {

    public int id;
    public String name;
    public String alias;
    public String category;
    public String objClass;

    // ── Core stats (pre-scaling) ──────────────────────────────────────────────
    public String scaling;
    public float hitPoints;
    public float eatDps;
    public float speed;
    public int wavePointCost;
    public int weight;
    public boolean canSpawnPlantFood;
    public String[] armorAliases;

    // ── Gargantuar-specific ───────────────────────────────────────────────────
    public String impType;
    public float healthThresholdToThrowImp;
    public float smashDamage;
    public float smashDuration;

    // ── Ra-specific ───────────────────────────────────────────────────────────
    public int maxClaimedSunCurrency;

    // ── TombRaiser-specific ───────────────────────────────────────────────────
    public int ammo;
    public int numberOfTombsToSpawn;
    public float timeBetweenRaisings;

    // ── Explorer-specific ─────────────────────────────────────────────────────
    public float maxTorchReach;

    // ── Hunter-specific ───────────────────────────────────────────────────────
    public int snowballsPerBarrage;
    public int farAttackRange;
    public int nearAttackRange;

    // ── IceAgeTroglobite-specific ────────────────────────────────────────────
    public int numberOfIceblocksToSpawnWith;

    // ── Size/misc ─────────────────────────────────────────────────────────────
    public boolean imp;

    public ZombieAnimationConfig animationConfig;

    /** Optional per-state overrides; when null the FSM states fall back to default labels. */
    public ZombieActionConfig walkConfig;
    public ZombieActionConfig eatConfig;
    public ZombieActionConfig dieConfig;
    public ZombieSkillConfig skillConfig;

    public String description;
}
