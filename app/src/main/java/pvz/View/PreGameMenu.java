package pvz.View;

import pvz.Controller.Game.PreNormalGameController;
import pvz.Enums.Commands.PreGameMenuCommand;
import pvz.Models.Seasons.Season;

import java.util.regex.Matcher;

public class PreGameMenu implements Menu{
    PreNormalGameController controller;

    public PreGameMenu(Season season, int levelNumber){
        this.controller = new PreNormalGameController(season, levelNumber);
    }

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
        return new Result("Select 7 plants to start the game.");
    }
}
