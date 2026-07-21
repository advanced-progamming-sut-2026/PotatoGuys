package pvz.models.games.map.behaviors;

import pvz.models.entities.projectile.Projectile;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;
import pvz.models.games.card.PlantCard;
import pvz.models.games.map.tile.Tile;

public interface TileBehavior {
    default void onProjectileHit(Projectile p, Tile tile) {}
    default void onZombieEnter(Zombie z, Tile tile) {}
    default boolean canPlant(PlantCard p, Tile tile) { return true; }
    default void onTick(GameContext ctx, Tile tile) {}
    default String getStatus() {return null;}
    default String getName() { return "Behavior"; }
}
