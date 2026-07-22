package pvz.models.entities.zombies.armor;

/**
 * Behaviour flags attached to an armour piece.
 * Values are parsed from the JSON {@code ArmorFlags} array (lower-case strings).
 *
 * <ul>
 *   <li>{@code DAMAGEABLE}  – the armour can take damage from projectiles.</li>
 *   <li>{@code DROPPABLE}   – the armour piece falls off when health reaches 0.</li>
 *   <li>{@code HELM}        – head slot; can be magnetically pulled by Magnet-shroom.</li>
 *   <li>{@code METALLIC}    – made of metal; Magnet-shroom will preferentially target this.</li>
 *   <li>{@code PASSDAMAGE}  – damage applied to this piece also passes through to the
 *                             next piece (or the zombie's own HP) simultaneously.
 *                             Models the Dark Ages Knight's Shoulder Armour.</li>
 * </ul>
 */
public enum ArmorFlag {
    DAMAGEABLE,
    DROPPABLE,
    HELM,
    METALLIC,
    PASSDAMAGE;

    /** Parses a lower-case JSON flag string (e.g. {@code "passdamage"}). */
    public static ArmorFlag fromString(String raw) {
        return ArmorFlag.valueOf(raw.toUpperCase().replace("-", "_"));
    }
}
