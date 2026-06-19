package Models.Seasons.SeasonEffects;

import Models.Plants.Plant;
import Models.Seasons.Levels.GameMap;
import Models.Seasons.Levels.Tile;
import Models.Zombies.Zombie;

public interface SeasonEffect {
    void applyOnTile(Tile tile, GameMap map);

    void onZombieSpawn(Zombie z, int lane);

    void onPlantPlaced(Plant plant);
}
