package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.ChapterSelectionController;
import pvz.Enums.Commands.ChapterSelectionMenuCommand;

public class ChapterSelectionMenu implements Menu {
    ChapterSelectionController controller = new ChapterSelectionController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = ChapterSelectionMenuCommand.ENTER_MENU.getMatcher(input)) != null) return controller.enterMenu(matcher);
        if ((matcher = ChapterSelectionMenuCommand.ENTER_CHAPTER.getMatcher(input)) != null) return controller.enterChapter(matcher);
        if ((matcher = ChapterSelectionMenuCommand.GREENHOUSE.getMatcher(input)) != null) return controller.greenhouse(matcher);
        if ((matcher = ChapterSelectionMenuCommand.TRAVEL_LOG.getMatcher(input)) != null) return controller.travelLog(matcher);
        if ((matcher = ChapterSelectionMenuCommand.LEADERBOARD.getMatcher(input)) != null) return controller.leaderboard(matcher);
        if ((matcher = ChapterSelectionMenuCommand.COIN_WALLET.getMatcher(input)) != null) return controller.coinWallet(matcher);
        if ((matcher = ChapterSelectionMenuCommand.GEM_WALLET.getMatcher(input)) != null) return controller.gemWallet(matcher);
        if ((matcher = ChapterSelectionMenuCommand.CHEAT_ADD.getMatcher(input)) != null) return controller.cheatAdd(matcher);
        if ((matcher = ChapterSelectionMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        return new Result("Invalid command in Chapter Selection Menu.", this);
    }

    @Override
    public String getName(){
        return "Chapter Selection Menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
