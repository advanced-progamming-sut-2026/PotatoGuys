package pvz.Models.User;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.data.ZombiePropertySheet;

import java.util.ArrayList;
import java.util.List;

public class Collection {
    private List<PlantPropertySheet> unlockedPlants;
    private List<ZombiePropertySheet> unlockedZombies;

    public Collection(){
        this.unlockedPlants=new ArrayList<>();
        this.unlockedZombies=new ArrayList<>();
    }

    public List<PlantPropertySheet> getUnlockedPlants() {
        return unlockedPlants;
    }

    public void setUnlockedPlants(List<PlantPropertySheet> unlockedPlants) {
        this.unlockedPlants = unlockedPlants;
    }

    public List<ZombiePropertySheet> getUnlockedZombies() {
        return unlockedZombies;
    }

    public void setUnlockedZombies(List<ZombiePropertySheet> unlockedZombies) {
        this.unlockedZombies = unlockedZombies;
    }

    public int getPlantLevel(PlantType type){
        for (PlantPropertySheet p: unlockedPlants){
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

    public void addPlant(Plant plant){
        unlockedPlants.add(plant);
    }

    public Plant getPlant(PlantType type){
        for (Plant p : unlockedPlants){
            if (p.getType() == type){
                return p;
            }
        }
        return null;
    }
}
