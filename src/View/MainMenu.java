package View;

import Controller.MainController;
import Models.Commands.MainMenuCommand;

import java.util.regex.Matcher;

public class MainMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = MainMenuCommand.ENTER_MENU.getMatcher(input)) != null) return MainController.enterMenu(matcher);
        if ((matcher = MainMenuCommand.SHOW_CURRENT.getMatcher(input)) != null) return MainController.showCurrent(matcher);
        if ((matcher = MainMenuCommand.EXIT.getMatcher(input)) != null) return MainController.exit(matcher);
        if ((matcher = MainMenuCommand.LOGOUT.getMatcher(input)) != null) return MainController.logout(matcher);
        return new Result("Invalid command in Main Menu.", this);
    }
}
