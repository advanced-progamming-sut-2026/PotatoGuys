package pvz.View;

import java.util.regex.Matcher;

import pvz.Controller.ProfileController;
import pvz.Enums.Commands.ProfileMenuCommand;

public class ProfileMenu implements Menu {
    ProfileController controller=new ProfileController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = ProfileMenuCommand.CHANGE_USERNAME.getMatcher(input)) != null) return controller.changeUsername(matcher);
        if ((matcher = ProfileMenuCommand.CHANGE_NICKNAME.getMatcher(input)) != null) return controller.changeNickname(matcher);
        if ((matcher = ProfileMenuCommand.CHANGE_EMAIL.getMatcher(input)) != null) return controller.changeEmail(matcher);
        if ((matcher = ProfileMenuCommand.CHANGE_PASSWORD.getMatcher(input)) != null) return controller.changePassword(matcher);
        if ((matcher = ProfileMenuCommand.SHOW_INFO.getMatcher(input)) != null) return controller.showInfo(matcher);
        if ((matcher = ProfileMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        return new Result("Invalid command in Profile Menu.", this);
    }

    @Override
    public String getName(){
        return "Profile menu";
    }
}
