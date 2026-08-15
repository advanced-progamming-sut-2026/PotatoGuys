package com.pvz.models.entities.plants.config;

/**
 * Plant Food config for wall-nut category plants.
 *
 * <p>{@code kind} mirrors the profile-level {@code PlantFoodKind} values:
 * <ul>
 *   <li>{@code ARMOR} — permanently raises max HP by {@code amount}.</li>
 *   <li>{@code FORCE_MOVE_ALL_IN_LANE} — pushes every zombie in the lane to an adjacent lane.</li>
 *   <li>{@code FULL_HEAL_AND_ABSORB} — fully heals and pulls nearby zombies into the lane.</li>
 * </ul>
 *
 * <p>{@code clips} is the plant-food animation sequence played while the effect
 * applies (each clip for its own catalog duration); empty falls back to a brief
 * hold on the sheet's idle clip.
 */
public class WallNutFeedConfig extends PlantActionConfig {
    public String kind = "ARMOR";
    public float amount = 0f;
    /** Optional permanent reflect-damage bonus applied alongside {@code ARMOR} (Endurian). */
    public float reflectBonus = 0f;
    public String[] clips = new String[0];
}
