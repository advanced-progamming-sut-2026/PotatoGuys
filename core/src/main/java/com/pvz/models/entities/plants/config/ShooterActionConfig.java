package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.pvz.models.entities.projectile.ProjectileType;

/**
 * Data-driven behavior for {@code SHOOTER}-category plants: an ordered set of
 * projectile firing patterns, each independently offset/aimed/delayed. Multiple
 * entries in {@link #patterns} express multi-shot (Repeater), multi-lane
 * (Threepeater), multi-directional (Rotobaga/Starfruit) and stacked (Pea Pod)
 * behaviors without any per-plant Java code.
 */
public class ShooterActionConfig extends PlantActionConfig {

    public Array<ProjectilePattern> patterns = new Array<>();

    /** One projectile fired as part of a shooter's action; velocity/offset are in grid-tile units. */
    public static class ProjectilePattern {
        public ProjectileType projectileType;
        public float damage;
        public int laneOffset;
        /** When true, fires this pattern in every lane on the map and ignores {@link #laneOffset} (e.g. Threepeater's Plant Food). */
        public boolean allLanes;
        public Vector2 positionOffset = new Vector2();
        public Vector2 velocity = new Vector2();
        public float delaySeconds;
    }
}
