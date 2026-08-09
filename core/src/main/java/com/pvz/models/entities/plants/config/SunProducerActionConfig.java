package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.math.Vector2;
import com.pvz.models.entities.plants.data.ProductionKind;

/**
 * Data-driven behavior for {@code SUN_PRODUCER}-category plants.
 * <ul>
 *   <li>{@code FIXED} — produces {@link #amount} every action interval (Sunflower).</li>
 *   <li>{@code STAGED} — produces {@link #amounts}[stage] as the plant grows,
 *       each stage unlocking after the matching entry in {@link #stageIntervalSeconds} (Sun-shroom).</li>
 *   <li>{@code ONESHOT} — produces {@link #amount} once, immediately (Gold Bloom).</li>
 * </ul>
 */
public class SunProducerActionConfig extends PlantActionConfig {

    public ProductionKind productionKind;
    public float amount;
    public float[] amounts;
    public float[] stageIntervalSeconds;
    public Vector2 spawnOffset = new Vector2();
}
