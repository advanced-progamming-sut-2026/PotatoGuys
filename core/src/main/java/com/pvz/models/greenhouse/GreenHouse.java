package com.pvz.models.greenhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GreenHouse {

    public static final int WIDTH = 4;
    public static final int HEIGHT = 3;

    private List<GreenHousePot> greenHousePots = new ArrayList<>();
    private transient final Random random = new Random();

    public GreenHouse() {
        for (int y = 1; y <= HEIGHT; y++) {
            for (int x = 1; x <= WIDTH; x++) {
                boolean locked = y >= 2;
                greenHousePots.add(new GreenHousePot(x, y, locked));
            }
        }
    }

    public GreenHousePot getPot(int x, int y) {
        for (GreenHousePot greenHousePot : greenHousePots) {
            if (greenHousePot.getX() == x && greenHousePot.getY() == y) {
                return greenHousePot;
            }
        }

        return null;
    }

    public List<GreenHousePot> getGreenHousePots() {
        return greenHousePots;
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 1 && x <= WIDTH && y >= 1 && y <= HEIGHT;
    }

    public boolean unlockPot(int x, int y) {
        GreenHousePot pot = getPot(x, y);

        if (pot == null || !pot.isLocked()) {
            return false;
        }

        pot.unlock();
        return true;
    }

    /**
     * Unlocks one random still-locked pot (used for the zombie "free pot" drop).
     *
     * @return the pot that was unlocked, or {@code null} if every pot is already unlocked.
     */
    public GreenHousePot unlockRandomPot() {
        List<GreenHousePot> lockedPots = new ArrayList<>();
        for (GreenHousePot pot : greenHousePots) {
            if (pot.isLocked()) {
                lockedPots.add(pot);
            }
        }

        if (lockedPots.isEmpty()) {
            return null;
        }

        GreenHousePot chosen = lockedPots.get(random.nextInt(lockedPots.size()));
        chosen.unlock();
        return chosen;
    }

    public int getUnlockedPotCount() {
        int count = 0;
        for (GreenHousePot pot : greenHousePots) {
            if (!pot.isLocked() && isValidCoordinate(pot.getX(), pot.getY())) {
                count++;
            }
        }
        return count;
    }

    public boolean plantPotAt(int x, int y, GreenHousePlant plant) {
        GreenHousePot pot = getPot(x, y);

        if (pot == null || pot.isLocked() || !pot.isEmpty() || plant == null) {
            return false;
        }

        pot.setPlant(plant);
        return true;
    }

    public boolean plantRandomPotAt(int x, int y, List<String> unlockedPlantTypesWithPlantFood) {
        GreenHousePlant plant = createRandomPlant(unlockedPlantTypesWithPlantFood);
        return plantPotAt(x, y, plant);
    }

    public GreenHousePlant collect(int x, int y) {
        GreenHousePot pot = getPot(x, y);

        if (pot == null || pot.isLocked() || pot.isEmpty()) {
            return null;
        }

        GreenHousePlant plant = pot.getPlant();

        if (!plant.isReady()) {
            return null;
        }

        pot.clearPlant();
        return plant;
    }

    public boolean grow(int x, int y) {
        GreenHousePot pot = getPot(x, y);

        if (pot == null || pot.isLocked() || pot.isEmpty()) {
            return false;
        }

        GreenHousePlant plant = pot.getPlant();

        if (plant.isReady()) {
            return false;
        }

        plant.makeReadyNow();
        return true;
    }

    private GreenHousePlant createRandomPlant(List<String> unlockedPlantTypesWithPlantFood) {
        boolean shouldPlantMariGold = random.nextBoolean();

        if (shouldPlantMariGold || unlockedPlantTypesWithPlantFood == null || unlockedPlantTypesWithPlantFood.isEmpty()) {
            return GreenHousePlant.createMariGold();
        }

        int index = random.nextInt(unlockedPlantTypesWithPlantFood.size());
        return GreenHousePlant.createUnlocked(unlockedPlantTypesWithPlantFood.get(index));
    }
}
