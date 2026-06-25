package Controller;

import View.GameMenu;
import View.Menu;
import View.Result;
import java.util.regex.Matcher;

public class CollectionController {
    public Result showPlants(Matcher matcher) { return null; }
    public Result showAllPlants(Matcher matcher) { return null; }
    public Result showZombies(Matcher matcher) { return null; }
    public Result showAllZombies(Matcher matcher) { return null; }
    public Result showPlantInfo(Matcher matcher) { return null; }
    public Result showZombieInfo(Matcher matcher) { return null; }
    public Result upgradePlant(Matcher matcher) { return null; }
    public Result purchasePlant(Matcher matcher) { return null; }
    public Result exit(Matcher matcher) {
        Menu nextMenu = new GameMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
