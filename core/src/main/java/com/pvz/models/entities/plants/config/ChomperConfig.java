package com.pvz.models.entities.plants.config;

public class ChomperConfig extends PlantActionConfig {
    public float digestionSeconds = 40.0f;
    public float biteRangeFactor = 1.6f;

    public String biteClip = "bite";
    public String specialClip = "special";
    public String digestClip = "special_idle";
    public String digestEndClip = "special_end";

    public String pfOnClip = "plantfood_on";
    public String pfClip = "plantfood";
    public String pfOffClip = "plantfood_off";
    public String pfBurpClip = "plantfood_burp";
    public String pfBurpEndClip = "plantfood_burp_end";
    public int pfMaxTargets = 3;
    public float pfPullSpeed = 300.0f;
}
