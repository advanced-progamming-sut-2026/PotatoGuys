package pvz.View.Game.modals;

import java.util.regex.Matcher;

import pvz.Controller.GameMenuController;
import pvz.Enums.Commands.GameMenuCommands;
import pvz.View.Menu;
import pvz.View.Result;

public class ChapterSellectionModal implements Menu {
    GameMenuController controller = new GameMenuController();

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameMenuCommands.ENTER_CHAPTER.getMatcher(input)) != null) return controller.enterChapter(matcher);
        if ((matcher = GameMenuCommands.HELP.getMatcher(input)) != null) return new Result(GameMenuCommands.getHelp());
        return new Result("Invalid command in Chapter Selection Modal.", this);
    }

    @Override
    public String getName(){
        return "Chapter Selection Modal";
    }

    @Override
    public Result onEnter() {
        return new Result("Select Chapter");
    }
}
