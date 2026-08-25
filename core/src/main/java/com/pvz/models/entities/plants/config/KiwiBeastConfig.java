package com.pvz.models.entities.plants.config;

public class KiwiBeastConfig extends PlantActionConfig {
    public float intervalSeconds = 2.0f;
    public float baseDamage = 15.0f;
    public float attackRangeFactor = 1.6f;
    public int attackRadius = 1;

    public String effectPamPath = "768/INITIAL/EFFECTS/KIWIBEAST_ATTACK_PULSE/KIWIBEAST_ATTACK_PULSE.PAM";
    public String effectClip = "animation";
    public float effectScale = 0.65f;

    public String tileHitPamPath = "768/INITIAL/EFFECTS/KIWIBEAST_ATTACK_PULSE/KIWIBEAST_ATTACK_PULSE.PAM";
    public String tileHitClip = "animation";
    public float tileHitScale = 0.5f;

    public String[] idleLabels = {"idle_stage1_", "idle_stage2_", "idle_stage3_"};
    public String[] attackLabels = {"attack_stage1", "attack_stage2", "attack_stage3"};
    public String[] growthLabels = {"growth_stage1", "growth_stage2"};
    public float[] stageIntervals = {24.0f, 72.0f};
    public float[] damageMultipliers = {1.0f, 2.0f, 3.0f};

    public String pfGrowthClip = "growth_stage1_2";
    public String pfAttackClip = "plantfood_stage3";
    public String pfEffectPamPath = "768/INITIAL/EFFECTS/KIWIBEAST_PF_PULSE/KIWIBEAST_PF_PULSE.PAM";
    public String pfEffectClip = "animation";
    public int pfRadius = 2;
    public float[] pfHitTimes = {0.5f, 1.27f, 2.27f};
}
