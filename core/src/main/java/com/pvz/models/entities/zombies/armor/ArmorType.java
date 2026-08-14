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
        String[] layers = pamLayers();
        return layers == null ? null : layers[0];
    }

    /**
     * PAM part names for this armour piece, one per damage layer: index 0 is the
     * intact look, index 1 the first (cracked) stage and index 2 the critical
     * stage. Taken directly from the {@code ArmorLayers} arrays of each armour's
     * data sheet; the current layer is picked by the armour's remaining-health
     * percentage ({@code ArmorLayerHealth} thresholds), see
     * {@link ArmorPiece#getLayerIndex()}.
     *
     * @return the three part names, or {@code null} for armours with no PAM part
     *         to reveal
     */
    public String[] pamLayers() {
        switch (this) {
            case CONE:           return new String[]{"zombie_armor_cone_norm",
                    "zombie_armor_cone_damage_01", "zombie_armor_cone_damage_02"};
            case BUCKET:         return new String[]{"zombie_armor_bucket_norm",
                    "zombie_armor_bucket_damage_01", "zombie_armor_bucket_damage_02"};
            case BRICK:          return new String[]{"zombie_armor_brick_norm",
                    "zombie_armor_brick_damage_01", "zombie_armor_brick_damage_02"};
            case CROWN:          return new String[]{"zombie_armor_crown_norm",
                    "zombie_armor_crown_damage_01", "zombie_armor_crown_damage_02"};
            case SHOULDER_ARMOR: return new String[]{"zombie_shoulder_armor_norm",
                    "zombie_shoulder_armor_damage_01", "zombie_shoulder_armor_damage_02"};
            case NEWSPAPER:      return new String[]{"_zombie_newspaper",
                    "_zombie_newspaper_dmg1", "_zombie_newspaper_dmg2"};
            case ICE_BLOCK:      return new String[]{"zombie_armor_iceblock_norm",
                    "zombie_armor_iceblock_damage1", "zombie_armor_iceblock_damage2"};
            default:             return null;
        }
    }

    /**
     * Name of the container part that groups this armour's damage-layer parts
     * inside the shared zombie sheet, or {@code null} when the parts are
     * attached directly to the animation.
     *
     * <p>Some sheets nest their armour parts under a container sprite whose
     * name contains {@code "armor"} (e.g. {@code _zombie_armor_crown_states},
     * {@code zombie_shoulder_armor}). libPVZ culls any part whose name carries
     * the ARMOR flag unless the visibility map reveals it — and culling a
     * container also hides its children — so the container must be force-shown
     * alongside the layer parts.
     */
    public String pamContainerName() {
        switch (this) {
            case CROWN:          return "_zombie_armor_crown_states";
            case SHOULDER_ARMOR: return "zombie_shoulder_armor";
            default:             return null;
        }
    }

    /**
     * Extra top-level parts that must be shown while this armour is alive,
     * regardless of the damage layer. These parts have no ARMOR flag, so
     * without an explicit map entry they would render in every layer.
     */
    public String[] pamAliveParts() {
        switch (this) {
            case NEWSPAPER: return new String[]{"_zombie_newspaper_hand"};
            default:        return new String[0];
        }
    }

    /**
     * Extra top-level parts that should only render at the critical (last)
     * damage layer. Like {@link #pamAliveParts()} they carry no ARMOR flag and
     * therefore need explicit visibility control.
     */
    public String[] pamCriticalParts() {
        switch (this) {
            case NEWSPAPER: return new String[]{"_zombie_newspaper_flame"};
            default:        return new String[0];
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
