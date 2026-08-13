package com.pvz.models.entities.zombies.config;

/** One entry of a {@code scalingPresets} recipe (mirrors {@code ScaledProp}). */
public class ScaledPropConfig {

    public String key;      // "Hitpoints" | "EatDPS" | "Speed" | "SmashDamage" …
    public String formula;  // "standard" | "constant"
    public float arg1;
    public float arg2;
}
