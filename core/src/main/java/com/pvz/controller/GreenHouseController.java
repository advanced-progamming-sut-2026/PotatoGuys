package com.pvz.controller;

import java.util.ArrayList;
import java.util.List;

import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.greenhouse.GreenHousePlant;
import com.pvz.models.greenhouse.GreenHousePot;
import com.pvz.models.greenhouse.HarvestResult;
import com.pvz.models.user.MyPlant;
import com.pvz.models.user.User;

/**
 * Backs the GreenHouseMenu screen. The previous version of this class was written for the
 * old text/console menu system (Result/Matcher based) and was fully commented out, so
 * nothing here was actually reachable from the game before. Rewritten with plain methods
 * a Scene2D screen can call directly.
 */
public class GreenHouseController {

    public GreenHouse getGreenHouse() {
        User user = getCurrentUser();
        GreenHouse gh = AppContext.getInstance().getGreenHouse();
        if (gh == null && user != null) {
            gh = user.getGreenHouse();
        }
        if (gh == null) {
            gh = new GreenHouse();
        }
        if (user != null) {
            user.setGreenHouse(gh);
        }
        AppContext.getInstance().setGreenHouse(gh);
        return gh;
    }

    private User getCurrentUser() {
        return AppContext.getInstance().getCurrentUser();
    }

    private List<String> getUnlockedPlantNames() {
        List<String> names = new ArrayList<>();
        User user = getCurrentUser();
        if (user != null && user.getProfile().getCollection() != null) {
            for (MyPlant p : user.getProfile().getCollection().getUnlockedPlants()) {
                if (p.getType() != null) {
                    names.add(p.getType().name());
                }
            }
        }
        return names;
    }

    /** Plants a random seed (mostly MariGold, occasionally an unlocked plant) into an empty, unlocked pot. */
    public boolean plant(int x, int y) {
        GreenHouse greenHouse = getGreenHouse();
        if (!greenHouse.isValidCoordinate(x, y)) return false;

        GreenHousePot pot = greenHouse.getPot(x, y);
        if (pot == null || pot.isLocked() || !pot.isEmpty()) return false;

        List<String> unlockedPlants = getUnlockedPlantNames();
        boolean planted = greenHouse.plantRandomPotAt(x, y, unlockedPlants);

        User user = getCurrentUser();
        if (planted && user != null) user.saveUser();
        return planted;
    }

    /** Harvests a ready plant: MariGold pays coins, an unlocked plant stores a boost. Returns a HarvestResult describing the prize, or null. */
    public HarvestResult collect(int x, int y) {
        GreenHouse greenHouse = getGreenHouse();
        if (!greenHouse.isValidCoordinate(x, y)) return null;

        GreenHousePlant plant = greenHouse.collect(x, y);
        if (plant == null) return null;

        User user = getCurrentUser();
        if (user == null) return HarvestResult.marigoldCoins(GreenHousePlant.MARIGOLD_REWARD);

        if (plant.isMariGold()) {
            user.getProfile().setCoins(user.getProfile().getCoins() + GreenHousePlant.MARIGOLD_REWARD);
            user.saveUser();
            return HarvestResult.marigoldCoins(GreenHousePlant.MARIGOLD_REWARD);
        }

        String plantTypeName = plant.getPlantType();
        PlantType plantType = null;
        for (PlantType pt : PlantType.values()) {
            if (pt.name().equals(plantTypeName)) {
                plantType = pt;
                break;
            }
        }

        if (plantType == null) {
            user.saveUser();
            return HarvestResult.newBoost(plantTypeName);
        }

        MyPlant myPlant = user.getProfile().getCollection().getPlant(plantType);
        if (myPlant == null) {
            user.saveUser();
            return HarvestResult.newBoost(plantTypeName);
        }

        HarvestResult result;
        if (!myPlant.isBoosted()) {
            myPlant.setBoosted(true);
            result = HarvestResult.newBoost(plantTypeName);
        } else {
            result = HarvestResult.alreadyBoosted(plantTypeName);
        }
        user.saveUser();
        return result;
    }

    /**
     * Returns how many diamonds it would cost to instantly finish the plant in pot (x, y),
     * or -1 if there is nothing growable there (locked / empty / already ready).
     */
    public int growNowCost(int x, int y) {
        GreenHousePot pot = growablePot(x, y);
        if (pot == null) return -1;
        return pot.getPlant().remainingHours();
    }

    /**
     * Instantly finishes growth for a diamond cost equal to the remaining hours.
     * Returns true only if it actually grew (i.e. the pot had a growing plant and
     * the user could afford the cost, which is then deducted and the save written).
     */
    public boolean growNow(int x, int y) {
        GreenHousePot pot = growablePot(x, y);
        if (pot == null) return false;

        int cost = pot.getPlant().remainingHours();
        User user = getCurrentUser();
        if (user == null || user.getProfile().getDiamonds() < cost) return false;

        user.getProfile().setDiamonds(user.getProfile().getDiamonds() - cost);
        getGreenHouse().grow(x, y);
        user.saveUser();
        return true;
    }

    /** The pot at (x, y) if it holds a plant that is not finished growing yet, else null. */
    private GreenHousePot growablePot(int x, int y) {
        GreenHouse greenHouse = getGreenHouse();
        if (!greenHouse.isValidCoordinate(x, y)) return null;

        GreenHousePot pot = greenHouse.getPot(x, y);
        if (pot == null || pot.isLocked() || pot.isEmpty()) return null;

        if (pot.getPlant().isReady()) return null;
        return pot;
    }
}
