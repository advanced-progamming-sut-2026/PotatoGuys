package pvz.View.Game.modals;

import java.util.regex.Matcher;

import pvz.Enums.Commands.GameMenuCommands;
import pvz.View.Menu;
import pvz.View.Result;
import pvz.View.Game.GameMenu;

public class GameModesModal implements Menu{

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameMenuCommands.ADVENTURE.getMatcher(input)) != null)
            return new Result("", new GameMenu(new ChapterSellectionModal()));
        return new Result("Invalid Command");
    }

    @Override
    public String getName() {
        return "Game Modes";
    }

    @Override
    public Result onEnter() {
        return new Result("Select Game Mode:\n1.Adventure\n2.Penny's Pursuit\n3.Arena.   Using: 'select game-mode -m <mode>' to select a mode.");
    }
}
