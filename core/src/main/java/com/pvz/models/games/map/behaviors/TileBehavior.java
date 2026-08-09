package com.pvz.models.games.map.behaviors;

import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;

public interface TileBehavior {
    default void onProjectileHit(Projectile p, Tile tile) {}
    default void onZombieEnter(Zombie z, Tile tile) {}
    default boolean canPlant(PlantCard p, Tile tile) { return true; }
    default void update(GameContext ctx, Tile tile, float dt) {}
    default String getStatus() {return null;}
    default String getName() { return "Behavior"; }
}
