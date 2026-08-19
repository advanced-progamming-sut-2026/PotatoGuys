package com.pvz.controller;

import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.data.PlantPropertySheet;
import com.pvz.models.entities.plants.config.PlantConfigRegistry;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.user.Collection;
import com.pvz.models.user.MyPlant;
import com.pvz.models.user.Profile;
import com.pvz.models.user.User;

/**
 * Controller for the Collection (Almanac) screen. Owns every rule the UI must never
 * decide on its own: how much an unlock costs, what happens when the player cannot
 * afford it, and persisting the result to the save.
 */
public class CollectionController {

    /** Flat coin cost to unlock a locked plant. */
    public static final int PURCHASE_COIN_COST = 2000;

    /**
     * Seed packets required to advance an owned plant from {@code currentLevel} to the next one.
     * Level 1→2 costs 5, 2→3 costs 10, 3→4 costs 20, then 20 per level beyond that.
     */
    public static int requiredPacketsForLevel(int currentLevel) {
        int targetLevel = currentLevel + 1;
        switch (targetLevel) {
            case 2: return 5;
            case 3: return 10;
            case 4: return 20;
            default: return 20 * Math.max(1, targetLevel - 3);
        }
    }

    /**
     * Coins required to advance an owned plant from {@code currentLevel} to the next one.
     * Level 1→2 costs 1000, 2→3 costs 2000, 3→4 costs 4000, doubling after that.
     */
    public static int requiredCoinsForLevel(int currentLevel) {
        int targetLevel = currentLevel + 1;
        return 1000 * (int) Math.pow(2, Math.max(0, targetLevel - 2));
    }

    private User user() {
        return AppContext.getInstance().getCurrentUser();
    }

    private Profile profile() {
        User user = user();
        return user != null ? user.getProfile() : null;
    }

    private Collection collection() {
        Profile profile = profile();
        return profile != null ? profile.getCollection() : null;
    }

    /** Current coin balance, or 0 when no user is logged in. */
    public int getCoins() {
        Profile profile = profile();
        return profile != null ? profile.getCoins() : 0;
    }

    /** Current diamond (gem) balance, or 0 when no user is logged in. */
    public int getDiamonds() {
        Profile profile = profile();
        return profile != null ? profile.getDiamonds() : 0;
    }

    /**
     * Unlocks a locked plant by spending {@link #PURCHASE_COIN_COST} coins.
     *
     * @return {@code null} on success, or an error message explaining what went wrong
     *         (unknown plant, already owned, insufficient coins).
     */
    public String purchasePlant(PlantType type) {
        User user = user();
        if (user == null) return "No player is logged in.";
        Profile profile = profile();
        Collection collection = collection();
        if (profile == null || collection == null) return "No save data available.";

        if (collection.getPlant(type) != null) {
            return type + " is already unlocked.";
        }
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(type);
        if (sheet == null) {
            return type + " is not available to unlock.";
        }

        int coins = profile.getCoins();
        if (coins < PURCHASE_COIN_COST) {
            return "Not enough coins! Unlocking " + sheet.getName() + " costs " + PURCHASE_COIN_COST
                    + " coins, but you only have " + coins + ".";
        }

        profile.setCoins(coins - PURCHASE_COIN_COST);
        collection.unlockPlant(type);
        user.saveUser();
        return null;
    }

    /** True when the plant exists, is not a mint and is not owned yet (so it can be bought). */
    public boolean canPurchase(PlantType type) {
        Collection collection = collection();
        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(type);
        if (sheet == null || sheet.isMint()) return false;
        if (collection == null) return true;
        return collection.getPlant(type) == null;
    }

    /**
     * Spends seed packets to raise an owned plant to its next level.
     *
     * @return {@code null} on success, or an error message explaining what went wrong
     *         (not owned, already max level, not enough packets).
     */
    public String upgradePlant(PlantType type) {
        User user = user();
        if (user == null) return "No player is logged in.";
        Profile profile = profile();
        Collection collection = collection();
        if (profile == null || collection == null) return "No save data available.";

        MyPlant plant = collection.getPlant(type);
        if (plant == null) return type + " is not unlocked yet.";

        PlantPropertySheet sheet = PlantConfigRegistry.getInstance().resolveSheet(type);
        int maxLevel = 1;
        if (sheet != null && sheet.getLevelUpgrades() != null) {
            maxLevel = sheet.getLevelUpgrades().size() + 1;
        }
        if (plant.getLevel() >= maxLevel) return type + " is already at max level.";

        int packetCost = requiredPacketsForLevel(plant.getLevel());
        int packets = collection.getSeedPackets(type);
        if (packets < packetCost) {
            return "Not enough packets! You need " + packetCost + " to reach level "
                    + (plant.getLevel() + 1) + ", but you only have " + packets + ".";
        }

        int coinCost = requiredCoinsForLevel(plant.getLevel());
        int coins = profile.getCoins();
        if (coins < coinCost) {
            return "Not enough coins! Upgrading to level " + (plant.getLevel() + 1)
                    + " costs " + coinCost + " coins, but you only have " + coins + ".";
        }

        collection.consumeSeedPackets(type, packetCost);
        profile.setCoins(coins - coinCost);
        plant.setLevel(plant.getLevel() + 1);
        user.saveUser();
        return null;
    }
}
