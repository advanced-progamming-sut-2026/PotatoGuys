package pvz.Models.User;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.AppContext;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Zombies.ZombieType;

public class Collection {
    private News news;
    private List<MyPlant> unlockedPlants;
    private List<ZombieType> unlockedZombies;

    public Collection(News news){
        this.news=news;
        this.unlockedPlants=new ArrayList<>();
        this.unlockedZombies=new ArrayList<>();
    }

    public List<MyPlant> getUnlockedPlants() {
        return unlockedPlants;
    }

    public void setUnlockedPlants(List<MyPlant> unlockedPlants) {
        this.unlockedPlants = unlockedPlants;
    }

    public void unlockPlant(PlantType plant){
        if(unlockedPlants.stream().anyMatch(p->p.getType()==plant)) return;
        MyPlant newPlant = new MyPlant();
        newPlant.setType(plant);
        newPlant.setLevel(1);
        newPlant.setBoosted(false);
        unlockedPlants.add(newPlant);
        news.getMessages().add(new Message(plant.toString()+" has been unlocked!"));
    }

    public void unlockZombie(ZombieType type){
        if (unlockedZombies.contains(type)) return;
        unlockedZombies.add(type);
        news.getMessages().add(new Message(type.toString()+" has been unlocked!"));
    }

    public MyPlant getPlant(PlantType type){
        for (MyPlant p : unlockedPlants){
            if (p.getType() == type){
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
