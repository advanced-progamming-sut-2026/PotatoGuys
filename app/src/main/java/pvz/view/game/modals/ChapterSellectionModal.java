package pvz.view.game.modals;

import java.util.regex.Matcher;

import pvz.controller.GameMenuController;
import pvz.enums.commands.GameMenuCommands;
import pvz.view.Menu;
import pvz.view.Result;

public class ChapterSellectionModal implements Menu {
    GameMenuController controller = new GameMenuController();

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameMenuCommands.ENTER_CHAPTER.getMatcher(input)) != null)
            return controller.selectChapter(matcher);
        if ((matcher = GameMenuCommands.HELP.getMatcher(input)) != null)
            return new Result(GameMenuCommands.getHelp());
        return new Result("Invalid command in Chapter Selection Modal.", this);
    }

    @Override
    public String getName() {
        return "Chapter Selection Modal";
    }

    @Override
    public Result onEnter() {
        return new Result("Select Chapter:\n1. Ancient Egypt\n" + //
                "2. Frostbite Caves\n" + //
                "3. Dark Ages\n" + //
                "4. Big Wave Beach ");
    }
}
