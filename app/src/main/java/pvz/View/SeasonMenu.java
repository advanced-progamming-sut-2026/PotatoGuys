package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.SeasonController;
import pvz.Enums.Commands.SeasonMenuCommand;

public class SeasonMenu implements Menu {
    SeasonController controller = new SeasonController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = SeasonMenuCommand.ENTER_MENU.getMatcher(input)) != null) return controller.enterMenu(matcher);
        if ((matcher = SeasonMenuCommand.ENTER_CHAPTER.getMatcher(input)) != null) return controller.enterChapter(matcher);
        if ((matcher = SeasonMenuCommand.GREENHOUSE.getMatcher(input)) != null) return controller.greenhouse(matcher);
        if ((matcher = SeasonMenuCommand.TRAVEL_LOG.getMatcher(input)) != null) return controller.travelLog(matcher);
        if ((matcher = SeasonMenuCommand.LEADERBOARD.getMatcher(input)) != null) return controller.leaderboard(matcher);
        if ((matcher = SeasonMenuCommand.COIN_WALLET.getMatcher(input)) != null) return controller.coinWallet(matcher);
        if ((matcher = SeasonMenuCommand.GEM_WALLET.getMatcher(input)) != null) return controller.gemWallet(matcher);
        if ((matcher = SeasonMenuCommand.CHEAT_ADD.getMatcher(input)) != null) return controller.cheatAdd(matcher);
        if ((matcher = SeasonMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        return new Result("Invalid command in Seasons Menu.", this);
    }

    @Override
    public String getName(){
        return "Seasons menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
