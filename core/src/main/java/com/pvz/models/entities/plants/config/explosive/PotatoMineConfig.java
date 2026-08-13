package com.pvz.models.entities.plants.config.explosive;

import com.pvz.models.entities.plants.actions.explosive.potato_mine.ExplosionType;
import com.pvz.models.entities.plants.config.PlantActionConfig;

public class PotatoMineConfig extends PlantActionConfig {
    public float plantTime=0.97f;
    public float idleTime=15f;
    public float recoverTime=0.82f;
    public float explosionTime=0.67f;
    public String recoverClip="recover";
    public String readyClip="idle2";
    public String explosionClip="attack";
    public ExplosionType explosionType=ExplosionType.SMALL;
}
