package pvz.view.game.modals;

import java.util.regex.Matcher;

import pvz.controller.GameMenuController;
import pvz.enums.commands.GameMenuCommands;
import pvz.models.AppContext;
import pvz.models.games.seasons.Season;
import pvz.view.Menu;
import pvz.view.Result;

public class SelectLevelModal implements Menu{
    private Season season;

    public SelectLevelModal(String seasonName){
        season = AppContext.getInstance().getCurrentUser().getProfile().getSeasonByName(seasonName);
    }

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameMenuCommands.SELECT_LEVEL.getMatcher(input)) != null) {
            return new GameMenuController().selectLevel(matcher , season);
        }
        return new Result("Invalid command in Select Level Menu");
    }

    @Override
    public String getName() {
        return "Select Level Menu";
    }

    @Override
    public Result onEnter() {
        return new Result("Enter level number to play.\nUsing: 'select level -l <level>' to select a level.");
    }
}
