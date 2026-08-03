package com.pvz.models.entities.zombies.data;

import java.util.List;

/**
 * Immutable POJO for a JSON {@code ArmorPropertySheet} object.
 *
 * <p>Stored in {@link ZombieRegistry} keyed by alias (e.g. {@code "ConeDefault"}).
 * The {@link pvz.models.entities.zombies.ZombieFactory} resolves each zombie's
 * {@code ZombieArmorProps} strings against this registry to build runtime
 * {@link pvz.models.entities.zombies.armor.ArmorPiece} components.
 */
public final class ArmorPropertySheet {

    private final String alias;
    private final String armorType;       // raw JSON value e.g. "Cone", "ShoulderArmor"
    private final float baseHealth;
    private final List<String> flags;     // raw flag strings e.g. ["damageable","helm"]
    private final float[] layerThresholds; // health-fraction thresholds e.g. [0.666, 0.333]

    public ArmorPropertySheet(String alias, String armorType, float baseHealth,
                               List<String> flags, float[] layerThresholds) {
        this.alias = alias;
        this.armorType = armorType;
        this.baseHealth = baseHealth;
        this.flags = List.copyOf(flags);
        this.layerThresholds = layerThresholds.clone();
    }

    public String getAlias()             { return alias; }
    public String getArmorType()         { return armorType; }
    public float getBaseHealth()         { return baseHealth; }
    public List<String> getFlags()       { return flags; }
    public float[] getLayerThresholds()  { return layerThresholds.clone(); }

    @Override
    public String toString() {
        return "ArmorSheet{alias='" + alias + "', type='" + armorType
                + "', hp=" + baseHealth + '}';
    }
}
