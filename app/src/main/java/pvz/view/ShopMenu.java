package pvz.view;

import java.util.regex.Matcher;

import pvz.controller.ShopController;
import pvz.enums.commands.ShopMenuCommands;

public class ShopMenu implements Menu {
    private Menu previousMenu;

    public ShopMenu() {
        this.previousMenu = new MainMenu();
    }

    public ShopMenu(Menu previousMenu) {
        this.previousMenu = previousMenu;
    }

    @Override
    public Result handleInput(String input) {
        ShopController controller = new ShopController(previousMenu);
        Matcher matcher;
        if ((matcher = ShopMenuCommands.ENTER_MENU.getMatcher(input)) != null) return controller.showPermanentItems(matcher);
        if ((matcher = ShopMenuCommands.SHOW_DAILY_OFFER.getMatcher(input)) != null) return controller.showDailyOffer(matcher);
        if ((matcher = ShopMenuCommands.BUY_ITEM.getMatcher(input)) != null) return controller.buyItem(matcher);
        if ((matcher = ShopMenuCommands.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        if ((matcher = ShopMenuCommands.HELP.getMatcher(input)) != null) return new Result(ShopMenuCommands.getHelp());
        return new Result("Invalid command in Shop Menu.", this);
    }

    @Override
    public String getName() {
        return "Shop menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}

