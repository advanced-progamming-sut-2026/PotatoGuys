package pvz.Models.Games.map;

import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Zombies.Zombie;

public class Tile {
    private TileType type;
    private List<Plant> plants;
    private List<Zombie> zombies;
    private boolean isFrozenPlant;
    private boolean plantable;

    public boolean isPlantable(){
        return plantable;
    }

    public void onZombieStep(Zombie z){

    }

    public TileType getType() {
        return type;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void addPlant(Plant plant) {
        if(this.isPlantable())
        plants.add(plant);
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public boolean isFrozenPlant() {
        return isFrozenPlant;
    }
}
