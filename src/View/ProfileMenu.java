package View;

import Controller.ProfileController;
import Models.Commands.ProfileMenuCommand;

import java.util.regex.Matcher;

public class ProfileMenu implements Menu {
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = ProfileMenuCommand.CHANGE_USERNAME.getMatcher(input)) != null) return ProfileController.changeUsername(matcher);
        if ((matcher = ProfileMenuCommand.CHANGE_NICKNAME.getMatcher(input)) != null) return ProfileController.changeNickname(matcher);
        if ((matcher = ProfileMenuCommand.CHANGE_EMAIL.getMatcher(input)) != null) return ProfileController.changeEmail(matcher);
        if ((matcher = ProfileMenuCommand.CHANGE_PASSWORD.getMatcher(input)) != null) return ProfileController.changePassword(matcher);
        if ((matcher = ProfileMenuCommand.SHOW_INFO.getMatcher(input)) != null) return ProfileController.showInfo(matcher);
        if ((matcher = ProfileMenuCommand.EXIT.getMatcher(input)) != null) return ProfileController.exit(matcher);
        return new Result("Invalid command in Profile Menu.", this);
    }
}
