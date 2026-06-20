package View;

import Controller.GameController;
import Models.Commands.GameMenuCommand;

import java.util.regex.Matcher;

public class GameMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameMenuCommand.ENTER_CHAPTER.getMatcher(input)) != null) return GameController.enterChapter(matcher);
        if ((matcher = GameMenuCommand.GREENHOUSE.getMatcher(input)) != null) return GameController.greenhouse(matcher);
        if ((matcher = GameMenuCommand.TRAVEL_LOG.getMatcher(input)) != null) return GameController.travelLog(matcher);
        if ((matcher = GameMenuCommand.LEADERBOARD.getMatcher(input)) != null) return GameController.leaderboard(matcher);
        if ((matcher = GameMenuCommand.COIN_WALLET.getMatcher(input)) != null) return GameController.coinWallet(matcher);
        if ((matcher = GameMenuCommand.GEM_WALLET.getMatcher(input)) != null) return GameController.gemWallet(matcher);
        if ((matcher = GameMenuCommand.CHEAT_ADD.getMatcher(input)) != null) return GameController.cheatAdd(matcher);
        if ((matcher = GameMenuCommand.EXIT.getMatcher(input)) != null) return GameController.exit(matcher);
        return new Result("Invalid command in Game Menu.", this);
    }
}
