package pvz.view;

import java.util.regex.Matcher;

import pvz.controller.user.LoginController;
import pvz.enums.commands.LoginMenuCommand;

public class LoginMenu implements Menu {
    LoginController controller = new LoginController();
    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = LoginMenuCommand.ENTER_MENU.getMatcher(input)) != null) return controller.enterMenu(matcher);
        if ((matcher = LoginMenuCommand.LOGIN.getMatcher(input)) != null) return controller.login(matcher);
        if ((matcher = LoginMenuCommand.FORGET_PASSWORD.getMatcher(input)) != null) return controller.forgetPassword(matcher);
        if ((matcher = LoginMenuCommand.ANSWER.getMatcher(input)) != null) return controller.answer(matcher);
        if ((matcher = LoginMenuCommand.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        if ((matcher = LoginMenuCommand.HELP.getMatcher(input)) != null) return new Result(LoginMenuCommand.getHelp());
        return new Result("Invalid command in Login Menu.", this);
    }

    @Override
    public String getName(){
        return "Login menu";
    }

    @Override
    public Result onEnter() {
        return null;
    }
}
