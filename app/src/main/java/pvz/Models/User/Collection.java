package pvz.Models.User;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Zombies.ZombieType;

public class Collection {
    private List<MyPlant> unlockedPlants;
    private List<ZombieType> unlockedZombies;

    public Collection(){
        this.unlockedPlants=new ArrayList<>();
        this.unlockedZombies=new ArrayList<>();
    }

    public List<MyPlant> getUnlockedPlants() {
        return unlockedPlants;
    }

    public void setUnlockedPlants(List<MyPlant> unlockedPlants) {
        this.unlockedPlants = unlockedPlants;
    }

    public void addPlant(PlantType plant , int level , boolean isBoost){
        MyPlant newPlant = new MyPlant();
        newPlant.Type = plant;
        newPlant.level = level;
        newPlant.isBoost = isBoost;
        unlockedPlants.add(newPlant);
    }

    public MyPlant getPlant(PlantType type){
        for (MyPlant p : unlockedPlants){
            if (p.Type == type){
                return p;
            }
        }
        return null;
    }

    public List<ZombieType> getUnlockedZombies() {
        return unlockedZombies;
    }

    public void setUnlockedZombies(List<ZombieType> unlockedZombies) {
        this.unlockedZombies = unlockedZombies;
    }
}
