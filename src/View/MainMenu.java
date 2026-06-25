package View;

import Controller.MainController;
import Models.Commands.MainMenuCommand;

import java.util.regex.Matcher;

public class MainMenu implements Menu {
    MainController controller=new MainController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = MainMenuCommand.ENTER_MENU.getMatcher(input)) != null) return controller.enterMenu(matcher);
        if ((matcher = MainMenuCommand.SHOW_CURRENT.getMatcher(input)) != null) return controller.showCurrent(matcher);
        if ((matcher = MainMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        if ((matcher = MainMenuCommand.LOGOUT.getMatcher(input)) != null) return controller.logout(matcher);
        return new Result("Invalid command in Main Menu.", this);
    }

    @Override
    public String getName(){
        return "Main menu";
    }
}
