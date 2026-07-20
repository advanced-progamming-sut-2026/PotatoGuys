package pvz.Models.Games.map.behaviors;

import pvz.Models.Entities.Projectile.Projectile;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.card.PlantCard;
import pvz.Models.Games.map.tile.Tile;

public interface TileBehavior {
    default void onProjectileHit(Projectile p, Tile tile) {}
    default void onZombieEnter(Zombie z, Tile tile) {}
    default boolean canPlant(PlantCard p, Tile tile) { return true; }
    default void onTick(GameContext ctx, Tile tile) {}
    default String getStatus() {return null;}
    default String getName() { return "Behavior"; }
}
