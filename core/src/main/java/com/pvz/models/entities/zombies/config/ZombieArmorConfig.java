package com.pvz.models.entities.zombies.config;

import java.util.List;

/** Armour definition referenced by a zombie's {@code armorAliases}. */
public class ZombieArmorConfig {

    public String alias;
    public String type;
    public float baseHealth;
    public List<String> flags;
    public float[] layerThresholds;
}
