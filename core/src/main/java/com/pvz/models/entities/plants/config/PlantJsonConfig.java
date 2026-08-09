package com.pvz.models.entities.plants.config;

/**
 * Top-level, data-driven plant record: identity/stat fields plus the two
 * polymorphic payloads ({@link #actionConfig}, {@link #pamAnimationConfig})
 * that fully describe a plant's behavior and presentation without any
 * per-plant Java class or switch statement.
 */
public class PlantJsonConfig {

    public int id;
    public String name;
    public String type;
    public String category;
    public int sunCost;
    public float baseHp;
    public Float actionIntervalSeconds;
    public Float rechargeSeconds;

    /** Resolved via the {@code "class"} tag inside the JSON object (see {@link PlantActionConfigLoader}). */
    public PlantActionConfig attackConfig;
    public PlantActionConfig feedConfig;
    public PamAnimationConfig pamAnimationConfig;
    public String description;
}
