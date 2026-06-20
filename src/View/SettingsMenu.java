package View;

import Controller.SettingsController;
import Models.Commands.SettingsMenuCommand;

import java.util.regex.Matcher;

public class SettingsMenu implements Menu {
    SettingsController controller=new SettingsController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = SettingsMenuCommand.CHANGE_DIFFICULTY.getMatcher(input)) != null) return controller.changeDifficulty(matcher);
        if ((matcher = SettingsMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        return new Result("Invalid command in Settings Menu.", this);
    }
}
