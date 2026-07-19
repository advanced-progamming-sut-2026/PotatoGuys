package pvz.View;

import java.util.regex.Matcher;

import pvz.Enums.Commands.MiniGamesMenuCommand;

public class MiniGamesMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = MiniGamesMenuCommand.HELP.getMatcher(input)) != null)
            return new Result(MiniGamesMenuCommand.getHelp());
        return new Result("No mini games available yet.");
    }

    @Override
    public String getName() {
        return "Mini Games";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
