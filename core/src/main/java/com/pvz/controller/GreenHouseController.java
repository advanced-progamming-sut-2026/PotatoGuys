package com.pvz.controller;

import java.util.ArrayList;
import java.util.List;

import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.greenhouse.GreenHousePlant;
import com.pvz.models.greenhouse.GreenHousePot;
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

    /** Harvests a ready plant: MariGold pays coins, an unlocked plant stores a boost. Returns what was collected, or null. */
    public GreenHousePlant collect(int x, int y) {
        GreenHouse greenHouse = getGreenHouse();
        if (!greenHouse.isValidCoordinate(x, y)) return null;

        GreenHousePlant plant = greenHouse.collect(x, y);
        if (plant == null) return null;

        User user = getCurrentUser();
        if (user == null) return plant;

        if (plant.isMariGold()) {
            user.getProfile().setCoins(user.getProfile().getCoins() + GreenHousePlant.MARIGOLD_REWARD);
        } else {
            String plantTypeName = plant.getPlantType();
            PlantType plantType = null;
            for (PlantType pt : PlantType.values()) {
                if (pt.name().equals(plantTypeName)) {
                    plantType = pt;
                    break;
                }
            }
            if (plantType != null) {
                MyPlant myPlant = user.getProfile().getCollection().getPlant(plantType);
                if (myPlant != null && !myPlant.isBoosted()) {
                    myPlant.setBoosted(true);
                }
            }
        }
        user.saveUser();
        return plant;
    }

    /** Instantly finishes growth for a diamond cost equal to the remaining hours. Returns false if it can't afford it / nothing to grow. */
    public boolean growNow(int x, int y) {
        GreenHouse greenHouse = getGreenHouse();
        if (!greenHouse.isValidCoordinate(x, y)) return false;

        GreenHousePot pot = greenHouse.getPot(x, y);
        if (pot == null || pot.isLocked() || pot.isEmpty()) return false;

        GreenHousePlant plant = pot.getPlant();
        if (plant.isReady()) return false;

        int cost = plant.remainingHours();
        User user = getCurrentUser();
        if (user == null || user.getProfile().getDiamonds() < cost) return false;

        user.getProfile().setDiamonds(user.getProfile().getDiamonds() - cost);
        greenHouse.grow(x, y);
        user.saveUser();
        return true;
    }
}
