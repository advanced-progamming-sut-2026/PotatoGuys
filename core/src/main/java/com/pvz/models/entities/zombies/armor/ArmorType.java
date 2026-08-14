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
    /**
     * Intact PAM part name for this armour piece, used to force-show the armour
     * via the PamPlayer visibility map when drawing a zombie preview (the armour
     * parts exist in the shared zombie sheet but are hidden by default).
     *
     * @return the part name, or {@code null} for armours with no PAM part to reveal
     *         (e.g. the newspaper, which is part of its own PAM's default clips)
     */
    public String pamPartName() {
        switch (this) {
            case CONE:           return "zombie_armor_cone_norm";
            case BUCKET:         return "zombie_armor_bucket_norm";
            case BRICK:          return "zombie_armor_brick_norm";
            case CROWN:          return "zombie_armor_crown_norm";
            case SHOULDER_ARMOR: return "zombie_shoulder_armor_norm";
            case ICE_BLOCK:      return "zombie_armor_iceblock_norm";
            default:             return null;
        }
    }

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
