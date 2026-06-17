package View;

import Controller.CollectionController;
import Models.Commands.CollectionMenuCommand;

import java.util.regex.Matcher;

public class CollectionMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = CollectionMenuCommand.SHOW_PLANTS.getMatcher(input)) != null) return CollectionController.showPlants(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_ALL_PLANTS.getMatcher(input)) != null) return CollectionController.showAllPlants(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_ZOMBIES.getMatcher(input)) != null) return CollectionController.showZombies(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_ALL_ZOMBIES.getMatcher(input)) != null) return CollectionController.showAllZombies(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_PLANT_INFO.getMatcher(input)) != null) return CollectionController.showPlantInfo(matcher);
        if ((matcher = CollectionMenuCommand.SHOW_ZOMBIE_INFO.getMatcher(input)) != null) return CollectionController.showZombieInfo(matcher);
        if ((matcher = CollectionMenuCommand.UPGRADE_PLANT.getMatcher(input)) != null) return CollectionController.upgradePlant(matcher);
        if ((matcher = CollectionMenuCommand.PURCHASE_PLANT.getMatcher(input)) != null) return CollectionController.purchasePlant(matcher);
        if ((matcher = CollectionMenuCommand.EXIT.getMatcher(input)) != null) return CollectionController.exit(matcher);
        return new Result("Invalid command in Collection Menu.", this);
    }
}
