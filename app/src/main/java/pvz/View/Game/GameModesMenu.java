package pvz.View.Game;

import java.util.regex.Matcher;

import pvz.Enums.Commands.GameModesMenuCommand;
import pvz.View.Menu;
import pvz.View.Result;

public class GameModesMenu implements Menu{
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameModesMenuCommand.ADVENTURE.getMatcher(input)) != null)
            return new Result("", new ChapterSelectionMenu());
        if ((matcher = GameModesMenuCommand.HELP.getMatcher(input)) != null)
            return new Result(GameModesMenuCommand.getHelp());
        return new Result("Invalid Command");
    }

    @Override
    public String getName() {
        return "Game Modes";
    }

    @Override
    public Result onEnter() {
        return new Result("Select Game Mode:\n1.Adventure");
    }
}
