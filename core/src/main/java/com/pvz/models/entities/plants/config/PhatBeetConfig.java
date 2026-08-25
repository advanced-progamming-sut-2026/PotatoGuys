package com.pvz.models.entities.plants.config;

public class PhatBeetConfig extends PlantActionConfig {
    public float intervalSeconds = 2.0f;
    public float baseDamage = 15.0f;
    public float attackRangeFactor = 1.6f;
    public int attackRadius = 1;

    public String effectPamPath = "768/FULL/EFFECTS/PHATBEETS_ATTACK_PULSE/PHATBEETS_ATTACK_PULSE.PAM";
    public String effectClip = "animation";
    public float effectScale = 0.65f;

    public String tileHitPamPath = "768/FULL/EFFECTS/PHATBEETS_TILE_HIT_SMALL/PHATBEETS_TILE_HIT_SMALL.PAM";
    public String tileHitClip = "animation";
    public float tileHitScale = 0.5f;

    public float pfDamage = 30.0f;
    public float pfIntervalSeconds = 2.0f;
    public int pfRadius = 2;
    public String pfEffectPamPath = "768/FULL/EFFECTS/PHATBEETS_PF_PULSE/PHATBEETS_PF_PULSE.PAM";
    public String pfEffectClip = "animation";
}
