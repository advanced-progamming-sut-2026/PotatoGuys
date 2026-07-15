package pvz.View;

import pvz.Controller.ShopController;
import pvz.Enums.Commands.ShopMenuCommands;
import pvz.Models.AppContext;
import pvz.Models.Games.GameContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopMenu implements Menu {
    private ShopController controller;

    public ShopMenu() {
        controller=new ShopController(AppContext.getInstance().getCurrentUser());
    }

    @Override
    public Result handleInput(String input) {
        // Handle listing permanent items
        Matcher matcher = ShopMenuCommands.EXIT.getMatcher(input);
        if (matcher != null) {
            return new Result(controller.showPermanentItems());
        }

        // Handle daily offer
        if (input.equals("shop daily")) {
            return new Result(controller.showDailyOffer());
        }

        // Regex for: shop buy -i <item_id> -n <count> [-t <plant_type>]
        // The plant_type part is optional (?: ...)?
        Pattern buyPattern = Pattern.compile("shop buy -i (\\d+) -n (\\d+)(?: -t (\\w+))?");

        if (matcher.matches()) {
            int itemId = Integer.parseInt(matcher.group(1));
            int count = Integer.parseInt(matcher.group(2));
            String plantType = matcher.group(3); // Will be null if not provided in the command

            String msg = controller.buyItem(itemId, count, plantType);
            return new Result(msg);
        }

        return new Result("Invalid command.");
    }

    @Override
    public String getName() {
        return "Shop";
    }

    @Override
    public Result onEnter() {
        return new Result("Entered Shop.");
    }
}