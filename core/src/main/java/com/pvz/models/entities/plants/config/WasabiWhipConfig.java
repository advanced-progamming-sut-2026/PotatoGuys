package com.pvz.models.entities.plants.config;

public class WasabiWhipConfig extends PlantActionConfig {
    public float intervalSeconds = 2.0f;
    public float baseDamage = 40.0f;
    public float attackRangeFactor = 1.6f;

    public String attackFrontClip = "attack";
    public String attackBackClip = "attack2";
    public String attackBothClip = "attack3";

    public String pfOnClip = "plantfood_on";
    public String pfClip = "plantfood";
    public String pfOffClip = "plantfood_off";
    public int pfRadius = 2;
    public float pfDuration = 1.0f;
}
