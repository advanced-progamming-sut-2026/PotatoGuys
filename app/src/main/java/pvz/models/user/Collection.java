package pvz.models.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pvz.models.entities.plants.enums.PlantType;
import pvz.models.entities.zombies.ZombieType;

public class Collection {
    private List<MyPlant> unlockedPlants = new ArrayList<>();
    private List<ZombieType> unlockedZombies = new ArrayList<>();

    // Tracks the amount of seed packets owned for each specific plant
    private Map<PlantType, Integer> seedPackets = new HashMap<>();

    // --- FIX 1: Restored the constructor that your Profile class expects ---
    private transient News news;

    public Collection() {}

    public Collection(News news) {
        this.news = news;
    }

    public List<MyPlant> getUnlockedPlants() { return unlockedPlants; }
    public List<ZombieType> getUnlockedZombies() { return unlockedZombies; }

    public MyPlant getPlant(PlantType type) {
        for (MyPlant p : unlockedPlants) {
            if (p.getType() == type) return p;
        }
        return null;
    }

    public void unlockPlant(PlantType type) {
        if (getPlant(type) == null) {
            // --- FIX 2: Using the empty constructor for MyPlant to match your existing class ---
            MyPlant newPlant = new MyPlant();
            newPlant.setType(type);
            newPlant.setLevel(1);
            newPlant.setBoosted(false);
            unlockedPlants.add(newPlant);
        }
    }

    public void unlockZombie(ZombieType type) {
        if (!unlockedZombies.contains(type)) {
            unlockedZombies.add(type);
            // If you originally added a news message here, you can do:
            // if (news != null) news.getMessages().add(new Message("You encountered a new zombie: " + type));
        }
    }

    // --- Seed Packet Management ---

    public int getSeedPackets(PlantType type) {
        return seedPackets.getOrDefault(type, 0);
    }

    public void addSeedPackets(PlantType type, int amount) {
        seedPackets.put(type, getSeedPackets(type) + amount);
    }

    public void consumeSeedPackets(PlantType type, int amount) {
        int current = getSeedPackets(type);
        seedPackets.put(type, Math.max(0, current - amount));
    }
}
