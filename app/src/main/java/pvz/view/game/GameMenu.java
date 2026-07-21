package pvz.view.game;

import java.util.regex.Matcher;

import pvz.controller.GameMenuController;
import pvz.enums.commands.GameMenuCommands;
import pvz.view.Menu;
import pvz.view.Result;

public class GameMenu implements Menu {
    private GameMenuController controller = new GameMenuController();
    private Menu modalMenu;

    public GameMenu(Menu modalMenu){
        this.modalMenu = modalMenu;
    }
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = GameMenuCommands.ENTER_MENU.getMatcher(input)) != null) return controller.enterMenu(matcher);
        if ((matcher = GameMenuCommands.ENTER_CHAPTER.getMatcher(input)) != null) return controller.selectChapter(matcher);
        if ((matcher = GameMenuCommands.GREENHOUSE.getMatcher(input)) != null) return controller.greenhouse(matcher);
        if ((matcher = GameMenuCommands.TRAVEL_LOG.getMatcher(input)) != null) return controller.travelLog(matcher);
        if ((matcher = GameMenuCommands.LEADERBOARD.getMatcher(input)) != null) return controller.leaderboard(matcher);
        if ((matcher = GameMenuCommands.COIN_WALLET.getMatcher(input)) != null) return controller.coinWallet(matcher);
        if ((matcher = GameMenuCommands.GEM_WALLET.getMatcher(input)) != null) return controller.gemWallet(matcher);
        if ((matcher = GameMenuCommands.CHEAT_ADD.getMatcher(input)) != null) return controller.cheatAdd(matcher);
        if ((matcher = GameMenuCommands.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        if ((matcher = GameMenuCommands.HELP.getMatcher(input)) != null) return new Result(GameMenuCommands.getHelp());
        if (modalMenu != null) return modalMenu.handleInput(input);
        return new Result("Invalid command in Game Menu.", this);
    }

    @Override
    public String getName(){
        return "Game Menu" + (modalMenu != null ? ": " + modalMenu.getName() : "");
    }

    @Override
    public Result onEnter() {
        if (modalMenu != null) return modalMenu.onEnter();
        return new Result("Select Chapter");
    }
}
