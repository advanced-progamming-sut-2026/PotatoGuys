package pvz.View;

import pvz.Controller.GreenHouseController;
import pvz.Models.GreenHouse.GreenHouse;
import pvz.Models.GreenHouse.GreenHousePot;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GreenHouseMenu implements Menu {
    GreenHouseController controller=new GreenHouseController(null,null);
    public GreenHouseMenu() {

    }

    @Override
    public Result handleInput(String input) {
        if (input.equals("show greenhouse")) {
            showGreenhouse();
            return new Result("");
        }

        Matcher plantMatcher = Pattern.compile("plant pot at \\((\\d+), (\\d+)\\)").matcher(input);
        if (plantMatcher.matches()) {
            String msg = controller.plant(Integer.parseInt(plantMatcher.group(1)), Integer.parseInt(plantMatcher.group(2)));
            return new Result(msg);
        }

        Matcher collectMatcher = Pattern.compile("collect \\((\\d+), (\\d+)\\)").matcher(input);
        if (collectMatcher.matches()) {
            String msg = controller.collect(Integer.parseInt(collectMatcher.group(1)), Integer.parseInt(collectMatcher.group(2)));
            return new Result(msg);
        }

        Matcher growMatcher = Pattern.compile("grow \\((\\d+), (\\d+)\\)").matcher(input);
        if (growMatcher.matches()) {
            String msg = controller.grow(Integer.parseInt(growMatcher.group(1)), Integer.parseInt(growMatcher.group(2)));
            return new Result(msg);
        }

        return new Result("Invalid command");
    }

    private void showGreenhouse() {
        for (GreenHousePot pot : controller.getGreenHouse().getGreenHousePots()) {
            String status = pot.isLocked() ? "Locked" : (pot.isEmpty() ? "Empty" : (pot.getPlant().isReady() ? "Ready" : "Growing"));
            System.out.println("Pot (" + pot.getX() + "," + pot.getY() + "): " + status);
        }
    }

    @Override
    public String getName() {
        return "GreenHouse";
    }

    @Override
    public Result onEnter() {
        return new Result("Entered Greenhouse.");
    }
}
