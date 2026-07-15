package pvz.Controller;

import java.util.List;

import pvz.Models.AppContext;
import pvz.Models.GreenHouse.GreenHouse;
import pvz.Models.GreenHouse.GreenHousePlant;
import pvz.Models.GreenHouse.GreenHousePot;
import pvz.View.Result;

public class GreenHouseController {
    private final GreenHouse greenHouse;
    private final List<String> unlockedPlants;

    public GreenHouseController(GreenHouse greenHouse, List<String> unlockedPlants) {
        this.greenHouse = greenHouse;
        this.unlockedPlants = unlockedPlants;
    }

    public String plant(int x, int y) {
        if (!greenHouse.isValidCoordinate(x, y)) return "Invalid coordinates.";
        GreenHousePot pot = greenHouse.getPot(x, y);
        if (pot.isLocked()) return "Pot is locked.";
        if (!pot.isEmpty()) return "Pot is not empty.";

        greenHouse.plantRandomPotAt(x, y, unlockedPlants);
        return "Plant placed successfully.";
    }

    public String collect(int x, int y) {
        if (!greenHouse.isValidCoordinate(x, y)) return "Invalid coordinates.";
        GreenHousePlant plant = greenHouse.collect(x, y);

        if (plant == null) return "No ready plant found.";

        return "Collected 500 coins.";
    }

    public String grow(int x, int y) {
        if (!greenHouse.isValidCoordinate(x, y)) return "Invalid coordinates.";
        GreenHousePot pot = greenHouse.getPot(x, y);

        if (pot.isLocked() || pot.isEmpty()) return "No plant to grow.";
        if (pot.getPlant().isReady()) return "Plant is already ready.";

        int cost = greenHouse.getGrowCost(x, y);
        greenHouse.grow(x, y);
        return "Plant grown instantly for " + cost + " gems.";
    }

    public Result back(){
        AppContext.getInstance().getCurrentUser().getProfile().getGreenHouseCollection().getPlants().add(null);
        AppContext.getInstance().getCurrentUser().saveUser();
        return new Result("exited green house");
    }

    public GreenHouse getGreenHouse() {
        return greenHouse;
    }
}
