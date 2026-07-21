package pvz.models.entities.zombies.effects;

/**
 * Every possible status condition that can be applied to a zombie.
 *
 * <p>Effects are stored as {@link StatusEffect} instances inside the zombie's
 * {@code activeEffects} map, keyed by this enum. Only one instance per type
 * can be active at a time; applying the same type again resets the duration.
 */
public enum EffectType {

    /**
     * Zombie is slowed to 50 % of base speed.
     * Applied by ice/snow projectiles. Cleared immediately by a BURNING hit.
     */
    CHILL,

    /**
     * Zombie is completely stopped and cannot act.
     * Modelled as a thick ice block with 600 HP that other plants must break.
     * Cleared by fire damage (instantly) or by chipping away the ice.
     */
    FROZEN,

    /**
     * Zombie is briefly stunned — cannot move or attack.
     * Applied by certain plant-food abilities and Snapdragon area effects.
     */
    STUN,

    /**
     * Zombie is on fire — clearing all ice effects and dealing no extra HP
     * damage in this simulation (fire damage is handled via direct HP reduction).
     */
    BURNING,

    /**
     * Poison DoT: bypasses armour, deals {@code poisonDpsPerTick} per tick.
     * Applied by Poison Pea and similar plants.
     */
    POISONED,

    /**
     * Zombie fights for the player (Hypno-shroom effect).
     * The zombie attacks other zombies instead of plants.
     */
    HYPNOTIZED,

    /**
     * Zombie has been turned into a harmless cat by the Dark Ages Wizard.
     * In this state the zombie does not move, attack, or take meaningful actions.
     * Cleared when the Wizard that cast the spell dies.
     */
    TRANSFORMED,

    /**
     * An octopus is stuck on the zombie's head (Big Wave Beach Octopus Zombie).
     * Mirrors the FROZEN effect visually but is mechanically distinct
     * (not cleared by fire; only removed by plant attacks on the octopus).
     */
    OCTOPUS
}
