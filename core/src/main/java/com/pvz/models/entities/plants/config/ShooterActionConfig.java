package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.pvz.models.entities.projectile.ProjectileType;

public class ShooterActionConfig extends PlantActionConfig {

    public float intervalSeconds;
    public Array<ProjectilePattern> patterns = new Array<>();

    public static class ProjectilePattern {
        public ProjectileType projectileType;
        public float damage;
        public int laneOffset;
        public boolean allLanes;
        public Vector2 positionOffset = new Vector2();
        public Vector2 velocity = new Vector2();
        public float delaySeconds;
    }
}
