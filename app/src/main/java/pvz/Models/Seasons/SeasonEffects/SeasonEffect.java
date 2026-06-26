package pvz.Models.Seasons.SeasonEffects;

import pvz.Models.Plants.Plant;
import pvz.Models.Seasons.Levels.GameMap;
import pvz.Models.Seasons.Levels.Tile;
import pvz.Models.Zombies.Zombie;

public interface SeasonEffect {
    void applyOnTile(Tile tile, GameMap map);

    void onZombieSpawn(Zombie z, int lane);

    void onPlantPlaced(Plant plant);
}
