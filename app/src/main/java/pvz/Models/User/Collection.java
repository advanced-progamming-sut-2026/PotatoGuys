package pvz.Models.User;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Zombies.Zombie;

import java.util.List;

public class Collection {
    private List<Plant> unlockedPlants;
    private List<Zombie> unlockedZombies;

    public List<Plant> getUnlockedPlants() {
        return unlockedPlants;
    }

    public void setUnlockedPlants(List<Plant> unlockedPlants) {
        this.unlockedPlants = unlockedPlants;
    }

    public List<Zombie> getUnlockedZombies() {
        return unlockedZombies;
    }

    public void setUnlockedZombies(List<Zombie> unlockedZombies) {
        this.unlockedZombies = unlockedZombies;
    }

    public int getPlantLevel(PlantType type){
        for (Plant p: unlockedPlants){
            if (p.getType()==type){
                return p.getLevel();
            }
        }
        return 1;
    }

    public boolean isPlantBoosted(PlantType type){
        for (Plant p:unlockedPlants){
            if (p.getType()==type){
                return p.isBoosted();
            }
        }
        return false;
    }
}
