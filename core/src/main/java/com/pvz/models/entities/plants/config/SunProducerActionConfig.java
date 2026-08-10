package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.entities.plants.data.ProductionKind;

public class SunProducerActionConfig extends PlantActionConfig {

    public ProductionKind productionKind;
    public float amount;
    public float[] amounts;
    public float[] stageIntervalSeconds;
    public Vector2 spawnOffset = new Vector2();
}
