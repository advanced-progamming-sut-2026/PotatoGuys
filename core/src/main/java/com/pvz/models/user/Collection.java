package com.pvz.models.user;

import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.zombies.ZombieType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Collection {
    private List<MyPlant> unlockedPlants = new ArrayList<>();
    private List<ZombieType> unlockedZombies = new ArrayList<>();
    private Map<PlantType, Integer> seedPackets = new HashMap<>();

    /**
     * True once legacy {@link MyPlant#getSeed()} values have been folded into the
     * {@link #seedPackets} map. The plant's own {@code seed} field used to be a second,
     * parallel packet counter (written by old shop items and editable in the save JSON),
     * but the Collection UI and level-ups only ever read the map — so those packets were
     * invisible. Gson resets this flag to {@code false} on load, which triggers the
     * one-time migration below.
     */
    private transient boolean packetsMigrated = false;

    /** Folds any leftover {@link MyPlant#getSeed()} packets into the map and zeroes the field. */
    private void migratePackets() {
        if (packetsMigrated) return;
        packetsMigrated = true;
        for (MyPlant p : unlockedPlants) {
            int seed = p.getSeed();
            if (seed > 0 && !seedPackets.containsKey(p.getType())) {
                seedPackets.put(p.getType(), seed);
                p.setSeed(0);
            }
        }
    }

    public Collection() {
    }

    public Collection(News news) {
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
        migratePackets();
        return seedPackets.getOrDefault(type, 0);
    }

    public void addSeedPackets(PlantType type, int amount) {
        seedPackets.put(type, getSeedPackets(type) + amount);
    }

    public void consumeSeedPackets(PlantType type, int amount) {
        seedPackets.put(type, Math.max(0, getSeedPackets(type) - amount));
    }
}
