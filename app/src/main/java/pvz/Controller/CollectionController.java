package pvz.Controller;

import java.util.regex.Matcher;

import pvz.View.SeasonMenu;
import pvz.View.Menu;
import pvz.View.Result;

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
        Menu nextMenu = new SeasonMenu();
        return new Result("Exited to " + nextMenu.getName(), nextMenu);
    }
}
