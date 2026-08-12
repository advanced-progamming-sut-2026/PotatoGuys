package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.entities.plants.data.ProductionKind;

public class SunProducerActionConfig extends PlantActionConfig {

    /** Seconds between production cycles (0 = produce immediately / one-shot). */
    public float intervalSeconds;
    public ProductionKind productionKind;
    public float amount;
    public float[] amounts;
    public float[] stageIntervalSeconds;
    public Vector2 spawnOffset = new Vector2();
    /** Sun pops out this many seconds into the attack/feed animation (defaults to 0 = instantly). */
    public float delaySeconds = 0f;
}
