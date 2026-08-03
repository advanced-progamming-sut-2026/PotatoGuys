package com.pvz.models.entities.plants.data;

public enum PlantFoodKind {
    /** No special effect (already a single-use consumable, or family-buff trigger). */
    NONE,
    /** Instantly credits the player with {@code amount} sun. */
    INSTANT_SUN,
    /** Greatly increased fire-rate for {@code durationSeconds}. */
    RAPID_FIRE,
    /** One large area-of-effect hit against up to {@code count} zombies (0 = whole lane). */
    AOE_BURST,
    /** Instantly destroys up to {@code count} zombies. */
    MULTI_INSTAKILL,
    /** Converts up to {@code count} zombies to fight for the player. */
    HYPNOTIZE,
    /** Freezes every zombie currently on the board. */
    FREEZE_ALL,
    /** Permanently raises {@code maxHp} by {@code amount} (armor-flavored plants). */
    PERMANENT_HP_BOOST,
    /** Forces every zombie in the plant's lane to an adjacent lane. */
    FORCE_MOVE_ALL_IN_LANE,
    /** Fully heals the plant and pulls nearby zombies into its lane. */
    FULL_HEAL_AND_ABSORB,
    /** Spawns {@code count} extra copies of the plant. */
    CLONE_SELF,
    /** Converts the zombie currently eating this plant into a permanent ally. */
    CONVERT_EATER_TO_ALLY,
    /** Temporary damage/utility aura affecting nearby plants or projectiles. */
    AURA_BUFF,
    /** Strips metal armor from up to {@code count} zombies. */
    MULTI_DISARM
}
