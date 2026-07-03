package pvz.View;

import pvz.Controller.PreGameController;
import pvz.Enums.Commands.PreGameMenuCommand;

import java.util.regex.Matcher;

public class PreGameMenu implements Menu{
    PreGameController controller=new PreGameController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher= PreGameMenuCommand.SHOW_ALL_PLANTS.getMatcher(input))!=null){ return controller.showAllPlants(matcher);}
        if ((matcher= PreGameMenuCommand.SHOW_AVAILABLE_PLANTS.getMatcher(input))!=null){ return controller.showAvailablePlants(matcher);}
        if ((matcher= PreGameMenuCommand.PLANT_ADD.getMatcher(input))!=null){ return controller.plantAdd(matcher);}
        if ((matcher= PreGameMenuCommand.PLANT_REMOVE.getMatcher(input))!=null){ return controller.plantRemove(matcher);}
        if ((matcher= PreGameMenuCommand.BOOST_PLANT.getMatcher(input))!=null){ return controller.boostPlant(matcher);}
        if ((matcher= PreGameMenuCommand.START_GAME.getMatcher(input))!=null){ return controller.startGame(matcher);}
        return new Result("Invalid command in Pre-Game Menu");
    }

    @Override
    public String getName() {
        return "Pre-Game Menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
