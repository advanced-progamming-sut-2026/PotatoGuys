package View;

import Controller.GameController;
import Models.Commands.GameMenuCommand;

import java.util.regex.Matcher;

public class GameMenu implements Menu {
    GameController controller = new GameController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameMenuCommand.ENTER_MENU.getMatcher(input)) != null) return controller.enterMenu(matcher);
        if ((matcher = GameMenuCommand.ENTER_CHAPTER.getMatcher(input)) != null) return controller.enterChapter(matcher);
        if ((matcher = GameMenuCommand.GREENHOUSE.getMatcher(input)) != null) return controller.greenhouse(matcher);
        if ((matcher = GameMenuCommand.TRAVEL_LOG.getMatcher(input)) != null) return controller.travelLog(matcher);
        if ((matcher = GameMenuCommand.LEADERBOARD.getMatcher(input)) != null) return controller.leaderboard(matcher);
        if ((matcher = GameMenuCommand.COIN_WALLET.getMatcher(input)) != null) return controller.coinWallet(matcher);
        if ((matcher = GameMenuCommand.GEM_WALLET.getMatcher(input)) != null) return controller.gemWallet(matcher);
        if ((matcher = GameMenuCommand.CHEAT_ADD.getMatcher(input)) != null) return controller.cheatAdd(matcher);
        if ((matcher = GameMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        return new Result("Invalid command in Game Menu.", this);
    }

    @Override
    public String getName(){
        return "Game menu";
    }
}
