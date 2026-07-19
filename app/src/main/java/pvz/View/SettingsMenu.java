package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.user.SettingsController;
import pvz.Enums.Commands.SettingsMenuCommand;

public class SettingsMenu implements Menu {
    SettingsController controller=new SettingsController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = SettingsMenuCommand.CHANGE_DIFFICULTY.getMatcher(input)) != null) return controller.changeDifficulty(matcher);
        if ((matcher = SettingsMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        if ((matcher = SettingsMenuCommand.HELP.getMatcher(input)) != null) return new Result(SettingsMenuCommand.getHelp());
        return new Result("Invalid command in Settings Menu.", this);
    }

    @Override
    public String getName(){
        return "Settings menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
