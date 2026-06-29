package pvz.Models.Seasons.SeasonEffects;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Seasons.Levels.GameMap;
import pvz.Models.Seasons.Levels.Tile;

public interface SeasonEffect {
    void applyOnTile(Tile tile, GameMap map);

    void onZombieSpawn(Zombie z, int lane);

    void onPlantPlaced(Plant plant);
}
