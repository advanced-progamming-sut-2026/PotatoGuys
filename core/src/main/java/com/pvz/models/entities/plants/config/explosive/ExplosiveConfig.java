package com.pvz.models.entities.plants.config.explosive;

import com.pvz.models.entities.plants.actions.explosive.ExplosionIntensity;
import com.pvz.models.entities.plants.actions.explosive.ExplosionType;
import com.pvz.models.entities.plants.config.PlantActionConfig;

public class ExplosiveConfig extends PlantActionConfig {
    public float plantTime=0.97f;
    public float idleTime=15f;
    public float recoverTime=0.82f;
    public float explosionTime=0.67f;
    public float damageDealTime=0.67f;
    public float effectDuration=2f;
    public float baseDamage=1800f;
    public float radarRangeCoefficient=1;
    public String plantClip="plant";
    public String idleClip="plant_idle";
    public String recoverClip="recover";
    public String readyClip="idle2";
    public String explosionClip="attack";
    public ExplosionType explosionType=ExplosionType.POTATO_MINE;
    public ExplosionIntensity explosionIntensity=ExplosionIntensity.LOW;
}
