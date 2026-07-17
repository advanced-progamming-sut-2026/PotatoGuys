package pvz.Models.Games.map;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.Enums.PlantTag;

public class Tile {
    private TileType type;
    private List<Plant> plants;
    // private List<Zombie> zombies;

    public Tile(){
        // zombies=new ArrayList<>();
        plants=new ArrayList<>();
        this.type = TileType.NORMAL;
    }

    public boolean isPlantable(Plant newPlant) {
        if(plants.isEmpty()){
            if(type == TileType.ICE || type == TileType.SLIP_DOWN || type == TileType.SLIP_UP || type == TileType.GRAVE)
                return false;
            if(type == TileType.WATER && !newPlant.getSheet().getTags().contains(PlantTag.WATER))
                return false;
        }

        if(!plants.isEmpty() && !newPlant.getSheet().getTags().contains(PlantTag.STACK)){
            return false;
        }
        return true;
    }

    public void setType(TileType type) {
        this.type = type;
    }
    
    public TileType getType() {
        return type;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void addPlant(Plant plant) {
        if(this.isPlantable(plant) && !plants.contains(plant))
            plants.add(plant);
    }

    public void removePlant(Plant plant) {
        plants.remove(plant);
    }

    // public List<Zombie> getZombies() {
    //     return zombies;
    // }

    // public void addZombie(Zombie zombie) {
    //     if(!zombies.contains(zombie))
    //         zombies.add(zombie);
    // }

    // public void removeZombie(Zombie zombie) {
    //     zombies.remove(zombie);
    // }

}
