package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.math.Vector2;

/**
 * Dedicated config for the Sun Bean plant (id 51).
 *
 * <p>Sun Bean behaves like a wall-nut (idle/damage clips swap as it loses HP) but
 * instead of reflecting damage it drops a visible sun on every bite. The
 * {@link #spawnOffset} lifts the dropped sun above the plant so it stays visible
 * and collectible instead of hiding behind the plant sprite.
 */
public class SunBeanConfig extends PlantActionConfig {
    public String idleClip = "idle";
    public String damage1Clip = "damage";
    public String damage2Clip = "damage2";
    public String damage3Clip = "damage3";

    /** Sun amount dropped per bite while being eaten. */
    public int sunOnHit = 5;

    /** Lifts the dropped sun above the plant so it remains visible and clickable. */
    public Vector2 spawnOffset = new Vector2(0f, 0.35f);
}
