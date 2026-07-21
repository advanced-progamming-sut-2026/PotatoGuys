package pvz.View.Game.modals;

import java.util.regex.Matcher;

import pvz.Controller.GameMenuController;
import pvz.Enums.Commands.GameMenuCommands;
import pvz.View.Menu;
import pvz.View.Result;

public class GameModesModal implements Menu{
    GameMenuController controller = new GameMenuController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameMenuCommands.SELECT_MODE.getMatcher(input)) != null)
            return controller.selectGameMode(matcher);
        return new Result("Invalid Command");
    }

    @Override
    public String getName() {
        return "Game Modes";
    }

    @Override
    public Result onEnter() {
        return new Result("Select Game Mode:\n1.Adventure\n2.Penny's Pursuit\n3.Arena.   \nUsing: 'select game-mode -m <mode>' to select a mode.");
    }
}
