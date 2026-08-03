package com.pvz.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.pvz.enums.AnsiColors;
import com.pvz.models.AppContext;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.greenhouse.GreenHouse;
import com.pvz.models.greenhouse.GreenHousePlant;
import com.pvz.models.greenhouse.GreenHousePot;
import com.pvz.models.user.MyPlant;
import com.pvz.models.user.User;

public class GreenHouseController {
/*

    private GreenHouse getGreenHouse() {
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

    public Result showGreenhouse(Matcher matcher) {
        GreenHouse greenHouse = getGreenHouse();
        return new Result(renderGreenhouse(greenHouse));
    }

    private String renderGreenhouse(GreenHouse greenHouse) {
        StringBuilder sb = new StringBuilder();
        appendHeader(sb, greenHouse);
        appendColumnHeaders(sb);
        appendDivider(sb);
        for (int y = 1; y <= GreenHouse.HEIGHT; y++) {
            appendRow(sb, greenHouse, y);
            appendDivider(sb);
        }
        return sb.toString();
    }

    private void appendHeader(StringBuilder sb, GreenHouse greenHouse) {
        sb.append("\n=== Greenhouse | Unlocked pots: ").append(greenHouse.getUnlockedPotCount())
                .append(" / ").append(GreenHouse.WIDTH * GreenHouse.HEIGHT).append(" ===\n");
    }

    private void appendColumnHeaders(StringBuilder sb) {
        sb.append("\n     ");
        for (int x = 1; x <= GreenHouse.WIDTH; x++) {
            sb.append(String.format(" P%-2d ", x));
        }
        sb.append("\n");
    }

    private void appendDivider(StringBuilder sb) {
        sb.append("    +");
        for (int x = 1; x <= GreenHouse.WIDTH; x++) {
            sb.append("----+");
        }
        sb.append("\n");
    }

    private void appendRow(StringBuilder sb, GreenHouse greenHouse, int y) {
        sb.append("    |");
        for (int x = 1; x <= GreenHouse.WIDTH; x++) {
            sb.append(getCellContent(greenHouse.getPot(x, y))).append('|');
        }
        sb.append("  Row ").append(y).append("\n");
    }

    private String getCellContent(GreenHousePot pot) {
        if (pot == null) {
            return "    ";
        }
        if (pot.isLocked()) {
            return AnsiColors.BRIGHT_BLACK + " LK " + AnsiColors.RESET;
        }
        if (pot.isEmpty()) {
            return "    ";
        }

        GreenHousePlant plant = pot.getPlant();
        if (plant.isReady()) {
            return AnsiColors.GREEN + " RD " + AnsiColors.RESET;
        }
        return AnsiColors.YELLOW + String.format("G%-3d", plant.remainingHours()) + AnsiColors.RESET;
    }

    public Result plant(Matcher matcher) {
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));
        GreenHouse greenHouse = getGreenHouse();

        if (!greenHouse.isValidCoordinate(x, y)) return new Result("Invalid coordinates.");
        GreenHousePot pot = greenHouse.getPot(x, y);
        if (pot == null) return new Result("Invalid coordinates.");
        if (pot.isLocked()) return new Result("Pot is locked.");
        if (!pot.isEmpty()) return new Result("Pot is not empty.");

        List<String> unlockedPlants = getUnlockedPlantNames();
        greenHouse.plantRandomPotAt(x, y, unlockedPlants);

        User user = getCurrentUser();
        if (user != null) user.saveUser();
        return new Result("Plant placed successfully.");
    }

    public Result collect(Matcher matcher) {
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));
        GreenHouse greenHouse = getGreenHouse();

        if (!greenHouse.isValidCoordinate(x, y)) return new Result("Invalid coordinates.");
        GreenHousePot pot = greenHouse.getPot(x, y);
        if (pot == null) return new Result("Invalid coordinates.");

        GreenHousePlant plant = greenHouse.collect(x, y);
        if (plant == null) return new Result("No ready plant found.");

        User user = getCurrentUser();
        if (user == null) return new Result("No user logged in.");

        if (plant.isMariGold()) {
            user.getProfile().setCoins(user.getProfile().getCoins() + GreenHousePlant.MARIGOLD_REWARD);
            user.saveUser();
            return new Result("Harvested MariGold. Collected " + GreenHousePlant.MARIGOLD_REWARD + " coins.");
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
                    user.saveUser();
                    return new Result("Harvested " + plantTypeName + ". A boost has been stored for " + plantTypeName + ".");
                } else {
                    user.saveUser();
                    return new Result("Harvested " + plantTypeName + ". Boost already stored. Pot emptied.");
                }
            }
            user.saveUser();
            return new Result("Harvested " + plantTypeName + ". Pot emptied.");
        }
    }

    public Result grow(Matcher matcher) {
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));
        GreenHouse greenHouse = getGreenHouse();

        if (!greenHouse.isValidCoordinate(x, y)) return new Result("Invalid coordinates.");
        GreenHousePot pot = greenHouse.getPot(x, y);
        if (pot == null) return new Result("Invalid coordinates.");
        if (pot.isLocked() || pot.isEmpty()) return new Result("No plant to grow.");

        GreenHousePlant plant = pot.getPlant();
        if (plant.isReady()) return new Result("Plant is already ready.");

        int cost = plant.remainingHours();
        User user = getCurrentUser();
        if (user == null) return new Result("No user logged in.");

        if (user.getProfile().getDiamonds() < cost) {
            return new Result("Not enough diamonds. Need " + cost + " diamonds, have " + user.getProfile().getDiamonds() + ".");
        }

        user.getProfile().setDiamonds(user.getProfile().getDiamonds() - cost);
        greenHouse.grow(x, y);
        user.saveUser();
        return new Result("Plant grown instantly for " + cost + " diamonds.");
    }

    public Result inspect(Matcher matcher) {
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));
        GreenHouse greenHouse = getGreenHouse();

        if (!greenHouse.isValidCoordinate(x, y)) return new Result("Invalid coordinates.");
        GreenHousePot pot = greenHouse.getPot(x, y);
        if (pot == null) return new Result("Invalid coordinates.");

        StringBuilder sb = new StringBuilder();
        sb.append("=== Pot (").append(x).append(", ").append(y).append(") ===");

        if (pot.isLocked()) {
            sb.append("\nStatus: LOCKED");
            return new Result(sb.toString());
        }
        if (pot.isEmpty()) {
            sb.append("\nStatus: Empty");
            sb.append("\nUse 'plant pot at (").append(x).append(", ").append(y).append(")' to plant.");
            return new Result(sb.toString());
        }

        GreenHousePlant plant = pot.getPlant();
        String plantName = plant.isMariGold() ? "MariGold" : plant.getPlantType();
        sb.append("\nPlant: ").append(plantName);
        sb.append("\nType: ").append(plant.isMariGold() ? "MariGold (coins reward)" : "Unlocked (boost reward)");
        sb.append("\nGrowth: ").append(plant.getGrowthHours()).append(" hours total");

        if (plant.isReady()) {
            sb.append("\nStatus: ").append(AnsiColors.GREEN).append("READY TO HARVEST").append(AnsiColors.RESET);
            sb.append("\nUse 'collect (").append(x).append(", ").append(y).append(")' to harvest.");
        } else {
            int remaining = plant.remainingHours();
            sb.append("\nStatus: ").append(AnsiColors.YELLOW).append("Growing (").append(remaining).append(" hours left)").append(AnsiColors.RESET);
            sb.append("\nUse 'grow (").append(x).append(", ").append(y).append(")' to speed up (costs ").append(remaining).append(" diamonds).");
        }
        return new Result(sb.toString());
    }

    public Result enterShop(Matcher matcher) {
        Menu nextMenu = new ShopMenu(new GreenHouseMenu());
        return new Result("Entered " + nextMenu.getName(), nextMenu);
    }

    public Result exit(Matcher matcher) {
        Menu nextMenu = new pvz.view.OldMainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
*/
}
