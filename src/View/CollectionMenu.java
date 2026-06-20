package View;

import Controller.CollectionController;
import Models.Commands.CollectionMenuCommand;

import java.util.regex.Matcher;

public class CollectionMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        CollectionController controller=new CollectionController();
        Matcher matcher;
        if ((matcher = CollectionMenuCommand.SHOW_PLANTS.getMatcher(input)) != null) return controller.showPlants(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_ALL_PLANTS.getMatcher(input)) != null) return controller.showAllPlants(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_ZOMBIES.getMatcher(input)) != null) return controller.showZombies(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_ALL_ZOMBIES.getMatcher(input)) != null) return controller.showAllZombies(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_PLANT_INFO.getMatcher(input)) != null) return controller.showPlantInfo(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_ZOMBIE_INFO.getMatcher(input)) != null) return controller.showZombieInfo(matcher);
        if ((matcher = CollectionMenuCommand.UPGRADE_PLANT.getMatcher(input)) != null) return controller.upgradePlant(matcher);
        if ((matcher = CollectionMenuCommand.PURCHASE_PLANT.getMatcher(input)) != null) return controller.purchasePlant(matcher);
        if ((matcher = CollectionMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        return new Result("Invalid command in Collection Menu.", this);
    }
}
