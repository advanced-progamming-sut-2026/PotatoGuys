package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.MainController;
import pvz.Enums.Commands.MainMenuCommand;

public class MainMenu implements Menu {
    MainController controller=new MainController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = MainMenuCommand.ENTER_MENU.getMatcher(input)) != null) return controller.enterMenu(matcher);
        if ((matcher = MainMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        if ((matcher = MainMenuCommand.LOGOUT.getMatcher(input)) != null) return controller.logout(matcher);
        return new Result("Invalid command in Main Menu.", this);
    }

    @Override
    public String getName(){
        return "Main menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
