package com.pvz.models.entities.zombies.armor;

/**
 * Armour types directly mapped from the JSON {@code ArmorType} field.
 * Each value corresponds to one entry in the game's ArmorPropertySheet data.
 */
public enum ArmorType {
    CONE,
    BUCKET,
    BRICK,
    SHOULDER_ARMOR,
    CROWN,
    NEWSPAPER,
    ICE_BLOCK;      // Frostbite Caves ice-block armour (ZombieIceageArmor3Default)

    /**
     * Case-insensitive lookup from the JSON string (e.g. {@code "ShoulderArmor"}).
     *
     * @throws IllegalArgumentException if the value is not recognised
     */
    public static ArmorType fromString(String raw) {
        switch (raw.toUpperCase().replace(" ", "_").replace("-", "_")) {
            case "CONE":          return CONE;
            case "BUCKET":        return BUCKET;
            case "BRICK":         return BRICK;
            case "SHOULDERARMOR":
            case "SHOULDER_ARMOR": return SHOULDER_ARMOR;
            case "CROWN":         return CROWN;
            case "NEWSPAPER":     return NEWSPAPER;
            case "ICEBLOCK":
            case "ICE_BLOCK":     return ICE_BLOCK;
            default:
                throw new IllegalArgumentException("Unknown ArmorType: " + raw);
        }
    }
}
