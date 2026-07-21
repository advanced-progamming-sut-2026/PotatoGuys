package pvz.Controller;

import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.data.PlantRegistry;
import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Entities.Zombies.data.ZombiePropertySheet;
import pvz.Models.Entities.Zombies.data.ZombieRegistry;
import pvz.Models.User.Collection;
import pvz.Models.User.MyPlant;
import pvz.Models.User.Profile;
import pvz.View.Menu;
import pvz.View.Result;
import pvz.View.Game.GameMenu;
import pvz.View.Game.modals.GameModesModal;

public class CollectionController {

    // Purchase costs
    private static final int PURCHASE_COIN_COST = 2000;
    private static final int PURCHASE_PACKET_COST = 10;

    // Base multipliers for scaling upgrade costs
    private static final int UPGRADE_COIN_BASE = 500;
    private static final int UPGRADE_PACKET_BASE = 10;

    private Profile getProfile() {
        return AppContext.getInstance().getCurrentUser().getProfile();
    }

    private Collection getCollection() {
        return getProfile().getCollection();
    }

    public Result showPlants(Matcher matcher) {
        StringBuilder output = new StringBuilder("Your unlocked plants:");
        if (getCollection().getUnlockedPlants().isEmpty()) {
            output.append("\nYou haven't unlocked any plants yet.");
            return new Result(output.toString());
        }
        for (MyPlant p : getCollection().getUnlockedPlants()) {
            PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(p.getType());
            output.append("\n- ").append(p.getType());
            output.append(" | Level: ").append(p.getLevel());
            output.append(" | Sun Cost: ").append(sheet.getSunCost());
            output.append(" | Seed Packets: ").append(getCollection().getSeedPackets(p.getType()));
            if (p.isBoosted()) output.append(" [BOOSTED]");
        }
        return new Result(output.toString());
    }

    public Result showAllPlants(Matcher matcher) {
        StringBuilder output = new StringBuilder("All defined plants in the game:");
        for (PlantType pt : PlantType.values()) {
            PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(pt);
            if (sheet == null) continue;
            output.append("\n- ").append(sheet.getType().toString());
            output.append(" | ").append(sheet.getCategory().toString());
            output.append(" | Sun: ").append(sheet.getSunCost());
            output.append(" | HP: ").append((int) sheet.getBaseHp());
            output.append(" | Recharge: ").append(sheet.getRechargeSeconds()).append("s");
        }
        return new Result(output.toString());
    }

    public Result showZombies(Matcher matcher) {
        StringBuilder output = new StringBuilder("Your encountered zombies:");
        if (getCollection().getUnlockedZombies().isEmpty()) {
            output.append("\nNo zombies seen yet. Their respective frames are empty!");
        }
        for (ZombieType z : getCollection().getUnlockedZombies()) {
            output.append("\n- ").append(z.name()).append(" (").append(z.getAlias()).append(")");
        }
        return new Result(output.toString());
    }

    public Result showAllZombies(Matcher matcher) {
        StringBuilder output = new StringBuilder("All defined zombies in the game:");
        for (ZombieType zt : ZombieType.values()) {
            output.append("\n- ").append(zt.name());
            ZombiePropertySheet sheet = ZombieRegistry.getInstance().getSheet(zt.getAlias());
            if (sheet != null) {
                output.append(" | HP: ").append((int) sheet.getHitPoints());
                output.append(" | Speed: ").append(sheet.getSpeed());
            }
        }
        return new Result(output.toString());
    }

    public Result showPlantInfo(Matcher matcher) {
        String name = matcher.group("plantName");

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()) {
            if (pt.name().equalsIgnoreCase(name)) {
                plantType = pt;
                break;
            }
        }
        if (plantType == null) {
            return new Result("Plant \"" + name + "\" not found.");
        }

        PlantPropertySheet storage = PlantRegistry.getInstance().getSheet(plantType);
        if (storage == null) {
            return new Result("No data available for this plant.");
        }

        StringBuilder output = new StringBuilder();
        output.append("=== ").append(plantType.toString()).append(" ===");
        output.append("\nCategory: ").append(storage.getCategory().toString());
        output.append("\nSun Cost: ").append(storage.getSunCost());
        output.append("\nBase HP: ").append((int) storage.getBaseHp());
        output.append("\nBase Recharge: ").append(storage.getRechargeSeconds()).append("s");
        output.append("\nBase Action Interval: ").append(storage.getActionIntervalSeconds()).append("s");

        MyPlant plant = getCollection().getPlant(plantType);
        int packets = getCollection().getSeedPackets(plantType);

        if (plant != null) {
            output.append("\n\n-- Your Plant Status --");
            output.append("\nLevel: ").append(plant.getLevel());
            output.append("\nSeed Packets Owned: ").append(packets);
            output.append("\nBoosted: ").append(plant.isBoosted() ? "Yes" : "No");
        } else {
            output.append("\n\nStatus: Not yet unlocked.");
            output.append("\nSeed Packets Owned: ").append(packets).append(" / ").append(PURCHASE_PACKET_COST);
        }
        return new Result(output.toString());
    }

    public Result showZombieInfo(Matcher matcher) {
        String name = matcher.group("zombieName");

        ZombieType zombieType = null;
        for (ZombieType zt : ZombieType.values()) {
            if (zt.name().equalsIgnoreCase(name)) {
                zombieType = zt;
                break;
            }
        }
        if (zombieType == null) {
            return new Result("Zombie \"" + name + "\" not found.");
        }

        StringBuilder output = new StringBuilder();
        output.append("=== ").append(zombieType.name()).append(" ===");
        output.append("\nAlias: ").append(zombieType.getAlias());

        ZombiePropertySheet sheet = ZombieRegistry.getInstance().getSheet(zombieType.getAlias());
        if (sheet != null) {
            output.append("\nHitPoints: ").append(sheet.getHitPoints());
            output.append("\nEat DPS: ").append(sheet.getEatDps());
            output.append("\nSpeed: ").append(sheet.getSpeed());
            output.append("\nWeight: ").append(sheet.getWeight());
        }
        return new Result(output.toString());
    }

    public Result purchasePlant(Matcher matcher) {
        String name = matcher.group("plantName");

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()) {
            if (pt.name().equalsIgnoreCase(name)) {
                plantType = pt;
                break;
            }
        }
        if (plantType == null) {
            return new Result("Plant \"" + name + "\" not found.");
        }

        if (getCollection().getPlant(plantType) != null) {
            return new Result("You already own this plant.");
        }

        if (PlantRegistry.getInstance().getSheet(plantType) == null) {
            return new Result("This plant is not available for purchase.");
        }

        int coins = getProfile().getCoins();
        int packets = getCollection().getSeedPackets(plantType);

        if (coins < PURCHASE_COIN_COST || packets < PURCHASE_PACKET_COST) {
            return new Result(String.format(
                    "Not enough resources to unlock! Need %d coins and %d seed packets. You currently have %d coins and %d packets.",
                    PURCHASE_COIN_COST, PURCHASE_PACKET_COST, coins, packets
            ));
        }

        // Deduct costs and unlock
        getProfile().setCoins(coins - PURCHASE_COIN_COST);
        getCollection().consumeSeedPackets(plantType, PURCHASE_PACKET_COST);
        getCollection().unlockPlant(plantType);

        // ---> FIX: SAVE THE USER <---
        AppContext.getInstance().getCurrentUser().saveUser();

        return new Result("Successfully purchased and unlocked " + plantType.toString() + "!\n"
                + "Remaining Coins: " + getProfile().getCoins() + "\n"
                + "Remaining Seed Packets: " + getCollection().getSeedPackets(plantType));
    }

    public Result upgradePlant(Matcher matcher) {
        String name = matcher.group("plantName");

        PlantType plantType = null;
        for (PlantType pt : PlantType.values()) {
            if (pt.name().equalsIgnoreCase(name)) {
                plantType = pt;
                break;
            }
        }
        if (plantType == null) {
            return new Result("Plant \"" + name + "\" not found.");
        }

        MyPlant owned = getCollection().getPlant(plantType);
        if (owned == null) {
            return new Result("You do not own this plant. Purchase it first.");
        }

        int currentLevel = owned.getLevel();
        if (currentLevel >= 4) { // Assuming 4 is the max level handled by PlantStatResolver
            return new Result(plantType.toString() + " is already at maximum level.");
        }

        // Costs scale up with each level
        int coinCost = UPGRADE_COIN_BASE * currentLevel;
        int packetCost = UPGRADE_PACKET_BASE * currentLevel;

        int coins = getProfile().getCoins();
        int packets = getCollection().getSeedPackets(plantType);

        if (coins < coinCost || packets < packetCost) {
            return new Result(String.format(
                    "Not enough resources to upgrade! Need %d coins and %d seed packets. You currently have %d coins and %d packets.",
                    coinCost, packetCost, coins, packets
            ));
        }

        // Deduct costs and upgrade
        getProfile().setCoins(coins - coinCost);
        getCollection().consumeSeedPackets(plantType, packetCost);
        owned.setLevel(currentLevel + 1);

        // ---> FIX: SAVE THE USER <---
        AppContext.getInstance().getCurrentUser().saveUser();

        return new Result(plantType.toString() + " upgraded successfully to level " + (currentLevel + 1) + "!\n"
                + "Remaining Coins: " + getProfile().getCoins() + "\n"
                + "Remaining Seed Packets: " + getCollection().getSeedPackets(plantType));
    }

    public Result exit(Matcher matcher) {
        Menu nextMenu = new GameMenu(new GameModesModal());
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}