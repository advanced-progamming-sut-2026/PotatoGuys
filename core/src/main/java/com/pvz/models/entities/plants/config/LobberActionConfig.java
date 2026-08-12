package com.pvz.models.entities.plants.config;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.pvz.models.entities.projectile.ProjectileType;

/**
 * Attack payload for lobber plants (Cabbage-pult, Kernel-pult, Melon-pult family).
 * Mirrors {@link ShooterActionConfig} but for parabolic lobbed projectiles: instead
 * of a fixed velocity each {@link LobPattern} aims at one or more random zombies
 * (see {@link LobPattern#targets}) and lets {@code ProjectileFactory} decide the
 * arc, splash and status effect from the {@code projectileType}.
 */
public class LobberActionConfig extends PlantActionConfig {

    public float intervalSeconds;
    public Array<LobPattern> patterns = new Array<>();

    public static class LobPattern {
        public ProjectileType projectileType;
        public float damage;
        /** How many random zombies to lob at; values &lt;= 0 lob at every zombie in range. */
        public int targets = 1;
        /** When true the range covers every zombie on the field instead of just the plant's lane. */
        public boolean allLanes;
        public Vector2 positionOffset = new Vector2();
        public float delaySeconds;
        /** Spawn chance 0..1 — a pattern with chance &lt; 1 fires only sometimes (e.g. Kernel-pult's butter). */
        public float chance = 1f;
    }
}
