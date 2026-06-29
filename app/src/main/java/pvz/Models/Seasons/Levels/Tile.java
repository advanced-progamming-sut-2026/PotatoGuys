package pvz.Models.Seasons.Levels;

import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Zombies.Zombie;

public class Tile {
    private TileType type;
    private int health;
    private int freezeLevel;
    private Plant plant;
    private List<Zombie> zombies;
    private boolean isFrozenPlant;

    public boolean isPlantable(){
        return true;
    }

    public void onZombieStep(Zombie z){

    }

    public TileType getType() {
        return type;
    }

    public int getHealth() {
        return health;
    }

    public int getFreezeLevel() {
        return freezeLevel;
    }

    public Plant getPlant() {
        return plant;
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public boolean isFrozenPlant() {
        return isFrozenPlant;
    }
}
