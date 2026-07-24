package pvz.models.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pvz.models.AppContext;
import pvz.models.entities.plants.enums.PlantType;
import pvz.models.entities.zombies.ZombieType;

public class Collection {
    private List<MyPlant> unlockedPlants = new ArrayList<>();
    private List<ZombieType> unlockedZombies = new ArrayList<>();
    private Map<PlantType, Integer> seedPackets = new HashMap<>();
    private transient News news;

    public Collection() {
    }

    public Collection(News news) {
        this.news = news;
    }

    public List<MyPlant> getUnlockedPlants() {
        return unlockedPlants;
    }

    public List<ZombieType> getUnlockedZombies() {
        return unlockedZombies;
    }

    public MyPlant getPlant(PlantType type) {
        for (MyPlant p : unlockedPlants) {
            if (p.getType() == type)
                return p;
        }
        return null;
    }

    public void unlockPlant(PlantType type) {
        if (getPlant(type) == null) {
            MyPlant newPlant = new MyPlant();
            newPlant.setType(type);
            newPlant.setLevel(1);
            newPlant.setBoosted(false);
            unlockedPlants.add(newPlant);

            // ---> NEW: Send a News Message! <---
            User user = AppContext.getInstance().getCurrentUser();
            if (user != null && user.getProfile().getNews() != null) {
                user.getProfile().getNews().getMessages().add(
                        new Message("New Plant Unlocked: " + type.name() + "!"));
            }
        }
    }

    public void unlockZombie(ZombieType type) {
        if (!unlockedZombies.contains(type)) {
            unlockedZombies.add(type);

            // ---> NEW: Send a News Message! <---
            User user = AppContext.getInstance().getCurrentUser();
            if (user != null && user.getProfile().getNews() != null) {
                user.getProfile().getNews().getMessages().add(
                        new Message("New Zombie Encountered: " + type.name() + "!"));
                // We purposefully do NOT call user.saveUser() here because this happens
                // mid-game, and we want to avoid disk-lag. The game naturally saves on
                // Win/Loss.
            }
        }
    }

    public int getSeedPackets(PlantType type) {
        return seedPackets.getOrDefault(type, 0);
    }

    public void addSeedPackets(PlantType type, int amount) {
        seedPackets.put(type, getSeedPackets(type) + amount);
    }

    public void consumeSeedPackets(PlantType type, int amount) {
        seedPackets.put(type, Math.max(0, getSeedPackets(type) - amount));
    }
}