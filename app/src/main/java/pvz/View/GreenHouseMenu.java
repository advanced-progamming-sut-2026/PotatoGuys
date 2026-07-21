package pvz.view;

import java.util.regex.Matcher;

import pvz.controller.GreenHouseController;
import pvz.enums.commands.GreenHouseMenuCommands;

public class GreenHouseMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        GreenHouseController controller = new GreenHouseController();
        Matcher matcher;
        if ((matcher = GreenHouseMenuCommands.ENTER_MENU.getMatcher(input)) != null) return controller.showGreenhouse(matcher);
        if ((matcher = GreenHouseMenuCommands.PLANT.getMatcher(input)) != null) return controller.plant(matcher);
        if ((matcher = GreenHouseMenuCommands.COLLECT.getMatcher(input)) != null) return controller.collect(matcher);
        if ((matcher = GreenHouseMenuCommands.GROW.getMatcher(input)) != null) return controller.grow(matcher);
        if ((matcher = GreenHouseMenuCommands.ENTER_SHOP.getMatcher(input)) != null) return controller.enterShop(matcher);
        if ((matcher = GreenHouseMenuCommands.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        if ((matcher = GreenHouseMenuCommands.HELP.getMatcher(input)) != null) return new Result(GreenHouseMenuCommands.getHelp());
        return new Result("Invalid command in GreenHouse Menu.", this);
    }

    @Override
    public String getName() {
        return "GreenHouse menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }

}
