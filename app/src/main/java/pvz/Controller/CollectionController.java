package pvz.Controller;

import java.util.regex.Matcher;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.PlantFactory;
import pvz.Models.Entities.Plants.data.PlantPropertySheet;
import pvz.Models.Entities.Plants.data.PlantRegistry;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.AppContext;
import pvz.Models.User.Collection;
import pvz.Models.User.Profile;
import pvz.View.ChapterSelectionMenu;
import pvz.View.Menu;
import pvz.View.Result;

public class CollectionController {
    private static final int PURCHASE_COST = 100;
    private static final int UPGRADE_BASE_COST = 50;
    private final PlantFactory plantFactory = new PlantFactory();

    private Profile getProfile() {
        return AppContext.getInstance().getCurrentUser().getProfile();
    }

    private Collection getCollection() {
        return getProfile().getCollection();
    }

    public Result showPlants(Matcher matcher) {
        StringBuilder output = new StringBuilder("Your unlocked plants:");
        for (Plant p : getCollection().getUnlockedPlants()) {
            output.append("\n- ").append(p.getType().toString());
            output.append(" | Level: ").append(p.getLevel());
            output.append(" | Sun Cost: ").append(p.getSunCost());
            if (p.isBoosted()) output.append(" [BOOSTED]");
        }
        return new Result(output.toString());
    }

    public Result showAllPlants(Matcher matcher) {
        StringBuilder output = new StringBuilder("All plants:");
        for (PlantType pt : PlantType.values()) {
            PlantPropertySheet sheet = PlantRegistry.getInstance().getSheet(pt);
            if (sheet == null) continue;
            output.append("\n- ").append(sheet.getType().toString());
            output.append(" | ").append(sheet.getCategory().toString());
            output.append(" | Sun: ").append(sheet.getSunCost());
            output.append(" | HP: ").append((int) sheet.getBaseHp());
            output.append(" | Recharge: ").append(sheet.getRechargeSeconds());
        }
        return new Result(output.toString());
    }

    public Result showZombies(Matcher matcher) {
        StringBuilder output = new StringBuilder("Your unlocked zombies:");
        for (Zombie z : getCollection().getUnlockedZombies()) {
            output.append("\n- ").append(z.getSheet().getAlias());
        }
        return new Result(output.toString());
    }

    public Result showAllZombies(Matcher matcher) {
        StringBuilder output = new StringBuilder("All zombie types:");
        for (ZombieType zt : ZombieType.values()) {
            output.append("\n- ").append(zt.name());
            output.append(" (").append(zt.getAlias()).append(")");
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
        output.append("Plant: ").append(plantType.toString());
        output.append("\nCategory: ").append(storage.getCategory().toString());
        output.append("\nSun Cost: ").append(storage.getSunCost());
        output.append("\nBase HP: ").append((int) storage.getBaseHp());
        output.append("\nBase Recharge: ").append(storage.getRechargeSeconds());
        output.append("\nBase Action Interval: ").append(storage.getActionIntervalSeconds());

        Plant owned = getCollection().getPlant(plantType);
        if (owned != null) {
            output.append("\n\nYour plant:");
            output.append("\nLevel: ").append(owned.getLevel());
            output.append("\nHP: ").append((int) owned.getHp());
            output.append("\nRecharge: ").append(owned.getRechargeSeconds());
            output.append("\nAction Interval: ").append(owned.getActionIntervalSeconds());
            output.append("\nBoosted: ").append(owned.isBoosted() ? "Yes" : "No");
        } else {
            output.append("\n\nStatus: Not yet unlocked.");
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
        output.append("Zombie: ").append(zombieType.name());
        output.append("\nAlias: ").append(zombieType.getAlias());
        return new Result(output.toString());
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

        Plant owned = getCollection().getPlant(plantType);
        if (owned == null) {
            return new Result("You do not own this plant. Purchase it first.");
        }

        int currentLevel = owned.getLevel();
        int cost = UPGRADE_BASE_COST * currentLevel;
        int coins = getProfile().getCoins();
        if (coins < cost) {
            return new Result("Not enough coins. Need " + cost + " coins, have " + coins + ".");
        }

        owned.setLevel(currentLevel + 1);
        getProfile().setCoins(coins - cost);

        return new Result(plantType.toString() + " upgraded to level " + (currentLevel + 1)
                + ". Cost: " + cost + " coins. Remaining: " + (coins - cost) + " coins.");
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
        if (coins < PURCHASE_COST) {
            return new Result("Not enough coins. Need " + PURCHASE_COST + " coins, have " + coins + ".");
        }

        Plant newPlant = plantFactory.createUnplaced(plantType, 1, false);
        getCollection().addPlant(newPlant);
        getProfile().setCoins(coins - PURCHASE_COST);

        return new Result("Purchased " + plantType.toString() + " for " + PURCHASE_COST + " coins. "
                + "Remaining: " + (coins - PURCHASE_COST) + " coins.");
    }

    public Result exit(Matcher matcher) {
        Menu nextMenu = new ChapterSelectionMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
