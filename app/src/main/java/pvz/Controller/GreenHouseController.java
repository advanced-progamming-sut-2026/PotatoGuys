package pvz.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import pvz.Models.AppContext;
import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.GreenHouse.GreenHouse;
import pvz.Models.GreenHouse.GreenHousePlant;
import pvz.Models.GreenHouse.GreenHousePot;
import pvz.Models.User.MyPlant;
import pvz.Models.User.User;
import pvz.View.GreenHouseMenu;
import pvz.View.MainMenu;
import pvz.View.Menu;
import pvz.View.Result;
import pvz.View.ShopMenu;

public class GreenHouseController {

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
        StringBuilder sb = new StringBuilder("Greenhouse:");
        for (GreenHousePot pot : greenHouse.getGreenHousePots()) {
            sb.append("\nPot (").append(pot.getX()).append(",").append(pot.getY()).append("): ");
            if (pot.isLocked()) {
                sb.append("Locked");
            } else if (pot.isEmpty()) {
                sb.append("Empty");
            } else {
                GreenHousePlant plant = pot.getPlant();
                if (plant.isReady()) {
                    sb.append("Ready");
                } else {
                    sb.append("Growing");
                }
                sb.append(" - ");
                if (plant.isMariGold()) {
                    sb.append("MariGold");
                } else {
                    sb.append(plant.getPlantType());
                }
                if (!plant.isReady()) {
                    sb.append(" (").append(plant.remainingHours()).append(" hours remaining)");
                }
            }
        }
        return new Result(sb.toString());
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

    public Result enterShop(Matcher matcher) {
        Menu nextMenu = new ShopMenu(new GreenHouseMenu());
        return new Result("Entered " + nextMenu.getName(), nextMenu);
    }

    public Result exit(Matcher matcher) {
        Menu nextMenu = new MainMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
